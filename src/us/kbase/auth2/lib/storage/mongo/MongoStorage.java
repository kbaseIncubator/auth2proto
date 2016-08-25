package us.kbase.auth2.lib.storage.mongo;


import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bson.Document;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;

import us.kbase.auth2.lib.AuthUser;
import us.kbase.auth2.lib.LocalUser;
import us.kbase.auth2.lib.UserName;
import us.kbase.auth2.lib.exceptions.AuthError;
import us.kbase.auth2.lib.exceptions.AuthException;
import us.kbase.auth2.lib.exceptions.AuthenticationException;
import us.kbase.auth2.lib.storage.AuthStorage;
import us.kbase.auth2.lib.storage.exceptions.AuthStorageException;
import us.kbase.auth2.lib.storage.exceptions.NoSuchTokenException;
import us.kbase.auth2.lib.storage.exceptions.StorageInitException;
import us.kbase.auth2.lib.token.HashedToken;
import us.kbase.auth2.lib.token.IncomingHashedToken;

public class MongoStorage implements AuthStorage {

	/* To add provider:
	 * 1) pull the document
	 * 		If it's a local account fail
	 * 2) Add the provider to the array if it's not there already
	 * 		If it is and non-indexed fields are the same, no-op, otherwise replace and proceed
	 * 3) update the document with the new array, querying on the contents of the old array with $all and $elemMatch to assure no changes have been made
	 * 4) If fail, go to 1
	 * 		Except if it's a duplicate key, then fail permanently
	 * 
	 * This ensures that the same provider / user id combination only exists in the users db once at most
	 * $addToSet will add the same provider /user id combo to an array if the email etc. is different
	 * Unique indexes don't ensure that the contents of arrays are unique, just that no two documents have the same array elements
	 * Splitting the user doc from the provider docs has a whole host of other issues, mostly wrt deletion
	 * 
	 * To remove provider:
	 * 1) pull the document
	 * 2) Remove the provider from the array
	 * 		If it's already gone no-op
	 * 3) update the document with $pull, querying on the contents of the updated array with $elemMatch to be sure at least one provider still exists
	 * 4) If fail fail permanently
	 *
	 *	This ensures that all accounts always have one provider
	 * 
	 * 
	 */
	
	//TODO TEST unit tests
	//TODO JAVADOC
	
	private static final int SCHEMA_VERSION = 1;
	
	private static final String COL_USERS = "users";
	private static final String COL_CONFIG = "config";
	private static final String COL_TOKEN = "tokens";
	
	private static final Map<String, Map<List<String>, IndexOptions>> INDEXES;
	private static final IndexOptions IDX_UNIQ =
			new IndexOptions().unique(true);
	private static final IndexOptions IDX_UNIQ_SPARSE =
			new IndexOptions().unique(true).sparse(true);
	static {
		//hardcoded indexes
		INDEXES = new HashMap<String, Map<List<String>, IndexOptions>>();
		
		//user indexes
		final Map<List<String>, IndexOptions> users = new HashMap<>();
		//find users and ensure user names are unique
		users.put(Arrays.asList(Fields.USER_NAME), IDX_UNIQ);
		//find user by provider and ensure providers are 1:1 with users
		users.put(Arrays.asList(
				Fields.USER_ID_PROVIDERS + Fields.FIELD_SEP +
					Fields.PROVIDER_FULL_NAME,
				Fields.USER_ID_PROVIDERS + Fields.FIELD_SEP +
					Fields.PROVIDER_USER_ID),
				IDX_UNIQ_SPARSE);
		INDEXES.put(COL_USERS, users);
		
		//token indexes
		final Map<List<String>, IndexOptions> token = new HashMap<>();
		token.put(Arrays.asList(Fields.TOKEN_USER_NAME), null);
		token.put(Arrays.asList(Fields.TOKEN_TOKEN), IDX_UNIQ);
		token.put(Arrays.asList(Fields.TOKEN_ID), IDX_UNIQ);
		INDEXES.put(COL_TOKEN, token);
		
		//config indexes
		final Map<List<String>, IndexOptions> cfg = new HashMap<>();
		//ensure only one config object
		cfg.put(Arrays.asList(Fields.CONFIG_KEY), IDX_UNIQ);
		INDEXES.put(COL_CONFIG, cfg);

	}
	
	private MongoDatabase db;
	
	public MongoStorage(final MongoDatabase db) throws StorageInitException {
		if (db == null) {
			throw new NullPointerException("db");
		}
		this.db = db;
		
		//TODO NOW indexes
		//TODO NOW check workspace startup for stuff to port over
		//TODO NOW check / set config with root user
		//TODO NOW check root password or something and warn if not set
		checkConfig();
		ensureIndexes();
	}
	
	private void checkConfig() throws StorageInitException  {
		final MongoCollection<Document> col = db.getCollection(COL_CONFIG);
		final Document cfg = new Document(
				Fields.CONFIG_KEY, Fields.CONFIG_VALUE);
		cfg.put(Fields.CONFIG_UPDATE, false);
		cfg.put(Fields.CONFIG_SCHEMA_VERSION, SCHEMA_VERSION);
		try {
			col.insertOne(cfg);
		} catch (MongoWriteException dk) {
			if (!isDuplicateKeyException(dk)) {
				throw new StorageInitException(
						"There was a problem communicating with the database",
						dk);
			}
			//ok, the version doc is already there, this isn't the first
			//startup
			if (col.count() != 1) {
				throw new StorageInitException(
						"Multiple config objects found in the database. " +
						"This should not happen, something is very wrong.");
			}
			final FindIterable<Document> cur = db.getCollection(COL_CONFIG)
					.find(Filters.eq(Fields.CONFIG_KEY, Fields.CONFIG_VALUE));
			final Document doc = cur.first();
			if ((Integer) doc.get(Fields.CONFIG_SCHEMA_VERSION) !=
					SCHEMA_VERSION) {
				throw new StorageInitException(String.format(
						"Incompatible database schema. Server is v%s, DB is v%s",
						SCHEMA_VERSION,
						doc.get(Fields.CONFIG_SCHEMA_VERSION)));
			}
			if ((Boolean) doc.get(Fields.CONFIG_UPDATE)) {
				throw new StorageInitException(String.format(
						"The database is in the middle of an update from " +
								"v%s of the schema. Aborting startup.", 
								doc.get(Fields.CONFIG_SCHEMA_VERSION)));
			}
		} catch (MongoException me) {
			throw new StorageInitException(
					"There was a problem communicating with the database", me);
		}
	}

	private void ensureIndexes() throws StorageInitException {
		for (String col: INDEXES.keySet()) {
			for (List<String> idx: INDEXES.get(col).keySet()) {
				final Document index = new Document();
				final IndexOptions opts = INDEXES.get(col).get(idx);
				for (String field: idx) {
					index.put(field, 1);
				}
				final MongoCollection<Document> dbcol = db.getCollection(col);
				try {
					if (opts == null) {
						dbcol.createIndex(index);
					} else {
						dbcol.createIndex(index, opts);
					}
				} catch (MongoException me) {
					throw new StorageInitException(
							"Failed to create index", me);
				}
			}
		}
	}
	
	@Override
	public void createLocalAccount(final LocalUser local)
			throws AuthException, AuthStorageException {
		final String pwdhsh = Base64.getEncoder().encodeToString(
				local.getPasswordHash());
		final String salt = Base64.getEncoder().encodeToString(
				local.getSalt());
		final Document u = new Document(
				Fields.USER_NAME, local.getUserName().getName())
				.append(Fields.USER_LOCAL, true)
				.append(Fields.USER_EMAIL, local.getEmail())
				.append(Fields.USER_FULL_NAME, local.getFullName())
				.append(Fields.USER_RESET_PWD, local.forceReset())
				.append(Fields.USER_PWD_HSH, pwdhsh)
				.append(Fields.USER_SALT, salt);
		try {
			db.getCollection(COL_USERS).insertOne(u);
		} catch (MongoWriteException mwe) {
			if (isDuplicateKeyException(mwe)) {
				System.out.println(mwe);
				throw new AuthException(AuthError.USER_ALREADY_EXISTS,
						local.getUserName().getName());
			} else {
				throw new AuthStorageException("Database write failed", mwe);
			}
		} catch (MongoException me) {
			throw new AuthStorageException(
					"Connection to database failed", me);
		}
	}
	
	@Override
	public LocalUser getLocalUser(final UserName userName)
			throws AuthStorageException, AuthenticationException {
		final Document user = getUserDoc(userName, true);
		return new LocalUser(
				new UserName(user.getString(Fields.USER_NAME)),
				user.getString(Fields.USER_EMAIL),
				user.getString(Fields.USER_FULL_NAME),
				Base64.getDecoder().decode(
						user.getString(Fields.USER_PWD_HSH)),
				Base64.getDecoder().decode(user.getString(Fields.USER_SALT)),
				user.getBoolean(Fields.USER_RESET_PWD));
	}

	private Document getUserDoc(final UserName userName, final boolean local)
			throws AuthStorageException, AuthenticationException {
		final Document user = findOne(COL_USERS,
				new Document(Fields.USER_NAME, userName.getName()));
		if (user == null || (local && !user.getBoolean(Fields.USER_LOCAL))) {
			throw new AuthenticationException(AuthError.NO_SUCH_USER,
					userName.getName());
		}
		return user;
	}

	private boolean isDuplicateKeyException(final MongoWriteException mwe) {
		return mwe.getError().getCategory().equals(
				ErrorCategory.DUPLICATE_KEY);
	}

	@Override
	public void storeToken(final HashedToken t) throws AuthStorageException {
		final Document td = new Document(
				Fields.TOKEN_USER_NAME, t.getUserName().getName())
				.append(Fields.TOKEN_ID, t.getId().toString())
				.append(Fields.TOKEN_NAME, t.getTokenName())
				.append(Fields.TOKEN_TOKEN, t.getTokenHash())
				.append(Fields.TOKEN_EXPIRY, t.getExpirationDate())
				.append(Fields.TOKEN_CREATION, t.getCreationDate());
		try {
			db.getCollection(COL_TOKEN).insertOne(td);
			/* could catch a duplicate key exception here but that indicates
			 * a programming error - should never try to insert a duplicate
			 *  token
			 */
		} catch (MongoException me) {
			throw new AuthStorageException(
					"Connection to database failed", me);
		}
	}

	/* Use this for finding documents where indexes should force only a single
	 * document. Assumes the indexes are doing their job.
	 */
	private Document findOne(final String collection, final Document query)
			throws AuthStorageException {
		try {
			return db.getCollection(collection).find(query).first();
		} catch (MongoException me) {
			throw new AuthStorageException(
					"Connection to database failed", me);
		}
	}

	@Override
	public HashedToken getToken(final IncomingHashedToken token)
			throws AuthStorageException {
		final Document t = findOne(COL_TOKEN, new Document(
				Fields.TOKEN_TOKEN, token.getTokenHash()));
		if (t == null) {
			throw new NoSuchTokenException("Token not found");
		}
		//TODO NOW if token expired, throw error
		return getToken(t);
	}
	
	private HashedToken getToken(final Document t)
			throws AuthStorageException {
		return new HashedToken(t.getString(Fields.TOKEN_NAME),
				UUID.fromString(t.getString(Fields.TOKEN_ID)),
				t.getString(Fields.TOKEN_TOKEN),
				new UserName(t.getString(Fields.TOKEN_USER_NAME)),
				t.getDate(Fields.TOKEN_CREATION),
				t.getDate(Fields.TOKEN_EXPIRY));
	}

	@Override
	public List<HashedToken> getTokens(final UserName userName)
			throws AuthStorageException {
		if (userName == null) {
			throw new NullPointerException("userName");
		}
		final List<HashedToken> ret = new LinkedList<>();
		try {
			final FindIterable<Document> ts = db.getCollection(COL_TOKEN).find(
					new Document(Fields.TOKEN_USER_NAME, userName.getName()));
			for (final Document d: ts) {
				ret.add(getToken(d));
			}
		} catch (MongoException e) {
			throw new AuthStorageException(
					"Connection to database failed", e);
		}
		return ret;
	}
	
	@Override
	public AuthUser getUser(final UserName userName)
			throws AuthenticationException, AuthStorageException {
		final Document user = getUserDoc(userName, false);
		return new AuthUser(new UserName(user.getString(Fields.USER_NAME)),
				user.getString(Fields.USER_EMAIL),
				user.getString(Fields.USER_FULL_NAME));
	}

}
