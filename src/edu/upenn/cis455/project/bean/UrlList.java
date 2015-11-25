package edu.upenn.cis455.project.bean;

import java.util.Set;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;


@Entity
public class UrlList
{
	@PrimaryKey
	private String parentUrl;
	private Set<String> urls;
	private boolean updated;
	private long lastCrawled;

	public UrlList()
	{
		super();
	}
	public UrlList(String parentUrl, Set<String> urls, boolean updated,
			long lastCrawled)
	{
		super();
		this.parentUrl = parentUrl;
		this.urls = urls;
		this.updated = updated;
		this.lastCrawled = lastCrawled;
	}

	public String getParentUrl()
	{
		return parentUrl;
	}

	public void setParentUrl(String parentUrl)
	{
		this.parentUrl = parentUrl;
	}

	public Set<String> getUrls()
	{
		return urls;
	}

	public void setUrls(Set<String> urls)
	{
		this.urls = urls;
	}

	public boolean isUpdated()
	{
		return updated;
	}

	public void setUpdated(boolean updated)
	{
		this.updated = updated;
	}

	public long getLastCrawled()
	{
		return lastCrawled;
	}

	public void setLastCrawled(long lastCrawled)
	{
		this.lastCrawled = lastCrawled;
	}

	@Override
	public String toString()
	{
		return "UrlList [parentUrl=" + parentUrl + ", urls=" + urls
				+ ", updated=" + updated + ", lastCrawled=" + lastCrawled + "]";
	}

}
