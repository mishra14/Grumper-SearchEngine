package edu.upenn.cis455.project.crawler;

import java.util.ArrayList;
import java.util.Date;
import java.util.TimerTask;

import edu.upenn.cis455.project.bean.DocumentRecord;
import edu.upenn.cis455.project.bean.Queue;
import edu.upenn.cis455.project.storage.DBWrapper;
import edu.upenn.cis455.project.storage.QueueDA;
import edu.upenn.cis455.project.storage.S3DocumentDA;

public class PushToDB extends TimerTask
{
	private int numWorkers;
	private ArrayList<DocumentRecord> crawledDocs;
	private Queue<String> urlQueue;
	
	public PushToDB(int numWorkers, ArrayList<DocumentRecord> crawledDocs, Queue<String> urlQueue){
		this.numWorkers = numWorkers;
		this.crawledDocs = crawledDocs;
		this.urlQueue = urlQueue;
	}
	
	@Override
	public void run()
	{
		System.out.println("Pushing to db");
		S3DocumentDA s3 = new S3DocumentDA();
		synchronized(crawledDocs){
			for(DocumentRecord doc : crawledDocs){
				s3.putDocument(doc);
			}
			
			crawledDocs.clear();
		}
		
		System.out.println("Added to db");
		
		//Put current state of urlQueue onto berkeley db
		QueueDA.putQueue(urlQueue, new Date());
		
		
	}
	
}
