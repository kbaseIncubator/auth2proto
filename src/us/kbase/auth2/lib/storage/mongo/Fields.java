package us.kbase.auth2.lib.storage.mongo;

public class Fields {

	//TODO JAVADOC
	
	public static final String FIELD_SEP = ".";

	public static final String MONGO_ID = "_id";

	//user fields
	public static final String USER_NAME = "user";
	public static final String USER_FULL_NAME = "full";
	public static final String USER_EMAIL = "email";
	public static final String USER_ID_PROVIDERS = "idprov";
	public static final String USER_LOCAL = "lcl";
	// these 3 are for local accounts only
	public static final String USER_PWD_HSH = "pwdhsh";
	public static final String USER_SALT = "salt";
	public static final String USER_RESET_PWD = "rstpwd";
	
	//user - id provider fields
	public static final String PROVIDER_NAME = "prov";
	public static final String PROVIDER_USER_NAME = "uname";
	public static final String PROVIDER_USER_ID = "uid";
	public static final String PROVIDER_USER_EMAIL = "uemail";
	public static final String PROVIDER_FULL_NAME = "ufull";

	//token fields
	public static final String TOKEN_USER_NAME = "user";
	public static final String TOKEN_TOKEN = "token";
	public static final String TOKEN_EXPIRY = "expires";
	public static final String TOKEN_ID = "id";
	public static final String TOKEN_NAME = "name";
	public static final String TOKEN_CREATION = "create";
	
	// configuration fields
	public static final String CONFIG_KEY = "config";
	public static final String CONFIG_VALUE = "config";
	public static final String CONFIG_UPDATE = "inupdate";
	public static final String CONFIG_SCHEMA_VERSION = "schemaver";

}
