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

import de.dfki.lt.nemex.a.data.Gazetteer;

/**
 * <h4>
 * The token-based inverted index (a.k.a. inverted list) class. This data structure is used
 * for storing lexical entries of the gazetteer for Nemex-F-1:FAERIE.</h4>
 */
public class InvertedList_FAERIE_tokenBased {

	private Gazetteer gazetteer;

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
	public InvertedList_FAERIE_tokenBased(Gazetteer gazetteer, int nGramSize,
			boolean ignoreDuplicateNgrams) {
		
		this.gazetteer = gazetteer;
		this.invertedIndex = new HashMap<String, List<Long>>();

		for (long i = 1; i <= this.gazetteer.getLexicalEntries().size(); i++) {

			// This is a Multi-Word Lexical (MWL) entry in the gazetteer known
			// as y.
			String y = this.gazetteer.getLexicalEntries().get(new Long(i))
					.get(1);
			

			List<String> tokenSetY = new ArrayList<String>();
			String[] tokensOfY = (y.split(" ")); //Tokenize y

			for (String token : tokensOfY) {

				if (this.invertedIndex.keySet().contains(token)) {
					
					this.invertedIndex.get(token).add(i - 1);

				} else {
					List<Long> tmpList = new ArrayList<Long>();
					tmpList.add(i - 1);
					this.invertedIndex.put(token, tmpList);
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
			
			System.out.println(key + "  --->  " + list);
			
		}
	}

}
