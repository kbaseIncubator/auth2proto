package us.kbase.auth2.cryptutils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64; // requires java 8


public class TokenGenerator {

	//TODO ONMOVE unit tests
	//TODO ONMOVE docs
	//TODO ONDB don't store the token, store a hash. For user requested tokens, associate a name. Store the date of creation & lifetime.
	
	// Inspiration from http://stackoverflow.com/a/41156/643675
	
	// note SecureRandom is thread safe
	private final SecureRandom random;
	
	public TokenGenerator() {
		random = new SecureRandom();
	}
	
	public String getToken() {
		final byte[] b = new byte[21]; //168 bits so 28 b64 chars
		random.nextBytes(b);
		return new String(Base64.getEncoder().encode(b),
				StandardCharsets.UTF_8);
	}
	
	//TODO ONMOVE remove
	public static void main(String[] args) throws Exception {
		String t = new TokenGenerator().getToken();
				
		System.out.println(t + " " + t.length());
	}
}
