package de.dfki.lt.nemex.f.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Candidates {
	private String gazetteerFilePath = "";
	
	public String getGazetteerFilePath() {
		return gazetteerFilePath;
	}

	public void setGazetteerFilePath(String gazetteerFilePath) {
		this.gazetteerFilePath = gazetteerFilePath;
	}

	private Map<Long, List<Candidate>> candidates = new HashMap<Long, List<Candidate>>();
	
	private int cnt = 0;
	

	public int getCnt() {
		return cnt;
	}

	public void setCnt(int cnt) {
		this.cnt = cnt;
	}

	public Map<Long, List<Candidate>> getCandidates() {
		return candidates;
	}

	public void setCandidates(Map<Long, List<Candidate>> candidates) {
		this.candidates = candidates;
	}
	
	public Candidates(String gazetteerFilePath){
		this.setGazetteerFilePath(gazetteerFilePath);
	}
	
	public void addCandidates(long entityIndex, List<Candidate> candidates){
		this.cnt = this.cnt + candidates.size();
		this.getCandidates().put(entityIndex, candidates);
	}
	
	public String toString() {
		String printString = "";
		for (long entityId : this.getCandidates().keySet()) {
			List<Candidate> candidates = this.getCandidates().get(entityId);
			if (!candidates.isEmpty()) {
				printString = printString + entityId + "=" + NemexFIndex.getEntry(this.getGazetteerFilePath(), entityId)
						+ "\n";
				printString = printString + candidates.toString() + "\n";
			}
		}
		return printString;
	}
}
