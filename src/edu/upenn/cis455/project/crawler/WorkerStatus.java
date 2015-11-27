package edu.upenn.cis455.project.crawler;

import java.math.BigInteger;
import java.util.Date;

/**
 * This is a bean class to hold all the information about a worker. This class
 * is used by the master.
 * 
 * @author cis455
 *
 */
public class WorkerStatus
{
	public static enum statusType
	{
		crawling, // actively crawling
		waiting, // no urls left to crawl
		idle // started but crawler threads not spawned
	};

	private String port;

	private String lastCrawledUrl;

	private String pagesCrawled;
	
	private Long numPagesCrawled;

	private statusType status;

	private long timestamp;
	
	private BigInteger one;

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

	public String getPort()
	{
		return port;
	}

	public void setPort(String port)
	{
		this.port = port;
	}

	public String getLastCrawledUrl()
	{
		return lastCrawledUrl;
	}

	public void setLastCrawledUrl(String lastCrawledUrl)
	{
		this.lastCrawledUrl = lastCrawledUrl;
	}

	public String getPagesCrawled()
	{
		return pagesCrawled;
	}

	public void setPagesCrawled(String pagesCrawled)
	{
		this.pagesCrawled = pagesCrawled;
	}

	public statusType getStatus()
	{
		return status;
	}

	public void setStatus(statusType status)
	{
		this.status = status;
	}

	public long getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(long timestamp)
	{
		this.timestamp = timestamp;
	}
	
	public void incrementCount(){
		this.numPagesCrawled++;
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!INCREMENTING by 1: "+numPagesCrawled.toString());
		setPagesCrawled(numPagesCrawled.toString());
	}
	
	@Override
	public String toString()
	{
		return "WorkerStatus [port=" + port + ", lastCrawledUrl="
				+ lastCrawledUrl + ", pagesCrawled=" + pagesCrawled
				+ ", status=" + status + ", timestamp=" + timestamp + "]";
	}
}
