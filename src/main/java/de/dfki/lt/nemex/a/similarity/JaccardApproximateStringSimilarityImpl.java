package de.dfki.lt.nemex.a.similarity;

/*
 *  Jaccard similarity measure for approximate String similarity
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
 * Concrete implementation of the Jaccard similarity function for Approximate String Similarity.
 * </H4>
 */
public class JaccardApproximateStringSimilarityImpl extends
		ApproximateStringSimilarityImpl {

	public JaccardApproximateStringSimilarityImpl(
			CharacterNgram characterNgram, InvertedList invertedList,
			boolean ignoreDuplicateNgrams) {
		super(characterNgram, invertedList, ignoreDuplicateNgrams);
//		System.out
//				.println("[INFO] Using Jaccard similarity measure for approximate string matching...");
	}

	@Override
	protected int findMinSizeForFeatureSetY(double similarityThreshold) {

		return (int) Math.ceil((similarityThreshold * this.getGrams().size()));
	}

	@Override
	protected int findMaxSizeForFeatureSetY(double similarityThreshold) {

		return (int) Math
				.floor(((1.0) * this.getGrams().size() / similarityThreshold));
	}

	@Override
	protected int findTauMinOverlap(double similarityThreshold,
			List<String> featureSetY) {
		List<String> featureSetX = this.getGrams();

		return (int) Math.ceil((((1.0) * (similarityThreshold * (featureSetX
				.size() + featureSetY.size())) / (1 + similarityThreshold))));
	}

	@Override
	protected int findTauMinOverlap(double similarityThreshold,
			int featureSetYSize) {
		List<String> featureSetX = this.getGrams();

		return (int) Math.ceil((((1.0) * (similarityThreshold * (featureSetX
				.size() + featureSetYSize)) / (1 + similarityThreshold))));
	}

}