package edu.upenn.cis455.bean;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * Entity class to hold user data
 * 
 * @author cis455
 *
 */
@Entity
public class User
{
	@PrimaryKey
	private String userName;
	private String password;

	public User()
	{

	}

	public User(String userName, String password)
	{
		super();
		this.userName = userName;
		this.password = password;
	}

	public String getUserName()
	{
		return userName;
	}

	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	@Override
	public String toString()
	{
		return "User [userName=" + userName + ", password=" + password + "]";
	}

}
