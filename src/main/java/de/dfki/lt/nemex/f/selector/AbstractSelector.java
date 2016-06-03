package de.dfki.lt.nemex.f.selector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.lt.nemex.f.data.Candidate;
import de.dfki.lt.nemex.f.data.NemexFBean;

abstract class AbstractSelector implements SelectorInterface{
	private static final Logger LOG = LoggerFactory.getLogger(AbstractSelector.class);

	private Map<Integer, List<Candidate>> candidates ;

	public Map<Integer, List<Candidate>> getCandidates() {
		return candidates;
	}
	public void setCandidates(Map<Integer, List<Candidate>> candidates) {
		this.candidates = candidates;
	}
	
	
	private NemexFBean nemexFBean;
	
	public NemexFBean getNemexFBean() {
		return nemexFBean;
	}
	public void setNemexFBean(NemexFBean nemexFBean) {
		this.nemexFBean = nemexFBean;
	}


	// printFoundMaxBucketCandidates:
	// compute ordered buckets for start positions
	// merge lengths of the elements of a bucket
	// determine range maximum

	
	int getRightMostStartElementWithMaxLength(
			List<Integer> foundBucket, Map<Integer, Map<Integer, Long>> foundCandidates, int leftSpanRelative, int maxLen) {
		for (Integer startElement : foundBucket){
			List<Integer> lengthListofStartElement = 
					new ArrayList<Integer>(foundCandidates.get(startElement).keySet());
			Collections.sort(lengthListofStartElement);
			int lenghOfLastElement = lengthListofStartElement.get(lengthListofStartElement.size()-1);
			if (lenghOfLastElement == maxLen) leftSpanRelative = startElement;
		}
		return leftSpanRelative;
	}

	 int findNextRightBucketSpan(List<Integer> restSortedStartPositionList) {
		int rightSpan = restSortedStartPositionList.size()-1;
		for (int right=0; right < restSortedStartPositionList.size()-1; right++){
			if (restSortedStartPositionList.get(right) < 
					(restSortedStartPositionList.get(right+1)-1)){
				rightSpan = right; break;
			}
		}
		return rightSpan;
	}

	// Receives a sorted list of start elements (of an entity).
	// retrieves for each start element its key list of length elements.
	// and adds them to treeset, which automatically sorts length elements by removing
	// non-unique elements;
	// I think that this is a reasonable operation, because a bucket list is usually small
	 List<Integer> appendLengthList(List<Integer> foundBucket,
			Map<Integer, Map<Integer, Long>> foundCandidates) {
		Set<Integer> lengthList = new TreeSet<Integer>();
		for (Integer startElement : foundBucket){
			List<Integer> lengthListofStartElement = 
					new ArrayList<Integer>(foundCandidates.get(startElement).keySet());
			lengthList.addAll(lengthListofStartElement);

			LOG.info("StartElem: " + startElement + " LenghList: " + lengthListofStartElement);
		}
		return new ArrayList<Integer>(lengthList);
	}
}
