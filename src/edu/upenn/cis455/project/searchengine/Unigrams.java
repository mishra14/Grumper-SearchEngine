package edu.upenn.cis455.project.searchengine;

import java.util.*;

import test.edu.upenn.cis455.project.DynamoDBtest;

import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;

import edu.upenn.cis455.project.storage.InvertedIndex;
import edu.upenn.cis455.project.storage.Postings;

public class Unigrams implements Runnable
{
	private int initialCapacity = 100;
	private Heap matchedUrls ;
	private String[] query = new String[20];
//	private HashMap<String, ArrayList<Postings>> unigramScores 
//						= new HashMap<String,ArrayList<Postings>>();
	private DynamoDBtest dbAccessor = new DynamoDBtest();

	public Unigrams(String[] query)
	{
		this.query = query;
		//createPriorityQueue();
	}
	
	@Override
	public void run()
	{
		matchedUrls = new Heap(initialCapacity);
		for (int i = 0; i < query.length; i++)
		{
			String term = query[i];
			PaginatedQueryList<InvertedIndex> resultList = dbAccessor.loadIndex(term);
			ArrayList<Postings> postings = resultList.get(0).getPostings();
			matchedUrls.addAll(postings);
		}
	}
	
	public Heap getMatchedUrls()
	{
		return matchedUrls;
	}
	
}
