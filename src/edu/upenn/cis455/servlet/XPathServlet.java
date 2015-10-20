package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.http.*;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import edu.upenn.cis455.bean.DocumentRecord;
import edu.upenn.cis455.crawler.HttpClient;
import edu.upenn.cis455.xpathengine.XPathEngine;
import edu.upenn.cis455.xpathengine.XPathEngineFactory;

/**
 * xPath servlet class generates a UI for user to enter a url and xpaths and
 * validates the document at the url against the xpaths
 * 
 * @author cis455
 *
 */
@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {

	/**
	 * This method fetches the document from the given url and validates the
	 * xpaths
	 */
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
				DocumentRecord document = httpClient.getDocument();
				// get all the xPaths
				String[] xPaths = xPathParam.split(";");
				XPathEngine engine = XPathEngineFactory.getXPathEngine();
				engine.setXPaths(xPaths);
				boolean[] result = engine.evaluate(document.getDocument());
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

	/**
	 * This method generates an html form for the user to enter url and xpaths
	 */
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
