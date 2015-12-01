package edu.upenn.cis455.project.searchengine;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;

import edu.upenn.cis455.project.storage.InvertedIndex;
import edu.upenn.cis455.project.storage.Postings;
import test.edu.upenn.cis455.project.DynamoDBtest;

public class SearchEngine extends HttpServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int maxResults = 10;
	private DynamoDBtest dbAccessor = new DynamoDBtest();
	private String[] rankedresults = new String[maxResults];
	private int initialCapacity = 100;
	private Heap unigramUrls = new Heap(initialCapacity);
	
	public void init()
	{
		
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		PrintWriter out = response.getWriter();
		
		out.write(searchPage);
		response.flushBuffer();
		
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		PrintWriter out = response.getWriter();
		String searchQuery = request.getParameter("query");
		out.write("No servlet yet. Using search method!");
		response.flushBuffer();
	}
	
	public void search(String searchQuery)
	{
		String[] queryTerms = new String[20];
		Thread trigrams, bigrams, unigrams, proximity;
		if (searchQuery.isEmpty())
		{
			System.out.println("Please enter a search query!");
		}
		
		else
		{
			queryTerms = searchQuery.split(" ");
			//TODO preprocess query
			if (queryTerms.length >= 3)
			{
				trigrams = new Thread(new Trigrams(queryTerms));
				bigrams = new Thread(new Bigrams(queryTerms));
				unigrams = new Thread(new Unigrams(queryTerms));
				proximity = new Thread(new Proximity(queryTerms));
				
				trigrams.start();
				bigrams.start();
				unigrams.start();
				proximity.start();
			}
			
			else if (queryTerms.length >= 2)
			{
				bigrams = new Thread(new Bigrams(queryTerms));
				unigrams = new Thread(new Unigrams(queryTerms));
				proximity = new Thread(new Proximity(queryTerms));
				
				bigrams.start();
				unigrams.start();
				proximity.start();
			}
			
			else
			{
				Unigrams unigramsObj = new Unigrams(queryTerms);
				unigrams = new Thread(unigramsObj);
				unigrams.start();
				try
				{
					unigrams.join();
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				unigramUrls = unigramsObj.getMatchedUrls();
				rankResults();
			}
		}
	}
	
	private void rankResults()
	{
		int numResults = Math.min(maxResults, unigramUrls.size());
		if (!unigramUrls.isEmpty())
		{
			for (int i = 0; i < numResults; i++)
			{
				System.out.println(unigramUrls.remove());
			}
		}
	}
	
	private void singleWordQuery(String query)
	{
		PaginatedQueryList<InvertedIndex> resultList = dbAccessor.loadIndex(query);
		ArrayList<Postings> postings = resultList.get(0).getPostings();
		int numResults = Math.min(postings.size(), maxResults);
		System.out.println("num of matces found: " + postings.size());

		for (int i = 0; i < numResults; i++)
		{
			System.out.println(postings.get(i).getPosting());
		}
	}
	
	public static void main(String args[]) throws IOException
	{
		SearchEngine searchEngine = new SearchEngine();
		//search.singleWordQuery("walnut");
		//searchEngine.search("rule");
		//searchEngine.search("or");
		//searchEngine.search("quite");
		//searchEngine.search("being");
//		searchEngine.search("well");
//		searchEngine.search("used");
//		searchEngine.search("side");
//		searchEngine.search("choose");
//		searchEngine.search("map"); 
		searchEngine.search("log");
		
	}
	private String searchPage = "<form action=\"/search\" method=\"post\"> "
			+ "<center>Name of Search Engine</center> <br> " //TODO name of engine
			+ "<center><input type=\"text\" name=\"query\"> </center>" + "<br><br>"
			+ "<center><input type = \"submit\" value = \"submit\"></center>"  + "<br><br>"
			+ "</form>";
}

