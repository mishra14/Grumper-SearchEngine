package edu.upenn.cis455.project.bean;

import java.util.Date;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class QueueRecord
{
	@PrimaryKey
	private String name;
	private Date lastUpdate;
	private Queue<String> urlQueue;
	
	public QueueRecord(){
		
	}
	
	public QueueRecord(Queue<String> urlQueue, Date lastUpdate){
		this.name = "UrlQueue";
		this.lastUpdate = lastUpdate;
		this.urlQueue = urlQueue;
	}
	
	public String getName()
	{
		return name;
	}

	public Date getLastUpdate()
	{
		return lastUpdate;
	}

	public Queue<String> getUrlQueue()
	{
		return urlQueue;
	}
	
}
