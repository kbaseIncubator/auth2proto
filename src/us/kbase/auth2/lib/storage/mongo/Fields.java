package us.kbase.auth2.lib.storage.mongo;

public class Fields {

	public static final String FIELD_SEP = ".";

	public static final String MONGO_ID = "_id";

	//user fields
	public static final String USER_NAME = "user";
	public static final String USER_FULL_NAME = "full";
	public static final String USER_EMAIL = "email";
	public static final String USER_PWD_HSH = "pwdhsh";
	public static final String USER_SALT = "salt";
	public static final String USER_RESET_PWD = "rstpwd";
	public static final String USER_ID_PROVIDERS = "idprov";
	
	//id provider fields

	//token fields
	
	// configuration fields
	public static final String CONFIG_KEY = "config";
	public static final String CONFIG_VALUE = "config";
	public static final String CONFIG_UPDATE = "inupdate";
	public static final String CONFIG_SCHEMA_VERSION = "schemaver";
	
	
}
