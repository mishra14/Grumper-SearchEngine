package test.edu.upenn.cis455.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import edu.upenn.cis455.project.storage.InvertedIndex;
import edu.upenn.cis455.project.storage.Postings;

public class DynamoDBtest
{
	private static AmazonDynamoDBClient db = new AmazonDynamoDBClient(
			new DefaultAWSCredentialsProviderChain());
	private static DynamoDBMapper mapper = new DynamoDBMapper(db);
	private static String line = "duck	http://abc.com 0.3, http://espn.com 0.5";
	private static String word;
	private static HashMap<String, Float> allPostings;
	private final static int MAX_LIST = 80;

	private static void saveIndex()
	{

		InvertedIndex index = new InvertedIndex();

		parseInput();
		index.setWord(word);
		int count = 0;
		Iterator<Entry<String, Float>> it = allPostings.entrySet().iterator();
		System.out.println("all:" + allPostings.toString());
		ArrayList<Postings> postingList = new ArrayList<Postings>();
		while (it.hasNext())
		{
			Map.Entry<String, Float> pair = (Map.Entry<String, Float>) it
					.next();
			Postings posting = new Postings();
			posting.setPosting(pair.getKey().toString());
			posting.setTfidf((float) pair.getValue());
			postingList.add(posting);
			System.out.println("adding - " + posting);
			it.remove();
			count++;
			if (count >= MAX_LIST || !it.hasNext())
			{
				System.out.println("Post: " + postingList.toString());
				index.setPostings(postingList);
				mapper.save(index);
				postingList = new ArrayList<Postings>();
				count = 0;

			}

		}

	}

	private static void parseInput()
	{
		allPostings = new HashMap<String, Float>();

		String[] input = line.split("\t", 2);

		word = input[0];
		String[] postings = input[1].split(",");
		System.out.println("Word:" + word);
		System.out.println("Whole list:" + input[1]);

		for (String posting : postings)
		{
			System.out.println("Posting:" + posting);
			String[] pair = posting.trim().split(" ");
			System.out.println("URL: " + pair[0] + " idf:" + pair[1]);
			allPostings.put(pair[0].trim(), Float.parseFloat(pair[1].trim()));
		}

	}

	private static void loadIndex()
	{
		InvertedIndex queryIndex = new InvertedIndex();
		queryIndex.setWord("duck");
		DynamoDBQueryExpression<InvertedIndex> query = new DynamoDBQueryExpression<InvertedIndex>()
				.withHashKeyValues(queryIndex);
		PaginatedQueryList<InvertedIndex> resultList = mapper.query(
				InvertedIndex.class, query);
		for (InvertedIndex index : resultList)
		{
			System.out.println(index);
		}
	}

	public static void main(String args[])

	{
		saveIndex();
		System.out.println("done saving");
		loadIndex();
		System.out.println("done loading");
	}
}
