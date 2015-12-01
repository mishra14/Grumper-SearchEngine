package edu.upenn.cis455.project.searchengine;

import java.io.*;
import java.util.concurrent.*;
import java.util.*;

import javax.servlet.http.*;

import edu.upenn.cis455.project.storage.Postings;

public class SearchEngine extends HttpServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int maxResults = 10;
	private int resultCount = 0;
	private int initialCapacity = 100;
	private Heap unigramUrls = new Heap(initialCapacity);
	private Heap bigramUrls = new Heap(initialCapacity);
	private Heap trigramUrls = new Heap(initialCapacity);
	//private ArrayList<Postings> finalResultUrls = new ArrayList<Postings>();
	private ArrayList<String> finalResultUrls = new ArrayList<String>();

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
		ExecutorService pool = Executors.newFixedThreadPool(4);
		
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
				Callable<Heap> callableUnigrams = new GetScoresCallable("UnigramIndex", queryTerms);
				Callable<Heap> callableBigrams = new GetScoresCallable("BigramIndex", queryTerms);
				Callable<Heap> callableTrigrams = new GetScoresCallable("TrigramIndex", queryTerms);
				Future<Heap> unigramFuture = pool.submit(callableUnigrams);
				Future<Heap> bigramFuture = pool.submit(callableBigrams);
				Future<Heap> trigramFuture = pool.submit(callableTrigrams);

				try
				{
					unigramUrls = unigramFuture.get();
					bigramUrls = bigramFuture.get();
					trigramUrls = trigramFuture.get();
				}
				catch (InterruptedException | ExecutionException e)
				{
					e.printStackTrace();
				}
				
				getRankedResults();
			}
			
			else if (queryTerms.length >= 2)
			{
				Callable<Heap> callableUnigrams = new GetScoresCallable("UnigramIndex", queryTerms);
				Callable<Heap> callableBigrams = new GetScoresCallable("BigramIndex", queryTerms);
				Future<Heap> unigramFuture = pool.submit(callableUnigrams);
				Future<Heap> bigramFuture = pool.submit(callableBigrams);

				try
				{
					unigramUrls = unigramFuture.get();
					bigramUrls = bigramFuture.get();
				}
				catch (InterruptedException | ExecutionException e)
				{
					e.printStackTrace();
				}
				
				getRankedResults();
			}
			
			else
			{
				Callable<Heap> callableUnigrams = new GetScoresCallable("UnigramIndex", queryTerms);
				Future<Heap> unigramFuture = pool.submit(callableUnigrams);
				try
				{
					unigramUrls = unigramFuture.get();
				}
				catch (InterruptedException | ExecutionException e)
				{
					e.printStackTrace();
				}
				
				getRankedResults();
			}
		}
	}
	
	private void getRankedResults()
	{
		if (resultCount < maxResults && !trigramUrls.isEmpty())
		{
			getRankedResults(trigramUrls);
		}
		
		if (resultCount < maxResults && !bigramUrls.isEmpty())
		{
			System.out.println("bigrams:");
			getRankedResults(bigramUrls);
		}
		
		if (resultCount < maxResults && !unigramUrls.isEmpty())
		{
			System.out.println("unigrams");
			getRankedResults(unigramUrls);
		}
		
		for (String result: finalResultUrls)
		{
			System.out.println(result);
		}
		
	}
	
	private void getRankedResults(Heap scoringFunction)
	{
		int numResults = Math.min(maxResults - resultCount, scoringFunction.size());
		
		for (int i = 0; i < numResults; i++)
		{
			String result = scoringFunction.remove().getPosting();
			if (!finalResultUrls.contains(result))
			{
				finalResultUrls.add(result);
				resultCount++;
			}
		}
	}
	
	public static void main(String args[]) throws IOException
	{
		SearchEngine searchEngine = new SearchEngine();
		//searchEngine.search("rule");
		//searchEngine.search("or");
		//searchEngine.search("quite");
		//searchEngine.search("being");
//		searchEngine.search("well");
//		searchEngine.search("used");
//		searchEngine.search("side");
//		searchEngine.search("choose");
//		searchEngine.search("map"); 
//		searchEngine.search("log");
		searchEngine.search("mashed potatoes and");
//		searchEngine.search("data contribute");
		//searchEngine.search("cookies melissa");

	}
	private String searchPage = "<form action=\"/search\" method=\"post\"> "
			+ "<center>Name of Search Engine</center> <br> " //TODO name of engine
			+ "<center><input type=\"text\" name=\"query\"> </center>" + "<br><br>"
			+ "<center><input type = \"submit\" value = \"submit\"></center>"  + "<br><br>"
			+ "</form>";
}

