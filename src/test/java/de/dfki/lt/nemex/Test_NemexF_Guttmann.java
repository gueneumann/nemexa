package de.dfki.lt.nemex;

import de.dfki.lt.nemex.f.NemexFController;
import de.dfki.lt.nemex.f.data.NemexFBean;
import de.dfki.lt.nemex.f.similarity.SimilarityMeasure;

public class Test_NemexF_Guttmann {
	
	//TODO Make this a property file

	public static void main(String[] args) {
		long time1;
		long time2;
		
		NemexFBean nemexFBean = new NemexFBean();
		
		// BEGIN - Setting parameters
		
		nemexFBean.setnGramSize(3);
		nemexFBean.setSimilarityMeasure(SimilarityMeasure.COSINE_SIMILARITY_MEASURE);
		nemexFBean.setSimilarityThreshold(0.9);
		// END of parameter setting
		
		// set aligner method
		nemexFBean.setSelector(new de.dfki.lt.nemex.f.selector.MiddleSelector(nemexFBean));
		
		// set dictionary path
		
		nemexFBean.setGazetteerFilePath("/Users/gune00/data/AmplexorData/CSD_Data_Delivery_v1/Controlled_Vocabulary/entriesType-nemex.txt");
		
		
		
		// initialize controller
		System.out.println("Loading dictionary ...");
		time1 = System.currentTimeMillis();
		NemexFController controller = new NemexFController(nemexFBean);
		time2 = System.currentTimeMillis();
		System.out.println("System time (msec): " + (time2-time1));
		
		// set query string
		nemexFBean.setQueryString(
				"It is known that insulin can form aggregates, fibrils and gel-like structures when it is subjected to chemical "
				+ "and/or physical stress, e.g. increased temperatures and shaking. This can lead to obstruction of the implantable "
				+ "pump and under-delivery of insulin. Hyperglycaemia, ketoacidosis or coma may develop within hours in case of malfunction "
				+ "of the pump system. As soon as patients notice a rapid increase in blood glucose, which does not respond to a bolus dose of insulin, "
				+ "the possibility of pump obstruction should be investigated by a physician trained to perform pump investigations. "
				+ "From experience gained in a 6-month comparative phase III study (HUBIN_L_05335) with Insuman Implantable administered via the "
				+ "Medtronic MiniMed Implantable Pump in 84 patients aged 26 to 80 years (see section 5.1) "
				+ "and from clinical experience with insulin human 100 IU/ml and 40 IU/ml, "
				+ "the following adverse reactions were observed."
			);
	
		
		// create ngram heap of input string
		controller.setCharacterNgramFromQueryString(nemexFBean.getQueryString());		
		
		// define aligner
//		System.out.println("Processing query  with settings");
//		nemexFBean.setAligner(new de.dfki.lt.nemex.f.aligner.BucketCountPruneAligner());
//		System.out.println(nemexFBean.toString());
//		
//		time1 = System.currentTimeMillis();
//		controller.reset();
//		controller.process();
//		controller.selectCandidates();
//		time2 = System.currentTimeMillis();
//		controller.printSelectedCandidates();
//		System.out.println("System time (msec): " + (time2-time1));
		
		// define aligner
		System.out.println("Processing query  with settings");
		nemexFBean.setAligner(new de.dfki.lt.nemex.f.aligner.BinaryCountPruneAligner());
		System.out.println(nemexFBean.toString());
		
		time1 = System.currentTimeMillis();
		controller.reset();
		controller.process();
		controller.selectCandidates();
		time2 = System.currentTimeMillis();
		controller.printSelectedCandidates();
		System.out.println("System time (msec): " + (time2-time1));
	}

}
