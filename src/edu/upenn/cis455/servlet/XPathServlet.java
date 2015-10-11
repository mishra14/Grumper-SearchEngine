package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.*;

@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {
	
	/* TODO: Implement user interface for XPath engine here */
	
	/* You may want to override one or both of the following methods */

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		/* TODO: Implement user interface for XPath engine here */
		PrintWriter out = response.getWriter();		
		out.print("<html><body>"
					+ request.toString()
					+ "<br>"
					+ request.getParameter("xPath")
					+ "<br>"
					+ request.getParameter("url")
					+ "</body></html>");
		
		response.flushBuffer();
		
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter out = response.getWriter();		
		out.print("<html><body>"
					+ "<h2> xPath Servlet form - </h2>"
					+ "<form method=\"post\" action=\"/servlet/xpath\">"
					+ "xPath:<br>"
					+ "<input type=\"text\" name=\"xPath\">"
					+ "<br>"
					+ "Source URL:<br>"
					+ "<input type=\"text\" name=\"url\">"
					+ "<br><br>"
					+ "<input type=\"submit\" value=\"submit\">"
					+ "</form>"
					+ "</body></html>");
		
		response.flushBuffer();
	}

}









