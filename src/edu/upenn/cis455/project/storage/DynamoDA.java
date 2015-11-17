package edu.upenn.cis455.project.storage;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DeleteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;

public class DynamoDA
{

	private DynamoDB dynamoDB;
	private Table table;

	public DynamoDA(String tableName)
	{
		this.dynamoDB = new DynamoDB(new AmazonDynamoDBClient(
				new ProfileCredentialsProvider()));
		this.table = dynamoDB.getTable(tableName);
	}

	public Item getItem(String key)
	{
		Item item = null;
		try
		{
			item = table.getItem("documentUrl", key);

		}
		catch (Exception e)
		{
			System.err.println("GetItem failed.");
			System.err.println(e.getMessage());
		}
		return item;
	}

	public void putItem(String key, String value)
	{
		Item item = new Item().withPrimaryKey("documentUrl", key).withString(
				"s3Key", value);
		table.putItem(item);
	}

	public void deleteItem(String key)
	{

		try
		{

			DeleteItemSpec deleteItemSpec = new DeleteItemSpec()
					.withPrimaryKey("documentUrl", key).withReturnValues(
							ReturnValue.ALL_OLD);

			DeleteItemOutcome outcome = table.deleteItem(deleteItemSpec);

			/*            // Check the response.
			            System.out.println("Printing item that was deleted...");
			            System.out.println(outcome.getItem().toJSONPretty());*/

		}
		catch (Exception e)
		{
			System.out
					.println("Error deleting item in " + table.getTableName());
			e.printStackTrace();
		}
	}

}
