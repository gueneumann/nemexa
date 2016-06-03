package de.dfki.lt.nemex.f.similarity;

/**
 * According to paper Li et al, SIGMOD, 2011.
 * Note here: sim-threshold denotes the maximum number of allowable edit operations
 * @author gune00
 *
 */
public class EditDistanceMeasure extends AbstractSimilarityMeasure {

	@Override
	/**
	 * |X| -  similarityThreshold -> MIN
	 */
	public int findMinSize(int ngramsLength, double similarityThreshold) {
		return (int) (ngramsLength - similarityThreshold);
	}

	@Override
	/**
	 * |X| +  similarityThreshold -> MAX
	 */
	public int findMaxSize(int ngramsLength, double similarityThreshold) {
		return (int) (ngramsLength + similarityThreshold);
	}

	@Override
	/**
	 * Edit distance min overlap T: max(|e|,|s|) - similarityThreshold * q
	 */
	public int findTauMinOverlap(int ngramsLengthQuery, int ngramsLengthTarget,
			double similarityThreshold) {
		return (int) (Math.max(ngramsLengthQuery, ngramsLengthTarget) -
			     similarityThreshold * this.getNemexFBean().getnGramSize());
	}

	@Override
	/**
	 * |X| -  (similarityThreshold * q)
	 */
	public int findLowerBoundOfEntity(int ngramsLengthTarget,
			double similarityThreshold) {
		return (int) (ngramsLengthTarget - similarityThreshold * this.getNemexFBean().getnGramSize()); 
	}

	@Override
	public int tighterUpperWindowSize(int ngramEntityLength, int subListLength,
			double similarityThreshold) {
		return ngramEntityLength;
	}

}
