package de.dfki.lt.nemex;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import de.dfki.lt.nemex.a.NEMEX_A;
import de.dfki.lt.nemex.a.similarity.SimilarityMeasure;

public class Test_XML_RPC {

	public static void main(String[] args) {
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		try {
			config.setServerURL(new URL("http://127.0.0.1:8080/nemex/xmlrpc"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		XmlRpcClient client = new XmlRpcClient();
		client.setConfig(config);

		Object[] params1 = new Object[] {
				new String(
						"/opt/tomcat7/apache-tomcat-7.0.39/webapps/nemex/resources/MedicalTerms-mwl-plain.txt"),
						new String("#"), new Boolean(true), new Integer(3),
						new Boolean(false) };

		String returnResult = "";

		try {
			returnResult = (String) client.execute(
					"ApproximateStringMatching.loadVocabulary", params1);
		} catch (XmlRpcException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Object[] params = new Object[] { new String("wolma"), returnResult,
				new String("DICE_SIMILARITY_MEASURE"), new Double(0.12) };

		try {
			Object[] returnList = (Object[]) client.execute(
					"ApproximateStringMatching.checkStringSimilarity", params);
			for (int i = 0; i < returnList.length; i++) {
				System.out.println((String) returnList[i]);
			}
		} catch (XmlRpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
