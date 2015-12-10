package edu.upenn.cis455.project.crawler;

import java.util.ArrayList;
import java.util.TimerTask;

import edu.upenn.cis455.project.bean.DocumentRecord;
import edu.upenn.cis455.project.storage.S3DocumentDA;

// TODO: Auto-generated Javadoc
/**
 * The Class PushToDB.
 */
public class PushToDB extends TimerTask
{
	
	/** The num workers. */
	private int numWorkers;
	
	/** The crawled docs. */
	private ArrayList<DocumentRecord> crawledDocs;

	/**
	 * Instantiates a new push to db.
	 *
	 * @param numWorkers the num workers
	 * @param crawledDocs the crawled docs
	 */
	public PushToDB(int numWorkers, ArrayList<DocumentRecord> crawledDocs)
	{
		this.numWorkers = numWorkers;
		this.crawledDocs = crawledDocs;
	}

	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	@Override
	public void run()
	{
		System.out.println("Pushing to db");
		S3DocumentDA s3 = new S3DocumentDA();
		synchronized (crawledDocs)
		{
			for (DocumentRecord doc : crawledDocs)
			{
				s3.putDocument(doc);
			}

			crawledDocs.clear();
		}

		System.out.println("Added to db");

	}

}
