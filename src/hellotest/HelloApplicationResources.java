package hellotest;

import us.kbase.common.service.JsonServerSyslog;

public class HelloApplicationResources {

	final JsonServerSyslog logger;
	
	public HelloApplicationResources() {
		logger = new JsonServerSyslog("HelloApp",
				"thisisafakekeythatshouldntexistihope",
				JsonServerSyslog.LOG_LEVEL_INFO, true);
	}
}
