package edu.upenn.cis455.servlet;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.upenn.cis455.bean.Channel;
import edu.upenn.cis455.bean.DocumentRecord;
import edu.upenn.cis455.bean.User;
import edu.upenn.cis455.storage.ChannelDA;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.DocumentRecordDA;
import edu.upenn.cis455.storage.UserDA;

public class XPathServletHelper
{

	private static int port = 8080;
	private static final String allChannelPath = "/allchannels";
	private static final String createChannelPath = "/createchannel";
	private static final String homePath = "/";
	private static final String loginPath = "/login";
	private static final String signupPath = "/signup";
	private static final String signupCompletePath = "/signupcomplete";
	private static final String loginVerifyPath = "/loginverify";
	private static final String createChannelCompletePath = "/createchannelcomplete";
	private static final String viewChannelPath = "/viewchannel";
	private static final String deleteChannelPath = "/deletechannel";

	private static final String sign = "<br><br>" + "Ankit Mishra<br>"
			+ "mankit<br>";

	private static final String servletHome = "<h2> Welcome to xPath Servlet Home page - </h2>"
			+ "<form action=\"http://localhost:"
			+ port
			+ "/servlet/xpath/login\">"
			+ "<input type=\"submit\" value=\"Login\">"
			+ "</form>"
			+ "<br>"
			+ "<form action=\"http://localhost:"
			+ port
			+ "/servlet/xpath/signup\">"
			+ "<input type=\"submit\" value=\"Sign Up\">"
			+ "</form>"
			+ "<br>"
			+ "<form action=\"http://localhost:"
			+ port
			+ "/servlet/xpath/allchannels\">"
			+ "<input type=\"submit\" value=\"View Channels\">"
			+ "</form>"
			+ sign;

	private static final String loginPage = "<h2> Welcome to xPath Servlet Login page - </h2>"
			+ "<form method = \"post\" action=\"http://localhost:"
			+ port
			+ "/servlet/xpath/loginverify\">"
			+ "<br>username <br/>"
			+ "<input type=\"text\" name=\"username\">"
			+ "<br>password <br/>"
			+ "<input type=\"password\" name=\"password\">"
			+ "<br>"
			+ "<input type=\"submit\" value=\"Login\">" + "</form>" + sign;

	private static final String signupPage = "<h2> Welcome to xPath Servlet Sign up page - </h2>"
			+ "<form method = \"post\" action=\"http://localhost:"
			+ port
			+ "/servlet/xpath/signupcomplete\">"
			+ "<br>username <br>"
			+ "<input type=\"text\" name=\"username\">"
			+ "<br>password <br>"
			+ "<input type=\"password\" name=\"password\">"
			+ "<br>retype password <br>"
			+ "<input type=\"password\" name=\"passwordReTyped\">"
			+ "<br>"
			+ "<input type=\"submit\" value=\"Sign up\">" + "</form>" + sign;

	private static final String createChannel = "<h2> Welcome to xPath Servlet new channel page - </h2>"
			+ "<form method = \"post\" action=\"http://localhost:"
			+ port
			+ "/servlet/xpath/createchannelcomplete\">"
			+ "<br>Channel Name <br>"
			+ "<input type=\"text\" name=\"channelName\">"
			+ "<br>xPaths [seperate multiple xpaths by a ; ] <br>"
			+ "<input type=\"text\" name=\"xPaths\">"
			+ "<br>"
			+ "<input type=\"submit\" value=\"Create\">" + "</form>" + sign;

	private static final String userHome = "<h2> Welcome to your Home page,  </h2>";

	public static String getServletHome()
	{
		return servletHome;
	}

	public static String getErrorPage(String reason)
	{
		return "<h2> Welcome to xPath Servlet - </h2>" + "<br><br>" + reason
				+ sign;
	}

	public static String getLoginPage()
	{
		return loginPage;
	}

	public static String getSignUpPage()
	{
		return signupPage;
	}

	public static String getUserHome(String username)
	{
		return userHome + username + "<br>";
	}

	public static int getPort()
	{
		return port;
	}

	public static void setPort(int port)
	{
		XPathServletHelper.port = port;
	}

	public static String getAllchannelpath()
	{
		return allChannelPath;
	}

	public static String getCreatechannelpath()
	{
		return createChannelPath;
	}

	public static String getHomepath()
	{
		return homePath;
	}

	public static String getLoginpath()
	{
		return loginPath;
	}

	public static String getSignuppath()
	{
		return signupPath;
	}

	public static String getSignupcompletepath()
	{
		return signupCompletePath;
	}

	public static String getLoginverifypath()
	{
		return loginVerifyPath;
	}

	public static String getCreateChannel() throws Exception
	{
		return createChannel;
	}

	public static String getCreatechannelcompletepath()
	{
		return createChannelCompletePath;
	}

	public static String getViewchannelpath()
	{
		return viewChannelPath;
	}

	public static String getDeletechannelpath()
	{
		return deleteChannelPath;
	}

	public static String getCss()
	{
		return "<head>" + "<style>" + "table, th, td {"
				+ "    border: 1px solid black;"
				+ "    border-collapse: collapse;" + "}" + "th, td {"
				+ "    padding: 5px;" + "}" + "</style>" + "</head>";
	}

	// helper methods
	public static String loginVerify(String dbPath, HttpServletRequest request,
			HttpServletResponse response) throws Exception
	{
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String pageContent = "username : " + username + " password : "
				+ password;
		if (username == null || password == null || username.isEmpty()
				|| password.isEmpty())
		{
			HttpSession session = request.getSession(true);
			session.setAttribute("loginError", "invalid username or password");
			response.sendRedirect(request.getContextPath()
					+ request.getServletPath() + "/login");
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
				session.removeAttribute("signupError");
				session.removeAttribute("loginError");
				if (session.getAttribute("loginReferer") != null)
				{
					response.sendRedirect((String) session
							.getAttribute("loginReferer"));
				}
				else
				{
					response.sendRedirect(request.getContextPath()
							+ request.getServletPath() + allChannelPath);
				}

			}
			else
			{
				pageContent += "Invalid username or password<br>"
						+ XPathServletHelper.getLoginPage();
			}
			DBWrapper.closeDBWrapper();
		}
		return pageContent + sign;
	}

	public static String signupComplete(String dbPath,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception
	{
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String passwordReTyped = request.getParameter("passwordReTyped");
		String pageContent = "username : " + username + " password : "
				+ password;
		if (username == null || password == null || passwordReTyped == null
				|| username.isEmpty() || password.isEmpty()
				|| passwordReTyped.isEmpty())
		{
			HttpSession session = request.getSession(true);
			session.setAttribute("signupError", "invalid username or password");
			response.sendRedirect(request.getContextPath()
					+ request.getServletPath() + "/signup");
		}
		else if (!password.equals(passwordReTyped))
		{
			HttpSession session = request.getSession(true);
			session.setAttribute("signupError", "password mismatch");
			response.sendRedirect(request.getContextPath()
					+ request.getServletPath() + "/signup");
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
					session.removeAttribute("signupError");
					session.removeAttribute("loginError");
					if (session.getAttribute("signupReferer") != null)
					{
						response.sendRedirect((String) session
								.getAttribute("signupReferer"));
					}
					else
					{
						response.sendRedirect(request.getContextPath()
								+ request.getServletPath() + allChannelPath);
					}
				}
			}
			else if (user != null)
			{
				HttpSession session = request.getSession(true);
				session.setAttribute("signupError", "user already exists");
				response.sendRedirect(request.getContextPath()
						+ request.getServletPath() + "/signup");
			}
			DBWrapper.closeDBWrapper();
		}
		return pageContent + sign;
	}

	public static String getAllChannels(String dbPath,
			HttpServletRequest request, HttpServletResponse responsee)
			throws Exception
	{
		StringBuilder pageContent = new StringBuilder();
		String username = getUserName(request);
		DBWrapper.openDBWrapper(dbPath);
		pageContent.append("<h2> Welcome "
				+ (username == null ? "" : username + ", ")
				+ " to xPath Servlet Channel page - </h2><br>");
		pageContent.append("Channels  - <br>");
		pageContent.append("<table>");
		pageContent.append("<tr>");
		pageContent.append("<th>Channel Name</th>");
		pageContent.append("<th>View Channel</th>");
		pageContent.append("<th>Delete Channel</th>");
		pageContent.append("<tr>");
		ArrayList<Channel> channels = ChannelDA.getAllChannels();
		for (Channel channel : channels)
		{
			pageContent.append("<tr>");
			pageContent.append("<td>" + channel + "</td>");
			pageContent.append("<td>" + "<form action=\"http://localhost:"
					+ port + "/servlet/xpath/viewchannel\">"
					+ "<input type=\"hidden\" name =\"channelName\"value=\""
					+ channel.getChannelName() + "\">"
					+ "<input type=\"submit\" value=\"View\">" + "</form>"
					+ "</td>");
			pageContent.append("<td>" + "<form action=\"http://localhost:"
					+ port + "/servlet/xpath/deletechannel\">"
					+ "<input type=\"hidden\" name =\"channelName\"value=\""
					+ channel.getChannelName() + "\">"
					+ "<input type=\"submit\" value=\"Delete\">" + "</form>"
					+ "</td>");
			pageContent.append("<tr>");
		}
		pageContent.append("</table>");
		pageContent.append("<br><form action=\"http://localhost:" + port
				+ "/servlet/xpath/createchannel\">"
				+ "<input type=\"submit\" value=\"New Channel\">" + "</form>"
				+ sign);
		DBWrapper.closeDBWrapper();
		return pageContent.toString();
	}

	public static String createChannelComplete(String dbPath,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception
	{
		String channelName = request.getParameter("channelName");
		String xPaths = request.getParameter("xPaths");
		HttpSession session = request.getSession(false);
		String username = getUserName(request);
		String pageContent = "username : " + username + " channelName : "
				+ channelName + " xPaths : " + xPaths;
		if (username == null || username.isEmpty())
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
		else if (channelName == null || xPaths == null || channelName.isEmpty()
				|| xPaths.isEmpty())
		{

			session = request.getSession(true);
			session.setAttribute("createChannelError",
					"Whoops - enter channel name and xpath");
			response.sendRedirect(request.getContextPath()
					+ request.getServletPath() + "/createchannel");

		}
		else
		{
			DBWrapper.openDBWrapper(dbPath);
			Channel channel = ChannelDA.getChannel(channelName);
			if (channel == null)
			{
				channel = new Channel(channelName, username);
				String[] xPathArray = xPaths.split(";");
				for (String xPath : xPathArray)
				{
					channel.addXPath(xPath);
				}
				ChannelDA.putChannel(channel);
				channel = ChannelDA.getChannel(channelName);
				if (channel != null)
				{
					session = request.getSession(true);
					session.removeAttribute("createChannelError");
					response.sendRedirect(request.getContextPath()
							+ request.getServletPath() + getAllchannelpath());
				}
				else
				{
					session = request.getSession(true);
					session.setAttribute("createChannelError",
							"Whoops - unable to create channel. Try again!!<br>");
					response.sendRedirect(request.getContextPath()
							+ request.getServletPath()
							+ XPathServletHelper.getCreatechannelpath());
				}
			}
			else if (channel != null)
			{
				session = request.getSession(true);
				session.setAttribute("createChannelError",
						"Whoops - Channel already exists. Try again!!<br>");
				response.sendRedirect(request.getContextPath()
						+ request.getServletPath()
						+ XPathServletHelper.getCreatechannelpath());
			}
			DBWrapper.closeDBWrapper();
		}
		return pageContent + sign;
	}

	public static String getChannel(String dbPath, String channelName)
			throws Exception
	{
		String pageContent;
		if (dbPath == null || dbPath.isEmpty())
		{
			pageContent = "Error : Whoops - unable to read channel, dbPath empty. Try again!!<br>";

		}
		else if (channelName == null || channelName.isEmpty())
		{
			pageContent = "Error : Whoops - unable to read channel, channel name missing empty. Try again!!<br>";
		}
		else
		{
			DBWrapper.openDBWrapper(dbPath);
			Channel channel = ChannelDA.getChannel(channelName);
			if (channel == null)
			{
				pageContent = "Error : Whoops - unable to read channel, channel not found. Try again!!<br>";
			}
			else
			{
				StringBuilder pageBuilder = new StringBuilder();
				pageBuilder.append("Channel - " + channel.getChannelName()
						+ "<br>");
				pageBuilder.append("Created By - " + channel.getUsername()
						+ "<br>");
				pageBuilder.append("<br><table>");
				pageBuilder.append("<th>xPaths </th>");
				for (String xPath : channel.getxPaths())
				{
					pageBuilder.append("<tr><td>" + xPath + "</td></tr>");
				}
				pageBuilder.append("</table><br>");
				pageBuilder.append("Documents - <br>");
				for (String documentId : channel.getDocumentIdList())
				{
					DocumentRecord document = DocumentRecordDA
							.getDocument(documentId);
					if (document == null)
					{
						pageBuilder
								.append("<tr><td>Opps!! not found</td></tr>");
					}
					else
					{
						pageBuilder.append("<tr><td>" + documentId
								+ "</td></tr>");

					}
				}
				pageContent = pageBuilder.toString();
			}
			DBWrapper.closeDBWrapper();
		}
		return getCss() + pageContent + sign;
	}

	private static String getUserName(HttpServletRequest request)
	{
		HttpSession session = request.getSession(false);
		String username;
		if (session == null)
		{
			username = null;
		}
		else
		{
			username = (String) session.getAttribute("username");
		}
		return username;
	}

	public static String deleteChannel(String dbPath,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception
	{
		String pageContent = null;
		String channelName = request.getParameter("channelName");
		String username = null;
		HttpSession session = request.getSession(false);
		if (session != null)
		{
			username = (String) session.getAttribute("username");
		}
		if (username == null || username.isEmpty())
		{
			session = request.getSession(true);
			session.setAttribute("loginError",
					"Whoops - please login to delete channel");
			session.setAttribute("loginReferer",
					request.getContextPath() + request.getServletPath()
							+ XPathServletHelper.getAllchannelpath());
			response.sendRedirect(request.getContextPath()
					+ request.getServletPath() + "/login");
		}
		else if (dbPath == null || dbPath.isEmpty())
		{
			pageContent = "Error : Whoops - unable to read channel, dbPath empty. Try again!!<br>";

		}
		else if (channelName == null || channelName.isEmpty())
		{
			pageContent = "Error : Whoops - unable to read channel, channel name missing empty. Try again!!<br>";
		}
		else
		{
			DBWrapper.openDBWrapper(dbPath);
			Channel channel = ChannelDA.getChannel(channelName);
			if (channel == null)
			{
				pageContent = "Error : Whoops - unable to delete channel. It doesn't exist!!<br>";
			}
			else
			{
				if (channel.getUsername().equals(username))
				{
					if (ChannelDA.removeChannel(channelName))
					{
						pageContent = "Woohoo - channel deleted!!";
					}
					else
					{
						pageContent = "Error : Whoops - unable to delete channel. Try again!!<br>";
					}
				}
				else
				{
					pageContent = "Error : Whoops - unable to delete channel. You are not the owner of the channel!!<br>";
				}

			}

			DBWrapper.closeDBWrapper();
		}
		return getCss() + pageContent + sign;
	}

}
