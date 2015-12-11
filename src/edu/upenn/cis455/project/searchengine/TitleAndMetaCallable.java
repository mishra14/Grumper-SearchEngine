package edu.upenn.cis455.project.searchengine;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.Callable;

import edu.upenn.cis455.project.dynamoDA.DynamoIndexerDA;
import edu.upenn.cis455.project.storage.Postings;

// TODO: Auto-generated Javadoc
/**
 * The Class TitleAndMetaCallable.
 */
public class TitleAndMetaCallable implements Callable<Heap>
{

	/** The query. */
	private ArrayList<String> query;
	
	/** The title and meta scores temp. */
	private Heap titleAndMetaScores, titleAndMetaScoresTemp;
	
	/** The db accessor. */
	private DynamoIndexerDA dbAccessor;
	
	/** The url word matches. */
	private HashMap<String, Integer> urlWordMatches; 
	
	/**
	 * Instantiates a new title and meta callable.
	 *
	 * @param query the query
	 */
	public TitleAndMetaCallable(ArrayList<String> query)
	{
		this.query = query;
		this.dbAccessor = new DynamoIndexerDA("Metadata");

	}
	
	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Heap call() throws Exception
	{
		System.out.println("in meta data");
		titleAndMetaScores = new Heap(100);
		titleAndMetaScoresTemp = new Heap(100);
		urlWordMatches = new HashMap<>();
		int numQueryTerms = query.size();
		
		for (String term : query)
		{
			List<Postings> postings = dbAccessor.loadIndex(term);
			if (postings == null)
			{
				System.out.println("No meta and title matches found for : " + term);
				continue;
			}
			
			if (postings.size() > 100)
				postings = postings.subList(0, 100);
			System.out.println("found title and meta matches for :" + term + " size: " + postings.size());
			computeUrlWordMatches(postings);
		}
		
		while (!titleAndMetaScoresTemp.isEmpty())
		{
			SimpleEntry<String, Float> tempEntry = titleAndMetaScoresTemp.remove();
			if (urlWordMatches.get(tempEntry.getKey()) == numQueryTerms)
				titleAndMetaScores.add(tempEntry);
		}
		return titleAndMetaScores;
	}
	
	/**
	 * Compute url word matches.
	 *
	 * @param postings the postings
	 */
	private void computeUrlWordMatches(List<Postings> postings)
	{		
		for (Postings posting: postings)
		{
			String url = posting.getPosting();
			Float score = posting.getTfidf();
			titleAndMetaScoresTemp.add(url, score);
			
			if (urlWordMatches.containsKey(url))
			{
				int count = urlWordMatches.get(url);
				urlWordMatches.put(url, count + 1);
			}
			
			else
			{
				urlWordMatches.put(url, 1);
			}
		}
	}
}
