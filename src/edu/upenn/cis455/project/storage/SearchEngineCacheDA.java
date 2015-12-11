package edu.upenn.cis455.project.storage;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

// TODO: Auto-generated Javadoc
/**
 * The Class SearchEngineCacheDA.
 */
public class SearchEngineCacheDA
{
	
	
	/**
	 * Put cached results info.
	 *
	 * @param cacheInfo the cache info
	 */
	public void putCachedResultsInfo(CachedResultsInfo cacheInfo)
	{
		if (DBWrapper.getStore() != null)
		{
			PrimaryIndex<String, CachedResultsInfo> resultsByQuery = 
						DBWrapper.getStore().getPrimaryIndex(String.class, CachedResultsInfo.class);
			if (resultsByQuery != null)
			{
				resultsByQuery.put(cacheInfo);
			}
		}
	}
	
	/**
	 * Gets the cached results info.
	 *
	 * @param query the query
	 * @return the cached results info
	 */
	public CachedResultsInfo getCachedResultsInfo(String query)
	{
		CachedResultsInfo cacheInfo = null;
		if (DBWrapper.getStore() != null)
		{
			PrimaryIndex<String, CachedResultsInfo> resultsByQuery = 
						DBWrapper.getStore().getPrimaryIndex(String.class, CachedResultsInfo.class);
			if (resultsByQuery != null)
			{	
				cacheInfo = resultsByQuery.get(query);
			}
		}
		return cacheInfo;
	}
}
