package de.dfki.lt.nemex.data;

import org.junit.Test;

public class AmplexorGazetteerTest {

	@Test
	public void test() throws GazetteerCreationException {
		String corpus = null;

		if (null == corpus) {
			String entriesFile = "/local/data/AmplexorData/CSD_Data_Delivery_v1/Controlled_Vocabulary/entriesType.txt";
			String nemexGazetteer = "/local/data/AmplexorData/CSD_Data_Delivery_v1/Controlled_Vocabulary/entriesType-nemex.txt";

			AmplexorGazetteer.loweCase = true;
			new AmplexorGazetteer(entriesFile, nemexGazetteer);
		}
	}
}
