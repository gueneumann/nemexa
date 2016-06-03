package de.dfki.lt.nemex.f.similarity;

public class EditSimilarityMeasure extends AbstractSimilarityMeasure{

	@Override
	/**
	 * ceiling( (|X| + NGRAM  - 1) * sim-threshold - (NGRAM - 1) ) -> MIN
	 */
	public int findMinSize(int ngramsLength, double similarityThreshold) {
		return (int) Math
				.ceil((ngramsLength + this.getNemexFBean().getnGramSize() -1)
						* similarityThreshold
						- (this.getNemexFBean().getnGramSize() -1));
	}

	@Override
	/**
	 * floor ( (|X| + NGRAM  - 1)/sim-threshold - (NGRAM - 1) ) -> MAX
	 */
	public int findMaxSize(int ngramsLength, double similarityThreshold) {
		return (int) Math
				.floor(((ngramsLength + this.getNemexFBean().getnGramSize() -1)
						/ similarityThreshold)
						- (this.getNemexFBean().getnGramSize() - 1));
	}

	@Override
	/**
	 * ceiling ( max(|e|,|s|) - (max(|e|,|s|) + q - 1) * (1 - similarityThreshold) * q
	 */
	public int findTauMinOverlap(int ngramsLengthQuery, int ngramsLengthTarget,
			double similarityThreshold) {
		return (int) Math
				.ceil((Math.max(ngramsLengthQuery, ngramsLengthTarget)
						- (Math.max(ngramsLengthQuery, ngramsLengthTarget) 
								+ this.getNemexFBean().getnGramSize() - 1)
								* (1 - similarityThreshold) 
								* this.getNemexFBean().getnGramSize()));
	}

	@Override
	/** (ceiling (- entity-length
		      (* (+ entity-length (- (nereid-adm-parameters-ngram-size *nereid-parameters*) 1))
			 (/ (- 1 sim-threshold) sim-threshold)
			 (nereid-adm-parameters-ngram-size *nereid-parameters*)))
		   )
	 */
	public int findLowerBoundOfEntity(int ngramsLengthTarget,
			double similarityThreshold) {
		return (int) Math
				.ceil(ngramsLengthTarget
						- ((ngramsLengthTarget + this.getNemexFBean().getnGramSize() - 1)
								* ((1 - similarityThreshold)/ similarityThreshold) 
								* this.getNemexFBean().getnGramSize())
						);
	}

	@Override
	public int tighterUpperWindowSize(int ngramEntityLength, int subListLength,
			double similarityThreshold) {
		// TODO Auto-generated method stub
		return ngramEntityLength;
	}

}
