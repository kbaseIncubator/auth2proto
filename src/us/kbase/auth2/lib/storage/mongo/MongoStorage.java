package us.kbase.auth2.lib.storage.mongo;


import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bson.Document;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import us.kbase.auth2.lib.AuthUser;
import us.kbase.auth2.lib.CustomRole;
import us.kbase.auth2.lib.LocalUser;
import us.kbase.auth2.lib.Role;
import us.kbase.auth2.lib.UserName;
import us.kbase.auth2.lib.exceptions.MissingParameterException;
import us.kbase.auth2.lib.exceptions.NoSuchTokenException;
import us.kbase.auth2.lib.exceptions.NoSuchUserException;
import us.kbase.auth2.lib.exceptions.UserExistsException;
import us.kbase.auth2.lib.identity.RemoteIdentity;
import us.kbase.auth2.lib.storage.AuthStorage;
import us.kbase.auth2.lib.storage.exceptions.AuthStorageException;
import us.kbase.auth2.lib.storage.exceptions.StorageInitException;
import us.kbase.auth2.lib.token.HashedToken;
import us.kbase.auth2.lib.token.IncomingHashedToken;
import us.kbase.auth2.lib.token.TemporaryHashedToken;

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
	
	/* Don't use mongo built in object mapping to create the returned objects
	 * since that tightly couples the classes to the storage implementation.
	 * Instead, if needed, create classes specific to the implementation for
	 * mapping purposes that produce the returned classes.
	 */
	
	//TODO TEST unit tests
	//TODO JAVADOC
	
	private static final int SCHEMA_VERSION = 1;
	
	private static final String COL_USERS = "users";
	private static final String COL_CONFIG = "config";
	private static final String COL_TOKEN = "tokens";
	private static final String COL_TEMP_TOKEN = "temptokens";
	private static final String COL_CUST_ROLES = "cust_roles";
	
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
				Fields.USER_IDENTITIES + Fields.FIELD_SEP +
					Fields.IDENTITIES_PROVIDER,
				Fields.USER_IDENTITIES + Fields.FIELD_SEP +
					Fields.IDENTITIES_ID),
				IDX_UNIQ_SPARSE);
		INDEXES.put(COL_USERS, users);
		
		//token indexes
		final Map<List<String>, IndexOptions> token = new HashMap<>();
		token.put(Arrays.asList(Fields.TOKEN_USER_NAME), null);
		token.put(Arrays.asList(Fields.TOKEN_TOKEN), IDX_UNIQ);
		token.put(Arrays.asList(Fields.TOKEN_ID), IDX_UNIQ);
		token.put(Arrays.asList(Fields.TOKEN_EXPIRY),
				// this causes the tokens to expire at their expiration date
				//TODO TEST that tokens expire appropriately
				new IndexOptions().expireAfter(0L, TimeUnit.SECONDS));
		INDEXES.put(COL_TOKEN, token);
		
		//temporary token indexes
		final Map<List<String>, IndexOptions> temptoken = new HashMap<>();
		temptoken.put(Arrays.asList(Fields.TEMP_TOKEN_TOKEN), IDX_UNIQ);
		temptoken.put(Arrays.asList(Fields.TEMP_TOKEN_EXPIRY),
				// this causes the tokens to expire at their expiration date
				//TODO TEST that tokens expire appropriately
				new IndexOptions().expireAfter(0L, TimeUnit.SECONDS));
		INDEXES.put(COL_TEMP_TOKEN, temptoken);
		
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
						"There was a problem communicating with the " +
						"database: " + dk.getMessage(), dk);
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
						"Incompatible database schema. Server is v%s, " +
						"DB is v%s", SCHEMA_VERSION,
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
					"There was a problem communicating with the database: " +
					me.getMessage(), me);
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
							"Failed to create index: " + me.getMessage(), me);
				}
			}
		}
	}
	
	@Override
	public void createLocalUser(final LocalUser local)
			throws UserExistsException, AuthStorageException {
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
				.append(Fields.USER_SALT, salt)
				.append(Fields.USER_ROLES, new LinkedList<String>())
				.append(Fields.USER_CUSTOM_ROLES, new LinkedList<String>());
		try {
			db.getCollection(COL_USERS).insertOne(u);
		} catch (MongoWriteException mwe) {
			if (isDuplicateKeyException(mwe)) {
				throw new UserExistsException(local.getUserName().getName());
			} else {
				throw new AuthStorageException("Database write failed", mwe);
			}
		} catch (MongoException me) {
			throw new AuthStorageException(
					"Connection to database failed", me);
		}
	}
	
	//note this always returns pwd info. Add boolean to avoid if needed.
	@Override
	public LocalUser getLocalUser(final UserName userName)
			throws AuthStorageException, NoSuchUserException {
		final Document user = getUserDoc(userName, true);
		@SuppressWarnings("unchecked")
		final List<String> rolestr =
				(List<String>) user.get(Fields.USER_ROLES);
		final List<Role> roles = rolestr.stream()
				.map(s -> Role.getRole(s)).collect(Collectors.toList());
		@SuppressWarnings("unchecked")
		final List<String> croles = (List<String>) user.get(
				Fields.USER_CUSTOM_ROLES);
		return new LocalUser(
				new UserName(user.getString(Fields.USER_NAME)),
				user.getString(Fields.USER_EMAIL),
				user.getString(Fields.USER_FULL_NAME),
				new HashSet<>(roles),
				new HashSet<>(croles),
				Base64.getDecoder().decode(
						user.getString(Fields.USER_PWD_HSH)),
				Base64.getDecoder().decode(user.getString(Fields.USER_SALT)),
				user.getBoolean(Fields.USER_RESET_PWD));
	}
	
	@Override
	public void createUser(final AuthUser user)
			throws UserExistsException, AuthStorageException {
		if (user.isLocal()) {
			throw new IllegalArgumentException("cannot create a local user");
		}
		if (user.getIdentities().size() > 1) {
			throw new IllegalArgumentException(
					"user can only have one identity");
		}
		final RemoteIdentity ri = user.getIdentities().iterator().next();
		final Document id = identityToDocument(ri);
				
		final Document u = new Document(
				Fields.USER_NAME, user.getUserName().getName())
				.append(Fields.USER_LOCAL, false)
				.append(Fields.USER_EMAIL, user.getEmail())
				.append(Fields.USER_FULL_NAME, user.getFullName())
				.append(Fields.USER_ROLES, new LinkedList<String>())
				.append(Fields.USER_CUSTOM_ROLES, new LinkedList<String>())
				.append(Fields.USER_IDENTITIES, Arrays.asList(id));
		try {
			db.getCollection(COL_USERS).insertOne(u);
		} catch (MongoWriteException mwe) {
			if (isDuplicateKeyException(mwe)) {
				//TODO NOW handle case where duplicate is a remote id, not the username
				throw new UserExistsException(user.getUserName().getName());
			} else {
				throw new AuthStorageException("Database write failed", mwe);
			}
		} catch (MongoException me) {
			throw new AuthStorageException(
					"Connection to database failed", me);
		}
	}

	private Document getUserDoc(final UserName userName, final boolean local)
			throws AuthStorageException, NoSuchUserException {
		final Document projection = local ? null : new Document(
				Fields.USER_PWD_HSH, 0).append(Fields.USER_SALT, 0);
		final Document user = findOne(COL_USERS,
				new Document(Fields.USER_NAME, userName.getName()),
				projection);
		if (user == null || (local && !user.getBoolean(Fields.USER_LOCAL))) {
			throw new NoSuchUserException(userName.getName());
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
	private Document findOne(
			final String collection,
			final Document query)
			throws AuthStorageException {
		return findOne(collection, query, null);
	}
	
	/* Use this for finding documents where indexes should force only a single
	 * document. Assumes the indexes are doing their job.
	 */
	private Document findOne(
			final String collection,
			final Document query,
			final Document projection)
			throws AuthStorageException {
		try {
			return db.getCollection(collection).find(query)
					.projection(projection).first();
		} catch (MongoException me) {
			throw new AuthStorageException(
					"Connection to database failed", me);
		}
	}

	@Override
	public HashedToken getToken(final IncomingHashedToken token)
			throws AuthStorageException, NoSuchTokenException {
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
	public Set<HashedToken> getTokens(final UserName userName)
			throws AuthStorageException {
		if (userName == null) {
			throw new NullPointerException("userName");
		}
		final Set<HashedToken> ret = new HashSet<>();
		try {
			final FindIterable<Document> ts = db.getCollection(COL_TOKEN).find(
					new Document(Fields.TOKEN_USER_NAME, userName.getName()));
			for (final Document d: ts) {
				ret.add(getToken(d));
			}
		} catch (MongoException e) {
			throw new AuthStorageException("Connection to database failed", e);
		}
		return ret;
	}
	
	@Override
	public AuthUser getUser(final UserName userName)
			throws AuthStorageException, NoSuchUserException {
		final Document user = getUserDoc(userName, false);
		return toUser(user);
	}

	private AuthUser toUser(final Document user) {
		@SuppressWarnings("unchecked")
		final List<String> rolestr =
				(List<String>) user.get(Fields.USER_ROLES);
		final List<Role> roles = rolestr.stream()
				.map(s -> Role.getRole(s)).collect(Collectors.toList());
		@SuppressWarnings("unchecked")
		final List<String> croles = (List<String>) user.get(
				Fields.USER_CUSTOM_ROLES);
		@SuppressWarnings("unchecked")
		final List<Document> ids = (List<Document>)
				user.get(Fields.USER_IDENTITIES);
		return new AuthUser(
				new UserName(user.getString(Fields.USER_NAME)),
				user.getString(Fields.USER_EMAIL),
				user.getString(Fields.USER_FULL_NAME),
				toIdentities(ids),
				new HashSet<>(roles),
				new HashSet<>(croles));
	}

	@Override
	public void deleteToken(
			final UserName userName,
			final UUID tokenId)
			throws AuthStorageException, NoSuchTokenException {
		try {
			final DeleteResult dr = db.getCollection(COL_TOKEN)
					.deleteOne(new Document(
							Fields.TOKEN_USER_NAME, userName.getName())
							.append(Fields.TOKEN_ID, tokenId.toString()));
			if (dr.getDeletedCount() != 1L) {
				throw new NoSuchTokenException(String.format(
						"No token %s for user %s exists",
						tokenId, userName.getName()));
			}
		} catch (MongoException e) {
			throw new AuthStorageException("Connection to database failed", e);
		}
	}

	@Override
	public void deleteTokens(final UserName userName)
			throws AuthStorageException {
		try {
			db.getCollection(COL_TOKEN).deleteMany(new Document(
					Fields.TOKEN_USER_NAME, userName.getName()));
		} catch (MongoException e) {
			throw new AuthStorageException("Connection to database failed", e);
		}
	}

	@Override
	public void setRoles(final UserName userName, final Set<Role> roles)
			throws AuthStorageException, NoSuchUserException {
		final Set<String> strrl = roles.stream().map(r -> r.getRole())
				.collect(Collectors.toSet());
		setRoles(userName, strrl, Fields.USER_ROLES);
	}

	private void setRoles(
			final UserName userName,
			final Set<String> roles,
			final String field)
			throws NoSuchUserException, AuthStorageException {
		try {
			final UpdateResult ret = db.getCollection(COL_USERS).updateOne(
					new Document(Fields.USER_NAME, userName.getName()),
					new Document("$set", new Document(field, roles)));
			// might not modify the roles if they're the same as input
			if (ret.getMatchedCount() != 1) {
				throw new NoSuchUserException(userName.getName());
			}
		} catch (MongoException e) {
			throw new AuthStorageException("Connection to database failed", e);
		}
	}

	@Override
	public void setCustomRole(final CustomRole role)
			throws AuthStorageException {
		try {
			db.getCollection(COL_CUST_ROLES).updateOne(
					new Document(Fields.ROLES_NAME, role.getName()),
					new Document("$set",
							new Document(Fields.ROLES_DESC, role.getDesc()))
					.append("$setOnInsert",
							new Document(Fields.ROLES_ID,
									role.getId().toString())),
					new UpdateOptions().upsert(true));
		} catch (MongoException e) {
			throw new AuthStorageException("Connection to database failed", e);
		}
	}
	
	@Override
	public Set<CustomRole> getCustomRoles() throws AuthStorageException {
		return getCustomRoles(new Document());
	}

	private Set<CustomRole> getCustomRoles(final Document query)
			throws AuthStorageException {
		try {
			final FindIterable<Document> roles =
					db.getCollection(COL_CUST_ROLES).find(query);
			final Set<CustomRole> ret = new HashSet<>();
			for (final Document d: roles) {
				ret.add(new CustomRole(
						UUID.fromString(d.getString(Fields.ROLES_ID)),
						d.getString(Fields.ROLES_NAME),
						d.getString(Fields.ROLES_DESC)));
			}
			return ret;
		} catch (MongoException e) {
			throw new AuthStorageException("Connection to database failed", e);
		} catch (MissingParameterException e) { // should be impossible
			throw new AuthStorageException(
					"Error in roles colletion - role with missing field", e);
		}
	}

	@Override
	public Set<CustomRole> getCustomRoles(final Set<UUID> roleIds)
			throws AuthStorageException {
		return getCustomRoles(new Document(Fields.ROLES_ID,
				new Document("$in", roleIds.stream().map(i -> i.toString())
						.collect(Collectors.toList()))));
	}

	@Override
	public void setCustomRoles(
			final UserName userName,
			final Set<String> roles)
			throws NoSuchUserException, AuthStorageException {
		setRoles(userName, roles, Fields.USER_CUSTOM_ROLES);
		
	}

	@Override
	public AuthUser getUser(final RemoteIdentity remoteID)
			throws AuthStorageException {
		final Document query = makeUserQuery(remoteID);
		//note a user with identities should never have these fields, but
		//doesn't hurt to be safe
		final Document projection = new Document(Fields.USER_PWD_HSH, 0)
				.append(Fields.USER_SALT, 0);
		final Document u = findOne(COL_USERS, query, projection);
		if (u == null) {
			return null;
		}
		AuthUser user = toUser(u);
		/* could do a findAndModify to set the fields on the first query, but
		 * 99% of the time a set won't be necessary, so don't write lock the
		 * DB/collection (depending on mongo version) unless necessary 
		 */
		final Iterator<RemoteIdentity> iter = user.getIdentities().iterator();
		RemoteIdentity update = null;;
		while (iter.hasNext()) {
			final RemoteIdentity ri = iter.next();
			if (ri.getProvider().equals(remoteID.getProvider()) &&
					ri.getId().equals(remoteID.getId()) &&
					!ri.equals(remoteID)) {
				update = ri;
			}
		}
		if (update != null) {
			final Set<RemoteIdentity> newIDs = new HashSet<>(
					user.getIdentities());
			newIDs.remove(update);
			newIDs.add(remoteID);
			user = new AuthUser(user.getUserName(), user.getEmail(),
					user.getFullName(), newIDs, user.getRoles(),
					user.getCustomRoles());
			updateIdentity(remoteID);
		}
		return user;
	}

	private Document makeUserQuery(final RemoteIdentity remoteID) {
		final Document query = new Document(Fields.USER_IDENTITIES,
				new Document("$elemMatch", new Document(
						Fields.IDENTITIES_PROVIDER, remoteID.getProvider())
						.append(Fields.IDENTITIES_ID, remoteID.getId())));
		return query;
	}
	
	//TODO TEST exercise this function with tests
	private void updateIdentity(final RemoteIdentity remoteID)
			throws AuthStorageException {
		final Document query = makeUserQuery(remoteID);
		
		final String pre = Fields.USER_IDENTITIES + ".$.";
		final Document update = new Document("$set", new Document(
				pre + Fields.IDENTITIES_USER, remoteID.getUsername())
				.append(pre + Fields.IDENTITIES_EMAIL, remoteID.getEmail())
				.append(pre + Fields.IDENTITIES_NAME, remoteID.getFullname())
				.append(pre + Fields.IDENTITIES_PRIME, remoteID.isPrimary()));
		try {
			// id might have been unlinked, so we just assume
			// the update worked.
			db.getCollection(COL_USERS).updateOne(query, update);
		} catch (MongoException e) {
			throw new AuthStorageException("Connection to database failed", e);
		}
	}

	@Override
	public void storeIdentitiesTemporarily(
			final TemporaryHashedToken t,
			final Set<RemoteIdentity> identitySet)
			throws AuthStorageException {
		final List<Document> ids = new LinkedList<>();
		for (final RemoteIdentity id: identitySet) {
			ids.add(identityToDocument(id));
		}
		
		final Document td = new Document(
				Fields.TEMP_TOKEN_ID, t.getId().toString())
				.append(Fields.TEMP_TOKEN_TOKEN, t.getTokenHash())
				.append(Fields.TEMP_TOKEN_EXPIRY, t.getExpirationDate())
				.append(Fields.TEMP_TOKEN_CREATION, t.getCreationDate())
				.append(Fields.TEMP_TOKEN_IDENTITIES, ids);
		try {
			db.getCollection(COL_TEMP_TOKEN).insertOne(td);
			/* could catch a duplicate key exception here but that indicates
			 * a programming error - should never try to insert a duplicate
			 *  token
			 */
		} catch (MongoException me) {
			throw new AuthStorageException(
					"Connection to database failed", me);
		}
		
	}

	private Document identityToDocument(final RemoteIdentity id) {
		return new Document(
				Fields.IDENTITIES_PROVIDER, id.getProvider())
				.append(Fields.IDENTITIES_ID, id.getId())
				.append(Fields.IDENTITIES_PRIME, id.isPrimary())
				.append(Fields.IDENTITIES_USER, id.getUsername())
				.append(Fields.IDENTITIES_NAME, id.getFullname())
				.append(Fields.IDENTITIES_EMAIL, id.getEmail());
	}
	
	@Override
	public Set<RemoteIdentity> getTemporaryIdentities(
			final IncomingHashedToken token)
			throws AuthStorageException, NoSuchTokenException {
		final Document d = findOne(COL_TEMP_TOKEN,
				new Document(Fields.TEMP_TOKEN_TOKEN, token.getTokenHash()));
		if (d == null) {
			throw new NoSuchTokenException("Token not found");
		}
		@SuppressWarnings("unchecked")
		final List<Document> ids =
				(List<Document>) d.get(Fields.TEMP_TOKEN_IDENTITIES);
		if (ids == null || ids.isEmpty()) {
			final String tid = d.getString(Fields.TEMP_TOKEN_ID);
			throw new AuthStorageException(String.format(
					"Temporary token %s has no associated IDs", tid));
		}
		final Set<RemoteIdentity> ret = toIdentities(ids);
		return ret;
	}

	private Set<RemoteIdentity> toIdentities(final List<Document> ids) {
		final Set<RemoteIdentity> ret = new HashSet<>();
		if (ids == null) {
			return ret;
		}
		for (final Document i: ids) {
			ret.add(new RemoteIdentity(
					i.getString(Fields.IDENTITIES_PROVIDER),
					i.getString(Fields.IDENTITIES_ID),
					i.getString(Fields.IDENTITIES_USER),
					i.getString(Fields.IDENTITIES_NAME),
					i.getString(Fields.IDENTITIES_EMAIL),
					i.getBoolean(Fields.IDENTITIES_PRIME)));
		}
		return ret;
	}
}
