package edu.upenn.cis455.project.bean;

import java.util.Set;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

// TODO: Auto-generated Javadoc
/**
 * The Class UrlList.
 */
@Entity
public class UrlList
{
	
	/** The parent url. */
	@PrimaryKey
	private String parentUrl;
	
	/** The urls. */
	private Set<String> urls;
	
	/** The updated. */
	private boolean updated;
	
	/** The last crawled. */
	private long lastCrawled;

	/**
	 * Instantiates a new url list.
	 */
	public UrlList()
	{
		super();
	}

	/**
	 * Instantiates a new url list.
	 *
	 * @param parentUrl the parent url
	 * @param urls the urls
	 * @param updated the updated
	 * @param lastCrawled the last crawled
	 */
	public UrlList(String parentUrl, Set<String> urls, boolean updated,
			long lastCrawled)
	{
		super();
		this.parentUrl = parentUrl;
		this.urls = urls;
		this.updated = updated;
		this.lastCrawled = lastCrawled;
	}

	/**
	 * Gets the parent url.
	 *
	 * @return the parent url
	 */
	public String getParentUrl()
	{
		return parentUrl;
	}

	/**
	 * Sets the parent url.
	 *
	 * @param parentUrl the new parent url
	 */
	public void setParentUrl(String parentUrl)
	{
		this.parentUrl = parentUrl;
	}

	/**
	 * Gets the urls.
	 *
	 * @return the urls
	 */
	public Set<String> getUrls()
	{
		return urls;
	}

	/**
	 * Sets the urls.
	 *
	 * @param urls the new urls
	 */
	public void setUrls(Set<String> urls)
	{
		this.urls = urls;
	}

	/**
	 * Checks if is updated.
	 *
	 * @return true, if is updated
	 */
	public boolean isUpdated()
	{
		return updated;
	}

	/**
	 * Sets the updated.
	 *
	 * @param updated the new updated
	 */
	public void setUpdated(boolean updated)
	{
		this.updated = updated;
	}

	/**
	 * Gets the last crawled.
	 *
	 * @return the last crawled
	 */
	public long getLastCrawled()
	{
		return lastCrawled;
	}

	/**
	 * Sets the last crawled.
	 *
	 * @param lastCrawled the new last crawled
	 */
	public void setLastCrawled(long lastCrawled)
	{
		this.lastCrawled = lastCrawled;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "UrlList [parentUrl=" + parentUrl + ", urls=" + urls
				+ ", updated=" + updated + ", lastCrawled=" + lastCrawled + "]";
	}

}
