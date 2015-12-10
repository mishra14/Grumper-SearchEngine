package edu.upenn.cis455.project.bean;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

// TODO: Auto-generated Javadoc
/**
 * Entity class to hold user data.
 *
 * @author cis455
 */
@Entity
public class User
{
	
	/** The user name. */
	@PrimaryKey
	private String userName;
	
	/** The password. */
	private String password;

	/**
	 * Instantiates a new user.
	 */
	public User()
	{

	}

	/**
	 * Instantiates a new user.
	 *
	 * @param userName the user name
	 * @param password the password
	 */
	public User(String userName, String password)
	{
		super();
		this.userName = userName;
		this.password = password;
	}

	/**
	 * Gets the user name.
	 *
	 * @return the user name
	 */
	public String getUserName()
	{
		return userName;
	}

	/**
	 * Sets the user name.
	 *
	 * @param userName the new user name
	 */
	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	/**
	 * Gets the password.
	 *
	 * @return the password
	 */
	public String getPassword()
	{
		return password;
	}

	/**
	 * Sets the password.
	 *
	 * @param password the new password
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "User [userName=" + userName + ", password=" + password + "]";
	}

}
