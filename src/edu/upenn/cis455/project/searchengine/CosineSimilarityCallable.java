package edu.upenn.cis455.project.searchengine;

import java.util.*;
import java.util.concurrent.*;

import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;

import edu.upenn.cis455.project.dynamoDA.DynamoIndexerDA;
import edu.upenn.cis455.project.storage.InvertedIndex;
import edu.upenn.cis455.project.storage.Postings;

public class CosineSimilarityCallable implements Callable<HashMap<String, Float>>
{
	private ArrayList<String> query;
	private HashMap<String, Float> cosineSimilarity, queryTf, seenUrlsDenominator;
	private DynamoIndexerDA dbAccessor;
	private float queryDenominator = 0;
	
	public CosineSimilarityCallable(ArrayList<String> query, String tablename)
	{
		this.query = new ArrayList<String>();
		setQueryNgrams(query, tablename);
		this.dbAccessor = new DynamoIndexerDA(tablename);

	}	
	
	@Override
	public HashMap<String, Float> call() throws Exception
	{
		System.out.println("in cosine sim callable");
		ArrayList<Postings> postings;
		cosineSimilarity = new HashMap<String, Float>();
		seenUrlsDenominator = new HashMap<String, Float>();
		
		if (!query.isEmpty())
		{
			computeQueryTf();
			
			for(String term : query)
			{
				System.out.println("finding matches for " + term + "...");
				PaginatedQueryList<InvertedIndex> resultList = dbAccessor.loadIndex(term);
				System.out.println("accessed dynamo, result list size: " + resultList.size());
				
				if (!resultList.isEmpty())
				{
					postings = new ArrayList<Postings>();
					System.out.println("found matching urls for ngram: " + term);
					for (InvertedIndex result: resultList)
					{
						ArrayList<Postings> currPosting = result.getPostings();
						//System.out.println("Postings: " + currPosting);
						if (currPosting != null)
							postings.add(currPosting.get(0));
						if (postings.size() == 100)
						{
							computeCosineSimilarity(term, postings);
							postings = new ArrayList<Postings>();
						}
					}
					
					if (!postings.isEmpty())
					{
						computeCosineSimilarity(term, postings);
						postings = new ArrayList<Postings>();
					}
				}	
			}
			
			//queryDenominator = (float) Math.sqrt(queryDenominator);
			
			for (String url: cosineSimilarity.keySet())
			{
				float tfidf = cosineSimilarity.get(url);
				//System.out.println("tfidf: " + tfidf);
				float denominator = seenUrlsDenominator.get(url);
				float cosineSim = (float) (tfidf/(Math.sqrt(denominator + queryDenominator)));
				//System.out.println("cosine sim: " + cosineSim);
				cosineSimilarity.put(url, cosineSim);
			}
		}
			
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
		float queryTermTfidf = (float) (queryTf.get(term) * idf);
		queryDenominator += Math.pow(queryTermTfidf, 2);
		
		for (Postings posting: postings)
		{
			url = posting.getPosting();
			if (cosineSimilarity.containsKey(url))
			{
				float currTfidf = cosineSimilarity.get(url);
				postingTfidf = posting.getTfidf();
				cosineSimilarity.put(url, currTfidf + postingTfidf*queryTermTfidf);
				float currDenominator = seenUrlsDenominator.get(url);
				seenUrlsDenominator.put(url, currDenominator + postingTfidf*postingTfidf);
			}
			
			else
			{
				postingTfidf = posting.getTfidf();
				cosineSimilarity.put(url, postingTfidf*queryTermTfidf);
				seenUrlsDenominator.put(url, postingTfidf*postingTfidf);
			}
		}
	}
	
	private void setQueryNgrams(ArrayList<String> queryTerms, String tablename)
	{	
		if (tablename.equals("BigramIndex") && queryTerms.size() >= 2)
		{
			System.out.println("Finding bigrams");
			for (int i = 0; i < queryTerms.size() - 1; i++)
			{
				StringBuffer term = new StringBuffer();
				term.append(queryTerms.get(i) + " ");
				term.append(queryTerms.get(i + 1));
				System.out.println("bigram: " + term);
				query.add(term.toString());
			}
		}
		
		else if (tablename.equals("TrigramIndex") && queryTerms.size() >= 3)
		{
			System.out.println("Finding trigrams");
			for (int i = 0; i < queryTerms.size() - 2; i++)
			{
				StringBuffer term = new StringBuffer();
				term.append(queryTerms.get(i) + " ");
				term.append(queryTerms.get(i + 1) + " ");
				term.append(queryTerms.get(i + 2));
				query.add(term.toString());
			}
		}
		
		else if (tablename.equals("UnigramIndex"))
		{
			System.out.println("Finding unigrams");
			this.query = queryTerms;
		}
	}
}
