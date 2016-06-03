package de.dfki.lt.nemex;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import de.dfki.lt.nemex.a.NEMEX_A;
import de.dfki.lt.nemex.a.data.Gazetteer;
import de.dfki.lt.nemex.a.data.InvertedList;

public class NemexLoadVocabsServlet extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		String vocabulary = request.getParameter("vocabulary");

		String rootPath = getServletConfig().getServletContext().getRealPath(
				"/");
		String gazetteerFilePath = rootPath + "resources" + File.separator
				+ request.getParameter("vocabulary");
		String delimiter = request.getParameter("delimiter");
		Boolean delimiterSwitchOff = Boolean.valueOf(request
				.getParameter("delimiterSwitchOff"));
		int nGramSize = Integer.valueOf(request.getParameter("nGramSize"));
		Boolean ignoreDuplicateNgrams = Boolean.valueOf(request
				.getParameter("ignoreDuplicateNgrams"));

		if (!NEMEX_A.loadedGazetteers.containsKey(gazetteerFilePath)) {

			System.out.println("[INFO] Creating the inverted list (index)...");
			System.out.println("[INFO] " + gazetteerFilePath);
			long startInvertedListCreationTime = System.currentTimeMillis();
			Gazetteer gazetteer = new Gazetteer(gazetteerFilePath, delimiter,
					delimiterSwitchOff);
			InvertedList invertedList = new InvertedList(gazetteer, nGramSize,
					ignoreDuplicateNgrams);
			long endInvertedListCreationTime = System.currentTimeMillis();
			System.out
					.println("[TIME INFO] Elapsed time for inverted list creation: "
							+ (endInvertedListCreationTime - startInvertedListCreationTime)
							+ " ms");

			NEMEX_A.loadedGazetteers.put(gazetteerFilePath, invertedList);

			System.out.println("[INFO] Vocabulary loaded successfully.");

			// response.sendRedirect("nemex.html");
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();

			out.println("<html>");
			out.println("<head>");
			out.println("<title>Welcome to NEMEX!</title>");
			out.println("</head>");
			out.println("<body>");
			out.println("<h1>Welcome to NEMEX!</h1>");
			out.println("<h3>DFKI Language Technology (LT) Lab.'s Playground for Text Mining, Information Extraction, RTE, etc.</h3>");

			out.println("<form action = \"NemexApproximateStringMatching\" method = \"get\">");
			out.println("<p>");
			out.println("<label>");
			out.println("After choosing a vocabulary, please type your query string, set the configurations and press the Query button:");
			out.println("</label><br /><br />");
			out.println("</p>");

			out.println("<p>");
			out.println("<label>");
			out.println("Loaded vocabularies:<br />");
			out.println("</label>");
			out.println("<select name = \"vocabulary\">");

			for (String gazetteerPath : NEMEX_A.loadedGazetteers.keySet()) {
				out.println("<option value=" + gazetteerPath + ">"
						+ gazetteerPath + "</option>");
			}

			out.println("</select>");
			out.println("</p>");

			out.println("<p>");
			out.println("<label>");
			out.println("Query String:<br />");
			out.println("</label>");
			out.println("<input type = \"text\" name = \"queryString\" />");
			out.println("</label>");
			out.println("</p>");

			out.println("<h3>Configurations:</h3>");

			out.println("<p>");
			out.println("Similarity Measure (Similarity Function):");
			out.println("<br>");
			out.println("<input type=\"radio\" name=\"similarityMeasure\" value=\"COSINE_SIMILARITY_MEASURE\">Cosine<br>");
			out.println("<input type=\"radio\" name=\"similarityMeasure\" value=\"DICE_SIMILARITY_MEASURE\" checked>Dice<br>");
			out.println("<input type=\"radio\" name=\"similarityMeasure\" value=\"JACCARD_SIMILARITY_MEASURE\">Jaccard<br>");
			out.println("<input type=\"radio\" name=\"similarityMeasure\" value=\"OVERLAP_SIMILARITY_MEASURE\">Overlap");
			out.println("</p>");

			out.println("<p>");
			out.println("<label>");
			out.println("Similarity Threshold for Approximate String Matching (between 0 and 1):");
			out.println("<br /><input type = \"text\" name = \"similarityThreshold\" value = \"0.75\" />");
			out.println("</label>");
			out.println("</p>");

			out.println("<input type = \"submit\" value = \"Query!\"> <input type = \"reset\">");

			out.println("</form>");

			out.println("<p>");
			out.println("<form action = \"/nemex/index.html\">");
			out.println("<br /><br />");
			out.println("<h3>");
			out.println("Click the button below if your desired vocabulary is not in the loaded vocabularies list:");
			out.println("</h3>");
			out.println("<input type= \"submit\" value = \"Load new vocabulary\">");
			out.println("</form>");
			out.println("</p>");

			out.println("</body>");
			out.println("</html>");

		} else {
			System.out.println("[INFO] The vocabulary is already loaded!");
			System.out.println("[INFO] Unloading...");
			NEMEX_A.loadedGazetteers.remove(gazetteerFilePath);
			System.gc();
			this.doPost(request, response);
		}

	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		String vocabulary = request.getParameter("vocabulary");

		String rootPath = getServletConfig().getServletContext().getRealPath(
				"/");
		String gazetteerFilePath = rootPath + "resources/"
				+ request.getParameter("vocabulary");
		String delimiter = request.getParameter("delimiter");
		Boolean delimiterSwitchOff = Boolean.valueOf(request
				.getParameter("delimiterSwitchOff"));
		int nGramSize = Integer.valueOf(request.getParameter("nGramSize"));
		Boolean ignoreDuplicateNgrams = Boolean.valueOf(request
				.getParameter("ignoreDuplicateNgrams"));

		if (!NEMEX_A.loadedGazetteers.containsKey(gazetteerFilePath)) {

			System.out.println("[INFO] Creating the inverted list (index)...");
			System.out.println("[INFO] " + gazetteerFilePath);
			long startInvertedListCreationTime = System.currentTimeMillis();
			Gazetteer gazetteer = new Gazetteer(gazetteerFilePath, delimiter,
					delimiterSwitchOff);
			InvertedList invertedList = new InvertedList(gazetteer, nGramSize,
					ignoreDuplicateNgrams);
			long endInvertedListCreationTime = System.currentTimeMillis();
			System.out
					.println("[TIME INFO] Elapsed time for inverted list creation: "
							+ (endInvertedListCreationTime - startInvertedListCreationTime)
							+ " ms");

			NEMEX_A.loadedGazetteers.put(gazetteerFilePath, invertedList);

			System.out.println("[INFO] Vocabulary loaded successfully.");

			// response.sendRedirect("nemex.html");
			response.setContentType("text/html");
			PrintWriter out = response.getWriter();

			out.println("<html>");
			out.println("<head>");
			out.println("<title>Welcome to NEMEX!</title>");
			out.println("</head>");
			out.println("<body>");
			out.println("<h1>Welcome to NEMEX!</h1>");
			out.println("<h3>DFKI Language Technology (LT) Lab.'s Playground for Text Mining, Information Extraction, RTE, etc.</h3>");

			out.println("<form action = \"NemexApproximateStringMatching\" method = \"get\">");
			out.println("<p>");
			out.println("<label>");
			out.println("After choosing a vocabulary, please type your query string, set the configurations and press the Query button:");
			out.println("</label><br /><br />");
			out.println("</p>");

			out.println("<p>");
			out.println("<label>");
			out.println("Loaded vocabularies:<br />");
			out.println("</label>");
			out.println("<select name = \"vocabulary\">");

			for (String gazetteerPath : NEMEX_A.loadedGazetteers.keySet()) {
				out.println("<option value=" + gazetteerPath + ">"
						+ gazetteerPath + "</option>");
			}

			out.println("</select>");
			out.println("</p>");

			out.println("<p>");
			out.println("<label>");
			out.println("Query String:<br />");
			out.println("</label>");
			out.println("<input type = \"text\" name = \"queryString\" />");
			out.println("</label>");
			out.println("</p>");

			out.println("<h3>Configurations:</h3>");

			out.println("<p>");
			out.println("Similarity Measure (Similarity Function):");
			out.println("<br>");
			out.println("<input type=\"radio\" name=\"similarityMeasure\" value=\"COSINE_SIMILARITY_MEASURE\">Cosine<br>");
			out.println("<input type=\"radio\" name=\"similarityMeasure\" value=\"DICE_SIMILARITY_MEASURE\" checked>Dice<br>");
			out.println("<input type=\"radio\" name=\"similarityMeasure\" value=\"JACCARD_SIMILARITY_MEASURE\">Jaccard<br>");
			out.println("<input type=\"radio\" name=\"similarityMeasure\" value=\"OVERLAP_SIMILARITY_MEASURE\">Overlap");
			out.println("</p>");

			out.println("<p>");
			out.println("<label>");
			out.println("Similarity Threshold for Approximate String Matching (between 0 and 1):");
			out.println("<br /><input type = \"text\" name = \"similarityThreshold\" value = \"0.75\" />");
			out.println("</label>");
			out.println("</p>");

			out.println("<input type = \"submit\" value = \"Query!\"> <input type = \"reset\">");

			out.println("</form>");

			out.println("<p>");
			out.println("<form action = \"/nemex/index.html\">");
			out.println("<br /><br />");
			out.println("<h3>");
			out.println("Click the button below if your desired vocabulary is not in the loaded vocabularies list:");
			out.println("</h3>");
			out.println("<input type= \"submit\" value = \"Load new vocabulary\">");
			out.println("</form>");
			out.println("</p>");

			out.println("</body>");
			out.println("</html>");

		} else {
			System.out.println("[INFO] The vocabulary is already loaded!");
			System.out.println("[INFO] Unloading...");
			NEMEX_A.loadedGazetteers.remove(gazetteerFilePath);
			System.gc();
			this.doPost(request, response);
		}
	}
}
