package de.dfki.lt.nemex.f.aligner;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.lt.nemex.f.NemexFContainer;
import de.dfki.lt.nemex.f.similarity.SimilarityMeasureInterface;

/**
 * Core idea of the BinaryCountPruneAligner is to generate substrings for an entity e from 
 * its position list that highly likely contain the entity → |e ∩ s| ≥T_lsize
 * Try to identify valid sublists directly, by simultaneously pruning the bad ones.
 * So, the idea is to use the sorted position list of an entity and to identify substrings 
 * in the input string that contain the elements from the position list in the given order using 
 * the allowed threshold. The last point means that we should allow additional ngrams in the input string
 * which are not part of position list.
 * 
 * The core idea is that already for a sublist of the position list of an entity we can 
 * check whether any substring that contains that sublist can be similar or not, by checking
 * whether the sublist has a min and max number of elements (which is based on the given threshold).
 * If the distance of the first and last element of the sublist in the substring is too small or 
 * too large, we can prune that sublist. Otherwise, we have to identify the exact substring window.
 * @author gune00
 *
 */
public class BinaryCountPruneAligner implements AlignerInterface {
	private static final Logger LOG = LoggerFactory.getLogger(BinaryCountPruneAligner.class);

	public BinaryCountPruneAligner(){;
	}

	/**
	 * Applies lazy-count pruning check on entity.
	 * If ok, then initializes the lower/upper bounds on entity length,
	 * also the count-array
	 * and calls the binary count pruning strategy.
	 * Finally, returns the found candidates.
	 * The main caller is very similar to the one defined for {@link BucketCountPruneAligner}
	 */
	@Override
	public Map<Integer, Map<Integer, Long>> findSubstringsForEntity(
			Long entityIndex, int entityNgramLength,
			SimilarityMeasureInterface simFct, double simThreshold,
			NemexFContainer container) {

		// Lazy-count pruning: 
		// T_l: lower bound of occurrence number of entity in window: if the number P_e 
		// for entity e is smaller than T_l we can prune e
		// if length of entity-position-list is < T_l, then prune entity
		// which mean: do not enumerate-candidate-pairs

		if (!(container.getPositionList().get(entityIndex).size()
				<
				container.getLowerEntityLength())
				)
		{
			// Initialize entity boundaries only if useful, i.e., entity has more than T_l overlappping ngrams
			// NOTE also performing reset of count hashmap
			container.initializeEntityBoundaries(
					entityNgramLength, simFct, simThreshold);

			// Do the binary count pruning
			binaryCountPruning(entityIndex, container);

			return container.getCandidatePairs(
					entityIndex, entityNgramLength, simFct, simThreshold);
		}
		else
			return null;
	}

	private void binaryCountPruning(Long entityIndex, NemexFContainer container) {
		// Note: the position list of e holds the occurrence of e for the whole document/segment
		List<Integer> sortedPositionList = container.getPositionList().get(entityIndex);
		// SORT the position list.
		Collections.sort(sortedPositionList);

		/* tau-e = upper bound of token numbers
		 * Upper bound of entity length for candidate substrings. 
		 * It means: substrings which are too long cannot be similar
		 */
		int upperBound = container.getMaxSizeForFeatureSetY();
		/* T-l = pruning threshold for approximating |intersect(e,s)| -> approximate lower bound
		 * T-l is the minimum number of shared elements between e and s so that they are similar
		 * under threshold
		 * It means: substrings which are too small cannot be similar
		 */
		int lowerEntityLength = container.getLowerEntityLength();

		LOG.info("*** entityIndex: " + entityIndex +"\n" +
				".... Position list: " + sortedPositionList + "; Size: " + sortedPositionList.size()
				+ " Lower/Upper: " + lowerEntityLength + ", " + upperBound
				);

		// Call the main horse 
		if (lowerEntityLength > 0)
			findCandidateWindows(sortedPositionList, lowerEntityLength, upperBound, container);
	}

	/**
	 * <p>Formally, if a valid substring s is similar to entity e, they
	 * must share enough common tokens (|e ∩ s| ≥ T-l).
	 * <p>A window is a sublist of the sorted position list P_e of entity e.
	 * <p>A valid window is a window with T-l <= P_e[i,...,j] <= tau_e
	 * <p>A candidate window is a valid window with t_e <= D[p_i,...,p_j]<= T_e.
	 * <p>Given the sorted position list and the length restrictions, identify the left
	 * and right parenthesis of the candidate window.
	 * @param sortedPositionList the sorted position list of entity e
	 * @param lowerEntityLength the lower bound of length for candidate windows -> when P_e is smaller than that can be pruned
	 * @param upperBound upper bound of length for candidate windows
	 * @param container
	 */
	private void findCandidateWindows(List<Integer> sortedPositionList,
			int lowerEntityLength, int upperBound, NemexFContainer container) {
		/* see algorithm 1, page 537 */
		int leftI = 1;
		/* loop over position list of entity e from left to right with leftI
		 * and use rightJ for getting the current sub-position list of e to check
		 * */
		/* Loop ends for i so that correct r can be computed */
		while (leftI <= (sortedPositionList.size() -  lowerEntityLength + 1)) {
			/* rightJ is set so that we initialize the first valid window as P_e[1 ... T_l], 
			 * see page 535 last sentence*/
			int rightJ = (leftI + lowerEntityLength - 1);

			LOG.info("Find substring with Span: " + leftI + ", " + rightJ 
					+ "; Loop while: " + (sortedPositionList.size() -  lowerEntityLength + 1)
					);

			// Now get the position list elements for the sublist
			int pj = sortedPositionList.get(rightJ - 1); // indexing starts from 0
			int pi = sortedPositionList.get(leftI - 1);
			List<Integer> subList = sortedPositionList.subList(leftI-1, rightJ);
			LOG.info("... valid substring ?: " + subList);
			LOG.info("... Substring: " + container.getNgramInputText().subList(0, subList.size()));
			LOG.info("... Pj " + pj + " - Pi  " + pi + " + 1 = " + (pj - pi + 1) + " <= " + upperBound + "?");

			// Length for substring  |D[pi...pj]|=pj-pi+1 is not larger than upperBound
			if ((pj - pi + 1) <= upperBound) {
				/* we have a valid substring with size
				 *t-l ≤ |Pe[i · · · j]| ≤ TAU-e
				 *Hence, find candidate window
				 * ⊥e ≤ |D[pi · · · pj ]| ≤ TAU-e
				 * Inside binaryInputSpan, we will use eventually tighter upper bounds depending on the simFct
				 */
				binaryInputSpan(leftI, rightJ, sortedPositionList, upperBound, container);
				leftI++; /* shift to the next substring */
			}
			else
			{	// candidate windows are too long
				// skip elements of invalid substrings by jumping leftI-> seems to be ok
				//leftI++;
				leftI = binaryInputShift(leftI, rightJ, sortedPositionList, lowerEntityLength, upperBound);
				LOG.info("Span after binary shift: " + leftI + 
						",  " + (leftI + lowerEntityLength - 1));
			}
		}
	}

	/**
	 * <p><b>The basic idea of binary shift and binary span is as follows:</b>
	 * <p>Given a valid window Pe[i · · · j], if pj −pi +1 > TAUe,
	 * we will not shift to Pe[(i + 1) · · · (j + 1)]. 
	 * 
	 * <p>Instead we want to directly shift to the first possible candidate window after i, 
	 * denoted by Pe[mid · · · (mid + j − i)], where mid satisfies
	 * 
	 * pmid+j−i − pmid + 1 ≤ TAU-e and for any i ≤ mid' < mid,
	 * 
	 * pmid'+j−i − pmid' + 1 > TAU-e. 
	 * 
	 * Similarly, if pj − pi + 1 ≤ e,
	 * 
	 * we will not iteratively span it to Pe[i · · · (j +1)], 
	 * 
	 * Pe[i · · · (j +2)], . . . , Pe[i · · · x]. 
	 * 
	 * Instead, we want to directly span to the
	 * last possible candidate window starting with i, denoted by
	 * Pe[i · · · x], where x satisfies px − pi + 1 ≤  e and for any x' > x, px' − pi + 1 > e.
	 * <p>
	 * <p> <b>More details</b>:
	 * <p>The binary span operation can directly span to Pe[i · · · x] and has two advantages. 
	 * <p>Firstly, in many applications, users want to identify the best similar pairs 
	 * (sharing common tokens as many as possible), and the binary span can efficiently
	 * find such substrings. 
	 * <p>Secondly, we do not need to find candidates of e for 
	 * Pe[i · · · (j + 1)], . . . , Pe[i · · · x] one by one. 
	 * <p>Instead since there may be many candidates between
	 * lo = pj −TAU-e + 1 and up = pi+x−j + TAU-e − 1, we find
	 * them in a batch manner. We group the candidates based on
	 * their token numbers. 
	 * <p>Entities in the same group have the same number of tokens. 
	 * Consider the group with g tokens,
	 * suppose Tg is the threshold computed using |e| and g. If
	 * |Pe[i · · · x]| < Tg, we prune all candidates in the group.
	 */

	// I think the implementation is correct, because I get still the same problems even when I do not call it and calling
	// leftI++ instead
	private void binaryInputSpan(int leftI, int rightJ,
			List<Integer> sortedPositionList, int upperBound,
			NemexFContainer container) {
		LOG.info(" Entering binaryInputSPAN with Span: " + leftI + ", " + rightJ + " upper: " + upperBound);

		int lower = rightJ;
		int upper = leftI + upperBound - 1;
		int mid = 0;

		while (lower <= upper) {
			LOG.info("..... lower span: "+ lower + " + upper span: " + upper);

			mid = (int) Math.ceil((upper + lower)/2.0);	

			/* MID is new right span eventually larger than rightJ */
			LOG.info("........ mid span: "+ mid);

			if ((mid <= sortedPositionList.size())){
				LOG.info("..... Pmid("+mid+"): " + sortedPositionList.get(mid - 1) 
						+ "; Pi("+leftI+"): " + sortedPositionList.get(leftI - 1));
			}

			/* Checks a sublist P_e[i ... MID]*/

			if ((mid <= sortedPositionList.size()) 
					&& // added by GN -> make sure that spans are not violated
					// Substring D[leftI...mid] is too long
					((sortedPositionList.get(mid - 1) 
							- sortedPositionList.get(leftI - 1) + 1) 
							> upperBound)) {
				upper = mid - 1;
			}
			else
			{
				lower = mid + 1;
			}
		}
		mid = upper;

		LOG.info("........ Final mid: "+ mid + "; P_e-Lenght: " + sortedPositionList.size());

		/* I do this because it can happen that upper would jump behind sorted list length, 
		 * but if I do not do it, I might miss some candidates.
		 *  I assume it is correct, because string is not too long!
		 */
		
		if (mid >= sortedPositionList.size()) {mid = sortedPositionList.size();}
		{
			enumerateCandidateWindows(sortedPositionList.subList(leftI-1, mid), container);
		}
	}

	/* TODO There seems to be a problem here. It seems not to be correct. It seems
	 * that I compute to many candidates but fewer NEs than bucketprune
	 * Maybe I need a special enumerate candidate pairs here, maybe using distance here ?
	 * Maybe I increment too often count-array?
	 */

	private void enumerateCandidateWindows(List<Integer> subList, NemexFContainer container) {
		/* Seems to be similar to lazy count pruning and BinaryCountPruner.findBuckets
		 * 
		 * Idea is probably because D[i ... mid] <= T_e
		 * From paper, page 534: 
		 * If |D[pi · · · pj ]| = pj−pi+1 > T_e, any valid substring containing all tokens in D[pi · · · pj ] 
		 * has larger than e tokens. Thus we can prune Pe[i · · · j]. 
		 * On the contrary, D[pi · · · pj] may be similar to e if ⊥e ≤ |D[pi · · · pj ]| ≤ T_e.
		 * */

		int pi = subList.get(0);
		int pj = subList.get(subList.size()-1);
		int distance = pj - pi + 1;
		int lo = container.getLowerEntityLength();
		int up = container.computeUpperBoundWindowSize(subList);
		

		if ( (pj!=0) && (lo <= distance) && (distance <= up))
		{
			LOG.info("<-- PosList: "+ subList + ", pi-1=" + (pi-1) + " pj=" + pj);
			LOG.info("<-- Substring: " + container.getNgramInputText().subList(pi, pj));
			LOG.info("... Lo=" + lo + " <= " + "Dist=" + distance + " <= Up=" + up);
			LOG.info("--> Enumerate candidates: " + subList);
			container.enumerateCandidatePairs(subList, lo, up);
		}	
	}

	/**
	 * <p><b>The basic idea of binary shift  is as follows:</b> 
	 * <p>If pj − pi + 1 ≤ TAU-e
	 * we will not iteratively span it to Pe[i · · · (j +1)], 
	 * Pe[i · · · (j + 2)], . . . , Pe[i · · · x]. 
	 * <p>Instead, we want to directly span to the
	 * last possible candidate window starting with i, denoted by
	 * Pe[i · · · x], where x satisfies px − pi + 1 ≤ TAU-e and for any
	 * x'> x, px' − pi + 1 > TAU-e
	 */

	private int binaryInputShift(int leftI, int rightJ,
			List<Integer> sortedPositionList, int lowerEntityLength,
			int upperBound) {
		LOG.info(" Entering binaryInputSHIFT with Span: " + leftI + ", " + rightJ);
		int lower = leftI;
		int upper = rightJ;
		while (lower <= upper){
			// System.out.println("..... lower: "+ lower + " + upper: " + upper);
			/* mid points to the last possible i. 
			 * It is computed using a binary search.*/
			int mid = (int) Math.ceil((upper + lower)/2.0);
			// System.out.println("........ mid: "+ mid);

			int pj = sortedPositionList.get(rightJ - 1);
			int pmid = sortedPositionList.get(mid - 1);
			// System.out.println("..... Pj("+rightJ+"): " + pj + "; Pmid("+mid+"): " + pmid);
			/* Depending on the upper bound condition, move i to the left/right, which means
			 * that the start of sublist is shrinked or stretched .*/
			if (((pj + (mid - leftI)) - pmid + 1) > upperBound) 
				/* Move i closer to j */
				lower = mid + 1;
			else
				/* Move j closer to i */
				upper = mid - 1;
		}
		leftI = lower; 
		rightJ = leftI + lowerEntityLength - 1;
		if ((leftI <= rightJ) && (rightJ <= sortedPositionList.size()) 
				// above added by GN, it makes sense: left should be not larger than right and right should not be larger than sublist
				&& 
				// pj-pi+1 ::= |D[pi...pj]|
				(sortedPositionList.get(rightJ - 1) 
						- 
						sortedPositionList.get(leftI - 1)
						+ 1) > upperBound) {
			/* Current sublist can be pruned because it is too long, so shift to next possible candidate*/
			// Recursive call here !
			leftI = binaryInputShift(leftI, rightJ, 
					sortedPositionList, lowerEntityLength, upperBound);

		}
		/* else return rightmost P_i */
		return leftI;
	}
}
