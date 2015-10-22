package edu.upenn.cis455.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
/*
 * Features - servlet/xpath/ Home page - 3 buttons - login, sign up and view
 * channels (if logged in then should re direct to user home page)
 * servlet/xpath/login Login - form with user name and password - should go to
 * error page or user home - start a session servlet/xpath/signup sign up page -
 * form with user name, password and password retype - should go to error page
 * or user home - start a session servlet/xpath/allchannels view channel page -
 * list all channels (subscribe should lead to login) servlet/xpath/userhome
 * user home - list all users channels with buttons for creating new channels
 * and logging out
 */
@SuppressWarnings("serial")
public class XPathServlet extends HttpServlet
{

	/**
	 * This method fetches the document from the given url and validates the
	 * xpaths
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException
	{
		String pathInfo = request.getPathInfo();
		String pageContent = null;
		if (pathInfo == null || pathInfo.equalsIgnoreCase("/")) // homepage
		{
			pageContent = XPathServletHelper.getServletHome();
		}
		else if (pathInfo.equalsIgnoreCase("/login"))
		{
			pageContent = XPathServletHelper.getLoginPage();
		}
		else if (pathInfo.equalsIgnoreCase("/signup"))
		{
			pageContent = XPathServletHelper.getSignUpPage();
		}
		else if (pathInfo.equalsIgnoreCase("/userhome"))
		{
			// TODO
		}
		else if (pathInfo.equalsIgnoreCase("/loginverify"))
		{
			String username = request.getParameter("username");
			String password = request.getParameter("password");
			String dbPath = getServletContext().getInitParameter("BDBstore");
			if(dbPath == null)
			{
				pageContent = "Whoops - Error connecting to DB <br>"
						+ XPathServletHelper.getSignUpPage();
			}
			else
			{
				try
				{
					pageContent = XPathServletHelper.loginVerify(username, password, dbPath);
				}
				catch (Exception e)
				{
					StringWriter stringWriter = new StringWriter();
					PrintWriter printWriter = new PrintWriter(stringWriter);
					e.printStackTrace(printWriter);
					pageContent = stringWriter.getBuffer().toString();
				}
			}
			
		}
		else if (pathInfo.equalsIgnoreCase("/signupcomplete"))
		{
			String username = request.getParameter("username");
			String password = request.getParameter("password");
			String passwordReTyped = request.getParameter("passwordReTyped");
			String dbPath = getServletContext().getInitParameter("BDBstore");
			if(dbPath == null)
			{
				pageContent = "Whoops - Error connecting to DB <br>"
						+ XPathServletHelper.getSignUpPage();
			}
			else
			{
				try
				{
					pageContent = XPathServletHelper.signupComplete(username, password, passwordReTyped, dbPath);
				}
				catch (Exception e)
				{
					StringWriter stringWriter = new StringWriter();
					PrintWriter printWriter = new PrintWriter(stringWriter);
					e.printStackTrace(printWriter);
					pageContent = stringWriter.getBuffer().toString();
				}
			}
		}
		else
		{
			pageContent = XPathServletHelper
					.getErrorPage("Whoops - unknown url");
		}
		PrintWriter out = response.getWriter();
		out.print("<html><body>" + pathInfo + pageContent + "</body></html>");
		response.flushBuffer();
	}

	/**
	 * This method generates an html form for the user to enter url and xpaths
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException
	{
		String pathInfo = request.getPathInfo();
		String pageContent = null;
		if (pathInfo == null || pathInfo.equalsIgnoreCase("/")) // homepage
		{
			pageContent = XPathServletHelper.getServletHome();
		}
		else if (pathInfo.equalsIgnoreCase("/login"))
		{
			pageContent = XPathServletHelper.getLoginPage();
		}
		else if (pathInfo.equalsIgnoreCase("/signup"))
		{
			pageContent = XPathServletHelper.getSignUpPage();
		}
		else if (pathInfo.equalsIgnoreCase("/userhome"))
		{

		}
		else
		{
			pageContent = XPathServletHelper
					.getErrorPage("Whoops - unknown url");
		}
		PrintWriter out = response.getWriter();
		out.print("<html><body>"  + pathInfo + pageContent + "</body></html>");
		response.flushBuffer();
	}
}
