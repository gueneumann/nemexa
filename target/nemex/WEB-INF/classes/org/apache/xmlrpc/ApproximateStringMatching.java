package org.apache.xmlrpc;

import de.dfki.lt.nemex.a.data.GazetteerNotLoadedException;
import java.util.ArrayList;
import java.util.List;

import org.apache.xmlrpc.XmlRpcException;

import de.dfki.lt.nemex.a.NEMEX_A;
import de.dfki.lt.nemex.a.data.Gazetteer;
import de.dfki.lt.nemex.a.data.InvertedList;
import de.dfki.lt.nemex.a.similarity.CosineApproximateStringSimilarityImpl;
import de.dfki.lt.nemex.a.similarity.DiceApproximateStringSimilarityImpl;
import de.dfki.lt.nemex.a.similarity.JaccardApproximateStringSimilarityImpl;
import de.dfki.lt.nemex.a.similarity.OverlapApproximateStringSimilarityImpl;
import de.dfki.lt.nemex.a.similarity.SimilarityMeasure;

public class ApproximateStringMatching {
	
	
	public String loadVocabulary(String vocabularyPath, String delimiter, Boolean delimiterSwitchOff, Integer nGramSize, Boolean ignoreDuplicateNgrams) {
		
		if(!NEMEX_A.loadedGazetteers.containsKey(vocabularyPath)) {
			
		Gazetteer gazetteer = new Gazetteer(vocabularyPath, delimiter,
				delimiterSwitchOff);

			
		InvertedList invertedList = new InvertedList(gazetteer, nGramSize,
				ignoreDuplicateNgrams);

			
		NEMEX_A.loadedGazetteers.put(vocabularyPath, invertedList);		
			
	}
		
		return vocabularyPath;

	}

	
	
	public List<String> checkStringSimilarity(String queryString, String gazetteerFilePath, String similarityMeasure, Double similarityThreshold) {
		
		List<String> returnList = new ArrayList<String>();		
		
		if (similarityMeasure.equals("DICE_SIMILARITY_MEASURE")) {
			try {
			returnList = NEMEX_A.checkSimilarity(queryString, gazetteerFilePath, SimilarityMeasure.DICE_SIMILARITY_MEASURE, Double.valueOf(similarityThreshold));
			} catch (GazetteerNotLoadedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (similarityMeasure
				.equals("JACCARD_SIMILARITY_MEASURE")) {
			try {
			returnList = NEMEX_A.checkSimilarity(queryString, gazetteerFilePath, SimilarityMeasure.JACCARD_SIMILARITY_MEASURE, Double.valueOf(similarityThreshold));
			} catch (GazetteerNotLoadedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (similarityMeasure
				.equals("COSINE_SIMILARITY_MEASURE")) {
			try {
			returnList = NEMEX_A.checkSimilarity(queryString, gazetteerFilePath, SimilarityMeasure.COSINE_SIMILARITY_MEASURE, Double.valueOf(similarityThreshold));
			} catch (GazetteerNotLoadedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (similarityMeasure
				.equals("OVERLAP_SIMILARITY_MEASURE")) {
			try {
			returnList = NEMEX_A.checkSimilarity(queryString, gazetteerFilePath, SimilarityMeasure.OVERLAP_SIMILARITY_MEASURE, Double.valueOf(similarityThreshold));
			} catch (GazetteerNotLoadedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return returnList;
		
	}
	
}
