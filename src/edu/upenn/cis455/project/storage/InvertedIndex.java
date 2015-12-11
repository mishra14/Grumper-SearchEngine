package edu.upenn.cis455.project.storage;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

// TODO: Auto-generated Javadoc
/**
 * The Class InvertedIndex.
 */
@DynamoDBTable(tableName = "CUSTOM_TABLE_NAME")
public class InvertedIndex
{
	
	/** The word. */
	private String word;
	
	/** The postings. */
	private String postings;

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
	 * Gets the postings.
	 *
	 * @return the postings
	 */
	@DynamoDBAttribute(attributeName = "Postings")
	public String getPostings()
	{
		return this.postings;
	}

	/**
	 * Sets the postings.
	 *
	 * @param postingsList the new postings
	 */
	public void setPostings(String postingsList)
	{
		this.postings = postingsList;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "InvertedIndex [word=" + word + ", postingsList=" + postings + "]";
	}
}
