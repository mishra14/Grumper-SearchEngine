package edu.upenn.cis455.project.searchengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;

import edu.upenn.cis455.project.storage.InvertedIndex;
import edu.upenn.cis455.project.storage.Postings;
import test.edu.upenn.cis455.project.DynamoIndexerDA;

public class GetScoresCallable implements Callable<Heap>
{
	private int initialCapacity = 100;
	private Heap matchedUrls;
	private DynamoIndexerDA dbAccessor;
	private String tablename;
	private ArrayList<String> query;
	
	public GetScoresCallable(String tablename, ArrayList<String> query)
	{
		this.tablename = tablename;
		this.query = query;
		this.dbAccessor = new DynamoIndexerDA(tablename);
	}

	@Override
	public Heap call() throws Exception
	{
		
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
			//getProximity();
		}
		//System.out.println("size of matched urls: " + matchedUrls.size());
		return matchedUrls;
	}
	
	private void getUnigramScores()
	{
		for (String term: query)
		{
			ArrayList<Postings> postings = new ArrayList<Postings>();
			PaginatedQueryList<InvertedIndex> resultList = dbAccessor.loadIndex(term);
			if (!resultList.isEmpty())
			{
				System.out.println("found matching urls for unigrams");
				postings = resultList.get(0).getPostings();
				computeUrlScores(postings);
			}
		}
	}
	
	private void getBigramScores()
	{
		for (int i = 0; i < query.size() - 1; i++)
		{
			ArrayList<Postings> postings = new ArrayList<Postings>();
			StringBuffer term = new StringBuffer();
			term.append(query.get(i) + " ");
			term.append(query.get(i + 1));
			PaginatedQueryList<InvertedIndex> resultList = dbAccessor.loadIndex(term.toString());
			if (!resultList.isEmpty())
			{
				postings = resultList.get(0).getPostings();
				computeUrlScores(postings);
			}
		}
	}
	
	private void getTrigramScores()
	{
		for (int i = 0; i < query.size() - 2; i++)
		{
			ArrayList<Postings> postings = new ArrayList<Postings>();
			StringBuffer term = new StringBuffer();
			term.append(query.get(i) + " ");
			term.append(query.get(i + 1) + " ");
			term.append(query.get(i + 2));
			PaginatedQueryList<InvertedIndex> resultList = dbAccessor.loadIndex(term.toString());
			if (!resultList.isEmpty())
			{
				postings = resultList.get(0).getPostings();
				computeUrlScores(postings);
			}
		}
	}
	
	private void computeUrlScores(ArrayList<Postings> postings)
	{
		HashMap<String, UrlScores> scores = new HashMap<String, UrlScores>();
		for (Postings posting : postings)
		{
			String url = posting.getPosting();
			System.out.println("found url: " + url);
			Float tfidf = posting.getTfidf();
			if (scores.containsKey(url))
			{
				UrlScores scoresObj = scores.get(url);
				scoresObj.setCount(scoresObj.getCount() + 1);
				scoresObj.setTfidf(scoresObj.getTfidf() + tfidf);
			}
			
			else
			{
				UrlScores scoresObj = new UrlScores(url, tfidf, 1);
				scores.put(url, scoresObj);
			}
		}
		
		matchedUrls.addAll(scores.values());
		System.out.println("added: " + matchedUrls.size());
	}
}
