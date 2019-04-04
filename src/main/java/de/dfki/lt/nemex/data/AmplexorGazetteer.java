package de.dfki.lt.nemex.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AmplexorGazetteer extends NemexGazetteer {
	
	public static boolean loweCase = false;

	public AmplexorGazetteer(String entriesFile, String gazetteerFName) throws GazetteerCreationException {
		super(entriesFile, gazetteerFName);
	}
	
	private String makeShadowedEntry (String entry) {
		String[] listOfstrings = entry.split(" ");
		String singleString = "";
		//System.out.println(listOfstrings.length);
		for (int i = 0; i < (listOfstrings.length-1) ; i++) {
			singleString=singleString+listOfstrings[i]+"#";
		}
		singleString=singleString+listOfstrings[listOfstrings.length-1];
		
		
		return singleString;
		
		
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
					String entryLine = (AmplexorGazetteer.loweCase)?line.toLowerCase():line;
					String[] entry = entryLine.split("###");
					String entryStr = makeShadowedEntry(entry[0]);
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

}
