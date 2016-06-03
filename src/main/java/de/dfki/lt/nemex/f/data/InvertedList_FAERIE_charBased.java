package de.dfki.lt.nemex.f.data;

/*
 *  Inverted lists, fast and efficient D.S. for Nemex-F-1:FAERIE.
 *  NEMEX
 *  April 2013
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.lt.nemex.a.data.Gazetteer;
import de.dfki.lt.nemex.a.ngram.CharacterNgram;
import de.dfki.lt.nemex.a.ngram.CharacterNgramUnique;
import de.dfki.lt.nemex.a.ngram.CharacterNgramWithDuplicate;

/**
 * <h4>
 * The character-based inverted index (a.k.a. inverted list) class. This data structure is used
 * for storing lexical entries of the gazetteer for Nemex-F-1:FAERIE.</h4>
 */
public class InvertedList_FAERIE_charBased {

	private static final Logger LOG = LoggerFactory.getLogger(InvertedList_FAERIE_charBased.class);

	private Gazetteer gazetteer;
	private int nGramSize;
	private boolean ignoreDuplicateNgrams;
	public boolean isIgnoreDuplicateNgrams() {
		return ignoreDuplicateNgrams;
	}

	public void setIgnoreDuplicateNgrams(boolean ignoreDuplicateNgrams) {
		this.ignoreDuplicateNgrams = ignoreDuplicateNgrams;
	}

	private Map<String, List<Long>> invertedIndex;

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
	public InvertedList_FAERIE_charBased(Gazetteer gazetteer, int nGramSize,
			boolean ignoreDuplicateNgrams) {

		this.gazetteer = gazetteer;
		this.nGramSize = nGramSize;
		this.ignoreDuplicateNgrams = ignoreDuplicateNgrams;

		this.invertedIndex = new HashMap<String, List<Long>>();

		LOG.info("[INFO] Building inverted index ... ");

		for (long i = 1; i <= this.gazetteer.getLexicalEntries().size(); i++) {

			// This is a Multi-Word Lexical (MWL) entry in the gazetteer known
			// as y.
			// 
			// NOTE that for an external entry
			// 1 -9.197762 abacterial#abdominoperineal NG:1:-9.197762 NG:1:657
			// it is internally represented as
			// ["-9.197762", "abacterial#abdominoperineal", "NG:1:-9.197762", "NG:1:657"]
			// and the ID is used in the inverted index

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

			// Loop over the ngrams of an entry
			// For each ngram: if already in hash, then extend inverted list of lexical IDs with new ID
			// USING ID-1, else add ngram and create new inverted index with initial element ID-1
			for (int j = 0; j < featureSetY.size(); j++) {

				if (this.invertedIndex.keySet().contains(featureSetY.get(j))) {
					//TODO: GN, Feb 2016 - added test to avoid adding dublicates into inverted index
					if (!this.invertedIndex.get(featureSetY.get(j)).contains(i-1))
						(this.invertedIndex.get(featureSetY.get(j))).add(i - 1);

				} else {
					List<Long> tmpList = new ArrayList<Long>();
					tmpList.add(i - 1);
					this.invertedIndex.put(featureSetY.get(j), tmpList);

				}
			}
		}
		LOG.info("[INFO]  ... DONE !");
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

	public Map<String, List<Long>> getInvertedIndex() {
		return invertedIndex;
	}

	public void setInvertedIndex(
			Map<String, List<Long>> invertedIndex) {
		this.invertedIndex = invertedIndex;
	}

	public void printInvertedList() {
		for(String key : this.invertedIndex.keySet()) {

			String list = "";

			for(Long num : this.invertedIndex.get(key)) {
				list += " " + num.toString();
			}

			LOG.info(key + "  --->  " + list);

		}
	}

}
