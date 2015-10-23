package edu.upenn.cis455.storage;

import com.sleepycat.persist.PrimaryIndex;

import edu.upenn.cis455.xpath.XPath;

public class XPathDA
{
	public static XPath getXPath(String xPathString) // returns null if the
														// entry wasn't found
	{
		XPath xPath = null;
		if (DBWrapper.getStore() != null)
		{
			PrimaryIndex<String, XPath> xPathPrimaryIndex = DBWrapper
					.getStore().getPrimaryIndex(String.class, XPath.class);
			if (xPathPrimaryIndex != null)
			{
				xPath = xPathPrimaryIndex.get(xPathString);
			}
		}
		return xPath;
	}

	public static XPath putXPath(XPath xPath) // returns null is the user did
												// not exist in the DB
	{
		XPath insertedXPath = null;
		if (DBWrapper.getStore() != null)
		{
			PrimaryIndex<String, XPath> xPathPrimaryIndex = DBWrapper
					.getStore().getPrimaryIndex(String.class, XPath.class);
			if (xPathPrimaryIndex != null)
			{
				insertedXPath = xPathPrimaryIndex.put(xPath);
			}
		}
		return insertedXPath;
	}

	public static boolean removeXPath(String xPathString)
	{
		if (DBWrapper.getStore() != null)
		{
			PrimaryIndex<String, XPath> xPathPrimaryIndex = DBWrapper
					.getStore().getPrimaryIndex(String.class, XPath.class);
			if (xPathPrimaryIndex != null)
			{
				return xPathPrimaryIndex.delete(xPathString);
			}
		}
		return false;
	}

}
