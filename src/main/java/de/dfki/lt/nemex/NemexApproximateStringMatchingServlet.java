package de.dfki.lt.nemex;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import de.dfki.lt.nemex.a.NEMEX_A;

public class NemexApproximateStringMatchingServlet extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		String queryString = request.getParameter("queryString");
		String similarityMeasure = request.getParameter("similarityMeasure");
		double similarityThreshold = Double.valueOf(request
				.getParameter("similarityThreshold"));
		String vocabulary = request.getParameter("vocabulary");
		// String rootPath =
		// getServletConfig().getServletContext().getRealPath("/");
		// String gazetteerFilePath = rootPath + "resources/" +
		// request.getParameter("vocabulary");
		String gazetteerFilePath = request.getParameter("vocabulary");

		out.println("<html>");
		out.println("<head>");
		out.println("<title>NEMEX Query Results</title>");
		out.println("</head>");
		out.println("<body>");
		out.println("<h1>*******NEMEX Query Results*******</h1>");

		out.println("<p>");
		out.println("<form action = \"/nemex\">");
		out.println("<input type= \"submit\" value = \"Back\">");
		out.println("</form>");
		out.println("</p>");

		out.println("<h3>Query String: " + queryString + " </h3>");
		out.println("<h2>Query Results:</h2>");

		out.println("<br />");
		NEMEX_A.checkSimilarity(queryString, gazetteerFilePath,
				similarityMeasure, similarityThreshold, out);
		out.println("<br />");

		out.println("<form action = \"NemexApproximateStringMatching\" method = \"post\">");
		out.println("<p>");
		out.println("<h2>");
		out.println("New Query:");
		out.println("</h2>");
		out.println("</p>");

		out.println("<p>");
		out.println("<label>");
		out.println("Loaded vocabularies:<br />");
		out.println("</label>");
		out.println("<select name = \"vocabulary\">");

		for (String gazetteerPath : NEMEX_A.loadedGazetteers.keySet()) {
			out.println("<option value=" + gazetteerPath + ">" + gazetteerPath
					+ "</option>");
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

		out.println("</body>");
		out.println("</html>");

	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		String queryString = request.getParameter("queryString");
		String similarityMeasure = request.getParameter("similarityMeasure");
		double similarityThreshold = Double.valueOf(request
				.getParameter("similarityThreshold"));
		String vocabulary = request.getParameter("vocabulary");
		// String rootPath =
		// getServletConfig().getServletContext().getRealPath("/");
		// String gazetteerFilePath = rootPath + "resources/" +
		// request.getParameter("vocabulary");
		String gazetteerFilePath = request.getParameter("vocabulary");

		out.println("<html>");
		out.println("<head>");
		out.println("<title>NEMEX Query Results</title>");
		out.println("</head>");
		out.println("<body>");
		out.println("<h1>*******NEMEX Query Results*******</h1>");

		out.println("<p>");
		out.println("<form action = \"/nemex\">");
		out.println("<input type= \"submit\" value = \"Back\">");
		out.println("</form>");
		out.println("</p>");

		out.println("<h3>Query String: " + queryString + " </h3>");
		out.println("<h2>Query Results:</h2>");

		out.println("<br />");
		NEMEX_A.checkSimilarity(queryString, gazetteerFilePath,
				similarityMeasure, similarityThreshold, out);
		out.println("<br />");

		out.println("<form action = \"NemexApproximateStringMatching\" method = \"post\">");
		out.println("<p>");
		out.println("<h2>");
		out.println("New Query:");
		out.println("</h2>");
		out.println("</p>");

		out.println("<p>");
		out.println("<label>");
		out.println("Loaded vocabularies:<br />");
		out.println("</label>");
		out.println("<select name = \"vocabulary\">");

		for (String gazetteerPath : NEMEX_A.loadedGazetteers.keySet()) {
			out.println("<option value=" + gazetteerPath + ">" + gazetteerPath
					+ "</option>");
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

		out.println("</body>");
		out.println("</html>");
	}
}
