package edu.upenn.cis455.project.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.json.JSONObject;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import edu.upenn.cis455.project.bean.DocumentRecord;
import edu.upenn.cis455.project.crawler.Hash;

public class S3DocumentDA
{
	private String bucketName;
	private String tableName;
	private DynamoDA dynamo;
	private AmazonS3 s3client;

	public S3DocumentDA()
	{
		this.bucketName = "edu.upenn.cis455.project.documents";
		this.tableName = "edu.upenn.cis455.project.documents";
		this.dynamo = new DynamoDA(tableName);
		this.s3client = new AmazonS3Client(new ProfileCredentialsProvider());
	}

	public boolean documentExists(DocumentRecord doc)
	{
		boolean result = true;
		try
		{
			String s3Key = Hash.hashKey(doc.getDocumentString());
			s3client.getObject(bucketName, s3Key);
		}
		catch (NoSuchAlgorithmException e)
		{
			System.out.println("S3DocumentDA : Hashing exception");
			e.printStackTrace();
		}
		catch (AmazonS3Exception ase)
		{
			// if (ase.getStatusCode() == 404) {
			result = false;
			// }
		}
		return result;
	}

	public DocumentRecord getDocument(String url)
	{
		DocumentRecord doc = null;
		// first get hash key from dynamo db
		Item item = dynamo.getItem(url);
		if (item != null)
		{
			// then get document from s3
			try
			{
				String key = (String) item.get("s3Key");
				S3Object s3Object = s3client.getObject(bucketName, key);
				byte[] bytes = new byte[(int) s3Object.getObjectMetadata()
						.getContentLength()];
				s3Object.getObjectContent().read(bytes);
				s3Object.getObjectContent().close();
				JSONObject json = new JSONObject(new String(bytes));
				doc = new DocumentRecord(url, json.getString("documentString"),
						json.getLong("lastCrawled"));
			}
			catch (AmazonS3Exception ase)
			{
				System.out.println("S3DocumentDA : document does not exist");
				ase.printStackTrace();
			}
			catch (IOException e)
			{
				System.out
						.println("S3DocumentDA : IOException while fetching document from S3");
				e.printStackTrace();
			}
		}
		return doc;
	}

	public void putDocument(DocumentRecord doc)
	{
		try
		{
			// hash url to get key
			String s3Key = Hash.hashKey(doc.getDocumentString());
			// put document into s3
			JSONObject docJson = doc.getJson();
			ObjectMetadata omd = new ObjectMetadata();
			omd.setContentType("text/html");
			omd.setContentLength(docJson.toString().length());
			ByteArrayInputStream inputStream = new ByteArrayInputStream(docJson
					.toString().getBytes());
			PutObjectRequest request = new PutObjectRequest(bucketName, s3Key,
					inputStream, omd);
			s3client.putObject(request);

			// put the key into dynamo
			dynamo.putItem(doc.getDocumentId(), s3Key);
		}
		catch (NoSuchAlgorithmException e)
		{
			System.out.println("S3DocumentDA : Hashing exception");
			e.printStackTrace();
		}
	}

	public void deleteDocument(DocumentRecord doc)
	{

		// first get hash key from dynamo db
		dynamo.deleteItem(doc.getDocumentId());
		try
		{
			s3client.deleteObject(bucketName,
					Hash.hashKey(doc.getDocumentString()));
		}
		catch (AmazonClientException | NoSuchAlgorithmException e)
		{
			System.out
					.println("S3DocumentDA : Exception while deleting document with id - "
							+ doc.getDocumentId());
			e.printStackTrace();
		}
	}

	/*public static void main(String[] args)
	{
		S3DocumentDA s3 = new S3DocumentDA();
		DocumentRecord doc = new DocumentRecord("http://ankitmishra.me",
				"This is a test document String", (new Date()).getTime());
		System.out.println(doc);
		System.out.println(s3.documentExists(doc));
		s3.putDocument(doc);
		System.out.println(s3.getDocument(doc.getDocumentId()));
		//s3.deleteDocument(doc);
		System.out.println(s3.documentExists(doc));
		System.out.println(s3.dynamo.getItem(doc.getDocumentId()));
	}*/
}
