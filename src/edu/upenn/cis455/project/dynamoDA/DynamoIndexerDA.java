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
		//setupDB();
		mapper = new DynamoDBMapper(db);
	}

//	private void setupDB()
//	{
//		File file = new File("credentials");
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
	
	
	public ArrayList<Postings> loadIndex(String word)
	{

		InvertedIndex result = mapper.load(InvertedIndex.class, word,  config);
		if (result != null)
		{
			String postingsList = result.getPostings();
			return unmarshall(postingsList);
		}
		else
			return null;
		
	}

	private ArrayList<Postings> unmarshall(String postingsList){
		
			ArrayList<Postings> list = new ArrayList<Postings>();
			String[] allPostings = postingsList.split("\t");
			for(String posting : allPostings){		
				try {
				Postings postings = new Postings();
				String[] pair = posting.trim().split(" ", 2);
				postings.setPosting(pair[0]);
				pair = pair[1].split(" ");
				postings.setTfidf(Float.parseFloat(pair[0].trim()));
				postings.setIdf(Float.parseFloat(pair[1].trim()));
				list.add(postings);
				} catch (Exception e){
					e.printStackTrace();
				}
			}
			return list;
		
	}
	
//	public static void main (String[] args){
//		DynamoIndexerDA dynamo = new DynamoIndexerDA("Unigram");
//		ArrayList<Postings> result = dynamo.loadIndex("barack");
//		for (Postings index : result)
//			 {
//			 System.out.println("RESULT " + index.toString());
//			 }
//	}
}
