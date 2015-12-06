package edu.upenn.cis455.project.emr;

import java.util.ArrayList;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;

public class PageRankDynamoThread extends Thread
{
	private String tableName;
	private long readCapacity;
	private long writeCapacity;

	public PageRankDynamoThread(String tableName, long readCapacity,
			long initialWriteCapacity)
	{
		super();
		this.tableName = tableName;
		this.readCapacity = readCapacity;
		this.writeCapacity = initialWriteCapacity;
	}

	public void run()
	{
		try
		{
			deleteOldTable();
			createNewTable();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	private void createNewTable() throws InterruptedException
	{
		String primaryKeyName = "hostName";
		DynamoDB dynamoDB = new DynamoDB(new AmazonDynamoDBClient(
				new ProfileCredentialsProvider()));

		ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();
		attributeDefinitions.add(new AttributeDefinition().withAttributeName(
				primaryKeyName).withAttributeType(ScalarAttributeType.S));

		ArrayList<KeySchemaElement> keySchema = new ArrayList<KeySchemaElement>();
		keySchema.add(new KeySchemaElement().withAttributeName(primaryKeyName)
				.withKeyType(KeyType.HASH));

		CreateTableRequest request = new CreateTableRequest()
				.withTableName(tableName)
				.withKeySchema(keySchema)
				.withAttributeDefinitions(attributeDefinitions)
				.withProvisionedThroughput(
						new ProvisionedThroughput().withReadCapacityUnits(
								readCapacity).withWriteCapacityUnits(
								writeCapacity));

		Table table = dynamoDB.createTable(request);
		table.waitForActive();

	}

	private void deleteOldTable() throws InterruptedException
	{
		DynamoDB dynamoDB = new DynamoDB(new AmazonDynamoDBClient(
				new ProfileCredentialsProvider()));
		Table table = dynamoDB.getTable(tableName);
		table.delete();
		table.waitForDelete();
	}

	public void updateTableWriteCapacity(long readCapacity, long writeCapacity)
			throws InterruptedException
	{
		DynamoDB dynamoDB = new DynamoDB(new AmazonDynamoDBClient(
				new ProfileCredentialsProvider()));

		Table table = dynamoDB.getTable(tableName);

		ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput()
				.withReadCapacityUnits(readCapacity).withWriteCapacityUnits(
						writeCapacity);

		table.updateTable(provisionedThroughput);

		table.waitForActive();
	}

}
