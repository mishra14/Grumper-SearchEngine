package edu.upenn.cis455.project.storage;

import com.sleepycat.persist.PrimaryIndex;

import edu.upenn.cis455.project.bean.RobotsInfo;

// TODO: Auto-generated Javadoc
/**
 * The Class RobotsInfoDA.
 */
public class RobotsInfoDA
{

	/**
	 * Gets the info.
	 *
	 * @param domain the domain
	 * @return the info
	 */
	public static RobotsInfo getInfo(String domain)
	{

		RobotsInfo info = null;
		if (DBWrapper.getStore() != null)
		{
			PrimaryIndex<String, RobotsInfo> userPrimaryIndex = DBWrapper
					.getStore().getPrimaryIndex(String.class, RobotsInfo.class);
			if (userPrimaryIndex != null)
			{
				info = userPrimaryIndex.get(domain);
			}
		}

		return info;
	}

	/**
	 * Put info.
	 *
	 * @param info the info
	 */
	public static void putInfo(RobotsInfo info)
	{
		if (DBWrapper.getStore() != null)
		{
			PrimaryIndex<String, RobotsInfo> userPrimaryIndex = DBWrapper
					.getStore().getPrimaryIndex(String.class, RobotsInfo.class);
			if (userPrimaryIndex != null)
			{
				userPrimaryIndex.put(info);
			}
		}
	}

	/**
	 * Contains.
	 *
	 * @param domain the domain
	 * @return true, if successful
	 */
	public static boolean contains(String domain)
	{
		if (getInfo(domain) != null)
			return true;

		return false;
	}

}
