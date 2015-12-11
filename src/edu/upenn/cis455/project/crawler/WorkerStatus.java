package edu.upenn.cis455.project.crawler;

import java.math.BigInteger;
import java.util.Date;

// TODO: Auto-generated Javadoc
/**
 * This is a bean class to hold all the information about a worker. This class
 * is used by the master.
 * 
 * @author cis455
 *
 */
public class WorkerStatus
{
	
	/**
	 * The Enum statusType.
	 */
	public static enum statusType
	{
		
		/** The crawling. */
		crawling, 
 /** The waiting. */
 // actively crawling
		waiting, 
 /** The idle. */
 // no urls left to crawl
		idle // started but crawler threads not spawned
	};

	/** The port. */
	private String port;

	/** The last crawled url. */
	private String lastCrawledUrl;

	/** The pages crawled. */
	private String pagesCrawled;
	
	/** The num pages crawled. */
	private Long numPagesCrawled;

	/** The status. */
	private statusType status;

	/** The timestamp. */
	private long timestamp;
	
	/** The one. */
	private BigInteger one;

	/**
	 * Instantiates a new worker status.
	 *
	 * @param port the port
	 * @param lastCrawledUrl the last crawled url
	 * @param pagesCrawled the pages crawled
	 * @param status the status
	 */
	public WorkerStatus(String port, String lastCrawledUrl,
			String pagesCrawled, statusType status)
	{
		super();
		this.port = port;
		this.lastCrawledUrl = lastCrawledUrl;
		this.pagesCrawled = pagesCrawled;
		this.status = status;
		this.timestamp = (new Date()).getTime();
		this.numPagesCrawled = new Long("0");
		one = new BigInteger("1");
	}

	/**
	 * Gets the port.
	 *
	 * @return the port
	 */
	public String getPort()
	{
		return port;
	}

	/**
	 * Sets the port.
	 *
	 * @param port the new port
	 */
	public void setPort(String port)
	{
		this.port = port;
	}

	/**
	 * Gets the last crawled url.
	 *
	 * @return the last crawled url
	 */
	public String getLastCrawledUrl()
	{
		return lastCrawledUrl;
	}

	/**
	 * Sets the last crawled url.
	 *
	 * @param lastCrawledUrl the new last crawled url
	 */
	public void setLastCrawledUrl(String lastCrawledUrl)
	{
		this.lastCrawledUrl = lastCrawledUrl;
	}

	/**
	 * Gets the pages crawled.
	 *
	 * @return the pages crawled
	 */
	public String getPagesCrawled()
	{
		return pagesCrawled;
	}

	/**
	 * Sets the pages crawled.
	 *
	 * @param pagesCrawled the new pages crawled
	 */
	public void setPagesCrawled(String pagesCrawled)
	{
		this.pagesCrawled = pagesCrawled;
	}

	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	public statusType getStatus()
	{
		return status;
	}

	/**
	 * Sets the status.
	 *
	 * @param status the new status
	 */
	public void setStatus(statusType status)
	{
		this.status = status;
	}

	/**
	 * Gets the timestamp.
	 *
	 * @return the timestamp
	 */
	public long getTimestamp()
	{
		return timestamp;
	}

	/**
	 * Sets the timestamp.
	 *
	 * @param timestamp the new timestamp
	 */
	public void setTimestamp(long timestamp)
	{
		this.timestamp = timestamp;
	}
	
	/**
	 * Increment count.
	 */
	public void incrementCount(){
		this.numPagesCrawled++;
//		System.out.println("!!!!!!!!!!!!!!!!!!!!!!INCREMENTING by 1: "+numPagesCrawled.toString());
		setPagesCrawled(numPagesCrawled.toString());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "WorkerStatus [port=" + port + ", lastCrawledUrl="
				+ lastCrawledUrl + ", pagesCrawled=" + pagesCrawled
				+ ", status=" + status + ", timestamp=" + timestamp + "]";
	}
}
