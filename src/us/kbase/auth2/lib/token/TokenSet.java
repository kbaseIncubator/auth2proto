package us.kbase.auth2.lib.token;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class TokenSet {

	private final HashedToken currentToken;
	private final List<HashedToken> tokens;
	
	public TokenSet(
			final HashedToken current,
			final List<HashedToken> tokens) {
		if (current == null) {
			throw new NullPointerException("current");
		}
		final List<HashedToken> nt = new LinkedList<>(tokens);
		final Iterator<HashedToken> i = nt.iterator();
		while (i.hasNext()) {
			if (i.next().getId().equals(current.getId())) {
				i.remove();
			}
		}
		this.currentToken = current;
		this.tokens = Collections.unmodifiableList(nt);
	}

	public HashedToken getCurrentToken() {
		return currentToken;
	}

	public List<HashedToken> getTokens() {
		return tokens;
	}
}
