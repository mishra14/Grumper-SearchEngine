package edu.upenn.cis455.servlet;

import edu.upenn.cis455.bean.User;
import edu.upenn.cis455.storage.DBWrapper;
import edu.upenn.cis455.storage.UserDA;

public class XPathServletHelper
{

	private static final String servletHome = "<h2> Welcome to xPath Servlet Home page - </h2>"
			+ "<form action=\"http://localhost:8080/servlet/xpath/login\">"
			+ "<input type=\"submit\" value=\"Login\">"
			+ "</form>"
			+ "<br/>"
			+ "<form action=\"http://localhost:8080/servlet/xpath/signup\">"
			+ "<input type=\"submit\" value=\"Sign Up\">"
			+ "</form><br><br>"
			+ "Ankit Mishra<br>" + "mankit<br>";

	private static final String loginPage = "<h2> Welcome to xPath Servlet Login page - </h2>"
			+ "<form method = \"post\" action=\"http://localhost:8080/servlet/xpath/loginverify\">"
			+ "<br>username <br/>"
			+ "<input type=\"text\" name=\"username\">"
			+ "<br>password <br/>"
			+ "<input type=\"password\" name=\"password\">"
			+ "<br>"
			+ "<input type=\"submit\" value=\"Login\">"
			+ "</form>"
			+ "<br>"
			+ "<br><br>" + "Ankit Mishra<br>" + "mankit<br>";

	private static final String signupPage = "<h2> Welcome to xPath Servlet Sign up page - </h2>"
			+ "<form method = \"post\" action=\"http://localhost:8080/servlet/xpath/signupcomplete\">"
			+ "<br>username <br>"
			+ "<input type=\"text\" name=\"username\">"
			+ "<br>password <br>"
			+ "<input type=\"password\" name=\"password\">"
			+ "<br>retype password <br>"
			+ "<input type=\"password\" name=\"passwordReTyped\">"
			+ "<br>"
			+ "<input type=\"submit\" value=\"Sign up\">"
			+ "</form>"
			+ "<br>"
			+ "<br><br>" + "Ankit Mishra<br>" + "mankit<br>";

	public static String getServletHome()
	{
		return servletHome;
	}

	public static String getErrorPage(String reason)
	{
		return "<h2> Welcome to xPath Servlet - </h2>" + "<br><br>" + reason
				+ "<br><br>" + "Ankit Mishra<br>" + "mankit<br>";
	}

	public static String getLoginPage()
	{
		return loginPage;
	}

	public static String getSignUpPage()
	{
		return signupPage;
	}

	public static String loginVerify(String username, String password,
			String dbPath) throws Exception
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
			String passwordReTyped, String dbPath) throws Exception
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
			if(user == null)
			{
				UserDA.putUser(new User(username, password));
				user = UserDA.getUser(username);
				if(user!=null)
				{
					pageContent += "created user - "+user.toString()+"<br>";
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
