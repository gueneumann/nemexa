package de.dfki.lt.nemex.f.selector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.lt.nemex.f.data.Candidate;
import de.dfki.lt.nemex.f.data.NemexFBean;

public class NoSelector extends AbstractSelector {
	private static final Logger LOG = LoggerFactory.getLogger(NoSelector.class);

	private Map<Integer, List<Candidate>> candidates ;

	public Map<Integer, List<Candidate>> getCandidates() {
		return candidates;
	}
	public void setCandidates(Map<Integer, List<Candidate>> candidates) {
		this.candidates = candidates;
	}
	
	public NoSelector(){
		super();
	}
	
	public NoSelector(NemexFBean nemexFbean){
		super();
		this.setNemexFBean(nemexFbean);
	}

	// factorize out all macthing substrings with correct span
	// TODO THIS IS probably inefficient and can be made easier !!, I will do it when I have a bit more time.

	@Override
	public List<Candidate> BucketCandidates (long entityId, Map<Integer, Map<Integer, Long>> foundCandidates){
		List<Candidate> candidatesForEntityId = new ArrayList<Candidate>();

		List<Integer> sortedStartPositionList = new ArrayList<Integer>(foundCandidates.keySet());
		Collections.sort(sortedStartPositionList);

		for (int leftBucketSpan=0; leftBucketSpan < sortedStartPositionList.size(); leftBucketSpan++){
			int rightBucketSpan = leftBucketSpan;
			// get the corresponding sorted sublist from sortedStartPositionList
			List<Integer> foundBucket = sortedStartPositionList.subList(leftBucketSpan, rightBucketSpan+1);
			// get the merged sorted and unique list of length elements from bucket (using TreeSet class)
			List<Integer> lengthList = appendLengthList(foundBucket, foundCandidates);

			// Determine range: maximum string

			// leftSpan is the start position of a bucket in the input string (using the ngram-representation)
			int leftSpan = foundBucket.get(0);

			// len is the maximum length (the last element of the lengthList) 
			int len = lengthList.get(lengthList.size()-1);

			// leftSpanrelative is the rightmost  startElement which contains len
			int leftSpanRelative = getRightMostStartElementWithMaxLength(foundBucket, foundCandidates, leftSpan, len);

			LOG.info("leftSpanRelative: " + leftSpanRelative + " =?= middleLen: " + len);

			//rightSpan is the end position of the bucket in the input string (using the ngram-representation)
			int rightSpan = (leftSpanRelative + len + this.getNemexFBean().getnGramSize() - 1);

			LOG.info("\nLeftBucketSpan: "+leftBucketSpan+", RightBucketSpan: " + rightBucketSpan 
					+ ";\nStartBucket: " + foundBucket + ";\nLengthsList: " + lengthList);

			String matchedSurfaceString = this.getNemexFBean().getQueryString().substring(leftSpan, rightSpan);
			Candidate maxCandidate = new Candidate(leftSpan, rightSpan, matchedSurfaceString, entityId);
			candidatesForEntityId.add(maxCandidate);

			// Jump to next bucket
			leftBucketSpan=rightBucketSpan;
		}
		return candidatesForEntityId;
	}
}
