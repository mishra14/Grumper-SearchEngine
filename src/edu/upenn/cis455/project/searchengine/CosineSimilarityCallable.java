package edu.upenn.cis455.project.searchengine;

import java.util.*;
import java.util.Map.*;
import java.util.concurrent.*;

import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;

import edu.upenn.cis455.project.dynamoDA.DynamoIndexerDA;
import edu.upenn.cis455.project.storage.InvertedIndex;
import edu.upenn.cis455.project.storage.Postings;

public class CosineSimilarityCallable implements Callable<PriorityQueue<Entry<String, Float>>>
{
	private int initialCapacity = 100;
	private ArrayList<String> query;
	private PriorityQueue<Entry<String, Float>> cosineSimilarity;
	private HashMap<String, Float> queryTf;
	private DynamoIndexerDA dbAccessor;
	private HashMap<String, Float> seenUrlsTfidf, seenUrlsDenominator;
	private float queryDenominator = 0;
	
	public CosineSimilarityCallable(ArrayList<String> query)
	{
		this.query = query;
		this.dbAccessor = new DynamoIndexerDA("UnigramIndex");

		setQueue();
	}
	
	private void setQueue()
	{
		cosineSimilarity = new PriorityQueue<Entry<String, Float>>(initialCapacity, 
				new Comparator<Entry<String, Float>>()
				{
					public int compare(Entry<String, Float> e1, Entry<String, Float> e2)
					{
						if (e1.getValue() > e2.getValue())
							return -1;
						else if (e1.getValue() == e2.getValue())
							return 0;
						else
							return 1;
					}
				});
	}
	
	@Override
	public PriorityQueue<Entry<String, Float>> call() throws Exception
	{
		System.out.println("in cosine sim callable");
		ArrayList<Postings> postings;
		seenUrlsTfidf = new HashMap<String, Float>();
		seenUrlsDenominator = new HashMap<String, Float>();
		
		computeQueryTf();
		
		for(String term : query)
		{
			PaginatedQueryList<InvertedIndex> resultList = dbAccessor.loadIndex(term);
			System.out.println("accessed dynamo");
			if (!resultList.isEmpty())
			{
				System.out.println("found matching urls for unigrams");
				postings = resultList.get(0).getPostings();
				computeCosineSimilarity(term, postings);
			}
			
		}
		
		//queryDenominator = (float) Math.sqrt(queryDenominator);
		
		for (String url: seenUrlsTfidf.keySet())
		{
			float tfidf = seenUrlsTfidf.get(url);
			float denominator = seenUrlsDenominator.get(url);
			System.out.println("denominator for url: " + denominator);
			System.out.println("query denominator: " + queryDenominator);
			float cosineSim = (float) (tfidf/(Math.sqrt(denominator + queryDenominator)));
			seenUrlsTfidf.put(url, cosineSim);
		}
		
		cosineSimilarity.addAll(seenUrlsTfidf.entrySet());
		
		System.out.println("cosine sim size: " + cosineSimilarity.size());
		return cosineSimilarity;
	}
	
	private void computeQueryTf()
	{
		HashMap<String, Float> tfMap = new HashMap<String, Float>();
		queryTf = new HashMap<String, Float>();
		for (String term: query)
		{
			if (tfMap.containsKey(term))
			{
				float tf = tfMap.get(term);
				tfMap.put(term, tf + 1);
			}
			
			else
			{
				tfMap.put(term, (float) 1);
			}
		}
		
		for (String term: tfMap.keySet())
		{
			queryTf.put(term, tfMap.get(term));
		}
	}
	
	private void computeCosineSimilarity(String term, ArrayList<Postings> postings)
	{
		String url;
		float postingTfidf;
		float idf = postings.get(0).getIdf();
		float queryTermTfidf = (float) (queryTf.get(term) * Math.log(idf));
		System.out.println("query tfidf: " + queryTermTfidf);
		queryDenominator += Math.pow(queryTermTfidf, 2);
		
		for (Postings posting: postings)
		{
			url = posting.getPosting();
			if (seenUrlsTfidf.containsKey(url))
			{
				float currTfidf = seenUrlsTfidf.get(url);
				postingTfidf = posting.getTfidf();
				seenUrlsTfidf.put(url, currTfidf + postingTfidf*queryTermTfidf);
				float currDenominator = seenUrlsDenominator.get(url);
				seenUrlsDenominator.put(url, currDenominator + postingTfidf*postingTfidf);
			}
			
			else
			{
				postingTfidf = posting.getTfidf();
				seenUrlsTfidf.put(url, postingTfidf*queryTermTfidf);
				seenUrlsDenominator.put(url, postingTfidf*postingTfidf);
			}
		}
	}
}
