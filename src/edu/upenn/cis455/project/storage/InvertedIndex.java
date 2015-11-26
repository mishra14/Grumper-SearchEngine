package edu.upenn.cis455.project.storage;

import java.util.ArrayList;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshalling;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "UnigramIndex")
public class InvertedIndex
{
	private String word;
	private ArrayList<Postings> postingsList;

	@DynamoDBHashKey(attributeName = "Word")
	public String getWord()
	{
		return this.word;
	}

	public void setWord(String word)
	{
		this.word = word;
	}

	@DynamoDBRangeKey(attributeName = "Postings")
	@DynamoDBMarshalling(marshallerClass = PostingsMarshaller.class)
	public ArrayList<Postings> getPostings()
	{
		return this.postingsList;
	}

	public void setPostings(ArrayList<Postings> postingsList)
	{
		this.postingsList = postingsList;
	}

	@Override
	public String toString()
	{
		return "InvertedIndex [word=" + word + ", postingsList=" + postingsList + "]";
	}

}
