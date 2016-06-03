package de.dfki.lt.nemex.data;

public class GazetteerCreationException extends Exception {

	/**
	 * Exception while creating a gazetteer from a corpus.
	 */
	private static final long serialVersionUID = -6581232725299795230L;

	public GazetteerCreationException() {
		super();
	}

	public GazetteerCreationException(String msg) {
		super(msg);
	}

	public GazetteerCreationException(Throwable cause) {
		super(cause);
	}

	public GazetteerCreationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
