package de.dfki.lt.nemex.a.ngram;

import java.util.Collection;
import java.util.List;

/*
 *  The Abstract class for character-based n-gram representation of Strings
 *  NEMEX
 *  October 2012
 *  Author: Amir H. Moin (amir.moin@dfki.de)
 *  LT Lab.
 *  German Research Center for Artificial Intelligence
 *  (Deutsches Forschungszentrum fuer Kuenstliche Intelligenz GmbH = DFKI)
 *  http://www.dfki.de
 *  Saarbruecken, Saarland, Germany
 */

/**
 * Abstract class for the feature-set of strings with character-based n-grams.
 * This is a common string representation.
 */
public abstract class CharacterNgram {

	/*
	 * Attributes
	 */
	private String str;
	private int gramSize;
	private Collection<String> ngrams;

	/*
	 * Methods
	 */
	public abstract void printNgrams();

	/*
	 * The Constructors
	 */
	public CharacterNgram(String str, int gramSize) {
		this.str = str;
		this.gramSize = gramSize;
	}

	/*
	 * The Getter and Setter Methods
	 */
	public String getStr() {
		return str;
	}

	public void setStr(String str) {
		this.str = str;
	}

	public int getGramSize() {
		return gramSize;
	}
	
	public Collection<String> getNgrams() {
		return ngrams;
	}

	public void setGramSize(int gramSize) {
		this.gramSize = gramSize;
	}

}
