package de.dfki.lt.nemex.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Base class for generating Nemex gazetteer from a corpus. This class should
 * be extended to override the
 * <code>annotate</corpus> function specific to requirements.
 * 
 * @author Madhumita
 * 
 */
public class NemexGazetteer implements GazetteerCreationComponent {

	/**
	 * Create a NemexA gazetteer from a given corpus
	 * 
	 * @param corpus
	 *            corpus to generate gazetteer from
	 * @param entriesFile
	 *            Path of file which will contain annotation entries with their
	 *            POS tags, to be added to gazetteer
	 * @param delimiter
	 *            multiword separator
	 * @param gazetteerFName
	 *            path of nemex gazetteer file
	 * @throws GazetteerCreationException
	 */
	public NemexGazetteer(String corpus, String entriesFile, String delimiter,
			String gazetteerFName) throws GazetteerCreationException {

		annotate(corpus, entriesFile, delimiter);
		Map<String, HashMap<String, Integer>> entries = getGazetteerEntries(entriesFile);
		writeGazetteer(gazetteerFName, entries);

	}

	/**
	 * Generate a Nemex gazetteer from a given file with entry strings and their
	 * POS tags
	 * 
	 * @param entriesFile
	 *            Path of file which contains annotation entries with their POS
	 *            tags, to be added to gazetteer
	 * @param gazetteerFName
	 *            path of nemex gazetteer file
	 * @throws GazetteerCreationException
	 */
	public NemexGazetteer(String entriesFile, String gazetteerFName)
			throws GazetteerCreationException {

		Map<String, HashMap<String, Integer>> entries = getGazetteerEntries(entriesFile);
		writeGazetteer(gazetteerFName, entries);
	}

	@Override
	public void annotate(String corpus, String fname, String delimiter) {
	}

	@Override
	public Map<String, HashMap<String, Integer>> getGazetteerEntries(
			String fname) throws GazetteerCreationException {

		Map<String, HashMap<String, Integer>> entries = new HashMap<String, HashMap<String, Integer>>();

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fname));
		} catch (FileNotFoundException e) {
			throw new GazetteerCreationException(e.getMessage());
		}

		String line;
		// Read entries file; every new line is a new entry
		try {
			while ((line = br.readLine()) != null) {
				//System.out.println("Current entry: "+ line);
				String[] entry = line.split(" ");
				String entryStr = entry[0];
				String pos = entry[1];

				// store frequency of each sense the entry is used in
				if (!entries.containsKey(entryStr)) {
					HashMap<String, Integer> senses = new HashMap<String, Integer>();
					senses.put(pos, 1);
					entries.put(entryStr, senses);
				} else {
					HashMap<String, Integer> senses = entries.get(entryStr);
					if (senses.containsKey(pos))
						senses.put(pos, senses.get(pos) + 1);
					else
						senses.put(pos, 1);
				}

			}
			br.close();
		} catch (IOException e) {
			throw new GazetteerCreationException(e.getMessage());
		}

		return entries;
	}

	@Override
	public void writeGazetteer(String fname,
			Map<String, HashMap<String, Integer>> entries)
			throws GazetteerCreationException {

		// total freq of all entries in all senses
		int totalEntryFreq = 0;
		// num of unique entries
		int numOfEntries = entries.size();

		// calculate total frequency of all entries for all senses
		// iterating through all entries
		for (HashMap<String, Integer> senses : entries.values())
			// iterating through all senses of the entry
			for (int senseFreq : senses.values())
				totalEntryFreq += senseFreq;

		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(fname));

			// header for Nemex gazetteer
			bw.write("0 UTF-8 EN " + totalEntryFreq + " " + numOfEntries);
			bw.write("\n");
			int id = 1;

			// iterating through all entries
			for (Entry<String, HashMap<String, Integer>> curEntry : entries
					.entrySet()) {
				String entryStr = curEntry.getKey();

				// total freq of current entry
				int curEntryFreq = 0;
				// iterating through all senses in the entry
				for (int freq : curEntry.getValue().values())
					curEntryFreq += freq;

				// Generating entry string in Nemex gazetteer format
				String str = id + " " + Math.log(curEntryFreq / (float)totalEntryFreq)
						+ " " + entryStr;

				// iterating over all senses in the entry
				for (Entry<String, Integer> sense : curEntry.getValue()
						.entrySet())
					str += " " + sense.getKey() + ":" + sense.getValue() + ":"
							+ Math.log(sense.getValue() / (float)totalEntryFreq);
				bw.write(str);
				bw.write("\n");
				id++;
			}
			bw.close();

		} catch (IOException e) {
			throw new GazetteerCreationException(e.getMessage());
		}

	}

}
