package de.dfki.lt.nemex.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This interface provides the required functions that should be implemented to
 * create a new gazetteer of entries from a corpus.
 * 
 * @author Madhumita
 * 
 */
public interface GazetteerCreationComponent {

	/**
	 * Annotates a given corpus with required annotation types, for e.g., named
	 * entities, and stores the annotations in an external file such that each
	 * new line in the file contains the annotation in the format
	 * "annotatedDelimitedString POSTag".
	 * 
	 * @param corpus
	 *            Path for the corpus to obtain the gazetteer from.
	 * @param fname
	 *            file with delimited annotation strings and its sense.
	 * @param delimiter
	 *            Multiword separator. All the whitespace characters in
	 *            annotated entry string should be replaced with the delimiter
	 *            specified while writing the file fname.
	 */
	void annotate(String corpus, String fname, String delimiter)
			throws GazetteerCreationException;;

	/**
	 * Generate a map of entries to be added in the gazetteer, with frequency of
	 * each sense it is used in.
	 * 
	 * @param fname
	 *            File with entries to be added, such that each new line is a
	 *            new entry in the format "entryString entryPOSTag"
	 * @return Map of gazetteer entries. The entry strings are the keys, value
	 *         is another map of each POS tag and frequency with which it has
	 *         been used.
	 * @throws IOException
	 */
	Map<String, HashMap<String, Integer>> getGazetteerEntries(String fname)
			throws GazetteerCreationException;

	/**
	 * From the generated annotations, write a gazetteer in required format.
	 * 
	 * @param fname
	 *            path for gazetteer
	 * @param entries
	 *            map of entries
	 * @param delimiter
	 *            multi-word separator
	 */
	void writeGazetteer(String fname,
			Map<String, HashMap<String, Integer>> entries)
			throws GazetteerCreationException;;

}
