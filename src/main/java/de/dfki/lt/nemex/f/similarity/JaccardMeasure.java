package de.dfki.lt.nemex.f.similarity;

public class JaccardMeasure extends AbstractSimilarityMeasure {

	@Override
	/**
	 * alpha * |X| -> aufgerundet
	 */
	public int findMinSize(int ngramsLength, double similarityThreshold) {
		return (int) Math
				.ceil((similarityThreshold * ngramsLength));
	}

	@Override
	/**
	 * |X|/ alpha -> abgerundet 
	 */
	public int findMaxSize(int ngramsLength, double similarityThreshold) {
		return (int)  Math
				.floor((ngramsLength / similarityThreshold));
	}

	@Override
	/**
	 * |E INTERS. S| >= (|E|+|S|) * THRESHOLD/(1+THRESHOLD)
	 */
	public int findTauMinOverlap(int ngramsLengthQuery, int ngramsLengthTarget,
			double similarityThreshold) {
		return (int) Math
				.ceil((ngramsLengthQuery + ngramsLengthTarget) *
						(similarityThreshold / 
								(1 + similarityThreshold)));
	}

	@Override
	/**
	 * alpha * |X| -> aufgerundet
	 */
	public int findLowerBoundOfEntity(int ngramsLengthTarget,
			double similarityThreshold) {
		return (int) Math
				.ceil((similarityThreshold * ngramsLengthTarget));
	}

	/**
	 * low_e <= |D[pi...pj]| <= min(|e|, |P_e[pi,...,pj]|)/THRESHOLD
	 */
	@Override
	public int tighterUpperWindowSize(int ngramEntityLength, int subListLength,
			double similarityThreshold) {
		return (int)
				(Math.min(ngramEntityLength, subListLength)/(similarityThreshold));
	}
}
