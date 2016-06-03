package de.dfki.lt.nemex.a.ngram;

/*
 *  Character-based n-gram representation of Strings, including duplicate n-grams.
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
import java.util.List;

/**
 * Concrete class for the feature-set of strings with character-based n-grams in
 * which there could be duplicate grams.
 */
public class CharacterNgramWithDuplicate extends CharacterNgram {

	/*
	 * Attributes
	 */
	private List<String> ngrams;

	/*
	 * The Constructors
	 */
	public CharacterNgramWithDuplicate(String str, int gramSize) {
		super(str, gramSize);
		this.ngrams = new ArrayList<String>();
		this.generateNgrams();
	}

	/*
	 * Methods
	 */
	private void generateNgrams() {
		//this.setStr("$$" + this.getStr() + "$$");
		int start = 0;
		int end = start + this.getGramSize() - 1;

		while (end < this.getStr().length()) {
			String nextGram = this.getStr().substring(start, end + 1);
			this.ngrams.add(nextGram);
			start++;
			end++;
		}
		// validateNgrams();
	}

	private void validateNgrams() {
		if (this.ngrams.size() + this.getGramSize() - 1 == this.getStr()
				.length()) {
			// System.out.println("[INFO] No. of grams validated.");
			return;
		} else {
			try {
				throw new Exception(
						"[ERROR] Wrong no. of ngrams...something must be wrong...");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}

	}

	public void printNgrams() {
		System.out.println("[INFO] String: " + this.getStr());
		System.out.println("[INFO] All " + this.getGramSize() + "-grams: ");
		for (String gram : this.ngrams) {
			System.out.println(gram);
		}

	}

	/*
	 * The Getter and Setter Methods
	 */
	public List<String> getNgrams() {
		return ngrams;
	}

	public void setNgrams(List<String> ngrams) {
		this.ngrams = ngrams;
	}
	
	public static void main(String[] args) {
		CharacterNgramWithDuplicate ngrams = new CharacterNgramWithDuplicate("Peter Parker is Spiderman!", 3);
		ngrams.printNgrams();
		List<String> grams = new ArrayList<String>();
		grams.addAll(((CharacterNgramWithDuplicate) ngrams).getNgrams());
		System.out.println(grams);
		
	}
}
