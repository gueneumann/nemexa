package de.dfki.lt.nemex.a.data;

/*
 *  Inverted lists, fast and efficient D.S. for storing and approximate lookup.
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.dfki.lt.nemex.a.ngram.CharacterNgram;
import de.dfki.lt.nemex.a.ngram.CharacterNgramUnique;
import de.dfki.lt.nemex.a.ngram.CharacterNgramWithDuplicate;

/**
 * <h4>
 * The Inverted index (a.k.a. inverted list) class. This data structure is used
 * for storing lexical entries of the gazetteer in the manner that makes very
 * fast and efficient string retrieval using the CPMerge algorithm possible.</h4>
 */
public class InvertedList {

	private Gazetteer gazetteer;
	private int nGramSize;
	private boolean ignoreDuplicateNgrams;
	private Map<String, Map<Integer, List<Long>>> invertedIndex;

	/**
	 * @param gazetteer
	 *            An instance of the gazeetteer class from which the inverted
	 *            index should be created.
	 * @param nGramSize
	 *            Size of the character-based n-grams
	 * @param ignoreDuplicateNgrams
	 *            This is true only if there should not be any duplicate n-grams
	 *            in the feature set of the strings.
	 */
	public InvertedList(Gazetteer gazetteer, int nGramSize,
			boolean ignoreDuplicateNgrams) {

		this.gazetteer = gazetteer;
		this.nGramSize = nGramSize;
		this.ignoreDuplicateNgrams = ignoreDuplicateNgrams;

		this.invertedIndex = new HashMap<String, Map<Integer, List<Long>>>();

		for (long i = 1; i <= this.gazetteer.getLexicalEntries().size(); i++) {

			// This is a Multi-Word Lexical (MWL) entry in the gazetteer known
			// as y.
			String y = this.gazetteer.getLexicalEntries().get(new Long(i))
					.get(1);
			CharacterNgram characterNgramsFor_y = null;
			List<String> featureSetY = new ArrayList<String>();

			if (!ignoreDuplicateNgrams) {
				characterNgramsFor_y = new CharacterNgramWithDuplicate(y,
						nGramSize);
				featureSetY
						.addAll(((CharacterNgramWithDuplicate) characterNgramsFor_y)
								.getNgrams());
			} else {
				characterNgramsFor_y = new CharacterNgramUnique(y, nGramSize);
				featureSetY
						.addAll(((CharacterNgramUnique) characterNgramsFor_y)
								.getNgrams());
			}

			for (int j = 0; j < featureSetY.size(); j++) {

				if (this.invertedIndex.keySet().contains(featureSetY.get(j))) {
					Map<Integer, List<Long>> innerMap = this.invertedIndex
							.get(featureSetY.get(j));
					if (innerMap.keySet().contains(featureSetY.size())) {
						innerMap.get(featureSetY.size()).add(i - 1);
					} else {
						List<Long> tmpList = new ArrayList<Long>();
						tmpList.add(i - 1);
						innerMap.put(featureSetY.size(), tmpList);
					}

				} else {
					Map<Integer, List<Long>> tmpMap = new HashMap<Integer, List<Long>>();
					List<Long> tmpList = new ArrayList<Long>();
					tmpList.add(i - 1);
					tmpMap.put(featureSetY.size(), tmpList);
					this.invertedIndex.put(featureSetY.get(j), tmpMap);
				}
			}
		}
	}

	/*
	 * Getters and Setters
	 */

	public Gazetteer getGazetteer() {
		return gazetteer;
	}

	public void setGazetteer(Gazetteer gazetteer) {
		this.gazetteer = gazetteer;
	}

	public int getnGramSize() {
		return nGramSize;
	}

	public void setnGramSize(int nGramSize) {
		this.nGramSize = nGramSize;
	}

	public Map<String, Map<Integer, List<Long>>> getInvertedIndex() {
		return invertedIndex;
	}

	public void setInvertedIndex(
			Map<String, Map<Integer, List<Long>>> invertedIndex) {
		this.invertedIndex = invertedIndex;
	}

	public boolean isIgnoreDuplicateNgrams() {
		return ignoreDuplicateNgrams;
	}

	public void setIgnoreDuplicateNgrams(boolean ignoreDuplicateNgrams) {
		this.ignoreDuplicateNgrams = ignoreDuplicateNgrams;
	}

}
