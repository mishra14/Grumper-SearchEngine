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

// TODO: Auto-generated Javadoc
/**
 * The Class PageRankDynamoThread.
 */
public class PageRankDynamoThread extends Thread
{

	/** The table name. */
	private String tableName;

	/** The read capacity. */
	private long readCapacity;

	/** The write capacity. */
	private long writeCapacity;

	/**
	 * Instantiates a new page rank dynamo thread.
	 *
	 * @param tableName
	 *            the table name
	 * @param readCapacity
	 *            the read capacity
	 * @param initialWriteCapacity
	 *            the initial write capacity
	 */
	public PageRankDynamoThread(String tableName, long readCapacity,
			long initialWriteCapacity)
	{
		super();
		this.tableName = tableName;
		this.readCapacity = readCapacity;
		this.writeCapacity = initialWriteCapacity;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
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

	/**
	 * Creates the new table.
	 *
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
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

	/**
	 * Delete old table.
	 *
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	private void deleteOldTable() throws InterruptedException
	{
		DynamoDB dynamoDB = new DynamoDB(new AmazonDynamoDBClient(
				new ProfileCredentialsProvider()));
		Table table = dynamoDB.getTable(tableName);
		table.delete();
		table.waitForDelete();
	}

	/**
	 * Update table write capacity.
	 *
	 * @param readCapacity
	 *            the read capacity
	 * @param writeCapacity
	 *            the write capacity
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
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
