package edu.upenn.cis455.project.storage;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import edu.upenn.cis455.project.bean.DocumentRecord;
import edu.upenn.cis455.project.crawler.Hash;

public class S3DocumentDA
{
	private String bucketName;
	private String tableName;
	private DynamoDA<String> dynamo;
	private AmazonS3 s3client;

	public S3DocumentDA()
	{
		File file = new File("/usr/share/jetty/webapps/credentials");
		String accessKey=null;
		String secretKey=null;
		try
		{
			String line;
			FileReader reader = new FileReader(file);
			BufferedReader in = new BufferedReader(reader);
			while((line = in.readLine())!=null)
			{
				if(line.contains("AWSAccessKeyId"))
				{
					accessKey = line.split("=")[1].trim();
				}
				else if(line.contains("AWSSecretKey"))
				{
					secretKey = line.split("=")[1].trim();
				}
			}
			in.close();
			reader.close();
		}
		catch (FileNotFoundException e)
		{
			System.out.println("S3DocumentDA : reading from local credential file failed");
			e.printStackTrace();
		}
		catch (IOException e)
		{
			System.out.println("S3DocumentDA : reading from local credential file failed");
			e.printStackTrace();
		}
		
		this.bucketName = "edu.upenn.cis455.project.documents";
		this.tableName = "edu.upenn.cis455.project.documents";
		this.dynamo = new DynamoDA<String>(tableName, String.class);
		BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey,secretKey);
		this.s3client = new AmazonS3Client(awsCreds);
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
		String key = dynamo.getValue("documentUrl", url, "s3Key");
		if (key != null)
		{
			// then get document from s3
			try
			{
				S3Object s3Object = s3client.getObject(bucketName, key);
				byte[] bytes = new byte[(int) s3Object.getObjectMetadata()
						.getContentLength()];
				s3Object.getObjectContent().read(bytes);
				s3Object.getObjectContent().close();
				ObjectMapper mapper = new ObjectMapper();
				mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
				mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
				doc = mapper.readValue(new String(bytes, "utf-8"), DocumentRecord.class);
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
			ObjectMapper mapper = new ObjectMapper();
			mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
			mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
			ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(doc);
			ObjectMetadata omd = new ObjectMetadata();
			omd.setContentType("text/html");
			omd.setContentLength(json.length());
			ByteArrayInputStream inputStream = new ByteArrayInputStream(
					json.getBytes());
			PutObjectRequest request = new PutObjectRequest(bucketName, s3Key,
					inputStream, omd);
			s3client.putObject(request);
			// put the key into dynamo
			dynamo.putItem("documentUrl", doc.getDocumentId(), "s3Key", s3Key);
		}
		catch (NoSuchAlgorithmException e)
		{
			System.out.println("S3DocumentDA : Hashing exception");
			e.printStackTrace();
		}
		catch (JsonProcessingException e)
		{
			System.out
					.println("S3DocumentDA : json processing excecption exception");
			e.printStackTrace();
		}
	}

	public void deleteDocument(DocumentRecord doc)
	{

		// first get hash key from dynamo db
		dynamo.deleteItem("documentUrl", doc.getDocumentId());
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

	/*public static void main(String[] args) throws IOException
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
		System.out.println(s3.dynamo.getItem("documentUrl",doc.getDocumentId()));
		
		//Object to JSON in file
	}*/
}
