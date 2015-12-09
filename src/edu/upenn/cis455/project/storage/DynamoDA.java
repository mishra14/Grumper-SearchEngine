package edu.upenn.cis455.project.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class DynamoDA<T>
{

	private DynamoDB dynamoDB;
	private Table table;
	private final Class<T> typeParameterClass;

	public DynamoDA(String tableName, Class<T> typeParameterClass)
	{
		File file = new File("rootkey.csv");
		String accessKey = null;
		String secretKey = null;
		try
		{
			String line;
			FileReader reader = new FileReader(file);
			BufferedReader in = new BufferedReader(reader);
			while ((line = in.readLine()) != null)
			{
				if (line.contains("AWSAccessKeyId"))
				{
					accessKey = line.split("=")[1].trim();
				}
				else if (line.contains("AWSSecretKey"))
				{
					secretKey = line.split("=")[1].trim();
				}
			}
			in.close();
			reader.close();
		}
		catch (FileNotFoundException e)
		{
			System.out.println("DynamoDA : reading from local credential file failed");
			e.printStackTrace();
		}
		catch (IOException e)
		{
			System.out.println("DynamoDA : reading from local credential file failed");
			e.printStackTrace();
		}
		BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey,
				secretKey);
		this.dynamoDB = new DynamoDB(new AmazonDynamoDBClient(awsCreds));
		this.table = dynamoDB.getTable(tableName);
		this.typeParameterClass = typeParameterClass;
	}

	public Item getItem(String primaryKey, String primaryKeyValue)
	{
		Item item = null;
		try
		{
			item = table.getItem(primaryKey, primaryKeyValue);

		}
		catch (Exception e)
		{
			System.out.println("DynamoDA : GetItem failed.");
			System.out.println(e.getMessage());
		}
		return item;
	}

	public T getValue(String primaryKey, String primaryKeyValue, String key)
	{
		Item item = getItem(primaryKey, primaryKeyValue);
		T result = null;
		if (item != null)
		{
			String json = item.getString(key);
			ObjectMapper mapper = new ObjectMapper();
			mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
			mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);

			try
			{
				result = mapper.readValue(json, typeParameterClass);
			}
			catch (IOException e)
			{
				System.out.println("DynamoDA : GetValue failed.");
				e.printStackTrace();
			}
		}
		return result;
	}

	public void putItem(String primaryKey, String primaryKeyValue,
			Map<String, T> otherPairs)
	{
		try
		{
			ObjectMapper mapper = new ObjectMapper();
			mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
			mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
			ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
			Item item = new Item().withPrimaryKey(primaryKey, primaryKeyValue);
			for (Map.Entry<String, T> entry : otherPairs.entrySet())
			{
				String json = ow.writeValueAsString(entry.getValue());
				item.withString(entry.getKey(), json);
			}
			table.putItem(item);
		}
		catch (JsonProcessingException e)
		{
			System.out.println("DynamoDA : json parsing exception");
			e.printStackTrace();
		}

	}

	public void putItem(String primaryKey, String primaryKeyValue, String key,
			T value)
	{
		try
		{
			ObjectMapper mapper = new ObjectMapper();
			mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
			mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
			ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
			Item item = new Item().withPrimaryKey(primaryKey, primaryKeyValue);
			String json = ow.writeValueAsString(value);
			item.withString(key, json);
			System.out.println(item);
			table.putItem(item);
		}
		catch (JsonProcessingException e)
		{
			System.out.println("DynamoDA : json parsing exception");
			e.printStackTrace();
		}

	}

	public void deleteItem(String primaryKey, String primaryKeyValue)
	{
		try
		{
			DeleteItemSpec deleteItemSpec = new DeleteItemSpec()
					.withPrimaryKey(primaryKey, primaryKeyValue)
					.withReturnValues(ReturnValue.ALL_OLD);
			// DeleteItemOutcome outcome =
			table.deleteItem(deleteItemSpec);
		}
		catch (Exception e)
		{
			System.out
					.println("Error deleting item in " + table.getTableName());
			e.printStackTrace();
		}
	}

}
