package edu.upenn.cis455.project.storage;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "CUSTOM_TABLE_NAME")
public class InvertedIndex
{
	private String word;
	private String postings;

	@DynamoDBHashKey(attributeName = "Word")
	public String getWord()
	{
		return this.word;
	}

	public void setWord(String word)
	{
		this.word = word;
	}
	
	@DynamoDBAttribute(attributeName = "Postings")
	public String getPostings()
	{
		return this.postings;
	}

	public void setPostings(String postingsList)
	{
		this.postings = postingsList;
	}

	@Override
	public String toString()
	{
		return "InvertedIndex [word=" + word + ", postingsList=" + postings + "]";
	}
}
