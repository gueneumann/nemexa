package de.dfki.lt.nemex.data;

import org.junit.Test;

public class NemexGazetteerTest {

	@Test
	public void test() throws GazetteerCreationException {
		String corpus = null;

		if (null == corpus) {
			String entriesFile = "./src/test/resources/sample.txt";
			String nemexGazetteer = "./src/test/resources/nemex-sample.txt";

			new NemexGazetteer(entriesFile, nemexGazetteer);
		}
	}
}
