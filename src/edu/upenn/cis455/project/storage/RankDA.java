package edu.upenn.cis455.project.storage;

import com.sleepycat.persist.PrimaryIndex;

import edu.upenn.cis455.project.bean.Rank;

/**
 * Data Accessor class for the document entity class
 * 
 * @author cis455
 *
 */
public class RankDA
{

	public static Rank getRank(String url)
	{
		Rank rank = null;
		if (DBWrapper.getStore() != null)
		{
			PrimaryIndex<String, Rank> userPrimaryIndex = DBWrapper.getStore()
					.getPrimaryIndex(String.class, Rank.class);
			if (userPrimaryIndex != null)
			{
				rank = userPrimaryIndex.get(url);
			}
		}
		return rank;
	}

	public static Rank putRank(Rank rank)
	{
		Rank inertedRank = null;
		if (DBWrapper.getStore() != null)
		{
			PrimaryIndex<String, Rank> userPrimaryIndex = DBWrapper.getStore()
					.getPrimaryIndex(String.class, Rank.class);
			if (userPrimaryIndex != null)
			{
				inertedRank = userPrimaryIndex.put(rank);
			}
		}
		return inertedRank;
	}

	public static boolean removeDocument(String url)
	{
		if (DBWrapper.getStore() != null)
		{
			PrimaryIndex<String, Rank> userPrimaryIndex = DBWrapper.getStore()
					.getPrimaryIndex(String.class, Rank.class);
			if (userPrimaryIndex != null)
			{
				return userPrimaryIndex.delete(url);
			}
		}
		return false;
	}

	public static long getSize()
	{
		long result = -1;
		if (DBWrapper.getStore() != null)
		{
			PrimaryIndex<String, Rank> userPrimaryIndex = DBWrapper.getStore()
					.getPrimaryIndex(String.class, Rank.class);
			if (userPrimaryIndex != null)
			{
				result = userPrimaryIndex.count();
			}
		}
		return result;
	}
}
