package edu.upenn.cis455.project.storage;


import java.util.ArrayList;
import java.util.HashMap;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;

public class PostingsMarshaller implements DynamoDBMarshaller<ArrayList<Postings>>
{

	@Override
	public String marshall(ArrayList<Postings> postings)
	{	
		System.out.println("Postings: " + postings.toString());
		StringBuilder marshalled = new StringBuilder();
		for (Postings posting : postings){
			marshalled.append(posting.getPosting() 
					+ " " + posting.getTfidf() + ",");
			
		}
		System.out.println(marshalled.toString() + " " + marshalled.length());
		marshalled.deleteCharAt(marshalled.length() - 1);
		return marshalled.toString();
	}

	@Override
	public ArrayList<Postings> unmarshall(Class<ArrayList<Postings>> clazz, String s)
	{
		ArrayList<Postings> list = new ArrayList<Postings>();
		String[] allPostings = s.split(",");
		for(String posting : allPostings){		
			String[] pair = posting.trim().split(" ");
			Postings postings = new Postings();
			postings.setPosting(pair[0]);
			postings.setTfidf(Float.parseFloat(pair[1]));
			
			list.add(postings);
		}
		return list;
	}
	

}
