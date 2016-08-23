package us.kbase.auth2.lib.storage.mongo;


import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;

import us.kbase.auth2.lib.LocalUser;
import us.kbase.auth2.lib.storage.AuthStorage;
import us.kbase.auth2.lib.storage.exceptions.AuthStorageException;
import us.kbase.auth2.lib.storage.exceptions.StorageInitException;
import us.kbase.auth2.service.exceptions.AuthError;
import us.kbase.auth2.service.exceptions.AuthException;

public class MongoStorage implements AuthStorage {

	//TODO TEST unit tests
	//TODO JAVADOC
	
	private static final int SCHEMA_VERSION = 1;
	
	private static final String COL_USERS = "users";
	private static final String COL_CONFIG = "config";
	
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
		users.put(Arrays.asList(Fields.USER_ID_PROVIDERS), IDX_UNIQ_SPARSE);
		INDEXES.put(COL_USERS, users);
		
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
		// TODO Auto-generated method stub
		final String pwdhsh = Base64.getEncoder().encodeToString(
				local.getPasswordHash());
		final String salt = Base64.getEncoder().encodeToString(
				local.getSalt());
		final Document u = new Document(Fields.USER_NAME, local.getUserName())
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
						local.getUserName());
			} else {
				throw new AuthStorageException("Database write failed", mwe);
			}
		} catch (MongoException me) {
			throw new AuthStorageException(
					"Connection to database failed", me);
		}
				
	}

	private boolean isDuplicateKeyException(final MongoWriteException mwe) {
		return mwe.getError().getCategory().equals(
				ErrorCategory.DUPLICATE_KEY);
	}

}
