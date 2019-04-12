package de.dfki.lt.nemex;

import de.dfki.lt.nemex.f.NemexFController;
import de.dfki.lt.nemex.f.aligner.AlignerInterface;
import de.dfki.lt.nemex.f.data.NemexFBean;
import de.dfki.lt.nemex.f.similarity.SimilarityMeasure;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

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
 * 
 * Run NemexF on each sentence (line) of each text file of Amplexor corpus
 */

public class Test_NemexF_AmpCorpus {
	private NemexFBean nemexFBean = new NemexFBean();

	private NemexFController controller = null;
	
	private int fileCnt = 0;

	public String inDir = "/local/data/AmplexorData/EMA_EPAR_sentences";

	public String dictionary = "/local/data/AmplexorData/CSD_Data_Delivery_v1/Controlled_Vocabulary/entriesType-nemex.txt";
	
	public String outFile = "/local/data/AmplexorData/EMA_EPAR_nemexMatches.txt";

	public void initNemex(int ngramSize, String simFunction, double similarityThreshold, AlignerInterface aligner) {
		long time1;
		long time2;
		// BEGIN - Setting parameters

		nemexFBean.setnGramSize(ngramSize);
		nemexFBean.setSimilarityMeasure(simFunction);
		nemexFBean.setSimilarityThreshold(similarityThreshold);
		// END of parameter setting

		// set aligner method
		nemexFBean.setAligner(aligner);
		nemexFBean.setSelector(new de.dfki.lt.nemex.f.selector.ScoreSelector(nemexFBean));

		// set dictionary path
		nemexFBean.setGazetteerFilePath(dictionary);

		System.out.println(nemexFBean.toString());

		// initialize controller
		System.out.println("Loading dictionary ...");
		time1 = System.currentTimeMillis();
		controller = new NemexFController(nemexFBean);
		time2 = System.currentTimeMillis();
		System.out.println("System time (msec): " + (time2 - time1));
	}

	private void withAmpFilequeryNemex(File ampFile) throws IOException {

		String line = "";
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(ampFile), "utf-8"));
		long time1;
		long time2;
		time1 = System.currentTimeMillis();
		System.out.println("Processing file " + this.fileCnt++ + ": " + ampFile.getName());

		while ((line = reader.readLine()) != null) {
			this.queryWithNemex(line);
		}

		time2 = System.currentTimeMillis();
		System.out.println("System time (msec): " + (time2 - time1));

		reader.close();
	}
	
	private void withAmpFilequeryNemexOutput(File ampFile, BufferedWriter writer) throws IOException {

		String line = "";
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(ampFile), "utf-8"));
		long time1;
		long time2;
		time1 = System.currentTimeMillis();
		System.out.println("Processing file " + this.fileCnt++ + ": " + ampFile.getName());

		while ((line = reader.readLine()) != null) {
			this.queryWithNemex(line);
			writer.write(line);
			writer.newLine();
			writer.write(this.controller.getCandidates().toString());
			writer.newLine();
		}

		time2 = System.currentTimeMillis();
		System.out.println("System time (msec): " + (time2 - time1));

		reader.close();
	}

	public void queryWithNemex(String queryString) {

		// set query string
		nemexFBean.setQueryString(queryString);

		// create ngram heap of input string
		controller.setCharacterNgramFromQueryString(nemexFBean.getQueryString());

		controller.reset();
		controller.process();
		controller.selectCandidates();
	}

	public void processAmpCorpusDir(String inDir, String outfilename) throws IOException {
		File path = new File(inDir);

		File[] files = path.listFiles();
		
		File outFile = new File(outfilename);
		BufferedWriter writer = 
				new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "utf-8"));
		
		long time1;
		long time2;
		time1 = System.currentTimeMillis();
		System.out.println("Processing corpus: " + inDir);
		
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {
				this.withAmpFilequeryNemexOutput(files[i], writer);
			}
		}
		time2 = System.currentTimeMillis();
		System.out.println("System time (msec) whole corpus: " + (time2 - time1));
	}

	public static void main(String[] args) throws IOException {

		Test_NemexF_AmpCorpus testRun = new Test_NemexF_AmpCorpus();
		testRun.initNemex(5, SimilarityMeasure.ED_SIMILARITY_MEASURE, 2.0, 
				new de.dfki.lt.nemex.f.aligner.BucketCountPruneAligner());
		
		testRun.processAmpCorpusDir(testRun.inDir, 
				"/local/data/AmplexorData/EMA_EPAR_nemexMatches_ED_2_bucket.txt");

	}

}
