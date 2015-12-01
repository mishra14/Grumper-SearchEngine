package edu.upenn.cis455.project.searchengine;

import java.io.*;
import java.util.concurrent.*;

import javax.servlet.http.*;

public class SearchEngine extends HttpServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int maxResults = 10;
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
		ExecutorService pool = Executors.newFixedThreadPool(4);
		
		if (searchQuery.isEmpty())
		{
			System.out.println("Please enter a search query!");
		}
		
		else
		{
			queryTerms = searchQuery.split(" ");
			//TODO preprocess query
			
			
			if (queryTerms.length == 1)
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
		int numResults = Math.min(maxResults, unigramUrls.size());
		if (!unigramUrls.isEmpty())
		{
			for (int i = 0; i < numResults; i++)
			{
				System.out.println(unigramUrls.remove().getPosting());
			}
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

