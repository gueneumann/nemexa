package de.dfki.lt.nemex.f.similarity;

import de.dfki.lt.nemex.f.data.NemexFBean;

public abstract class AbstractSimilarityMeasure implements SimilarityMeasureInterface{
private NemexFBean nemexFBean;
	
	public NemexFBean getNemexFBean() {
		return nemexFBean;
	}
	public void setNemexFBean(NemexFBean nemexFBean) {
		this.nemexFBean = nemexFBean;
	}

}
