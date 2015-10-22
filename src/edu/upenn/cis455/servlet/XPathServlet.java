package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.*;

import edu.upenn.cis455.bean.User;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.UserDA;

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
			HttpSession session = request.getSession(false);
			if(session == null || session.getAttribute("username") == null)
			{
				pageContent = "Whoops - Please login<br>"
						+ XPathServletHelper.getLoginPage();
			}
			else
			{
				String username = (String) session.getAttribute("username");
				pageContent = XPathServletHelper.getUserHome(username);
			}
		}
		else if (pathInfo.equalsIgnoreCase("/loginverify"))
		{
			String username = request.getParameter("username");
			String password = request.getParameter("password");
			String dbPath = getServletContext().getInitParameter("BDBstore");
			if (dbPath == null)
			{
				pageContent = "Whoops - Error connecting to DB <br>"
						+ XPathServletHelper.getSignUpPage();
			}
			else
			{
				try
				{
					pageContent = loginVerify(username, password, dbPath,
							request, response);
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
			if (dbPath == null)
			{
				pageContent = "Whoops - Error connecting to DB <br>"
						+ XPathServletHelper.getSignUpPage();
			}
			else
			{
				try
				{
					pageContent = signupComplete(username, password,
							passwordReTyped, dbPath, request, response);
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
		out.print("<html><body>" +pathInfo + pageContent + "</body></html>");
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
		String extras = "";
		extras = request.getProtocol()+request.getLocalAddr()+request.getLocalName()+request.getLocalPort()+request.getContextPath()+request.getServletPath();
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
			HttpSession session = request.getSession(false);
			if(session == null || session.getAttribute("username") == null)
			{
				pageContent = "Whoops - Please login<br>"
						+ XPathServletHelper.getLoginPage();
			}
			else
			{
				String username = (String) session.getAttribute("username");
				pageContent = XPathServletHelper.getUserHome(username);
			}
		}
		else
		{
			pageContent = XPathServletHelper
					.getErrorPage("Whoops - unknown url");
		}
		PrintWriter out = response.getWriter();
		out.print("<html><body>" + extras + "<br>"+ pathInfo + pageContent + "</body></html>");
		response.flushBuffer();
	}

	
	
	
	
	//helper methods
	public static String loginVerify(String username, String password,
			String dbPath, HttpServletRequest request,
			HttpServletResponse response) throws Exception
	{
		String pageContent = "username : " + username + " password : "
				+ password;
		if (username == null || password == null || username.isEmpty()
				|| password.isEmpty())
		{
			pageContent += "Whoops - Invalid username or password <br>"
					+ XPathServletHelper.getLoginPage();
		}
		else
		{
			DBWrapper.openDBWrapper(dbPath);
			User user = UserDA.getUser(username);
			if (user != null && user.getPassword().equals(password))
			{
				pageContent += "Login successful<br>"
						+ XPathServletHelper.getLoginPage();
				HttpSession session = request.getSession(true);
				session.setAttribute("username", username);
				response.sendRedirect(request.getContextPath()+request.getServletPath()+"/userhome");
			}
			else
			{
				pageContent += "Invalid username or password<br>"
						+ XPathServletHelper.getLoginPage();
			}
			DBWrapper.closeDBWrapper();
		}
		return pageContent;
	}

	public static String signupComplete(String username, String password,
			String passwordReTyped, String dbPath, HttpServletRequest request,
			HttpServletResponse response) throws Exception
	{
		String pageContent = "username : " + username + " password : "
				+ password;
		if (username == null || password == null || passwordReTyped == null
				|| username.isEmpty() || password.isEmpty()
				|| passwordReTyped.isEmpty())
		{
			pageContent += "Whoops - Invalid username or password <br>"
					+ XPathServletHelper.getSignUpPage();
		}
		else if (!password.equals(passwordReTyped))
		{
			pageContent += "Whoops - passwords do not match <br>"
					+ XPathServletHelper.getSignUpPage();
		}
		else
		{
			DBWrapper.openDBWrapper(dbPath);
			User user = UserDA.getUser(username);
			if (user == null)
			{
				UserDA.putUser(new User(username, password));
				user = UserDA.getUser(username);
				if (user != null)
				{
					pageContent += "created user - " + user.toString() + "<br>";
					HttpSession session = request.getSession(true);
					session.setAttribute("username", username);
				}
			}
			else if (user != null)
			{
				pageContent += "Whoops - Username already exists<br>"
						+ XPathServletHelper.getSignUpPage();
			}
			DBWrapper.closeDBWrapper();
		}
		return pageContent;
	}
}
