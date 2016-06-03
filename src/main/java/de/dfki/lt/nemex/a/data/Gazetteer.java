package de.dfki.lt.nemex.a.data;

/*
 *  The Gazetteer (a.k.a. vocabulary or lexicon)
 *  NEMEX
 *  October 2012
 *  Author: Amir H. Moin (amir.moin@dfki.de)
 *  LT Lab.
 *  German Research Center for Artificial Intelligence
 *  (Deutsches Forschungszentrum fuer Kuenstliche Intelligenz GmbH = DFKI)
 *  http://www.dfki.de
 *  Saarbruecken, Saarland, Germany
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.lt.nemex.Main_NemexA;
import de.dfki.lt.nemex.a.NEMEX_A;

public class Gazetteer implements java.io.Serializable {
	private static final Logger LOG = LoggerFactory.getLogger(Gazetteer.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 4711L;
	private String filePath;
	private String encoding;
	private String language;
	private String delimiter;
	private long noOfEntries;
	private long noOfUniqueEntries;
	private HashMap<Long, List<String>> lexicalEntries;

	/**
	 * @param filePath
	 *            File path of the Gazetteer (vocabulary).
	 * @param delimiter
	 *            The delimiter for separating the multi-word lexical entries in
	 *            the gazetteer.
	 * @param delimiterSwitchOff
	 *            This is false only if there is no delimiter in the gazetteer.
	 */
	public Gazetteer(String filePath, String delimiter,
			boolean delimiterSwitchOff) {

		LOG.info("Reading the Gazetteer...");

		this.filePath = filePath;
		this.delimiter = delimiter;
		this.lexicalEntries = new HashMap<Long, List<String>>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(filePath));
			String line;
			// Read lexicon file linewise
			while ((line = br.readLine()) != null) {
				// System.out.println(line);
				int index = line.indexOf(' ');
				long mapKey = Long.valueOf(line.substring(0, index));
				String mapValueStr = line.substring(index + 1);
				List<String> mapValue = Arrays.asList(mapValueStr.split(" "));
				this.lexicalEntries.put(mapKey, mapValue);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Meta information of first entry
		this.encoding = this.lexicalEntries.get(new Long(0)).get(0);
		this.language = this.lexicalEntries.get(new Long(0)).get(1);
		this.noOfEntries = Long.valueOf(this.lexicalEntries.get(new Long(0))
				.get(2));
		this.noOfUniqueEntries = Long.valueOf(this.lexicalEntries.get(
				new Long(0)).get(3));

		// Remove meta-information-lexicon entry
		this.lexicalEntries.remove(new Long(0));

		LOG.info("Gazetteer file path: " + this.filePath);
		LOG.info("Encoding: " + this.encoding);
		LOG.info("Language: " + this.language);
		LOG.info("Delimiter: " + this.delimiter);
		LOG.info("Delimiter Enabled: "
				+ String.valueOf(!delimiterSwitchOff));
		LOG.info("No. of lexical entries: " + this.noOfEntries);
		LOG.info("No. of UNIQUE lexical entries: "
				+ this.noOfUniqueEntries);

		if (!delimiterSwitchOff) {
			for (int i = 1; i <= this.getLexicalEntries().size(); i++) {
				if ((this.getLexicalEntries().get(new Long(i)).get(1))
						.contains(this.delimiter)) {
					this.getLexicalEntries()
					.get(new Long(i))
					.set(1,
							(this.getLexicalEntries().get(new Long(i))
									.get(1)).replace(this.delimiter, " "));
				}
			}
		}

	}

	public void addNewEntry(String entry) {

		// GN: entry as string does not make sense; need list array
		// I think, this function anyway need to be updated more carefully
		// because it does no validation of existing entries nor does it update the weights
		this.setNoOfEntries(this.getNoOfEntries() + 1);

		List<String> entryStrList = Arrays.asList(entry.split(" "));

		// GN: NOT good; need a bidirectional map between index and token-list of entry
		boolean flag = false;
		for (int i = 1; i <= this.getLexicalEntries().size(); i++) {
			if ((this.getLexicalEntries().get(new Long(i)).get(1))
					.equals(entryStrList.get(1))) {
				flag = true; 
				// GN, April, 2014: added this
				break;
			}
		}
		if (!flag) {
			this.setNoOfUniqueEntries(this.getNoOfUniqueEntries() + 1);
		}

		this.getLexicalEntries().put(
				new Long(this.getLexicalEntries().size() + 1), entryStrList);

		InvertedList gazetteerInvertedList = NEMEX_A.loadedGazetteers.get(this
				.getFilePath());
		try {
			NEMEX_A.exportGazetteer(this.getFilePath());
		} catch (GazetteerNotLoadedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NEMEX_A.importAndLoadGazetteer(this.getFilePath() + ".ser",
				this.getFilePath(), this.getDelimiter(),
				Main_NemexA.delimiterSwitchOff,
				gazetteerInvertedList.getnGramSize(),
				gazetteerInvertedList.isIgnoreDuplicateNgrams());

	}

	public void serialize(String path) {
		try {
			FileOutputStream fileOut = new FileOutputStream(path);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this);
			out.close();
			fileOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Gazetteer deserialize(String path) {
		Gazetteer gazetteer = null;
		try {
			FileInputStream fileIn = new FileInputStream(path);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			gazetteer = (Gazetteer) in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException e) {
			e.printStackTrace();

		} catch (ClassNotFoundException e) {
			e.printStackTrace();

		}

		return gazetteer;

	}

	// Getters and Setters
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public long getNoOfEntries() {
		return noOfEntries;
	}

	public void setNoOfEntries(long noOfEntries) {
		this.noOfEntries = noOfEntries;
	}

	public long getNoOfUniqueEntries() {
		return noOfUniqueEntries;
	}

	public void setNoOfUniqueEntries(long noOfUniqueEntries) {
		this.noOfUniqueEntries = noOfUniqueEntries;
	}

	public HashMap<Long, List<String>> getLexicalEntries() {
		return lexicalEntries;
	}

	public void setLexicalEntries(HashMap<Long, List<String>> lexicalEntries) {
		this.lexicalEntries = lexicalEntries;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

}
