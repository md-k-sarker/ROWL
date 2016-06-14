package org.swrltab.util.parser;

import org.checkerframework.checker.nullness.qual.NonNull;

public class SWRLAtomNotFoundException extends SWRLParseException {
	private static final long serialVersionUID = 1L;

	public SWRLAtomNotFoundException(@NonNull String message) {
		super(message);
	}
}
