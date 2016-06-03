package de.dfki.lt.nemex.f.aligner;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.lt.nemex.f.NemexFContainer;
import de.dfki.lt.nemex.f.similarity.SimilarityMeasureInterface;

/**
 * <p>Bucket-Count pruning
 * 
 * <p>a bucket: a subsequence of the position list, with "close-enough" elements
 * <p>Formally: given two neighbor elements from P_e, p_i and p_i+1, any substring containing
 * <p>both elements, has at least (p_i+1 - p_i - 1) mismatched elements. -> These are just the elements
 * <p>that are between p_i and p_i+1. Since p_i and p_i+1 belong to entity e, the elements between them
 * <p>cannot belong to them. Thus if the the size of these other elements is too large compared to threshold, e can be ignored
 * <p>for this substring.
 * 
 * @author gune00
 *
 */

public class BucketCountPruneAligner implements AlignerInterface {
	private static final Logger LOG = LoggerFactory.getLogger(BucketCountPruneAligner.class);
	public BucketCountPruneAligner(){;
	}

	// Lazy-count pruning: 
	// T_l: lower bound of occurrence number of entity in window: if the number P_e 
	// for entity e is smaller than T_l we can prune e
	// TAU = ( T_e - T_l )
	// (defun unvalid-neighbor-distance (p1 p2 Tau) (> (- p2 p1 1) Tau))

	/**
	 * Applies lazy-count pruning check on entity.
	 * If ok, then initializes the lower/upper bounds on entity length
	 * and calls the bucket count pruning strategy.
	 * Finally, returns the found candidates.
	 */
	@Override
	public Map<Integer, Map<Integer, Long>> findSubstringsForEntity(
			Long entityIndex, int entityNgramLength,
			SimilarityMeasureInterface simFct, double simThreshold,
			NemexFContainer container) {

		// Lazy-count pruning: 
		// if length of entity-position-list is < T_l, then prune entity
		// which mean: do not enumerate-candidate-pairs

//		LOG.info("access pos list: " + entityIndex + " " + container.getPositionList().get(entityIndex).toString());
		if (!(container.getPositionList().get(entityIndex).size()
				<
				container.getLowerEntityLength())
				)
		{
			// Initialize entity boundaries only if useful
			// NOTE also reset count array
			container.initializeEntityBoundaries(
					entityNgramLength, simFct, simThreshold);

			// Do the bucket count pruning
			bucketCountPruning(entityIndex, container);

			return container.getCandidatePairs(
					entityIndex, entityNgramLength, simFct, simThreshold);
		}
		else
			return null;
	}

	// Pruner specific functions

	/*
	 * main caller for bucket-count-pruning
	 * sort position list, compute threshold, compute buckets incrementally
	 * For each bucket, check whether it is a valid substring and if so store the candidates
	 */

	private void bucketCountPruning(Long entityIndex, NemexFContainer container) {
		// Do I need to copy the list first ? -> no, seems to be ok so.
		List<Integer> sortedPositionList = container.getPositionList().get(entityIndex);
		// SORT the position list.
		Collections.sort(sortedPositionList);

		int upperBound = container.getMaxSizeForFeatureSetY();
		/* T-l = pruning threshold for approximating |intersect(e,s)| -> approximate lower bound
		 * T-l is the minimum number of shared elements between e and s so that they are similar
		 * under threshold
		 * It means: substrings which are too small cannot be similar
		 */
		int lowerEntityLength = container.getLowerEntityLength();

//		LOG.info("*** entityIndex: " + entityIndex +"\n" +
//				".... Position list: " + sortedPositionList + "; Size: " + sortedPositionList.size()
//				+ " Lower/Upper: " + lowerEntityLength + ", " + upperBound
//				);

		int approximateEntityLength = (upperBound - lowerEntityLength);
		findBuckets(approximateEntityLength, sortedPositionList, container);	
	}

	/*
	 * Now, find all buckets for complete position list
	 * for each found bucket (a sublist of the sorted position list), 
	 * count entity occurrence
	 * -> this is what I mean by incremental approach
	 * This is how I understand the text on page 534 (second paragraph)
	 * I think this is correct, because a position list stores the ngram occurrence of 
	 * an entity e in the complete document, and as just, all possible occurrences of e in 
	 * the document. A bucket represents a single possible occurrence of a substring.
	 */

	// Non-recursive version:
	// Idea: iterate over position list which gives us left span for new bucket
	// calls findBucket() to get right span for new bucket
	// check the subsequence of position list for candidate matching string


	private void findBuckets(int approximateEntityLength,
			List<Integer> sortedPositionList, NemexFContainer container) {
		for (int leftSpan = 0; leftSpan < sortedPositionList.size(); leftSpan++){
			// get the rest of sorted position list for which we want to find new buckets
			List<Integer> restSortedPositionList = 
					sortedPositionList.subList(leftSpan, sortedPositionList.size());
			// we found the right span of a new bucket
			int rightSpan = leftSpan + this.findBucket(approximateEntityLength, restSortedPositionList);
			// So, extract the bucket from the sorted position list
			List<Integer> bucket = sortedPositionList.subList(leftSpan, rightSpan+1);

			//			System.out.println("Rest Sorted PosList: " + restSortedPositionList 
			//			+ " Approx Entity length: " + approximateEntityLength);
			//			System.out.println("Left: " + leftSpan + "; Right " + rightSpan);
			//			System.out.println("Bucket " + bucket + " Entity length: " + container.getLowerEntityLength());

			// Lazy-count pruning: 
			// if length of entity-position-list is < T_l, then prune entity
			// which mean: do not enumerate-candidate-pairs
			if (!(bucket.size()
					<
					container.getLowerEntityLength())
					){
				//System.out.println("Passed Bucket " + bucket);
				container.enumerateCandidatePairsMain(bucket);
			}		
			// skip bucket which means move left span by right span positions
			leftSpan = rightSpan;
		}
	}

	/*
	 * A bucket is a sublist of a sorted position list of an entity e
	 * where the sequence of ngrams belong to e modulo those elements
	 * which are allowed because of the similarity threshold (these are ngrams which do not belong
	 * to e). 
	 * findBucket() starts with an initialized bucket using the first element of position list
	 * and checks for the remaining elements whether they are to be added to the bucket. For the first
	 * element for which this does not hold, return the bucket and the remaining position list
	 * (make-bucket 2 '(2 3 4 9 14 19) '(1)) -> ((4 3 2 1) 9 14 19)
	 */

	/*
	 * For Java: non-recursive version
	 * Compute left and right span of a bucket.
	 * Iterate through the rest list of the sorted position list and return just the end-point of the found bucket.
	 * The rest list is just the previous rest list without the found bucket. It is initialized with the
	 * sorted position list.
	 * The initial bucket consists always of the first element, so start iteration from second element.
	 */

	private int findBucket(int approximateEntityLength, List<Integer> restSortedPositionList){
		// initialize right span of bucket
		int rightSpan = 0;
		for (int i = 1; i < restSortedPositionList.size(); i++){
			if (this.unvalidNeighborDistance(
					restSortedPositionList.get(rightSpan), 
					restSortedPositionList.get(i), 
					approximateEntityLength))
				// If distance is too far for two adjacent elements in the position list
				// we have found the right span of the bucket and stop.
				break;
			else
				// otherwise we extend the right span by one
				rightSpan = i;
		}
		return rightSpan;
	}

	private boolean unvalidNeighborDistance(int p1, int p2, int tau){
		//		System.out.println("p2: " + p2 + ", p1: " + p1 + ", -1 > " 
		//				+ " tau: " + tau + " is " + ((p2 - p1 - 1) > tau));
		return ( (p2 - p1 - 1) > tau);
	}
}
