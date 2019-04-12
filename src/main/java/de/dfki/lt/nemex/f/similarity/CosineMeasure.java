package de.dfki.lt.nemex.f.similarity;

public class CosineMeasure extends AbstractSimilarityMeasure {

	/**
	 * similarityThreshold^2 * |X| -> aufgerundet
	 */
	@Override
	public int findMinSize(int ngramsLength, double similarityThreshold) {
		return (int) Math
				.ceil((similarityThreshold * similarityThreshold * ngramsLength));
	}

	/**
	 * |X|/ similarityThreshold^2 -> abgerundet 
	 */
	@Override
	public int findMaxSize(int ngramsLength, double similarityThreshold) {
		return (int) Math
				.floor((ngramsLength / (similarityThreshold * similarityThreshold)));
	}

	@Override
	public int findTauMinOverlap(int ngramsLengthQuery, int ngramsLengthTarget, double similarityThreshold) {
		return (int) Math.
				ceil((similarityThreshold * Math.sqrt(ngramsLengthQuery * ngramsLengthTarget)));
	}

	/**
	 * similarityThreshold^2 * |X| -> aufgerundet
	 */
	public int findLowerBoundOfEntity(int ngramsLengthEntity, double similarityThreshold){
		return (int) Math
				.ceil((similarityThreshold * similarityThreshold * ngramsLengthEntity));
	}

	/**
	 * low_e <= |D[pi...pj]| <= min(|e|, |P_e[pi,...,pj]|)/(THRESHOLD*THRESHOLD)
	 */
	@Override
	public int tighterUpperWindowSize(int ngramEntityLength, int subListLength,
			double similarityThreshold) {
		return (int)
				(Math.min(ngramEntityLength, subListLength)/(similarityThreshold*similarityThreshold));
	}
	
}
