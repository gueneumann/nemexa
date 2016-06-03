package de.dfki.lt.nemex;

/*
 *  The Main Class of NEMEX-F
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.dfki.lt.nemex.a.data.Gazetteer;
import de.dfki.lt.nemex.a.similarity.SimilarityMeasure;
import de.dfki.lt.nemex.f.data.InvertedList_FAERIE_charBased;

/**
 * <H4>
 * This class is the main class of NEMEX-F. The configurations.xml file contains
 * the users-defined configurations.</H4>
 * OLD VERSION !
 */
public class Main_NemexF {

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

	public static void main(String[] args) {

		readConfigurations("src/main/webapp/resources/configurations.xml");

		/*
		 * Start NEMEX up.
		 */
		System.out.println("Welcome to NEMEX-F!");
		System.out.println();

		// TODO
		/*
		 * GN on May 2014:
		 * Here is the place to create a NEMEX_F class and perform Faerie on a text string.
		 * The code below should then be moved to this class in similar way as done for NEMEX_A
		 */
		Gazetteer gazetteer = new Gazetteer(gazetteerFilePath, delimiter,
				delimiterSwitchOff);
		InvertedList_FAERIE_charBased invertedList = new InvertedList_FAERIE_charBased(
				gazetteer, nGramSize, ignoreDuplicateNgrams);
		invertedList.printInvertedList();
		
		

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

		/*
		 * Shut NEMEX down.
		 */
		System.out.println();
		System.out.println("[INFO] NEMEX-F is shutting down!");
	}

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

			NodeList nList = doc.getElementsByTagName("nemex-f");

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;

					// System.out.println("Attr: " +
					// eElement.getAttribute("id"));
					// System.out.println("queryString: " +
					// eElement.getElementsByTagName("queryString").item(0).getTextContent());
					Main_NemexF.queryString = eElement
							.getElementsByTagName("queryString").item(0)
							.getTextContent();
					// System.out.println("gazetteerFilePath: " +
					// eElement.getElementsByTagName("gazetteerFilePath").item(0).getTextContent());
					Main_NemexF.gazetteerFilePath = eElement
							.getElementsByTagName("gazetteerFilePath").item(0)
							.getTextContent();
					// System.out.println("delimiter: " +
					// eElement.getElementsByTagName("delimiter").item(0).getTextContent());
					Main_NemexF.delimiter = eElement
							.getElementsByTagName("delimiter").item(0)
							.getTextContent();
					// System.out.println("delimiterSwitchOff: " +
					// eElement.getElementsByTagName("delimiterSwitchOff").item(0).getTextContent());
					Main_NemexF.delimiterSwitchOff = Boolean.valueOf(eElement
							.getElementsByTagName("delimiterSwitchOff").item(0)
							.getTextContent());
					// System.out.println("nGramSize: " +
					// eElement.getElementsByTagName("nGramSize").item(0).getTextContent());
					Main_NemexF.nGramSize = (int) Integer.valueOf(eElement
							.getElementsByTagName("nGramSize").item(0)
							.getTextContent());
					// System.out.println("ignoreDuplicateNgrams: " +
					// eElement.getElementsByTagName("ignoreDuplicateNgrams").item(0).getTextContent());
					Main_NemexF.ignoreDuplicateNgrams = Boolean
							.valueOf(eElement
									.getElementsByTagName(
											"ignoreDuplicateNgrams").item(0)
									.getTextContent());
					// System.out.println("similarityMeasure: " +
					// eElement.getElementsByTagName("similarityMeasure").item(0).getTextContent());
					if (eElement.getElementsByTagName("similarityMeasure")
							.item(0).getTextContent()
							.equals(SimilarityMeasure.DICE_SIMILARITY_MEASURE)) {
						Main_NemexF.similarityMeasure = SimilarityMeasure.DICE_SIMILARITY_MEASURE;
					} else if (eElement
							.getElementsByTagName("similarityMeasure")
							.item(0)
							.getTextContent()
							.equals(SimilarityMeasure.JACCARD_SIMILARITY_MEASURE)) {
						Main_NemexF.similarityMeasure = SimilarityMeasure.JACCARD_SIMILARITY_MEASURE;
					} else if (eElement
							.getElementsByTagName("similarityMeasure")
							.item(0)
							.getTextContent()
							.equals(SimilarityMeasure.COSINE_SIMILARITY_MEASURE)) {
						Main_NemexF.similarityMeasure = SimilarityMeasure.COSINE_SIMILARITY_MEASURE;
					} else if (eElement
							.getElementsByTagName("similarityMeasure")
							.item(0)
							.getTextContent()
							.equals(SimilarityMeasure.OVERLAP_SIMILARITY_MEASURE)) {
						Main_NemexF.similarityMeasure = SimilarityMeasure.OVERLAP_SIMILARITY_MEASURE;
					}

					// System.out.println("similarityThreshold: " +
					// eElement.getElementsByTagName("similarityThreshold").item(0).getTextContent());
					Main_NemexF.similarityThreshold = (double) Double
							.valueOf(eElement
									.getElementsByTagName("similarityThreshold")
									.item(0).getTextContent());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static final long MEGABYTE = 1024L * 1024L;

	public static long bytesToMegabytes(long bytes) {
		return bytes / MEGABYTE;
	}

}
