package edu.upenn.cis455.servlet;

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

	private static final String userHome = "<h2> Welcome to your Home page,  </h2>";

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

	public static String getUserHome(String username)
	{
		return userHome + username + "<br>";
	}
}
