package de.dfki.lt.nemex.f.data;

import java.util.List;

public class Candidate {
	private  long entityIndex ;
	private  List<String> dictionaryEntry;
	private int leftSpan ;
	private int rightSpan ;
	private String matchedSurfaceString ;
	
	// GN 
	// I added this variable in ordre to store the real similarity score for the pair (entityIndex, matchedSurfaceString)
	private double score = 0.5;
	
	
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public long getEntityIndex() {
		return entityIndex;
	}
	public void setEntityIndex(long entityIndex) {
		this.entityIndex = entityIndex;
	}
	public List<String> getDictionaryEntry() {
		return dictionaryEntry;
	}
	public void setDictionaryEntry(List<String> dictionaryEntry) {
		this.dictionaryEntry = dictionaryEntry;
	}
	public int getLeftSpan() {
		return leftSpan;
	}
	public void setLeftSpan(int leftSpan) {
		this.leftSpan = leftSpan;
	}
	public int getRightSpan() {
		return rightSpan;
	}
	public void setRightSpan(int rightSpan) {
		this.rightSpan = rightSpan;
	}
	public String getMatchedSurfaceString() {
		return matchedSurfaceString;
	}
	public void setMatchedSurfaceString(String matchedSurfaceString) {
		this.matchedSurfaceString = matchedSurfaceString;
	}

	public Candidate(int leftSpan, int rightSpan,
			String matchedSurfaceString, long entityId) {
		this.setEntityIndex(entityId);
		this.setLeftSpan(leftSpan);
		this.setRightSpan(rightSpan);
		this.setMatchedSurfaceString(matchedSurfaceString);
	}

	public String toString(){
		String printString ="";
		printString =	this.getMatchedSurfaceString()+"=[" + leftSpan + "," + rightSpan + "]" + ":" + this.score;
		return printString;
	}
}
