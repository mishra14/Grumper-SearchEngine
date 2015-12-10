package edu.upenn.cis455.project.dynamoDA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.mapreduce.Reducer.Context;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputExceededException;

import edu.upenn.cis455.project.crawler.Hash;
import edu.upenn.cis455.project.storage.InvertedIndex_old;
import edu.upenn.cis455.project.storage.InvertedIndex;
import edu.upenn.cis455.project.storage.Postings;

public class DynamoIndexerDA
{
	private String AWSAccessKeyId;
	private String AWSSecretKey;
	private AmazonDynamoDBClient db;
	private DynamoDBMapper mapper;
	private DynamoDBMapperConfig config;
	// private HashMap<String, Float> allPostings;
	private final static int MAX_LIST = 50;
	private final static int MAX_SIZE = 200800;
	private final static int BATCH_LIMIT = 25;
	private String tableName;
	private final static int scoreLen = 15;

	public DynamoIndexerDA(String tableName)
	{
		this.tableName = tableName;
		this.config = new DynamoDBMapperConfig(
				new DynamoDBMapperConfig.TableNameOverride(this.tableName));
		db = new AmazonDynamoDBClient();
		// setupDB();
		mapper = new DynamoDBMapper(db);
	}

//	private void setupDB()
//	{
//		File file = new File("rootkey.csv");
//		FileReader reader;
//		try
//		{
//			reader = new FileReader(file);
//			BufferedReader br = new BufferedReader(reader);
//			String line = br.readLine();
//			AWSAccessKeyId = line.split("=")[1].trim();
//			line = br.readLine();
//			AWSSecretKey = line.split("=")[1].trim();
//			db = new AmazonDynamoDBClient(
//					new BasicAWSCredentials(AWSAccessKeyId, AWSSecretKey));
//			br.close();
//
//		}
//		catch (FileNotFoundException e)
//		{
//
//			e.printStackTrace();
//		}
//		catch (IOException e)
//		{
//			e.printStackTrace();
//		}
//
//	}

	public void save(String word, String postings){
		InvertedIndex index = new InvertedIndex();
		index.setWord(word);
		index.setPostings(postings);
		mapper.save(index, config);
		
	}
	
	public void saveIndex(String word, ArrayList<Postings> allPostings,
			Context context)
	{

		InvertedIndex_old index = new InvertedIndex_old();
		index.setWord(word);
		ArrayList<Postings> postingsList = new ArrayList<Postings>();
		int count = 0;
		int size = allPostings.size();
		long range = 0;
		for (int i = 0; i < size; i++)
		{
			postingsList.add(allPostings.get(i));
			count++;

			if (count >= MAX_LIST || i == size - 1)
			{
				try
				{
					index.setRangeKey(range);
					range++;
					index.setPostings(postingsList);
					mapper.save(index, config);
					postingsList = new ArrayList<Postings>();
					count = 0;
					if ((range % 100) == 0)
						context.progress();

				}
				catch (ProvisionedThroughputExceededException e)
				{
					e.printStackTrace();
				}

			}

		}
	}

	public void saveIndexWithBackOff(String word,
			ArrayList<Postings> allPostings, Context context)
					throws InterruptedException
	{
		InvertedIndex_old index = new InvertedIndex_old();
		index.setWord(word);
		// ArrayList<Postings>allPostings = parseAllPostings(postingsString);
		ArrayList<Postings> postingsList = new ArrayList<Postings>();
		int size = allPostings.size();
		long range = 0;
		for (int i = 0; i < size; i++)
		{
			int backOffCount = 0;
			int backOffDuration = 50;
			boolean done = false;
			// while (!done && backOffCount < 50)
			// {
			postingsList.add(allPostings.get(i));
			try
			{
				index.setRangeKey(range);
				index.setPostings(postingsList);
				mapper.save(index, config);
				range++;
				postingsList.clear();
				done = true;
				if ((range % 100) == 0)
					context.progress();
			}
			catch (ProvisionedThroughputExceededException e)
			{
				e.printStackTrace();
				if ((range % 100) == 0)
					context.progress();
			}
			// backOffCount++;
			// Thread.sleep(backOffCount * backOffDuration);
		}
		// }
		// }

	}

	public void saveMultipleIndex(String word, ArrayList<Postings> allPostings,
			Context context)
	{
		int numPostings = allPostings.size();
		if (numPostings < MAX_LIST)
		{
			// write to db
			InvertedIndex_old index = new InvertedIndex_old();
			try
			{
				index.setWord(word);
				index.setPostings(allPostings);
				index.setRangeKey(0);
				mapper.save(index, config);
			}
			catch (ProvisionedThroughputExceededException e)
			{
				e.printStackTrace();
			}

		}
		else
		{
			// breakdown the list into multiple and do batch write
			ArrayList<Postings> postingsList = new ArrayList<Postings>();
			ArrayList<InvertedIndex_old> rowEntry = new ArrayList<InvertedIndex_old>();
			int totalLen = 0;
			Postings postings;
			long range = 0;
			for (int i = 0; i < numPostings; i++)
			{
				if ( i > 2000)
					break;
				if ((range % 100) == 0)
					context.progress();
				postings = allPostings.get(i);
				int len = getSize(postings.getPosting());
				int temp = totalLen + len;
				if (temp <= MAX_SIZE && (i < numPostings - 1))
				{
					totalLen = temp;
					postingsList.add(postings);
				}
				else if (temp > MAX_SIZE
						|| (temp <= MAX_SIZE && (i == numPostings - 1)))
				{
					if (i == numPostings - 1)
					{
						postingsList.add(postings);
					}
					if (rowEntry.size() < BATCH_LIMIT)
					{

						InvertedIndex_old index = new InvertedIndex_old();
						index.setWord(word);
						index.setPostings(postingsList);
						index.setRangeKey(range);
						range++;
						rowEntry.add(index);
						postingsList.clear();
						totalLen = 0;

					}
					else
					{
						try
						{
							mapper.batchWrite(rowEntry, Collections.emptyList(),
									config);
							rowEntry.clear();
							postingsList.clear();
							totalLen = 0;
						}
						catch (ProvisionedThroughputExceededException e)
						{
							e.printStackTrace();
						}
					}

				}

			}

			if (!rowEntry.isEmpty())
				mapper.batchWrite(rowEntry, Collections.emptyList(), config);
			// mapper.batchSave(rowEntry.toArray(new
			// InvertedIndex[rowEntry.size()]), config);

		}

	}

	private int getSize(String posting)
	{
		int len = posting.length();
		int totalLen = len * 2 + scoreLen;
		return totalLen;
	}
	// public ArrayList<Postings> parseAllPostings(String postingsList){
	// ArrayList<Postings> list = new ArrayList<Postings>();
	// String[] postingsContent = postingsList.split("\t");
	// for (String posting : postingsContent)
	// {
	// Postings postings = new Postings();
	// String[] pair = posting.trim().split(" ", 2);
	// postings.setPosting(pair[0]);
	// pair = pair[1].split(" ");
	// postings.setTfidf(Float.parseFloat(pair[0].trim()));
	// postings.setIdf(Float.parseFloat(pair[1].trim()));
	// list.add(postings);
	//
	// }
	// return list;
	// }

	public ArrayList<Postings> loadIndex(String word)
	{
//		InvertedIndex queryIndex = new InvertedIndex();
//		queryIndex.setWord(word);
//		DynamoDBQueryExpression<InvertedIndex> query = new DynamoDBQueryExpression<InvertedIndex>()
//				.withHashKeyValues(queryIndex);
		InvertedIndex result = mapper.load(InvertedIndex.class, word,  config);
		String postingsList = result.getPostings();
		return unmarshall(postingsList);
		
//		PaginatedQueryList<InvertedIndex> resultList = mapper
//				.query(InvertedIndex.class, query, config);
//		 for (InvertedIndex index : resultList)
//		 {
//		 System.out.println("RESULT " + index);
//		 }
//		return resultList;
		
		
	}

	private ArrayList<Postings> unmarshall(String postingsList){
		try {
			ArrayList<Postings> list = new ArrayList<Postings>();
			String[] allPostings = postingsList.split("\t");
			for(String posting : allPostings){		
				Postings postings = new Postings();
				String[] pair = posting.trim().split(" ", 2);
				postings.setPosting(pair[0]);
				pair = pair[1].split(" ");
				postings.setTfidf(Float.parseFloat(pair[0].trim()));
				postings.setIdf(Float.parseFloat(pair[1].trim()));
				list.add(postings);
			}
			return list;
		} catch (Exception e){
			
		}
		return null;
	}
	
	public static void main (String[] args){
		DynamoIndexerDA dynamo = new DynamoIndexerDA("Unigram");
		ArrayList<Postings> result = dynamo.loadIndex("barack");
		for (Postings index : result)
			 {
			 System.out.println("RESULT " + index.toString());
			 }
	}
}
