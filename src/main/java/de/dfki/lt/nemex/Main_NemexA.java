package de.dfki.lt.nemex;

/*
 *  The Main Class of NEMEX-A
 *  NEMEX
 *  April 2013
 *  Author: Amir H. Moin (amir.moin@dfki.de)
 *  LT Lab.
 *  German Research Center for Artificial Intelligence
 *  (Deutsches Forschungszentrum fuer Kuenstliche Intelligenz GmbH = DFKI)
 *  http://www.dfki.de
 *  Saarbruecken, Saarland, Germany
 */

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.dfki.lt.nemex.a.NEMEX_A;
import de.dfki.lt.nemex.a.data.GazetteerNotLoadedException;
import de.dfki.lt.nemex.a.similarity.SimilarityMeasure;

/**
 * <H4>
 * This class is the main class of NEMEX-A. The configurations.xml file contains
 * the users-defined configurations.</H4>
 */
public class Main_NemexA {

	// Note: Defining parameters in this class is deprecated. Now, NEMEX
	// supports config file for setting the input parameters.
	// The config file resides at: src/main/webapp/resources/configurations.xml

	/*
	 * START OF CONFIGURATIONS
	 */

	/*
	 * 1. Query String x
	 */

	public static String queryString = "";

	/*
	 * 2. Gazetteer V
	 */
	public static String gazetteerFilePath = "";

	/*
	 * 3. Delimiter within the Multi-Word Lexical (MWL) entries in the
	 * Gazetteer.
	 */
	public static String delimiter = "";

	/*
	 * 4. Set this to 'true' only if you would like to switch the delimiter in
	 * the multi-word lexical entries of the gazetteer off.
	 */
	public static boolean delimiterSwitchOff;

	/*
	 * 5. Set this to the desired character-gram size.
	 */
	public static int nGramSize;

	/*
	 * 6. Set this to 'true' only if you would like to have unique n-grams in
	 * the feature-set of strings.
	 */
	public static boolean ignoreDuplicateNgrams;

	/*
	 * 7. Set this to the desired similarity measure.
	 */
	public static String similarityMeasure;

	/*
	 * 8. Set this to the desired similarity threshold (between 0 and 1.0, 1.0
	 * corresponds to the highest degree of similarity).
	 */
	public static double similarityThreshold;

	//
	/*
	 * END OF CONFIGURATIONS
	 */

	private static void readConfigurations(String fileName) {
		try {
			File fXmlFile = new File(fileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = (Document) dBuilder.parse(fXmlFile);
			((org.w3c.dom.Document) doc).getDocumentElement().normalize();

			if (!doc.getDocumentElement().getNodeName().equals("nemex")) {
				throw new DOMException(DOMException.SYNTAX_ERR,
						"Invalid XML Configurations syntax!");
			}

			NodeList nList = doc.getElementsByTagName("nemex-a");

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					// System.out.println("Attr: " +
					// eElement.getAttribute("id"));
					// System.out.println("queryString: " +
					// eElement.getElementsByTagName("queryString").item(0).getTextContent());
					Main_NemexA.queryString = eElement
							.getElementsByTagName("queryString").item(0)
							.getTextContent();
					// System.out.println("gazetteerFilePath: " +
					// eElement.getElementsByTagName("gazetteerFilePath").item(0).getTextContent());
					Main_NemexA.gazetteerFilePath = eElement
							.getElementsByTagName("gazetteerFilePath").item(0)
							.getTextContent();
					// System.out.println("delimiter: " +
					// eElement.getElementsByTagName("delimiter").item(0).getTextContent());
					Main_NemexA.delimiter = eElement
							.getElementsByTagName("delimiter").item(0)
							.getTextContent();
					// System.out.println("delimiterSwitchOff: " +
					// eElement.getElementsByTagName("delimiterSwitchOff").item(0).getTextContent());
					Main_NemexA.delimiterSwitchOff = Boolean.valueOf(eElement
							.getElementsByTagName("delimiterSwitchOff").item(0)
							.getTextContent());
					// System.out.println("nGramSize: " +
					// eElement.getElementsByTagName("nGramSize").item(0).getTextContent());
					Main_NemexA.nGramSize = (int) Integer.valueOf(eElement
							.getElementsByTagName("nGramSize").item(0)
							.getTextContent());
					// System.out.println("ignoreDuplicateNgrams: " +
					// eElement.getElementsByTagName("ignoreDuplicateNgrams").item(0).getTextContent());
					Main_NemexA.ignoreDuplicateNgrams = Boolean
							.valueOf(eElement
									.getElementsByTagName(
											"ignoreDuplicateNgrams").item(0)
											.getTextContent());
					// System.out.println("similarityMeasure: " +
					// eElement.getElementsByTagName("similarityMeasure").item(0).getTextContent());
					if (eElement.getElementsByTagName("similarityMeasure")
							.item(0).getTextContent()
							.equals(SimilarityMeasure.DICE_SIMILARITY_MEASURE)) {
						Main_NemexA.similarityMeasure = SimilarityMeasure.DICE_SIMILARITY_MEASURE;
					} else if (eElement
							.getElementsByTagName("similarityMeasure")
							.item(0)
							.getTextContent()
							.equals(SimilarityMeasure.JACCARD_SIMILARITY_MEASURE)) {
						Main_NemexA.similarityMeasure = SimilarityMeasure.JACCARD_SIMILARITY_MEASURE;
					} else if (eElement
							.getElementsByTagName("similarityMeasure")
							.item(0)
							.getTextContent()
							.equals(SimilarityMeasure.COSINE_SIMILARITY_MEASURE)) {
						Main_NemexA.similarityMeasure = SimilarityMeasure.COSINE_SIMILARITY_MEASURE;
					} else if (eElement
							.getElementsByTagName("similarityMeasure")
							.item(0)
							.getTextContent()
							.equals(SimilarityMeasure.OVERLAP_SIMILARITY_MEASURE)) {
						Main_NemexA.similarityMeasure = SimilarityMeasure.OVERLAP_SIMILARITY_MEASURE;
					}

					// System.out.println("similarityThreshold: " +
					// eElement.getElementsByTagName("similarityThreshold").item(0).getTextContent());
					Main_NemexA.similarityThreshold = (double) Double
							.valueOf(eElement
									.getElementsByTagName("similarityThreshold")
									.item(0).getTextContent());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//
	/*
	 * END OF CONFIGURATIONS
	 */
	
	private static final long MEGABYTE = 1024L * 1024L;

	//
	/*
	 * END OF CONFIGURATIONS
	 */
	
	public static long bytesToMegabytes(long bytes) {
		return bytes / MEGABYTE;
	}

	// Test function
	public static void main(String[] args) {
	
		readConfigurations("src/main/webapp/resources/configurations.xml");
	
		/*
		 * Start NEMEX up.
		 */
		System.out.println("Welcome to NEMEX-A!");
		System.out.println();
	
		/*
		 * NEMEX-A will find all the lexical entries in the gazetteer which are
		 * similar to the query string.
		 */
	
		NEMEX_A.loadNewGazetteer(gazetteerFilePath, delimiter,
				delimiterSwitchOff, nGramSize, ignoreDuplicateNgrams);
		try {
			System.out.println();
	
			List<String> retrievedStrings = new ArrayList<String>();
			retrievedStrings = NEMEX_A.checkSimilarity(queryString,
					gazetteerFilePath, similarityMeasure, similarityThreshold);
	
			System.out.println("No. of retrieved items: "
					+ retrievedStrings.size());
			System.out.println("Retrieved items: ");
			for (String str : retrievedStrings) {
				System.out.println(str);
			}
		} catch (GazetteerNotLoadedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		System.out.println();
		System.out.println("[MEMORY INFO]");
		Runtime runtime = Runtime.getRuntime();
		System.out.println("[MEMORY INFO] Running Java Garbage Collector...");
		runtime.gc();
		System.out.println("Total memory: "
				+ bytesToMegabytes(runtime.totalMemory()) + " MB");
		System.out
		.println("Used memory: "
				+ bytesToMegabytes(runtime.totalMemory()
						- runtime.freeMemory()) + " MB");
	
		// Test: adding a new entry
		NEMEX_A.loadedGazetteers.get(Main_NemexA.gazetteerFilePath).getGazetteer()
		.addNewEntry("-9.197762 domineall NG:1:-9.197762");
		System.out.println("added new entry: " + 
				NEMEX_A.loadedGazetteers.get(Main_NemexA.gazetteerFilePath).getGazetteer().getLexicalEntries().get(new Long(5))
				);
	
		// Do querying again
		try {
			System.out.println();
	
			List<String> retrievedStrings = new ArrayList<String>();
			retrievedStrings = NEMEX_A.checkSimilarity(queryString,
					gazetteerFilePath, similarityMeasure, similarityThreshold);
	
			System.out.println("No. of retrieved items: "
					+ retrievedStrings.size());
			System.out.println("Retrieved items: ");
			for (String str : retrievedStrings) {
				System.out.println(str);
			}
		} catch (GazetteerNotLoadedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		System.out.println();
		System.out.println("[MEMORY INFO]");
	
		System.out.println("[MEMORY INFO] Running Java Garbage Collector...");
		runtime.gc();
		System.out.println("Total memory: "
				+ bytesToMegabytes(runtime.totalMemory()) + " MB");
		System.out
		.println("Used memory: "
				+ bytesToMegabytes(runtime.totalMemory()
						- runtime.freeMemory()) + " MB");
	
		/*
		 * Shut NEMEX down.
		 */
		System.out.println();
		System.out.println("[INFO] NEMEX-A is shutting down!");
	}

}
