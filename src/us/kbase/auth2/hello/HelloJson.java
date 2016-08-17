package us.kbase.auth2.hello;

public class HelloJson {

	private final String bar;
	private final String whee = "whoo";
	
	public HelloJson(String foo) {
		bar = foo;
	}

	public String getBar() {
		return bar;
	}

	public String getWhee() {
		return whee;
	}

}
