package edu.upenn.cis455.project.storage;

import java.util.Date;

import com.sleepycat.persist.PrimaryIndex;

import edu.upenn.cis455.project.bean.Queue;
import edu.upenn.cis455.project.bean.QueueRecord;

public class QueueDA
{
	public static Queue<String> getQueue(){ //Return null if no queue exists
		
		QueueRecord record = null;
		if (DBWrapper.getStore() != null){
			PrimaryIndex<String, QueueRecord> queue = DBWrapper.getStore().getPrimaryIndex(String.class, QueueRecord.class);
			if(queue!=null){
				record = queue.get("UrlQueue");
				queue.delete("UrlQueue");
			}
		}
		
		if(record == null)
			return null;
		
		return record.getUrlQueue();
	}
	
	public static void putQueue(Queue<String> urlQueue, Date last){
		
		QueueRecord record = new QueueRecord(urlQueue, last);
		if (DBWrapper.getStore() != null)
		{
			PrimaryIndex<String, QueueRecord> userPrimaryIndex = DBWrapper
					.getStore().getPrimaryIndex(String.class,
							QueueRecord.class);
			if (userPrimaryIndex != null)
			{
				userPrimaryIndex.put(record);
			}
		}
		
	}
	
}
