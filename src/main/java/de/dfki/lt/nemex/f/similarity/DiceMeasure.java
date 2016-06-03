package de.dfki.lt.nemex.f.similarity;

public class DiceMeasure extends AbstractSimilarityMeasure {

	@Override
	/** similarityThreshold/2-similarityThreshold * |X| -> aufgerundet
	 * 
	 */
	public int findMinSize(int ngramsLength, double similarityThreshold) {
		return (int) Math
				.ceil((ngramsLength * 
						(similarityThreshold /
								(2 - similarityThreshold))
						));
	}

	@Override
	/** (2-similarityThreshold/similarityThreshold) * |X| -> abgerundet
	 * 
	 */
	public int findMaxSize(int ngramsLength, double similarityThreshold) {
		return (int) Math
				.floor((ngramsLength * 
						(2 - similarityThreshold) /
						similarityThreshold
						));
	}

	@Override
	/** similarityThreshold/2 * (|X|+|Y|)
	 * 
	 */
	public int findTauMinOverlap(int ngramsLengthQuery, int ngramsLengthTarget,
			double similarityThreshold) {
		return (int) Math
				.ceil(((similarityThreshold / 2) *
						(ngramsLengthQuery + ngramsLengthTarget)));
	}

	@Override
	/** similarityThreshold/2-similarityThreshold * |X| -> aufgerundet
	 * 
	 */
	public int findLowerBoundOfEntity(int ngramsLengthTarget,
			double similarityThreshold) {
		return (int) Math
				.ceil((ngramsLengthTarget * 
						(similarityThreshold /
								(2 - similarityThreshold))
						));
	}

	/**
	 * low_e <= |D[pi...pj]| <= min(|e|, |P_e[pi,...,pj]|) * (2-THRESHOLD/THRESHOLD)
	 */
	@Override
	public int tighterUpperWindowSize(int ngramEntityLength, int subListLength,
			double similarityThreshold) {
		return (int) (Math
				.min(ngramEntityLength, subListLength) * ((2 - similarityThreshold) / similarityThreshold)
				);
	}

}
