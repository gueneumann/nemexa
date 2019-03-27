package de.dfki.lt.nemex.a.similarity;

/*
 *  The Abstract class for approximate similarity measure of Strings
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.dfki.lt.nemex.a.data.InvertedList;
import de.dfki.lt.nemex.a.ngram.CharacterNgram;
import de.dfki.lt.nemex.a.ngram.CharacterNgramUnique;
import de.dfki.lt.nemex.a.ngram.CharacterNgramWithDuplicate;

/**
 * <H4>
 * This abstract class implements the Approximate String Similarity Service
 * interface. Each similarity measure (i.e., similarity function) must extend
 * it.</H4>
 */
public abstract class ApproximateStringSimilarityImpl implements
		ApproximateStringSimilarityService {

	List<String> grams;
	InvertedList invertedList;
	boolean ignoreDuplicateNgrams;
	int gramSize;

	/**
	 * @param characterNgram
	 *            The feature-set of the query string (known as set X).
	 * @param invertedList
	 *            The inverted index instance, which is already created from the
	 *            gazetteer.
	 * @param ignoreDuplicateNgrams
	 *            Set this boolean value to true only if there should not be any
	 *            duplicate grams in the feature set of strings. *
	 */
	public ApproximateStringSimilarityImpl(CharacterNgram characterNgram,
			InvertedList invertedList, boolean ignoreDuplicateNgrams) {
		this.gramSize = characterNgram.getGramSize();
		this.ignoreDuplicateNgrams = ignoreDuplicateNgrams;
		this.invertedList = invertedList;
		this.grams = new ArrayList<String>();
		if (!ignoreDuplicateNgrams) {
			grams.addAll(((CharacterNgramWithDuplicate) characterNgram)
					.getNgrams());
		} else {
			grams.addAll(((CharacterNgramUnique) characterNgram).getNgrams());
		}
	}

	/**
	 * @param similarityThreshold
	 *            This is a decimal number between zero and one. One corresponds
	 *            to exact match.
	 * @return Minimum size of the feature-set Y.
	 */
	protected abstract int findMinSizeForFeatureSetY(double similarityThreshold);

	/**
	 * @param similarityThreshold
	 *            This is a decimal number between zero and one. One corresponds
	 *            to exact match.
	 * @return Maximum size of the feature-set Y.
	 */
	protected abstract int findMaxSizeForFeatureSetY(double similarityThreshold);

	/**
	 * @param similarityThreshold
	 *            This is a decimal number between zero and one. One corresponds
	 *            to exact match.
	 * @param featureSetY
	 *            The feature-set (n-grams) of the lexical entry y (known as set
	 *            Y).
	 * @return Minimum tau-overlap.
	 */
	protected abstract int findTauMinOverlap(double similarityThreshold,
			List<String> featureSetY);

	/**
	 * @param similarityThreshold
	 *            This is a decimal number between zero and one. One corresponds
	 *            to exact match.
	 * @param featureSetY
	 *            The feature-set (n-grams) of the lexical entry y (known as set
	 *            Y).
	 * @return Minimum tau-overlap.
	 */
	protected abstract int findTauMinOverlap(double similarityThreshold,
			int featureSetYSize);

	public List<String> doApproximateStringMatchingUsingCPMerge(
			double similarityThreshold) {
		
		List<Long> finalRetrievalListLongs = new ArrayList<Long>();
		List<String> finalRetrievalListStrings = new ArrayList<String>();

		int minSizeForFeatureSetY = findMinSizeForFeatureSetY(similarityThreshold);
		int maxSizeForFeatureSetY = findMaxSizeForFeatureSetY(similarityThreshold);

		// System.out.println("Min Size of Y: " + minSizeForFeatureSetY);
		// System.out.println("Max Size of Y: " + maxSizeForFeatureSetY);

		int tau = 0;

		for (int l = minSizeForFeatureSetY; l <= maxSizeForFeatureSetY; l++) {
			tau = this.findTauMinOverlap(similarityThreshold, l);
			List<Long> list = new ArrayList<Long>();
			list = doOverlapJoinUsingCPMerge(tau, l);

			if (list != null && list.size() != 0) {
				finalRetrievalListLongs.addAll(list);
			}
		}

		// Converting indices of the lexical entries of the gazetteer to Strings
		// (i.e. lexical entries):
		for (long index : finalRetrievalListLongs) {
			finalRetrievalListStrings.add(this.getInvertedList().getGazetteer()
					.getLexicalEntries().get(new Long(index + 1)).get(1));
		}

		return finalRetrievalListStrings;
	}

	private List<Long> doOverlapJoinUsingCPMerge(int tau, int l) {

		List<Long> finalRetrievalList = new ArrayList<Long>();
		List<String> featureSetX = this.getGrams();

		// Sort feature set X, based on the number of candidate strings for each
		// gram in the inverted list:
		// Less common feature in X, ..., most common feature in X
		Map<String, Integer> tmpSortingMap = new HashMap<String, Integer>();
		for (String gram : featureSetX) {
			List<Long> tmpSortingList = new ArrayList<Long>();
			if (this.invertedList.getInvertedIndex().containsKey(gram)) {
				if (this.invertedList.getInvertedIndex().get(gram)
						.containsKey(new Integer(l))) {
					tmpSortingList.addAll((this.invertedList.getInvertedIndex()
							.get(gram)).get(new Integer(l)));
				}
			}

			tmpSortingMap.put(gram, tmpSortingList.size());
		}

		// Sorting the hashmap based on the Integer values
		List<Map.Entry<String, Integer>> entries = new ArrayList<Map.Entry<String, Integer>>(
				tmpSortingMap.size());
		entries.addAll(tmpSortingMap.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
			@Override
			public int compare(final Map.Entry<String, Integer> entry1,
					final Map.Entry<String, Integer> entry2) {
				return entry1.getValue().compareTo(entry2.getValue());
			}
		});

		Map<String, Integer> tmpSortedMap = new LinkedHashMap<String, Integer>();

		for (Map.Entry<String, Integer> entry : entries) {
			tmpSortedMap.put(entry.getKey(), entry.getValue());

		}

		Map<Long, Integer> occurrencesMap = new HashMap<Long, Integer>();

		List<String> tmpSortedMapKeys = new ArrayList<String>();
		List<Integer> tmpSortedMapValues = new ArrayList<Integer>();
		tmpSortedMapKeys.addAll(tmpSortedMap.keySet());
		tmpSortedMapValues.addAll(tmpSortedMap.values());

		// Step 1: Create the signatures set and collect the initial candidates.
		int num = featureSetX.size() - tau + 1; // Size of the signatures set.

		int i = 0;
		for (i = 0; i < num; i++) {

			if (i >= tmpSortedMap.size())
				break;

			String gram = tmpSortedMapKeys.get(i);

			if (!this.invertedList.getInvertedIndex().containsKey(gram)) {
				continue;
			}
			if (!this.invertedList.getInvertedIndex().get(gram)
					.containsKey(new Integer(l))) {
				continue;
			}

			List<Long> tmpList = this.invertedList.getInvertedIndex().get(gram)
					.get(new Integer(l));

			for (long item : tmpList) {

				if (!occurrencesMap.containsKey(new Long(item))) {
					occurrencesMap.put(new Long(item), new Integer(1));
				} else {
					int oldOccurrenceCount = occurrencesMap.get(new Long(item));
					occurrencesMap.put(new Long(item), new Integer(
							++oldOccurrenceCount));
				}
			}

		}

		if (occurrencesMap.isEmpty()) {
			return finalRetrievalList;
		}

		boolean flag = false;
		// Step 2: Count the number of matches with the remaining ones.
		for (; i < featureSetX.size(); i++) {
			if (i >= tmpSortedMap.size())
				break;
			flag = true;
			String gram = tmpSortedMapKeys.get(i);
			if (!this.invertedList.getInvertedIndex().containsKey(gram)) {
				continue;
			}
			if (!this.invertedList.getInvertedIndex().get(gram)
					.containsKey(new Integer(l))) {
				continue;
			}

			List<Long> tmpList = this.invertedList.getInvertedIndex().get(gram)
					.get(new Integer(l));

			for (long signature : occurrencesMap.keySet()) {

				if (occurrencesMap.get(new Long(signature)).equals(
						new Integer(-1))) {
					continue;
				}
				if (tmpList.contains(new Long(signature))) {
					int oldOccurrenceCount = occurrencesMap.get(new Long(
							signature));
					int newOccurrenceCount = oldOccurrenceCount
							+ Collections.frequency(tmpList,
									new Long(signature));

					occurrencesMap.put(new Long(signature), new Integer(
							newOccurrenceCount));
				}

				if ((occurrencesMap.get(new Long(signature)) >= tau)) {

					// This candidate has sufficient matches.
					finalRetrievalList.add(new Long(signature));
					occurrencesMap.put(new Long(signature), new Integer(-1));
				} else if ((occurrencesMap.get(new Long(signature))
						+ featureSetX.size() - i - 1) < tau) {
					// No chance anymore!
					// occurrencesMap.put(new Long(signature), new Integer(-1));
				}
				// else {
				// This candidate still has the chance
				// }
			}

		}

		if (!flag) {
			// Step2 is not performed!
			for (long item : occurrencesMap.keySet()) {
				if ((int) occurrencesMap.get(new Long(item)) >= tau) {
					finalRetrievalList.add(item);
				}
			}
		}

		return finalRetrievalList;
	}

	/*
	 * Note: This method has deliberately implemented the approach in an
	 * inefficient and trivial manner, since it does not use the signature-based
	 * CPMerge algorithm. It should be used for instruction and comparison
	 * purposes. It corresponds to the AllScan algorithm mentioned in the
	 * SimString paper.
	 */
	public List<String> doApproximateStringMatchingUsingAllScan(
			double similarityThreshold) {
		System.out
				.println("[WARNING] Algorithm: AllScan. This algorithm is inefficient and should be used only for instruction and comparison purposes! Please use the CPMerge algorithm instead.");
		System.out.println("[INFO] Similarity Threshold: "
				+ similarityThreshold);

		int minSizeForFeatureSetY = findMinSizeForFeatureSetY(similarityThreshold);
		int maxSizeForFeatureSetY = findMaxSizeForFeatureSetY(similarityThreshold);

		List<String> featureSetX = this.getGrams();
		List<Long> tmpRetrievalList = new ArrayList<Long>();
		List<String> finalRetrievalList = new ArrayList<String>();

		for (String queryGram : featureSetX) {
			if (!this.invertedList.getInvertedIndex().keySet()
					.contains(queryGram)) {
				continue;
			} else {
				Map<Integer, List<Long>> innerMap = this.invertedList
						.getInvertedIndex().get(queryGram);
				for (int key : innerMap.keySet()) {
					if (minSizeForFeatureSetY > 0
							&& key < minSizeForFeatureSetY)
						continue;
					else if (maxSizeForFeatureSetY > 0
							&& key > maxSizeForFeatureSetY)
						continue;
					else {
						tmpRetrievalList.addAll(innerMap.get(new Integer(key)));
					}
				}
			}
		}

		for (long index : tmpRetrievalList) {
			String y = this.getInvertedList().getGazetteer()
					.getLexicalEntries().get(new Long(index + 1)).get(1);
			List<String> featureSetY = new ArrayList<String>();
			if (!this.ignoreDuplicateNgrams) {
				CharacterNgramWithDuplicate characterNgram = new CharacterNgramWithDuplicate(
						y, this.gramSize);
				featureSetY = characterNgram.getNgrams();
			} else {
				CharacterNgramUnique characterNgram = new CharacterNgramUnique(
						y, this.gramSize);
				featureSetY.addAll(characterNgram.getNgrams());
			}
			int tau = findTauMinOverlap(similarityThreshold, featureSetY);

			if (Collections.frequency(tmpRetrievalList, index) >= tau) {

				finalRetrievalList.add(y);
			}
		}

		return finalRetrievalList;
	}

	/*
	 * Getters and Setters
	 */

	public InvertedList getInvertedList() {
		return invertedList;
	}

	public void setInvertedList(InvertedList invertedList) {
		this.invertedList = invertedList;
	}

	public List<String> getGrams() {
		return grams;
	}

	public void setGrams(List<String> grams) {
		this.grams = grams;
	}

}
