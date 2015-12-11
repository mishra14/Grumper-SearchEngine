package edu.upenn.cis455.project.storage;

import java.util.ArrayList;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshalling;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;


// TODO: Auto-generated Javadoc
/**
 * The Class InvertedIndex_old.
 */
@DynamoDBTable(tableName = "CUSTOM_TABLE_NAME")
public class InvertedIndex_old
{
	
	/** The word. */
	private String word;
	
	/** The range. */
	private long range;
	
	/** The postings list. */
	private ArrayList<Postings> postingsList;

	/**
	 * Gets the word.
	 *
	 * @return the word
	 */
	@DynamoDBHashKey(attributeName = "Word")
	public String getWord()
	{
		return this.word;
	}

	/**
	 * Sets the word.
	 *
	 * @param word the new word
	 */
	public void setWord(String word)
	{
		this.word = word;
	}

	/**
	 * Gets the range key.
	 *
	 * @return the range key
	 */
	@DynamoDBRangeKey(attributeName = "Range")
	public long getRangeKey(){
		return this.range;
	}
	
	/**
	 * Sets the range key.
	 *
	 * @param range the new range key
	 */
	public void setRangeKey(long range){
		this.range = range;
	}
	
	/**
	 * Gets the postings.
	 *
	 * @return the postings
	 */
	@DynamoDBAttribute(attributeName = "Postings")
	@DynamoDBMarshalling(marshallerClass = PostingsMarshaller.class)
	public ArrayList<Postings> getPostings()
	{
		return this.postingsList;
	}

	/**
	 * Sets the postings.
	 *
	 * @param postingsList the new postings
	 */
	public void setPostings(ArrayList<Postings> postingsList)
	{
		this.postingsList = postingsList;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "InvertedIndex [word=" + word + ", postingsList=" + postingsList + "]";
	}
}
