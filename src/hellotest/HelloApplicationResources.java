package hellotest;

import us.kbase.common.service.JsonServerSyslog;

public class HelloApplicationResources {

	final JsonServerSyslog logger;
	
	public HelloApplicationResources() {
		//TODO AUTH configure name
		//TODO KBASECOMMON allow null for the fake config prop arg
		logger = new JsonServerSyslog("HelloApp",
				"thisisafakekeythatshouldntexistihope",
				JsonServerSyslog.LOG_LEVEL_INFO, true);
	}
}
