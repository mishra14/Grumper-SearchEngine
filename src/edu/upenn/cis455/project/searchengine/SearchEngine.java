package edu.upenn.cis455.project.searchengine;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.*;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;

import javax.servlet.http.*;

import com.amazonaws.services.dynamodbv2.document.Item;

import edu.upenn.cis455.project.scoring.Stemmer;
import edu.upenn.cis455.project.storage.DynamoDA;
import edu.upenn.cis455.project.storage.DBWrapper;
import edu.upenn.cis455.project.storage.SearchEngineCacheDA;

public class SearchEngine extends HttpServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int maxResults = 20;
	private int resultCount = 0;
	private HashMap<String, Float> cosineSimilarityUnigrams;
	private HashMap<String, Float> cosineSimilarityBigrams;
	private HashMap<String, Float> cosineSimilarityTrigrams;
	private HashMap<String, Float> pageRankMap;
	private Heap urlTfidfScores, urlFinalScores;
	private StringBuffer resultsForCache;
	private SearchEngineCacheDA cacheDA = new SearchEngineCacheDA();

	public void init()
	{
		
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		PrintWriter out = response.getWriter();
		String searchQuery = request.getParameter("query");
		System.out.println("search query received: "+searchQuery);
		String results = search(searchQuery);
		System.out.println("results: " + results);
		out.write(results);
		response.flushBuffer();	
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		PrintWriter out = response.getWriter();
		String searchQuery = request.getParameter("query");
		System.out.println("search query received: "+searchQuery);
		String results = search(searchQuery);
		out.write(results);
		response.flushBuffer();
	}
	
	public String search(String searchQuery)
	{
		String results = null;
		ExecutorService pool = Executors.newFixedThreadPool(3);;
		try {
			String BDBStore = "/home/cis455/SeacheEngineCache";
			DBWrapper.openDBWrapper(BDBStore);
			
			if (cacheDA.getCachedResultsInfo(searchQuery) != null)
			{
				System.out.println("Query was cached");
				results = getCachedResults(searchQuery);
				
			}
			
			else
			{
				System.out.println("Query not cached");
				ArrayList<String> queryTerms = new ArrayList<String>();
				cosineSimilarityUnigrams = new HashMap<String, Float>();
				cosineSimilarityBigrams = new HashMap<String, Float>();
				cosineSimilarityTrigrams = new HashMap<String, Float>();
				urlTfidfScores = new Heap(100);
				pageRankMap = new HashMap<>();
				
				if (searchQuery.isEmpty())
				{
					System.out.println("Search query was empty");
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
					}
					
					Callable<HashMap<String, Float>> callableCosineSimUnigrams = new CosineSimilarityCallable(queryTerms, "UnigramIndex");
					Callable<HashMap<String, Float>> callableCosineSimBigrams = new CosineSimilarityCallable(queryTerms, "BigramIndex");
					Callable<HashMap<String, Float>> callableCosineSimTrigrams = new CosineSimilarityCallable(queryTerms, "TrigramIndex");
					
					Future<HashMap<String, Float>> cosSimUnigramsFuture = pool.submit(callableCosineSimUnigrams);
					Future<HashMap<String, Float>> bigramFuture = pool.submit(callableCosineSimBigrams);
					Future<HashMap<String, Float>> trigramFuture = pool.submit(callableCosineSimTrigrams);

					try
					{
						cosineSimilarityUnigrams = cosSimUnigramsFuture.get();
						cosineSimilarityBigrams = bigramFuture.get();
						cosineSimilarityTrigrams = trigramFuture.get();						
					}
					catch (InterruptedException | ExecutionException e)
					{
						e.printStackTrace();
					}
					
					computeScores();
					getRankedResults();
				}
				
				System.out.println("Adding query to cache");
				results = resultsForCache.toString();
				//CachedResultsInfo cacheInfo = new CachedResultsInfo(searchQuery, results, new Date());
				//dbw.putCachedResultsInfo(cacheInfo);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		finally
		{
			System.out.println("Closing dbwrapper");
			DBWrapper.closeDBWrapper();
			pool.shutdown();
		}
		System.out.println("Results are: " + results);
		return results;
	}
	
	private void computeScores()
	{
		Float pageRank = new Float(1);
		String hostname;
		urlFinalScores = new Heap(100);
		DynamoDA<Float> pageRankDA = new DynamoDA<Float>("edu.upenn.cis455.project.pagerank", Float.class);
		for (String url : cosineSimilarityUnigrams.keySet())
		{
			float score = (float) (0.17*cosineSimilarityUnigrams.get(url));
			if (cosineSimilarityBigrams.containsKey(url))
			{
				score += 0.33*cosineSimilarityBigrams.get(url);
			}
			
			if (cosineSimilarityTrigrams.containsKey(url))
			{
				score += 0.49*cosineSimilarityTrigrams.get(url);
			}
			
			urlTfidfScores.add(url, score);
		}
		
		for (int i = 0; i < 100 && !urlTfidfScores.isEmpty(); i++)
		{
			SimpleEntry<String, Float> urlCosineSim = urlTfidfScores.remove();
			String url = urlCosineSim.getKey();
			Float cosineSim = urlCosineSim.getValue();
			try
			{
				hostname = new URL(url).getHost();
				if (pageRankMap.containsKey(hostname))
				{
					//System.out.println("getting pagerank from map: " + hostname);
					pageRank = pageRankMap.get(hostname);
				}
				
				else
				{
					//System.out.println("getting pagerank from db");
					Item item = pageRankDA.getItem("hostName", hostname);
					if (item != null)
					{
						pageRank = item.getFloat("rank");
						pageRankMap.put(hostname, pageRank);
						//System.out.println("pagerank of " + hostname + " = " + pageRank);
					}
				}
				
				urlFinalScores.add(url, (float) (0.8*cosineSim + 0.2*pageRank));
			}
			catch (MalformedURLException e)
			{
				System.out.println("Exception: Malformed Url " + url);
				continue;
			}
		}
	}
	
	private void getRankedResults()
	{
		resultsForCache = new StringBuffer();
		System.out.println("SEARCH RESULTS:");
		
		while (resultCount < maxResults && !urlFinalScores.isEmpty())
		{
			SimpleEntry<String, Float> finalScore = urlFinalScores.remove();
			resultsForCache.append(finalScore.getKey() + " ");
			System.out.println(finalScore.getKey() + ": " + String.valueOf(finalScore.getValue()));
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
	    		allTerms.add(stem(word));
	    	}
	    }
		
		return allTerms;
	}
	
	public String getCachedResults(String query)
	{
		String cachedResults = cacheDA.getCachedResultsInfo(query).getResults();
		System.out.println("SEARCH RESULTS:");
		for (String result: cachedResults.split(" "))
		{
			System.out.println(result);
		}
		return cachedResults;
	}
	
	public String stem(String word)
	{
		Stemmer stemmer = new Stemmer();
		char[] charArray = word.toCharArray();
		stemmer.add(charArray, word.length());
		stemmer.stem();
		String stemmedWord = stemmer.toString();
		return stemmedWord;
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
		//searchEngine.search("mark zuckerberg");
		searchEngine.search("barack");
		//searchEngine.search("university of pennsylvania");
		//searchEngine.search("india");
		//searchEngine.search("adamson university");
		//searchEngine.search("taylor swift");
//		searchEngine.search("log");
//		searchEngine.search("am an and");
		//searchEngine.search("happy birthday");
		//searchEngine.search("chestnut pie");
		//searchEngine.search("adele");
		//searchEngine.search("cooked beet greens");
		//searchEngine.search("cookies melissa");

	}
	
	private static ArrayList<String> stopwords =
			new ArrayList<String> (Arrays.asList(("a,about,above,"
					+ "after,again,against,all,am,an,and,any,are,"
					+ "aren't,as,at,be,because,been,before,being,"
					+ "below,between,both,but,by,could,"
					+ "couldn't,did,didn't,do,does,doesn't,doing,don't,"
					+ "down,during,each,few,for,from,further,had,hadn't,"
					+ "has,hasn't,have,haven't,having,he,he'd,he'll,he's,"
					+ "her,here,here's,hers,herself,him,himself,his,"
					+ "how's,i,i'd,i'll,i'm,i've,if,in,into,is,isn't,it,"
					+ "it's,its,itself,let's,me,more,mustn't,my,myself,"
					+ "no,nor,of,off,on,once,only,or,other,ought,our,ours,"
					+ "ourselves,out,over,own,shan't,she,she'd,she'll,"
					+ "she's,should,shouldn't,so,some,such,than,that,that's,"
					+ "the,their,theirs,them,themselves,then,there,there's,"
					+ "these,they,they'd,they'll,they're,they've,this,those,"
					+ "through,to,too,under,until,up,very,was,wasn't,we,we'd,"
					+ "we'll,we're,we've,were,weren't,what's,when's,"
					+ "where's,while,who's,why's,with,"
					+ "won't,would,wouldn't,you,you'd,you'll,you're,you've,your,"
					+ "yours,yourself,yourselves,").split(",")));
}

