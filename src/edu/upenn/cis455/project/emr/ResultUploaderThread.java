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

public class ResultUploaderThread extends Thread
{
	private static int MAX_LIST_SIZE = 1000;
	private DynamoDB dynamo;
	private String tableName;
	private String primaryKeyName;
	private String rangeKeyName;
	private String valueKeyName;
	private List<File> filePaths;
	private int id;

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
			StringBuilder value = new StringBuilder();
			while ((line = reader.readLine()) != null)
			{
				EmrResult emrResult = new EmrResult(line);
				if (emrResult.isValid())
				{
					int count = 0;
					int range = 0;
					for (int i = 0; i < emrResult.getValue().length(); i++)
					{
						if (emrResult.getValue().charAt(i) == '\t')
						{
							count++;
							if (count > 100)
							{
								EmrResult result = new EmrResult(
										emrResult.getKey(), value.toString());
								value.setLength(0);
								results.add(result);
								while (results.size() > MAX_LIST_SIZE)
								{
									List<EmrResult> resultsToBeWritten = results
											.subList(0, MAX_LIST_SIZE);
									range = batchWriteEmrResults(
											resultsToBeWritten, range);
									results.removeAll(resultsToBeWritten);
								}
								count = 0;
							}
							else
							{
								value.append(emrResult.getValue().charAt(i));
							}
						}
						else
						{
							value.append(emrResult.getValue().charAt(i));
						}
					}
					if (results.size() > 0)
					{
						batchWriteEmrResults(results, range);
					}
				}
			}
			reader.close();
		}
	}

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
