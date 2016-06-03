package de.dfki.lt.nemex.a.data;

public class GazetteerNotLoadedException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4712L;
	String mistake;

	public GazetteerNotLoadedException() {
		super();
		mistake = "unknown";
	}

	public GazetteerNotLoadedException(String err) {
		super(err);
		mistake = err;
	}

	@Override
	public String getLocalizedMessage() {
		return mistake;
	}

}
