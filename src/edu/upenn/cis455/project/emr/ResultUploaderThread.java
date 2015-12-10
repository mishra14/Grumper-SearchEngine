package edu.upenn.cis455.project.emr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.BatchWriteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.TableWriteItems;
import com.amazonaws.services.dynamodbv2.model.WriteRequest;

import edu.upenn.cis455.project.bean.EmrResult;
import edu.upenn.cis455.project.crawler.Hash;

// TODO: Auto-generated Javadoc
/**
 * The Class ResultUploaderThread.
 */
public class ResultUploaderThread extends Thread
{
	
	/** The max list size. */
	private static int MAX_LIST_SIZE = 25;
	
	/** The dynamo. */
	private DynamoDB dynamo;
	
	/** The table name. */
	private String tableName;
	
	/** The primary key name. */
	private String primaryKeyName;
	
	/** The range key name. */
	private String rangeKeyName;
	
	/** The value key name. */
	private String valueKeyName;
	
	/** The file paths. */
	private List<File> filePaths;
	
	/** The id. */
	private int id;

	/**
	 * Instantiates a new result uploader thread.
	 *
	 * @param tableName the table name
	 * @param primaryKeyName the primary key name
	 * @param rangeKeyName the range key name
	 * @param valueKeyName the value key name
	 * @param files the files
	 * @param id the id
	 */
	public ResultUploaderThread(String tableName, String primaryKeyName,
			String rangeKeyName, String valueKeyName, List<File> files, int id)
	{
		super();
		this.dynamo = new DynamoDB(new AmazonDynamoDBClient(
				IndexerEmrController.getCredentials()));
		this.tableName = tableName;
		this.primaryKeyName = primaryKeyName;
		this.rangeKeyName = rangeKeyName;
		this.valueKeyName = valueKeyName;
		this.filePaths = files;
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run()
	{
		try
		{
			s3ToDynamoPostings(filePaths);
		}
		catch (IOException e)
		{
			System.out.println("ResultUploaderThread " + id + " : exception");
			e.printStackTrace();
		}

	}

	/**
	 * S3 to dynamo postings.
	 *
	 * @param files the files
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void s3ToDynamoPostings(List<File> files) throws IOException
	{
		List<EmrResult> results = new ArrayList<EmrResult>();
		for (File file : files)
		{
			System.out.println("Result Uploader Thread " + id
					+ " Reading File - " + file.getAbsolutePath());
			FileReader fileReader = new FileReader(file);
			BufferedReader reader = new BufferedReader(fileReader);
			String line;
			while ((line = reader.readLine()) != null)
			{
				EmrResult emrResult = new EmrResult(line);
				if (emrResult.isValid())
				{
					results.add(emrResult);
					while (results.size() > MAX_LIST_SIZE)
					{
						List<EmrResult> resultsToBeWritten = results.subList(0,
								MAX_LIST_SIZE);
						batchWriteEmrResults(resultsToBeWritten, 0);
						results.removeAll(resultsToBeWritten);
					}

				}
				if (results.size() > 0)
				{
					batchWriteEmrResults(results, 0);
				}
			}
			reader.close();
		}
	}

	/**
	 * Batch write emr results.
	 *
	 * @param results the results
	 * @param range the range
	 * @return the int
	 */
	public int batchWriteEmrResults(List<EmrResult> results, int range)
	{
		try
		{
			TableWriteItems writeItems = new TableWriteItems(tableName);
			for (EmrResult result : results)
			{
				PrimaryKey primaryKey = new PrimaryKey(primaryKeyName,
						result.getKey(), rangeKeyName, range);
				Item item = new Item().withPrimaryKey(primaryKey).with(
						valueKeyName, result.getValue());
				writeItems.addItemToPut(item);
				range++;
			}
			try
			{
				BatchWriteItemOutcome outcome = dynamo
						.batchWriteItem(writeItems);
				do
				{
					// Check for unprocessed keys
					Map<String, List<WriteRequest>> unprocessedItems = outcome
							.getUnprocessedItems();

					if (outcome.getUnprocessedItems().size() > 0)
					{
						outcome = dynamo
								.batchWriteItemUnprocessed(unprocessedItems);
					}

				}
				while (outcome.getUnprocessedItems().size() > 0);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

		}
		catch (Exception e)
		{
			System.out
					.println("EMR Controller : Failed to batch write items - ");
			e.printStackTrace();
		}
		return range;
	}
}
