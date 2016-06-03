package de.dfki.lt.nemex.a.ngram;

/*
 *  Character-based n-gram representation of Strings, without accepting duplicate n-grams.
 *  NEMEX
 *  October 2012
 *  Author: Amir H. Moin (amir.moin@dfki.de)
 *  LT Lab.
 *  German Research Center for Artificial Intelligence
 *  (Deutsches Forschungszentrum fuer Kuenstliche Intelligenz GmbH = DFKI)
 *  http://www.dfki.de
 *  Saarbruecken, Saarland, Germany
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Concrete class for the feature-set of strings with character-based n-grams in
 * which there should not be any duplicate grams.
 */
public class CharacterNgramUnique extends CharacterNgram {

	/*
	 * Attributes
	 */
	private Set<String> ngrams;

	/*
	 * Constructor
	 */
	public CharacterNgramUnique(String str, int gramSize) {
		super(str, gramSize);
		this.ngrams = new HashSet<String>();
		this.generateNgrams();
	}

	/*
	 * Methods
	 */
	private void generateNgrams() {
		// this.setStr("$$" + this.getStr() + "$$");
		int start = 0;
		int end = start + this.getGramSize() - 1;

		while (end < this.getStr().length()) {
			this.ngrams.add(this.getStr().substring(start, end + 1));
			start++;
			end++;
		}
	}

	@Override
	public void printNgrams() {
		System.out.println("The string is: " + this.getStr());
		System.out.println("The " + this.getGramSize() + "-grams are: ");
		for (String gram : this.ngrams) {
			System.out.println(gram);
		}
	}

	/*
	 * Getters and Setters
	 */
	public Set<String> getNgrams() {
		return ngrams;
	}

	public void setNgrams(Set<String> ngrams) {
		this.ngrams = ngrams;
	}
	
	public static void main(String[] args) {
		CharacterNgramUnique ngrams = new CharacterNgramUnique("Peter Parker is Sipderman!", 3);
		ngrams.printNgrams();
		List<String> grams = new ArrayList<String>();
		grams.addAll(((CharacterNgramUnique) ngrams).getNgrams());
		System.out.println(grams);
		
	}

}
