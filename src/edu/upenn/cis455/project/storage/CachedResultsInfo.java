package edu.upenn.cis455.project.storage;

import java.util.Date;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

// TODO: Auto-generated Javadoc
/**
 * The Class CachedResultsInfo.
 */
@Entity
public class CachedResultsInfo
{
	
	/** The query. */
	@PrimaryKey
	private String query;
	
	/** The results. */
	private String results;
	
	/** The last accessed. */
	private Date lastAccessed;
	
	/**
	 * Instantiates a new cached results info.
	 */
	public CachedResultsInfo()
	{
		
	}
	
	/**
	 * Instantiates a new cached results info.
	 *
	 * @param query the query
	 * @param results the results
	 * @param lastAccessed the last accessed
	 */
	public CachedResultsInfo(String query, String results, Date lastAccessed)
	{
		this.query = query;
		this.results = results;
		this.lastAccessed = lastAccessed;
	}
	
	/**
	 * Gets the url.
	 *
	 * @return the url
	 */
	public String getUrl()
	{
		return query;
	}
	
	/**
	 * Sets the url.
	 *
	 * @param query the new url
	 */
	public void setUrl(String query)
	{
		this.query = query;
	}

	/**
	 * Gets the results.
	 *
	 * @return the results
	 */
	public String getResults()
	{
		return results;
	}

	/**
	 * Sets the results.
	 *
	 * @param results the new results
	 */
	public void setResults(String results)
	{
		this.results = results;
	}

	/**
	 * Gets the last accessed.
	 *
	 * @return the last accessed
	 */
	public Date getLastAccessed()
	{
		return lastAccessed;
	}

	/**
	 * Sets the last accessed.
	 *
	 * @param lastAccessed the new last accessed
	 */
	public void setLastAccessed(Date lastAccessed)
	{
		this.lastAccessed = lastAccessed;
	}
	
	
}
