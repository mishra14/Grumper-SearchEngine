package edu.upenn.cis455.project.storage;

import com.sleepycat.persist.PrimaryIndex;

import edu.upenn.cis455.project.bean.Rank;

// TODO: Auto-generated Javadoc
/**
 * Data Accessor class for the document entity class.
 *
 * @author cis455
 */
public class RankDA
{

	/**
	 * Gets the rank.
	 *
	 * @param url the url
	 * @return the rank
	 */
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

	/**
	 * Put rank.
	 *
	 * @param rank the rank
	 * @return the rank
	 */
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

	/**
	 * Removes the document.
	 *
	 * @param url the url
	 * @return true, if successful
	 */
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

	/**
	 * Gets the size.
	 *
	 * @return the size
	 */
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
