package de.dfki.lt.nemex.a;

/*
 *  This is a singleton class for NEMEX-A.
 *  NEMEX-A is an advanced re-implementation of SimString in Java. 
 *  NEMEX-A is the first among the three components of NEMEX: NEMEX-A, NEMEX-F and NEMEX-R.
 *  NEMEX
 *  October 2012
 *  Author: Amir H. Moin (amir.moin@dfki.de)
 *  LT Lab.
 *  German Research Center for Artificial Intelligence
 *  (Deutsches Forschungszentrum fuer Kuenstliche Intelligenz GmbH = DFKI)
 *  http://www.dfki.de
 *  Saarbruecken, Saarland, Germany
 */

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.print.attribute.HashAttributeSet;

import de.dfki.lt.nemex.a.data.Gazetteer;
import de.dfki.lt.nemex.a.data.GazetteerNotLoadedException;
import de.dfki.lt.nemex.a.data.InvertedList;
import de.dfki.lt.nemex.a.ngram.CharacterNgram;
import de.dfki.lt.nemex.a.ngram.CharacterNgramUnique;
import de.dfki.lt.nemex.a.ngram.CharacterNgramWithDuplicate;
import de.dfki.lt.nemex.a.similarity.ApproximateStringSimilarityImpl;
import de.dfki.lt.nemex.a.similarity.CosineApproximateStringSimilarityImpl;
import de.dfki.lt.nemex.a.similarity.DiceApproximateStringSimilarityImpl;
import de.dfki.lt.nemex.a.similarity.JaccardApproximateStringSimilarityImpl;
import de.dfki.lt.nemex.a.similarity.OverlapApproximateStringSimilarityImpl;
import de.dfki.lt.nemex.a.similarity.SimilarityMeasure;

public class NEMEX_A {

	public static Map<String, InvertedList> loadedGazetteers = new HashMap<String, InvertedList>();

	public static boolean loadNewGazetteer(String gazetteerFilePath,
			String delimiter, Boolean delimiterSwitchOff, int nGramSize,
			Boolean ignoreDuplicateNgrams) {

		if (NEMEX_A.loadedGazetteers.containsKey(gazetteerFilePath)) {
			return false;
		}

		Gazetteer gazetteer = new Gazetteer(gazetteerFilePath, delimiter,
				delimiterSwitchOff);
		InvertedList invertedList = new InvertedList(gazetteer, nGramSize,
				ignoreDuplicateNgrams);
		NEMEX_A.loadedGazetteers.put(gazetteerFilePath, invertedList);
		return true;
	}

	public static boolean unloadGazetteer(String gazetteerFilePath) {

		if (!NEMEX_A.loadedGazetteers.containsKey(gazetteerFilePath)) {
			return false;
		}

		NEMEX_A.loadedGazetteers.remove(gazetteerFilePath);
		System.gc();
		return true;
	}

	public static void exportGazetteer(String gazetteerFilePath)
			throws GazetteerNotLoadedException {
		NEMEX_A.loadedGazetteers.get(gazetteerFilePath).getGazetteer()
		.serialize(gazetteerFilePath + ".ser");

		NEMEX_A.unloadGazetteer(gazetteerFilePath);

	}

	public static boolean importAndLoadGazetteer(
			String serializedGazetteerFilePath, String gazetteerFilePath,
			String delimiter, Boolean delimiterSwitchOff, int nGramSize,
			Boolean ignoreDuplicateNgrams) {

		if (NEMEX_A.loadedGazetteers.containsKey(gazetteerFilePath)) {

			return false;
		}

		Gazetteer gazetteer = Gazetteer
				.deserialize(serializedGazetteerFilePath);
		InvertedList invertedList = new InvertedList(gazetteer, nGramSize,
				ignoreDuplicateNgrams);
		NEMEX_A.loadedGazetteers.put(gazetteerFilePath, invertedList);
		return true;
	}

	public static void checkSimilarity(String queryString,
			String gazetteerFilePath, String similarityMeasure,
			double similarityThreshold, PrintWriter out) {

		InvertedList invertedList = NEMEX_A.loadedGazetteers
				.get(gazetteerFilePath);

		/*
		 * Generate the n-grams (i.e. the feature set X) for the query string x.
		 */
		long startQueryStringGramGenerationTime = System.currentTimeMillis();
		out.println("<br />");
		out.println("[INFO] Creating " + invertedList.getnGramSize()
				+ "-grams for the Query String: " + queryString);
		out.println("<br />");
		CharacterNgram characterNgram = null;
		if (!invertedList.isIgnoreDuplicateNgrams()) {
			characterNgram = new CharacterNgramWithDuplicate(queryString,
					invertedList.getnGramSize());
		} else {
			characterNgram = new CharacterNgramUnique(queryString,
					invertedList.getnGramSize());
		}
		// characterNgram.printNgrams();
		long endQueryStringGramGenerationTime = System.currentTimeMillis();
		out.println("[TIME INFO] Elapsed time for generating n-grams for the query string: "
				+ (endQueryStringGramGenerationTime - startQueryStringGramGenerationTime)
				+ " ms");
		out.println("<br />");

		/*
		 * Check the similarity
		 */
		long startSimilarityTime = System.currentTimeMillis();
		out.println("<br />");
		ApproximateStringSimilarityImpl approximateStringSimilarity = null;
		if (similarityMeasure.equals(SimilarityMeasure.DICE_SIMILARITY_MEASURE)) {
			approximateStringSimilarity = new DiceApproximateStringSimilarityImpl(
					characterNgram, invertedList,
					invertedList.isIgnoreDuplicateNgrams());
		} else if (similarityMeasure
				.equals(SimilarityMeasure.JACCARD_SIMILARITY_MEASURE)) {
			approximateStringSimilarity = new JaccardApproximateStringSimilarityImpl(
					characterNgram, invertedList,
					invertedList.isIgnoreDuplicateNgrams());
		} else if (similarityMeasure
				.equals(SimilarityMeasure.COSINE_SIMILARITY_MEASURE)) {
			approximateStringSimilarity = new CosineApproximateStringSimilarityImpl(
					characterNgram, invertedList,
					invertedList.isIgnoreDuplicateNgrams());
		} else if (similarityMeasure
				.equals(SimilarityMeasure.OVERLAP_SIMILARITY_MEASURE)) {
			approximateStringSimilarity = new OverlapApproximateStringSimilarityImpl(
					characterNgram, invertedList,
					invertedList.isIgnoreDuplicateNgrams());
		}
		// List<String> similarEntries = approximateStringSimilarity
		// .doApproximateStringMatchingUsingAllScan(similarityThreshold);
		List<String> similarEntries = new ArrayList<>();

		if (approximateStringSimilarity != null){
			similarEntries = approximateStringSimilarity
					.doApproximateStringMatchingUsingCPMerge(similarityThreshold);
		}
		else 
		{System.err.println("Unknown ApproximateStringSimilarityImpl: " + similarityMeasure);

		}

		long endSimilarityTime = System.currentTimeMillis();

		out.println("Retrieved entries: ");
		out.println("<br />");
		out.println("<br />");
		for (String str : similarEntries) {
			out.println(str);
			out.println("<br />");
		}
		out.println("<br />");
		out.println("<br />");
		out.println("No. of retrieved entries: " + similarEntries.size());
		out.println("<br />");
		out.println("[TIME INFO] Elapsed time for approximate string matching: "
				+ (endSimilarityTime - startSimilarityTime) + " ms");
		out.println("<br />");
	}

	public static List<String> checkSimilarity(String queryString,
			String gazetteerFilePath, String similarityMeasure,
			double similarityThreshold) throws GazetteerNotLoadedException {

		if (!NEMEX_A.loadedGazetteers.containsKey(gazetteerFilePath)) {
			throw new GazetteerNotLoadedException(
					"ERROR: This vocabulary is not yet loaded!");
		}

		InvertedList invertedList = NEMEX_A.loadedGazetteers
				.get(gazetteerFilePath);

		/*
		 * Generate the n-grams (i.e. the feature set X) for the query string x.
		 */
		CharacterNgram characterNgram = null;
		if (!invertedList.isIgnoreDuplicateNgrams()) {
			characterNgram = new CharacterNgramWithDuplicate(queryString,
					invertedList.getnGramSize());
		} else {
			characterNgram = new CharacterNgramUnique(queryString,
					invertedList.getnGramSize());
		}
		// characterNgram.printNgrams();
		

		/*
		 * Check the similarity
		 */
		

		ApproximateStringSimilarityImpl approximateStringSimilarity = null;
		if (similarityMeasure.equals(SimilarityMeasure.DICE_SIMILARITY_MEASURE)) {
			approximateStringSimilarity = new DiceApproximateStringSimilarityImpl(
					characterNgram, invertedList,
					invertedList.isIgnoreDuplicateNgrams());
		} else if (similarityMeasure
				.equals(SimilarityMeasure.JACCARD_SIMILARITY_MEASURE)) {
			approximateStringSimilarity = new JaccardApproximateStringSimilarityImpl(
					characterNgram, invertedList,
					invertedList.isIgnoreDuplicateNgrams());
		} else if (similarityMeasure
				.equals(SimilarityMeasure.COSINE_SIMILARITY_MEASURE)) {
			approximateStringSimilarity = new CosineApproximateStringSimilarityImpl(
					characterNgram, invertedList,
					invertedList.isIgnoreDuplicateNgrams());
		} else if (similarityMeasure
				.equals(SimilarityMeasure.OVERLAP_SIMILARITY_MEASURE)) {
			approximateStringSimilarity = new OverlapApproximateStringSimilarityImpl(
					characterNgram, invertedList,
					invertedList.isIgnoreDuplicateNgrams());
		}
		// List<String> similarEntries = approximateStringSimilarity
		// .doApproximateStringMatchingUsingAllScan(similarityThreshold);
		List<String> similarEntries = new ArrayList<>();

		if (approximateStringSimilarity != null){
			similarEntries = approximateStringSimilarity
					.doApproximateStringMatchingUsingCPMerge(similarityThreshold);
		}
		else 
		{System.err.println("Unknown ApproximateStringSimilarityImpl: " + similarityMeasure);

		}

		return similarEntries;

	}

	// public ArrayList<String> testGetRandomStringsFromGazetteer(long nos) {
	//
	// long noOfStrings = nos;
	// ArrayList<Long> randomNums = new ArrayList<Long>();
	// ArrayList<String> returnList = new ArrayList<String>();
	//
	// if (this.gazetteer.getNoOfEntries() < nos) {
	// noOfStrings = this.gazetteer.getNoOfEntries();
	// }
	//
	// for (long i = 0; i < noOfStrings; i++) {
	// Random random = new Random();
	// long randomNum = (long) Math.ceil(random.nextDouble()
	// * (this.gazetteer.getNoOfEntries() - 2));
	// randomNums.add(randomNum);
	// }
	//
	// for (int i = 0; i < randomNums.size(); i++) {
	// if (this.gazetteer.getLexicalEntries().containsKey(
	// randomNums.get(i)))
	// returnList.add(this.gazetteer.getLexicalEntries()
	// .get(randomNums.get(i)).get(1));
	// }
	//
	// return returnList;
	// }

}
