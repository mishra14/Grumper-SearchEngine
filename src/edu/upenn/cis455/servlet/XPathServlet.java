package edu.upenn.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.*;

/**
 * xPath servlet class generates a UI for user to enter a url and xpaths and
 * validates the document at the url against the xpaths
 * 
 * Features - servlet/xpath/ Home page - 3 buttons - login, sign up and view
 * channels (if logged in then should re direct to user home page)
 * 
 * servlet/xpath/login Login - form with user name and password - should go to
 * error page or user home - start a session
 * 
 * servlet/xpath/signup sign up page - form with user name, password and
 * password retype - should go to error page or user home - start a session
 * 
 * servlet/xpath/allchannels view channel page -list all channels (subscribe
 * should lead to login)
 * 
 * servlet/xpath/userhome - user home - list all users channels with buttons for
 * creating new channels and logging out
 * 
 * @author cis455
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
		XPathServletHelper.setPort(request.getLocalPort());
		String pathInfo = request.getPathInfo();
		String pageContent = null;
		if (pathInfo.equalsIgnoreCase(XPathServletHelper.getLoginverifypath()))
		{
			String dbPath = getServletContext().getInitParameter("BDBstore");
			if (dbPath == null)
			{
				pageContent = "Whoops - Error connecting to DB <br>"
						+ XPathServletHelper.getLoginPage();
			}
			else
			{
				try
				{
					pageContent = XPathServletHelper.loginVerify(dbPath,
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
		else if (pathInfo.equalsIgnoreCase(XPathServletHelper
				.getSignupcompletepath()))
		{
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
					pageContent = XPathServletHelper.signupComplete(dbPath,
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
		else if (pathInfo.equalsIgnoreCase(XPathServletHelper
				.getCreatechannelcompletepath()))
		{

			String dbPath = getServletContext().getInitParameter("BDBstore");
			if (dbPath == null)
			{
				pageContent = "Whoops - Error connecting to DB <br>";
			}
			else
			{
				try
				{
					pageContent = XPathServletHelper.createChannelComplete(
							dbPath, request, response);
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
		out.print("<html><body>" + XPathServletHelper.getCss() + pageContent
				+ "</body></html>");
		response.flushBuffer();
	}

	/**
	 * This method generates an html form for the user to enter url and xpaths
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException
	{

		XPathServletHelper.setPort(request.getLocalPort());
		String pathInfo = request.getPathInfo();
		String pageContent = null;
		if (pathInfo == null
				|| pathInfo.equalsIgnoreCase(XPathServletHelper.getHomepath())) // homepage
		{
			pageContent = XPathServletHelper.getServletHome();
		}
		else if (pathInfo.equalsIgnoreCase(XPathServletHelper.getLoginpath()))
		{
			HttpSession session = request.getSession(false);
			if (session != null)
			{
				if (session.getAttribute("username") != null)
				{
					response.sendRedirect(request.getContextPath()
							+ request.getServletPath()
							+ XPathServletHelper.getAllchannelpath());
				}
				else if (session.getAttribute("loginError") != null)
				{
					pageContent += "error : "
							+ session.getAttribute("loginError") + "<br>";
				}

			}
			pageContent += XPathServletHelper.getLoginPage();
		}
		else if (pathInfo.equalsIgnoreCase(XPathServletHelper.getSignuppath())) // signup
																				// page
		{
			HttpSession session = request.getSession(false);
			if (session != null)
			{
				pageContent += "error : " + session.getAttribute("signupError")
						+ "<br>";
			}
			pageContent += request.getHeader("referer") + "<br>";
			pageContent += XPathServletHelper.getSignUpPage();
		}
		else if (pathInfo.equalsIgnoreCase(XPathServletHelper
				.getAllchannelpath())) // all channels
		{
			String dbPath = getServletContext().getInitParameter("BDBstore");
			if (dbPath == null)
			{
				pageContent = "Whoops - Error connecting to DB <br>";
			}
			else
			{
				try
				{
					pageContent = XPathServletHelper.getAllChannels(dbPath,
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
		else if (pathInfo.equalsIgnoreCase(XPathServletHelper
				.getCreatechannelpath())) // create channel page
		{
			HttpSession session = request.getSession(false);
			if (session == null || session.getAttribute("username") == null)
			{
				session = request.getSession(true);
				session.setAttribute("loginError",
						"Whoops - please login to create channel");
				session.setAttribute("loginReferer",
						request.getContextPath() + request.getServletPath()
								+ XPathServletHelper.getCreatechannelpath());
				response.sendRedirect(request.getContextPath()
						+ request.getServletPath() + "/login");
			}
			else
			{
				if (session.getAttribute("createChannelError") != null)
				{
					pageContent = "error : "
							+ session.getAttribute("createChannelError")
							+ "<br>";
				}
				try
				{
					pageContent += XPathServletHelper.getCreateChannel();
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
		else if (pathInfo.equalsIgnoreCase(XPathServletHelper
				.getViewchannelpath())) // view channel page
		{
			String dbPath = getServletContext().getInitParameter("BDBstore");
			String channelName = request.getParameter("channelName");
			try
			{
				pageContent = XPathServletHelper
						.getChannel(dbPath, channelName);
			}
			catch (Exception e)
			{
				StringWriter stringWriter = new StringWriter();
				PrintWriter printWriter = new PrintWriter(stringWriter);
				e.printStackTrace(printWriter);
				pageContent = stringWriter.getBuffer().toString();
			}
		}
		else if (pathInfo.equalsIgnoreCase(XPathServletHelper
				.getDeletechannelpath())) // delete channel page
		{
			String dbPath = getServletContext().getInitParameter("BDBstore");
			try
			{
				pageContent = XPathServletHelper.deleteChannel(dbPath, request,
						response);
			}
			catch (Exception e)
			{
				StringWriter stringWriter = new StringWriter();
				PrintWriter printWriter = new PrintWriter(stringWriter);
				e.printStackTrace(printWriter);
				pageContent = stringWriter.getBuffer().toString();
			}
		}
		else
		{
			pageContent = XPathServletHelper
					.getErrorPage("Whoops - unknown url");
		}
		PrintWriter out = response.getWriter();
		out.print("<html>" + XPathServletHelper.getCss() + "<body>"
				+ pageContent + "</body></html>");
		response.flushBuffer();
	}
}
