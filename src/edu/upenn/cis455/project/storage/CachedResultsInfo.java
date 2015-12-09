package edu.upenn.cis455.project.storage;

import java.util.Date;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class CachedResultsInfo
{
	@PrimaryKey
	private String query;
	
	private String results;
	private Date lastAccessed;
	
	public CachedResultsInfo()
	{
		
	}
	
	public CachedResultsInfo(String query, String results, Date lastAccessed)
	{
		this.query = query;
		this.results = results;
		this.lastAccessed = lastAccessed;
	}
	
	public String getUrl()
	{
		return query;
	}
	
	public void setUrl(String query)
	{
		this.query = query;
	}

	public String getResults()
	{
		return results;
	}

	public void setResults(String results)
	{
		this.results = results;
	}

	public Date getLastAccessed()
	{
		return lastAccessed;
	}

	public void setLastAccessed(Date lastAccessed)
	{
		this.lastAccessed = lastAccessed;
	}
	
	
}
