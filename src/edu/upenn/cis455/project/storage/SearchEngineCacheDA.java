package edu.upenn.cis455.project.storage;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

public class SearchEngineCacheDA
{
	
	
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
