package edu.upenn.cis455.project.searchengine;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;

import edu.upenn.cis455.project.storage.InvertedIndex;
import edu.upenn.cis455.project.storage.Postings;
import test.edu.upenn.cis455.project.DynamoIndexerDA;

public class GetScoresCallable implements Callable<Heap>
{
	private int initialCapacity = 100;
	private Heap matchedUrls ;
	private String[] query = new String[20];
	private DynamoIndexerDA dbAccessor;
	
	public GetScoresCallable(String tablename, String[] query)
	{
		this.query = query;
		this.dbAccessor = new DynamoIndexerDA(tablename);
	}
	
	@Override
	public Heap call() throws Exception
	{
		matchedUrls = new Heap(initialCapacity);
		for (int i = 0; i < query.length; i++)
		{
			String term = query[i];
			PaginatedQueryList<InvertedIndex> resultList = dbAccessor.loadIndex(term);
			ArrayList<Postings> postings = resultList.get(0).getPostings();
			matchedUrls.addAll(postings);
		}
		return matchedUrls;
	}

}
