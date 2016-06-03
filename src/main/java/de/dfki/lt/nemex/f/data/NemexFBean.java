package de.dfki.lt.nemex.f.data;

import de.dfki.lt.nemex.f.aligner.AlignerInterface;
import de.dfki.lt.nemex.f.selector.SelectorInterface;
import de.dfki.lt.nemex.f.similarity.CosineMeasure;
import de.dfki.lt.nemex.f.similarity.DiceMeasure;
import de.dfki.lt.nemex.f.similarity.EditDistanceMeasure;
import de.dfki.lt.nemex.f.similarity.EditSimilarityMeasure;
import de.dfki.lt.nemex.f.similarity.JaccardMeasure;
import de.dfki.lt.nemex.f.similarity.SimilarityMeasure;
import de.dfki.lt.nemex.f.similarity.SimilarityMeasureInterface;

public class NemexFBean {
	/*
	 * START OF CONFIGURATIONS
	 */

	/*
	 * 1. Query String x
	 */

	private String queryString = "";

	/*
	 * 2. Gazetteer V
	 */
	private String gazetteerFilePath = "";

	/*
	 * 3. Delimiter within the Multi-Word Lexical (MWL) entries in the
	 * Gazetteer.
	 */
	private String delimiter = "#";

	/*
	 * 4. Set this to 'true' only if you would like to switch the delimiter in
	 * the multi-word lexical entries of the gazetteer off.
	 */
	private boolean delimiterSwitchOff = false;

	/*
	 * 5. Set this to the desired character-gram size.
	 */
	private int nGramSize;

	/*
	 * 6. Set this to 'true' only if you would like to have unique n-grams in
	 * the feature-set of strings.
	 */
	private boolean ignoreDuplicateNgrams = false;
	/*
	 * 7. Set this to the desired similarity measure.
	 */
	private String similarityMeasure;

	/*
	 * 8. Set this to the desired similarity threshold (between 0 and 1.0, 1.0
	 * corresponds to the highest degree of similarity).
	 */
	private double similarityThreshold;

	/*
	 * 9. the similarity function interface selected based on value of NemexFDataObject.similarityMeasure
	 */

	private SimilarityMeasureInterface simFct;

	/*
	 * 10. aligner interface
	 */
	private AlignerInterface aligner;


	/*
	 * 10. selector interface
	 */
	private SelectorInterface selector;


	public String getQueryString() {
		return queryString;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public String getGazetteerFilePath() {
		return gazetteerFilePath;
	}

	public void setGazetteerFilePath(String gazetteerFilePath) {
		this.gazetteerFilePath = gazetteerFilePath;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public boolean isDelimiterSwitchOff() {
		return delimiterSwitchOff;
	}

	public void setDelimiterSwitchOff(boolean delimiterSwitchOff) {
		this.delimiterSwitchOff = delimiterSwitchOff;
	}

	public int getnGramSize() {
		return nGramSize;
	}

	public void setnGramSize(int nGramSize) {
		this.nGramSize = nGramSize;
	}

	public boolean isIgnoreDuplicateNgrams() {
		return ignoreDuplicateNgrams;
	}

	public void setIgnoreDuplicateNgrams(boolean ignoreDuplicateNgrams) {
		this.ignoreDuplicateNgrams = ignoreDuplicateNgrams;
	}

	public String getSimilarityMeasure() {
		return similarityMeasure;
	}

	public void setSimilarityMeasure(String similarityMeasure) {
		this.similarityMeasure = similarityMeasure;
		this.setSimFct();
	}

	public double getSimilarityThreshold() {
		return similarityThreshold;
	}

	public void setSimilarityThreshold(double similarityThreshold) {
		this.similarityThreshold = similarityThreshold;
	}

	public SimilarityMeasureInterface getSimFct() {
		return simFct;
	}

	public void setSimFct(SimilarityMeasureInterface simFct) {
		this.simFct = simFct;
	}

	public AlignerInterface getAligner() {
		return aligner;
	}

	public void setAligner(AlignerInterface aligner) {
		this.aligner = aligner;
	}

	public SelectorInterface getSelector() {
		return selector;
	}

	public void setSelector(SelectorInterface selector) {
		this.selector = selector;
	}

	private void setSimFct() {
		if (this.similarityMeasure == SimilarityMeasure.COSINE_SIMILARITY_MEASURE){
			this.simFct = new CosineMeasure();
		}
		else
			if (this.similarityMeasure == SimilarityMeasure.ED_SIMILARITY_MEASURE){
				this.simFct = new EditDistanceMeasure();
			}
			else 
				if (this.similarityMeasure == SimilarityMeasure.JACCARD_SIMILARITY_MEASURE){
					this.simFct = new JaccardMeasure();
				}
				else
					if (this.similarityMeasure == SimilarityMeasure.DICE_SIMILARITY_MEASURE){
						this.simFct = new DiceMeasure();
					}
					else
						if (this.similarityMeasure == SimilarityMeasure.EDS_SIMILARITY_MEASURE){
							this.simFct = new EditSimilarityMeasure();
						}
						else
						{
							System.err.println("Still to implement :- " + this.similarityMeasure);
							System.exit(0);
						}	
		this.simFct.setNemexFBean(this);
	}

	public  void setNemexFBeanDefaults(){
		this.delimiter = "#";
		this.delimiterSwitchOff = false;
		this.ignoreDuplicateNgrams = false;
	}

	public NemexFBean(){
		this.setNemexFBeanDefaults();
	}
	
	public String toString(){
		return
				this.getGazetteerFilePath()
				+ "\n" + 
				this.getnGramSize()
				+ "\n" + 
				this.similarityMeasure
				+ "\n" + 
				this.getSimilarityThreshold()
				+ "\n" + 
				this.getAligner()
				+ "\n" + 
				this.getSelector()
				+ "\n"
				;
	}
}
