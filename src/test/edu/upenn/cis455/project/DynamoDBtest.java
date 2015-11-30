package test.edu.upenn.cis455.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import edu.upenn.cis455.project.storage.InvertedIndex;
import edu.upenn.cis455.project.storage.Postings;

public class DynamoDBtest
{
	private AmazonDynamoDBClient db = new AmazonDynamoDBClient(
			new BasicAWSCredentials("AKIAJW5SHL6JM2RZLTXQ", "+U+QT1nqEUzVEREpZZjYSmUdwHA/3Enb3L3i2n9N"));
	private DynamoDBMapper mapper = new DynamoDBMapper(db);
	private HashMap<String, Float> allPostings;
	private final static int MAX_LIST = 80;
	
	

	public void saveIndex(String word, String postingsList)
	{

		InvertedIndex index = new InvertedIndex();

		parseInput(postingsList);
		index.setWord(word);
		int count = 0;
		Iterator<Entry<String, Float>> it = allPostings.entrySet().iterator();
		ArrayList<Postings> postingList = new ArrayList<Postings>();
		while (it.hasNext())
		{
			Map.Entry<String, Float> pair = (Map.Entry<String, Float>) it
					.next();
			Postings posting = new Postings();
			posting.setPosting(pair.getKey().toString());
			posting.setTfidf((float) pair.getValue());
			postingList.add(posting);
			it.remove();
			count++;
			if (count >= MAX_LIST || !it.hasNext())
			{
				index.setPostings(postingList);
				mapper.save(index);
				postingList = new ArrayList<Postings>();
				count = 0;

			}

		}

	}

	public void parseInput(String postingsList)
	{
		allPostings = new HashMap<String, Float>();

		//String[] input = line.split("\t", 2);

		//word = input[0];
		String[] postings = postingsList.split(",");
//		System.out.println("Word:" + word);
//		System.out.println("Whole list:" + input[1]);

		for (String posting : postings)
		{
			String[] pair = posting.trim().split(" ");
			allPostings.put(pair[0].trim(), Float.parseFloat(pair[1].trim()));
		}

	}

	public void loadIndex(String word)
	{
		InvertedIndex queryIndex = new InvertedIndex();
		queryIndex.setWord(word);
		DynamoDBQueryExpression<InvertedIndex> query = new DynamoDBQueryExpression<InvertedIndex>()
				.withHashKeyValues(queryIndex);
		PaginatedQueryList<InvertedIndex> resultList = mapper.query(
				InvertedIndex.class, query);
		for (InvertedIndex index : resultList)
		{
			System.out.println(index);
		}
	}

//	public static void main(String args[])
//	{
//		DynamoDBtest dynamo = new DynamoDBtest();
//		dynamo.saveIndex("humpty", "google.com 0.8");;
//		
//		
//		System.out.println("done saving");
//		loadIndex();
//		System.out.println("done loading");
//	}
}
