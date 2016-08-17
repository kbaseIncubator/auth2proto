package hellotest;

public class HelloJson {

	private String bar;
	private String whee;
	
	
//	 for jackson
	@SuppressWarnings("unused")
	private HelloJson() {}
	
	public HelloJson(String foo, String baz) {
		bar = foo;
		whee = baz;
	}

	public String getBar() {
		return bar;
	}

	public String getWhee() {
		return whee;
	}

}
