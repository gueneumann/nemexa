package de.dfki.lt.nemex;

import java.util.List;

import de.dfki.lt.nemex.a.NEMEX_A;
import de.dfki.lt.nemex.a.data.GazetteerNotLoadedException;
import de.dfki.lt.nemex.a.similarity.SimilarityMeasure;

/*
<nemex-a>
<gazetteerFilePath>src/main/webapp/resources/MedicalTerms-mwl-plain.txt</gazetteerFilePath>
<delimiter>#</delimiter>
<delimiterSwitchOff>true</delimiterSwitchOff>
<nGramSize>3</nGramSize>
<ignoreDuplicateNgrams>false</ignoreDuplicateNgrams>
<similarityMeasure>COSINE_SIMILARITY_MEASURE</similarityMeasure>
<similarityThreshold>0.90</similarityThreshold>
</nemex-a>

*/

/*
 * 
 * NemexA:
 * Formally, the problem of approximate dictionary matching can be described as follows: 
 * Given a dictionary of natural language entities E = {e1, e2, . . . , en}, 
 * a query q, a similarity function, and a threshold: 
 * find all “similar” pairs <q, ei> with respect to the given function and threshold, 
 * where ei ∈ E. 
 * Note, that this notion assumes that the query is just considered as a simple text string and 
 * thus does not require that it is pre-processed, e.g., by means of a tokenizer in case of multi-term entries
 * 
 * NOTE:
 * Multi term entries are separated by a specific delimiter; it is not required that the query also is like this.
 * be 
 * NOTE:
 * The difference to NemexF:
 * NemexF assumes that the query is a document, so it does consider substrings.
 */

public class Test_NemexA {
	
	public static void main(String[] args) throws GazetteerNotLoadedException {
		long time1;
		long time2;
		
		boolean delimiterSwitchOff = true;
		boolean ignoreDuplicateNgrams = false;
		
		int nGramSize = 3;
		
		String similarityMeasure = SimilarityMeasure.COSINE_SIMILARITY_MEASURE;
		double similarityThreshold = 0.7;
		
		String delimiter = "#";
		String dictionaryFile = "/local/data/SimStringcolingDB/GeneLexicon-mwl-plain.txt";
		
		System.out.println("Loading dictionary ...");
		time1 = System.currentTimeMillis();
				
		NEMEX_A.loadNewGazetteer(dictionaryFile, delimiter, delimiterSwitchOff, nGramSize, ignoreDuplicateNgrams);
		
		time2 = System.currentTimeMillis();
		System.out.println("System time (msec): " + (time2 - time1));
		
		System.out.println("Processing query  with settings");
		time1 = System.currentTimeMillis();
		
		String queryString = "gene";
		
		List<String> similarEntries = NEMEX_A.checkSimilarity(queryString, dictionaryFile, similarityMeasure, similarityThreshold);
		
		System.out.println("Number of found entries: " + similarEntries.size());
		for (String str : similarEntries) {
			System.out.println(str);
		}
		time2 = System.currentTimeMillis();
		System.out.println("System time (msec): " + (time2 - time1));
		
	}

}
