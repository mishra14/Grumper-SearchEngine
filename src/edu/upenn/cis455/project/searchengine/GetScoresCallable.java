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
	private String tablename;
	
	public GetScoresCallable(String tablename, String[] query)
	{
		this.tablename = tablename;
		this.query = query;
		this.dbAccessor = new DynamoIndexerDA(tablename);
	}
	
	@Override
	public Heap call() throws Exception
	{
		System.out.println("in callable");
		matchedUrls = new Heap(initialCapacity);
		
		if (tablename.equals("UnigramIndex"))
		{
			getUnigramScores();
		}
		
		else if (tablename.equals("BigramIndex"))
		{
			getBigramScores();
		}
		
		else if (tablename.equals("TrigramIndex"))
		{
			getTrigramScores();
		}
		
		else
		{
			getProximity();
		}
		System.out.println("size of matched urls: " + matchedUrls.size());
		return matchedUrls;
	}
	
	private void getUnigramScores()
	{
		for (int i = 0; i < query.length; i++)
		{
			String term = query[i];
			PaginatedQueryList<InvertedIndex> resultList = dbAccessor.loadIndex(term);
			System.out.println("result list");
			ArrayList<Postings> postings = resultList.get(0).getPostings();
			matchedUrls.addAll(postings);
		}
	}
	
	private void getBigramScores()
	{
		for (int i = 0; i < query.length - 1; i++)
		{
			StringBuffer term = new StringBuffer();
			term.append(query[i] + " ");
			term.append(query[i+1]);
			PaginatedQueryList<InvertedIndex> resultList = dbAccessor.loadIndex(term.toString());
			ArrayList<Postings> postings = resultList.get(0).getPostings();
			matchedUrls.addAll(postings);
		}
	}
	
	private void getTrigramScores()
	{
		for (int i = 0; i < query.length - 2; i++)
		{
			StringBuffer term = new StringBuffer();
			term.append(query[i] + " ");
			term.append(query[i+1] + " ");
			term.append(query[i+2]);
			PaginatedQueryList<InvertedIndex> resultList = dbAccessor.loadIndex(term.toString());
			ArrayList<Postings> postings = resultList.get(0).getPostings();
			matchedUrls.addAll(postings);
		}
	}
	
	private void getProximity()
	{
		
	}

}
