package edu.upenn.cis455.project.bean;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import edu.upenn.cis455.project.crawler.info.URLInfo;

// TODO: Auto-generated Javadoc
/**
 * The Class UrlRecord.
 */
@Entity
public class UrlRecord
{
	
	/** The url. */
	@PrimaryKey
	String url;
	
	/** The domain. */
	String domain;
	
	/**
	 * Instantiates a new url record.
	 */
	public UrlRecord(){
		
	}
	
	/**
	 * Instantiates a new url record.
	 *
	 * @param url the url
	 */
	public UrlRecord(String url){
		this.url = url;
		URLInfo info = new URLInfo(url);
		this.domain = info.getHostName();
	}
	
	/**
	 * Gets the url.
	 *
	 * @return the url
	 */
	public String getUrl()
	{
		return url;
	}

	/**
	 * Gets the domain.
	 *
	 * @return the domain
	 */
	public String getDomain()
	{
		return domain;
	}
}
