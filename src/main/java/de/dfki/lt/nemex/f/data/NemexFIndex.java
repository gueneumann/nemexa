package de.dfki.lt.nemex.f.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.dfki.lt.nemex.a.data.Gazetteer;

/**
 * This class keeps the dictionary and its inverted index. It can also maintain several
 * dictionaries and inverted indices.
 * 
 * @author gune00
 *
 */
public class NemexFIndex {

	// Define Faerie based inverted index for dictionary

	public static Map<String, InvertedList_FAERIE_charBased> loadedGazetteers = 
			new HashMap<String, InvertedList_FAERIE_charBased>();

			public static boolean loadNewGazetteer(String gazetteerFilePath,
					String delimiter, Boolean delimiterSwitchOff, int nGramSize,
					Boolean ignoreDuplicateNgrams) {

				// Dictionary is already loaded
				if (NemexFIndex.loadedGazetteers.containsKey(gazetteerFilePath)) {
					return false;
				}

				// Make the dictionary
				Gazetteer gazetteer = new Gazetteer(gazetteerFilePath, delimiter,
						delimiterSwitchOff);
				// Make inverted index and combine with dictionary
				InvertedList_FAERIE_charBased invertedList = new InvertedList_FAERIE_charBased(
						gazetteer, nGramSize, ignoreDuplicateNgrams);
				
				// invertedList.printInvertedList();

				// Store inverted index with dictionary filename
				NemexFIndex.loadedGazetteers.put(gazetteerFilePath, invertedList);
				return true;
			}

			public static boolean unloadGazetteer(String gazetteerFilePath) {

				if (!NemexFIndex.loadedGazetteers.containsKey(gazetteerFilePath)) {
					return false;
				}

				NemexFIndex.loadedGazetteers.remove(gazetteerFilePath);
				System.gc();
				return true;
			}

			public static List<Long> getInvertedIndex(String gazetteerFilePath, String ngram){
				List<Long> invertedIndex = NemexFIndex.loadedGazetteers.get(gazetteerFilePath).getInvertedIndex().get(ngram);
				if (invertedIndex == null) {
					return new ArrayList<Long>();}
				return invertedIndex;
			}

			public static List<Long> getInvertedIndexAndCopy(String gazetteerFilePath, String ngram){
				List<Long> invertedIndex = NemexFIndex.loadedGazetteers.get(gazetteerFilePath).getInvertedIndex().get(ngram);
				// System.out.println("Ngram:" + ngram + " InvertedIndex: " + invertedIndex);
				List<Long> invertedIndexCopy = new ArrayList<Long>();
				if (invertedIndex == null) {
					return invertedIndexCopy;}
				else {
					for (int i=0; i < invertedIndex.size(); i++){
						invertedIndexCopy.add(new Long(invertedIndex.get(i)));
					}
					
					// System.out.println("Ngram:" + ngram + "Inverted Copy: " + invertedIndexCopy);
					
					return invertedIndexCopy;
				}

			}

			public static List<String> getEntry(String gazetteerFilePath, Long index){
				List<String> entry = NemexFIndex.loadedGazetteers.get(gazetteerFilePath).getGazetteer().getLexicalEntries().get(index);
				return entry;

			}

			/* TODO
			 * - printing status of loaded dictionaries
			 * - if inverted index is removed, make sure that lexicon is also removed
			 * - allow compressed/space-efficient storage of dictionary
			 */
}
