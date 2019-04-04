package de.dfki.lt.nemex;

import de.dfki.lt.nemex.f.NemexFController;
import de.dfki.lt.nemex.f.data.NemexFBean;
import de.dfki.lt.nemex.f.similarity.SimilarityMeasure;

public class Test_NemexF {
	
	//TODO Make this a property file

	public static void main(String[] args) {
		long time1;
		long time2;
		
		NemexFBean nemexFBean = new NemexFBean();
		
		// BEGIN - Setting parameters
		
		// test Faerie Paper

//				nemexFBean.setnGramSize(1);
//				nemexFBean.setGazetteerFilePath("src/main/webapp/resources/faerie.txt");
//		
//				nemexFBean.setSimilarityMeasure(SimilarityMeasure.ED_SIMILARITY_MEASURE);
//				nemexFBean.setSimilarityThreshold(0.0);
//		
//				nemexFBean.setQueryString("an efficient filter for approximate membership checking. venkaee "
//						+ "shga kamunshik kabarati, dong xin, surauijt chadhurisigmod.");
		
				//nemexFBean.setQueryString("chakrabarti");
		
				//nemexFBean.setQueryString("venkaee shga kamunshi");

		// test with examples from:
		nemexFBean.setnGramSize(5);
		

//		nemexFBean.setSimilarityMeasure(SimilarityMeasure.COSINE_SIMILARITY_MEASURE);
//		nemexFBean.setSimilarityThreshold(0.8);
//
//		nemexFBean.setGazetteerFilePath("resources/MedicalTerms-mwl-plain.txt");
//		nemexFBean.setQueryString(
//				"Cytochemical myeloperoxidase (MPO) positivity represents the gold standard for discrimination "
//						+ "between lymphatic and myeloid blasts. Rarely, cytochemical MPO reaction may be positive in >or=3% of "
//						+ "blasts with clear lymphoblastep morphology. We present 5 patients with cytochemically MPO-positive acute "
//						+ "leukemia classified as lymphoblastic by cytomorphology and anlymphophlastic (n=3) or biphenotypic (n=2) "
//						+ "by immunophenotyping, who entered first-line treatment for lymphoblastic leukemia. "
//						+ "The former 3 are in first remission and both with biphenotypic leukemia relapsed with acute myeloid leukemia. "
//						+ "The study primarily shows that cytochemical MPO expression in childhood acute leukemia revealing typical "
//						+ "lymfoblastic morphology and phenotype does rarely exist. Although a small number of patients studied, "
//						+ "cytochemical MPO expression in acute leukemia does not seem to require myeloid leukemia treatment in case of "
//						+ "otherwise lymphoblastic cytomorphology and phenotype."
//						);
				
//				nemexFBean.setQueryString("lymfoblastic");

//		// NE-List:
		nemexFBean.setnGramSize(3);
		nemexFBean.setSimilarityMeasure(SimilarityMeasure.EDS_SIMILARITY_MEASURE);
		nemexFBean.setSimilarityThreshold(0.99);
		
//		// "/Users/gune00/data/NE-Lists/pantelWikiListSeeds.txt"
		nemexFBean.setGazetteerFilePath("/Users/gune00/data/NE-Lists/CrossNER/all.txt");
		nemexFBean.setQueryString(
				"Blackrock is a 1997 Australian drama film directed by Steven Vidler and written by Nick Enright. "
				+ "In Blackrock, a fictional beachside working-class suburb, a young surfer witnesses his friends raping a girl. "
				+ "When she is found murdered the next day, he is torn between revealing what he saw and protecting his friends. "
				+ "Filming locations included Stockton, New South Wales, where a girl named Leigh Leigh was murdered in 1989. "
				+ "While the film was never marketed as the story of her death, many viewers incorrectly believed it to "
				+ "be a factual account of the crime. Her family objected to what they saw as a fictionalisation of her murder, "
				+ "and residents of Stockton opposed the decision to shoot scenes there. The film received generally positive critical "
				+ "reviews in Australia, where it was nominated for five AACTA Awards and won two AWGIE Awards, "
				+ "though it received mixed reviews elsewhere. Adapted from Enright's play of the same name, "
				+ "the film stars Laurence Breuls, Simon Lyndon and Linda Cropper, "
				+ "and features the first credited film performance of Heath Ledger. "
			);
//		
		// END of parameter setting
		
		// set aligner and selector method
		nemexFBean.setSelector(new de.dfki.lt.nemex.f.selector.MiddleSelector(nemexFBean));
		
		// initialize controller
		NemexFController controller = new NemexFController(nemexFBean);
		// create ngram list of input string
		controller.setCharacterNgramFromQueryString(nemexFBean.getQueryString());
		
		// define aligner
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
