package de.dfki.lt.nemex;

import de.dfki.lt.nemex.f.NemexFController;
import de.dfki.lt.nemex.f.data.NemexFBean;
import de.dfki.lt.nemex.f.similarity.SimilarityMeasure;

/*
 * 
 * NemexF:
 * Formally, the problem of approximate entity matching can be described as follows: 
 * Given a dictionary of natural language entities E = {e1, e2, . . . , en}, 
 * a document D, a similarity function, and a threshold: 
 * find all “similar” pairs <s, ei> with respect to the given function and threshold, 
 * where s is a substring of D and ei ∈ E. 
 * Note, that this notion assumes that the document is just considered as a simple text string and 
 * thus does not require that it is pre-processed, e.g., by means of a tokenizer.
 * 
 * Here: 
 * the document d is specified as a query.
 * 
 * NOTE:
 * The difference to NemexA:
 * NemexA assumes that the query is a candidate entries, so it does not consider substrings.
 */

public class Test_NemexF_Amplexor {

	// TODO Make this a property file
	
	
	// define function that loops over file of lines and calls nemexf
	
	public static void main(String[] args) {
		long time1;
		long time2;

		NemexFBean nemexFBean = new NemexFBean();

		// BEGIN - Setting parameters

		nemexFBean.setnGramSize(4);
		nemexFBean.setSimilarityMeasure(SimilarityMeasure.COSINE_SIMILARITY_MEASURE);
		nemexFBean.setSimilarityThreshold(0.5);
		// END of parameter setting

		// set aligner method
		nemexFBean.setAligner(new de.dfki.lt.nemex.f.aligner.BinaryCountPruneAligner());
		nemexFBean.setSelector(new de.dfki.lt.nemex.f.selector.MiddleSelector(nemexFBean));
		

		// set dictionary path
		nemexFBean.setGazetteerFilePath(
				"/Users/gune00/data/AmplexorData/CSD_Data_Delivery_v1/Controlled_Vocabulary/entriesType-nemex.txt");

		System.out.println(nemexFBean.toString());
		
		// initialize controller
		System.out.println("Loading dictionary ...");
		time1 = System.currentTimeMillis();
		NemexFController controller = new NemexFController(nemexFBean);
		time2 = System.currentTimeMillis();
		System.out.println("System time (msec): " + (time2 - time1));

		
		// set query string
		nemexFBean.setQueryString(
				"There are no fixed rules for insulin dose regimen. A part of the daily insulin dose (\"basal rate\") is infused continuously by "
				+ "the implantable pump and the remaining part of the daily dose is administered by the patient, using the same pump, as a bolus "
				+ "before meals. "
				+ "The basal metabolic requirement is usually 40% to 60% of the total daily insulin requirement. "
				+ "hanges in basal and bolus doses are controlled by means of a small, hand held unit (Personal Pump Communicator (PPC)) "
				+ "which communicates with the pump via radio waves. The detailed operating instructions about the"
				+ "implantable pump, its functions and the necessary safety precautions are described "
				+ "in the Physician’s Manual accompanying the infusion pump."
				+ "");

		// create ngram heap of input string
		controller.setCharacterNgramFromQueryString(nemexFBean.getQueryString());

		System.out.println("Processing query  with settings");
		time1 = System.currentTimeMillis();
		controller.reset();
		controller.process();
		controller.selectCandidates();
		time2 = System.currentTimeMillis();
		controller.printSelectedCandidates();
		System.out.println("System time (msec): " + (time2 - time1));
	}

}
