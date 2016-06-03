package de.dfki.lt.nemex.f.selector;

import java.util.List;
import java.util.Map;

import de.dfki.lt.nemex.f.data.Candidate;
import de.dfki.lt.nemex.f.data.NemexFBean;

public interface SelectorInterface {
	public List<Candidate> BucketCandidates (long entityId, Map<Integer, Map<Integer, Long>> foundCandidates);
	public void setNemexFBean(NemexFBean nemexFBean);
	public NemexFBean getNemexFBean();
	public String toString();
}
