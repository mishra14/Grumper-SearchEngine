package edu.upenn.cis455.project.searchengine;

import java.io.*;
import java.util.concurrent.*;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.*;

import javax.servlet.http.*;

public class SearchEngine extends HttpServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int maxResults = 50;
	private int resultCount = 0;
	private HashMap<String, Float> cosineSimilarityUnigrams;
	private HashMap<String, Float> cosineSimilarityBigrams;
	private HashMap<String, Float> cosineSimilarityTrigrams;
	private Heap urlFinalScores;
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
		ArrayList<String> queryTerms = new ArrayList<String>();
		cosineSimilarityUnigrams = new HashMap<String, Float>();
		cosineSimilarityBigrams = new HashMap<String, Float>();
		cosineSimilarityTrigrams = new HashMap<String, Float>();
		urlFinalScores = new Heap(100);
		
		ExecutorService pool = Executors.newFixedThreadPool(5);
		
		if (searchQuery.isEmpty())
		{
			System.out.println("Please enter a search query!");
		}
		
		else
		{
			queryTerms = getQueryTerms(searchQuery, false);
			System.out.println("query terms are: " + queryTerms);
			
			if (queryTerms.size() == 0)
			{
				System.out.println("including stop words");
				queryTerms = getQueryTerms(searchQuery, true);
				System.out.println("query terms are now: " + queryTerms);
				//Callable<HashMap<String, Integer>>
				//TODO get proximity
			}
			
			Callable<HashMap<String, Float>> callableCosineSimUnigrams = new CosineSimilarityCallable(queryTerms, "UnigramIndex");
			//Callable<HashMap<String, Float>> callableCosineSimBigrams = new CosineSimilarityCallable(queryTerms, "BigramIndex");
			//Callable<HashMap<String, Float>> callableCosineSimTrigrams = new CosineSimilarityCallable(queryTerms, "TrigramIndex");
			
			Future<HashMap<String, Float>> cosSimUnigramsFuture = pool.submit(callableCosineSimUnigrams);
			//Future<HashMap<String, Float>> bigramFuture = pool.submit(callableCosineSimBigrams);
			//Future<HashMap<String, Float>> trigramFuture = pool.submit(callableCosineSimTrigrams);

			try
			{
				cosineSimilarityUnigrams = cosSimUnigramsFuture.get();
				//cosineSimilarityBigrams = bigramFuture.get();
				//cosineSimilarityTrigrams = trigramFuture.get();
			}
			catch (InterruptedException | ExecutionException e)
			{
				e.printStackTrace();
			}
			
			computeScores();
			getRankedResults();
		}
	}
	
	private void computeScores()
	{
		for (String url : cosineSimilarityUnigrams.keySet())
		{
			float score = cosineSimilarityUnigrams.get(url);
			
			if (cosineSimilarityBigrams.containsKey(url))
			{
				score += cosineSimilarityBigrams.get(url);
			}
			
			if (cosineSimilarityTrigrams.containsKey(url))
			{
				score += cosineSimilarityTrigrams.get(url);
			}
			
			urlFinalScores.add(url, score);
		}
	}
	
	private void getRankedResults()
	{
		System.out.println("SEARCH RESULTS:");
		
		while (resultCount < maxResults && !urlFinalScores.isEmpty())
		{
			System.out.println(urlFinalScores.remove().getKey());
			resultCount++;
		}
	}
	
	public ArrayList<String> getQueryTerms(String content, Boolean includeStopWords){
		ArrayList<String> allTerms = new ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer(content, " ,.?\"!-");
		String word;
		while (tokenizer.hasMoreTokens()) {
			word = tokenizer.nextToken();
	    	word = word.trim().toLowerCase().replaceAll("[^a-z0-9 ]", "");
	    	if (includeStopWords || (!stopwords.contains(word) && !word.equals(""))){
	    		allTerms.add(word);
	    	}
	    }
		
		return allTerms;
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
//		searchEngine.search("pakistan");
//		searchEngine.search("Adamson university"); 
//		searchEngine.search("new york");
//		searchEngine.search("banana");
//		searchEngine.search("university of pennsylvania");
//		searchEngine.search("penn");
		//searchEngine.search("temple university");
		//searchEngine.search("taylor swift");
//		searchEngine.search("log");
//		searchEngine.search("am an and");
//		searchEngine.search("happy birthday");
		//searchEngine.search("chestnut pie");
		searchEngine.search("adele");
		//searchEngine.search("cooked beet greens");
		//searchEngine.search("cookies melissa");

	}
	private String searchPage = "<form action=\"/search\" method=\"post\"> "
			+ "<center>Name of Search Engine</center> <br> " //TODO name of engine
			+ "<center><input type=\"text\" name=\"query\"> </center>" + "<br><br>"
			+ "<center><input type = \"submit\" value = \"submit\"></center>"  + "<br><br>"
			+ "</form>";
	
	private static ArrayList<String> stopwords =
			new ArrayList<String> (Arrays.asList(("a,about,above,"
					+ "after,again,against,all,am,an,and,any,are,"
					+ "aren't,as,at,be,because,been,before,being,"
					+ "below,between,both,but,by,can't,cannot,could,"
					+ "couldn't,did,didn't,do,does,doesn't,doing,don't,"
					+ "down,during,each,few,for,from,further,had,hadn't,"
					+ "has,hasn't,have,haven't,having,he,he'd,he'll,he's,"
					+ "her,here,here's,hers,herself,him,himself,his,how,"
					+ "how's,i,i'd,i'll,i'm,i've,if,in,into,is,isn't,it,"
					+ "it's,its,itself,let's,me,more,most,mustn't,my,myself,"
					+ "no,nor,not,of,off,on,once,only,or,other,ought,our,ours,"
					+ "ourselves,out,over,own,same,shan't,she,she'd,she'll,"
					+ "she's,should,shouldn't,so,some,such,than,that,that's,"
					+ "the,their,theirs,them,themselves,then,there,there's,"
					+ "these,they,they'd,they'll,they're,they've,this,those,"
					+ "through,to,too,under,until,up,very,was,wasn't,we,we'd,"
					+ "we'll,we're,we've,were,weren't,what,what's,when,when's,"
					+ "where,where's,which,while,who,who's,whom,why,why's,with,"
					+ "won't,would,wouldn't,you,you'd,you'll,you're,you've,your,"
					+ "yours,yourself,yourselves,").split(",")));
}

