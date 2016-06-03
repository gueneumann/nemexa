package de.dfki.lt.nemex.f.similarity;

import de.dfki.lt.nemex.f.data.NemexFBean;

public interface SimilarityMeasureInterface {

	/**
	 * <p>This computes lower bound for entity e.
	 * <p>Details are in Lemma 2 (sec 2.3) of LiDengFeng, 2011.
	 * <p>Defined as L_o in Mass-Join, Deng et al. 2014.
	 * @param ngramsLength
	 * @param similarityThreshold
	 * @return
	 */
	public int findMinSize(int ngramsLength, double similarityThreshold);
	
	/**
	 * <p>This compute upper bound for entity e.
	 * <p>Details are in Lemma 2 (sec 2.3) of LiDengFeng, 2011.
	 * <p>Defined as L_u in Mass-Join, Deng et al. 2014.
	 * @param ngramsLength
	 * @param similarityThreshold
	 * @return
	 */
	public int findMaxSize(int ngramsLength, double similarityThreshold);
	
	/**
	 * <p>This computes threshold T.
	 * <p>Details are in Lemma 1 (sec 2.2) of LiDengFeng, 2011
	 * @param ngramsLengthQuery
	 * @param ngramsLengthTarget
	 * @param similarityThreshold
	 * @return
	 */
	public int findTauMinOverlap(int ngramsLengthQuery, int ngramsLengthTarget, double similarityThreshold);
	
	/**
	 * <p>This computes threshold T_l for entity e. It corresponds to the lower part of the min overlap computation of Lemma 1
	 * <p>Details are in Lemma 3 (sec 4.1.) of LiDengFeng, 2011
	 * @param ngramsLengthTarget
	 * @param similarityThreshold
	 * @return
	 */
	public int findLowerBoundOfEntity(int ngramsLengthTarget, double similarityThreshold);
	
	/**
	 * Tighter upper bound for candidate window size for dice, jaccard, cosine. See Paper, page 534, second column, last paragraph
	 * @param ngramEntityLength
	 * @param subListLength
	 * @param similarityThreshold
	 * @return
	 */
	public int tighterUpperWindowSize(int ngramEntityLength, int subListLength, double similarityThreshold);
	
	public void setNemexFBean(NemexFBean nemexFBean);
	public NemexFBean getNemexFBean();
}
