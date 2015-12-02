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
					+ " " + posting.getTfidf() + " " + posting.getIdf() + ",");
			
		}
		marshalled.deleteCharAt(marshalled.length() - 1);
		System.out.println(marshalled.toString());
		return marshalled.toString();
	}

	@Override
	public ArrayList<Postings> unmarshall(Class<ArrayList<Postings>> clazz, String s)
	{
		System.out.println("String : " + s);
		ArrayList<Postings> list = new ArrayList<Postings>();
		String[] allPostings = s.split(",");
		for(String posting : allPostings){		
			System.out.println(posting);
			Postings postings = new Postings();
			String[] pair = posting.trim().split(" ", 2);
			postings.setPosting(pair[0]);
			pair = pair[1].split(" ");
			postings.setTfidf(Float.parseFloat(pair[0].trim()));
			postings.setIdf(Float.parseFloat(pair[1].trim()));
			list.add(postings);
		}
		return list;
	}
	

}
