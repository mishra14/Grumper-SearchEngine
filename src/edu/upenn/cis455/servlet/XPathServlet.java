package edu.upenn.cis455.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javax.servlet.http.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet {
	
	private static final String BASE_PATH = "/usr/share/jetty/webapps/";
	/* TODO: Implement user interface for XPath engine here */
	
	/* You may want to override one or both of the following methods */

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		/* TODO: Implement user interface for XPath engine here */
		PrintWriter out = response.getWriter();		
		URL sourceUrl = new URL(request.getParameter("url"));
		String sourceFileName = sourceUrl.getFile();
		File sourceFile = new File(BASE_PATH+sourceFileName);
		sourceFile.mkdirs();
		Path destinationPath = new File(BASE_PATH+sourceFileName).toPath();
		Files.copy(sourceUrl.openStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
		byte[] fileBytes = Files.readAllBytes(destinationPath);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		
		String xPathParam = request.getParameter("xPath");
		
		if(xPathParam == null || xPathParam.isEmpty())
		{
			//error case
			out.print("Invalid xPath");
		}
		else
		{
			//get all the xPaths
			
			out.print("Null xpath");
		}
		
		try 
		{
			db = dbf.newDocumentBuilder();
			Document document = db.parse(sourceFile);
			document.normalize();
			
		} 
		catch (ParserConfigurationException e) 
		{
			out.print(e.toString());
		} 
		catch (SAXException e)
		{
			out.print(e.toString());
		} 
		catch (Exception e)
		{
			out.print(e.toString());
		} 
		
		//out.print(new String(fileBytes));

		deleteFileAndParents(destinationPath);
		
		
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
					+ "</form><br><br>"
					+ "Ankit Mishra<br>"
					+ "mankit<br>"
					+ "</body></html>");
		
		response.flushBuffer();
	}
	
	public void deleteFileAndParents(Path filePath) throws IOException
	{
		if(filePath==null || filePath.endsWith(BASE_PATH))
		{
			return;
		}
		else
		{
			if(Files.isRegularFile(filePath))
			{
				Files.deleteIfExists(filePath);
			}
			else if(Files.isDirectory(filePath))
			{
				try
				{
					Files.deleteIfExists(filePath);
				}
				catch(DirectoryNotEmptyException e)
				{
					return;
				}
			}
			deleteFileAndParents(filePath.getParent());
		}
	}
}









