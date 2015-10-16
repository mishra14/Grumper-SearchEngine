package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import javax.servlet.http.*;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import edu.upenn.cis455.http.HttpClient;
import edu.upenn.cis455.xpathengine.XPathEngine;
import edu.upenn.cis455.xpathengine.XPathEngineFactory;

@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		PrintWriter out = response.getWriter();
		URL sourceUrl = new URL(request.getParameter("url"));
		String xPathParam = request.getParameter("xPath");

		if (xPathParam == null || xPathParam.isEmpty()) {
			// error case
			// respond with faliure
			out.print("Invalid xPath");
		} else {
			try {
				HttpClient httpClient = new HttpClient(sourceUrl);
				Document document = httpClient.getDocument();
				// get all the xPaths
				String[] xPaths = xPathParam.split(";");
				XPathEngine engine = XPathEngineFactory.getXPathEngine();
				engine.setXPaths(xPaths);
				boolean[] result = engine.evaluate(document);
				for (int i = 0; i < result.length; i++) {
					out.println(xPaths[i] + ":"
							+ ((result[i]) ? "Success" : "Faliure"));
				}
			} catch (ParserConfigurationException e) {
				out.print(e.toString());
			} catch (SAXException e) {
				out.print(e.toString());
			} catch (Exception e) {
				out.print(e.toString());
			}
		}
		response.flushBuffer();

	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		PrintWriter out = response.getWriter();
		out.print("<html><body>" + "<h2> xPath Servlet form - </h2>"
				+ "<form method=\"post\" action=\"/servlet/xpath\">"
				+ "xPath:[separate multiple xPaths with a ';']<br>"
				+ "<input type=\"text\" name=\"xPath\">" + "<br>"
				+ "Source URL:<br>" + "<input type=\"text\" name=\"url\">"
				+ "<br><br>" + "<input type=\"submit\" value=\"submit\">"
				+ "</form><br><br>" + "Ankit Mishra<br>" + "mankit<br>"
				+ "</body></html>");

		response.flushBuffer();
	}

}
