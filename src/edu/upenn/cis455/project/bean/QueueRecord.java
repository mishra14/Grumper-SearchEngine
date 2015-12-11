package edu.upenn.cis455.project.bean;

import java.util.Date;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

// TODO: Auto-generated Javadoc
/**
 * The Class QueueRecord.
 */
@Entity
public class QueueRecord
{
	
	/** The name. */
	@PrimaryKey
	private String name;
	
	/** The last update. */
	private Date lastUpdate;
	
	/** The url queue. */
	private Queue<String> urlQueue;
	
	/**
	 * Instantiates a new queue record.
	 */
	public QueueRecord(){
		
	}
	
	/**
	 * Instantiates a new queue record.
	 *
	 * @param urlQueue the url queue
	 * @param lastUpdate the last update
	 */
	public QueueRecord(Queue<String> urlQueue, Date lastUpdate){
		this.name = "UrlQueue";
		this.lastUpdate = lastUpdate;
		this.urlQueue = urlQueue;
	}
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Gets the last update.
	 *
	 * @return the last update
	 */
	public Date getLastUpdate()
	{
		return lastUpdate;
	}

	/**
	 * Gets the url queue.
	 *
	 * @return the url queue
	 */
	public Queue<String> getUrlQueue()
	{
		return urlQueue;
	}
	
}
