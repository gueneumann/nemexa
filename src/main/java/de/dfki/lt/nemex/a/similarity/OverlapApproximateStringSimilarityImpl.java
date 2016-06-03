package de.dfki.lt.nemex.a.similarity;

/*
 *  Overlap similarity measure for approximate String similarity
 *  NEMEX
 *  October 2012
 *  Author: Amir H. Moin (amir.moin@dfki.de)
 *  LT Lab.
 *  German Research Center for Artificial Intelligence
 *  (Deutsches Forschungszentrum fuer Kuenstliche Intelligenz GmbH = DFKI)
 *  http://www.dfki.de
 *  Saarbruecken, Saarland, Germany
 */

import java.math.BigDecimal;
import java.util.List;

import de.dfki.lt.nemex.a.data.InvertedList;
import de.dfki.lt.nemex.a.ngram.CharacterNgram;

/**
 * <H4>
 * Concrete implementation of the Overlap similarity function for Approximate String Similarity.
 * </H4>
 */
public class OverlapApproximateStringSimilarityImpl extends
		ApproximateStringSimilarityImpl {

	public OverlapApproximateStringSimilarityImpl(
			CharacterNgram characterNgram, InvertedList invertedList,
			boolean ignoreDuplicateNgrams) {
		super(characterNgram, invertedList, ignoreDuplicateNgrams);
//		System.out
//				.println("[INFO] Using Overlap similarity measure for approximate string matching...");
	}

	@Override
	protected int findMinSizeForFeatureSetY(double similarityThreshold) {

		return 1;
	}

	@Override
	protected int findMaxSizeForFeatureSetY(double similarityThreshold) {

		return Integer.MAX_VALUE;
	}

	@Override
	protected int findTauMinOverlap(double similarityThreshold,
			List<String> featureSetY) {
		List<String> featureSetX = this.getGrams();

		return (int) Math.ceil(similarityThreshold
				* Math.min(featureSetX.size(), featureSetY.size()));
	}

	@Override
	protected int findTauMinOverlap(double similarityThreshold,
			int featureSetYSize) {
		List<String> featureSetX = this.getGrams();

		return (int) Math.ceil(similarityThreshold
				* Math.min(featureSetX.size(), featureSetYSize));
	}

}