package us.kbase.auth2.cryptutils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base32;


public class TokenGenerator {

	//TODO TEST unit tests
	//TODO JAVADOC
	
	//TODO ONDB don't store the token, store a hash. For user requested tokens, associate a name. Store the date of creation & lifetime.
	
	// Inspiration from http://stackoverflow.com/a/41156/643675
	
	// note SecureRandom is thread safe
	private final SecureRandom random;
	
	public TokenGenerator() throws NoSuchAlgorithmException {
		random = SecureRandom.getInstance("SHA1PRNG");
	}
	
	public String getToken() {
		final byte[] b = new byte[20]; //160 bits so 32 b32 chars
		random.nextBytes(b);
		return new Base32().encodeAsString(b);
	}
	
	//TODO ONMOVE remove
	public static void main(String[] args) throws Exception {
		String t = new TokenGenerator().getToken();
				
		System.out.println(t + " " + t.length());
	}
}
