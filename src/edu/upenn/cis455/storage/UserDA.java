package edu.upenn.cis455.storage;

import com.sleepycat.persist.PrimaryIndex;

public class UserDA 
{

	public static User getUser(String userName)  //returns null if the entry wasn't found
	{
		User user = null;
		if(DBWrapper.getStore()!=null)
		{
			PrimaryIndex<String, User> userPrimaryIndex = DBWrapper.getStore().getPrimaryIndex(String.class, User.class);
			if(userPrimaryIndex!=null)
			{
				user = userPrimaryIndex.get(userName);
			}
		}
		return user;
	}
	
	public static User putUser(User user)	//returns null is the user did not exist in the DB
	{
		User insertedUser = null;
		if(DBWrapper.getStore()!=null)
		{
			PrimaryIndex<String, User> userPrimaryIndex = DBWrapper.getStore().getPrimaryIndex(String.class, User.class);
			if(userPrimaryIndex!=null)
			{
				insertedUser =  userPrimaryIndex.put(user);
			}
		}
		return insertedUser;
	}
}
