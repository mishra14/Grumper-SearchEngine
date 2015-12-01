package test.edu.upenn.cis455.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import edu.upenn.cis455.project.storage.InvertedIndex;
import edu.upenn.cis455.project.storage.Postings;

public class DynamoIndexerDA
{
	private String AWSAccessKeyId;
	private String AWSSecretKey;
	private AmazonDynamoDBClient db;
	private DynamoDBMapper mapper;
	private DynamoDBMapperConfig config;
	private HashMap<String, Float> allPostings;
	private final static int MAX_LIST = 80;
	private String tableName;
	
	public DynamoIndexerDA(String tableName){
		this.tableName = tableName;
		this.config = new DynamoDBMapperConfig(new DynamoDBMapperConfig.TableNameOverride(this.tableName));
		setDB();
		mapper = new DynamoDBMapper(db);
	}

	private void setDB(){
		File file = new File ("rootkey.csv");
		FileReader reader;
		try
		{
			reader = new FileReader(file);
			BufferedReader br = new BufferedReader(reader);
			String line = br.readLine();
			AWSAccessKeyId = line.split("=")[1].trim();
			line = br.readLine();
			AWSSecretKey = line.split("=")[1].trim();
			db = new AmazonDynamoDBClient(
					new BasicAWSCredentials(AWSAccessKeyId, AWSSecretKey));
			br.close();
			
		}
		catch (FileNotFoundException e)
		{
			
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		
	}
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
				System.out.println("INDEX " + index);
				System.out.println("Table " + tableName);

				mapper.save(index, config);
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

	public PaginatedQueryList<InvertedIndex> loadIndex(String word)
	{
		InvertedIndex queryIndex = new InvertedIndex();
		queryIndex.setWord(word);
		DynamoDBQueryExpression<InvertedIndex> query = new DynamoDBQueryExpression<InvertedIndex>()
				.withHashKeyValues(queryIndex);
		PaginatedQueryList<InvertedIndex> resultList = mapper.query(
				InvertedIndex.class, query, config);
//		for (InvertedIndex index : resultList)
//		{
//			System.out.println(index);
//		}
		return resultList;
	}

//	public static void main(String args[])
//	{
//		String tableName = "UnigramIndex";
//		DynamoIndexerDA dynamo = new DynamoIndexerDA(tableName);
//		
//	
//		dynamo.saveIndex("Dumpty", "google.com 0.8");;
//		
//		
//		System.out.println("done saving");
//		dynamo.loadIndex("dumpty");
//		System.out.println("done loading");
//	}
}
