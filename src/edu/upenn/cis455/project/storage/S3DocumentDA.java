package edu.upenn.cis455.project.storage;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
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
			System.out
					.println("S3DocumentDA : reading from local credential file failed");
			e.printStackTrace();
		}
		catch (IOException e)
		{
			System.out
					.println("S3DocumentDA : reading from local credential file failed");
			e.printStackTrace();
		}

		this.bucketName = "edu.upenn.cis455.project.documents";
		this.tableName = "edu.upenn.cis455.project.documents";
		this.dynamo = new DynamoDA<String>(tableName, String.class);
		BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey,
				secretKey);
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
				GetObjectRequest req = new GetObjectRequest(bucketName, key);
				S3Object s3Object = s3client.getObject(req);
				InputStream objectData = s3Object.getObjectContent();
		        BufferedReader reader = new BufferedReader(new InputStreamReader(objectData));
		        StringBuilder s3Content = new StringBuilder();
		        String line;
				while((line=reader.readLine())!=null)
				{
					s3Content.append(line+"\r\n");
				}
				reader.close();
				objectData.close();
				ObjectMapper mapper = new ObjectMapper();
				mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
				mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
				doc = mapper.readValue(s3Content.toString(),
						DocumentRecord.class);
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
			ByteArrayInputStream inputStream = new ByteArrayInputStream(json.getBytes());
			ObjectMetadata omd = new ObjectMetadata();
			omd.setContentLength(json.getBytes().length);
			PutObjectRequest request = new PutObjectRequest(bucketName, s3Key,
					inputStream, omd);
			s3client.putObject(request);
			
			// put the key into dynamo
			dynamo.putItem("documentUrl", doc.getDocumentId(), "s3Key", s3Key);
			
			
			/*
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
			dynamo.putItem("documentUrl", doc.getDocumentId(), "s3Key", s3Key);*/
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

	public void deleteDocument(String url)
	{
		// first get hash key from dynamo db
		String key = dynamo.getValue("documentUrl", url, "s3Key");
		if (key != null)
		{
			dynamo.deleteItem("documentUrl", url);
			try
			{
				s3client.deleteObject(bucketName, key);
			}
			catch (AmazonClientException e)
			{
				System.out
						.println("S3DocumentDA : Exception while deleting document with id - "
								+ url);
				e.printStackTrace();
			}
		}
	}

	/*public static void main(String[] args) throws IOException, NoSuchAlgorithmException
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

		String wiki = "https://en.wikipedia.org/wiki/List_of_HTTP_status_codes#5xx_Server_Error";//"https://www.wikidata.org/wiki/Wikidata:Main_Page";
		// System.out.println(s3.dynamo.getItem("documentUrl", wiki));
		HttpClient client = new HttpClient(new Queue<String>());

		String documentString = client.fetch(wiki);
		DocumentRecord doc = new DocumentRecord(wiki, documentString, new Date().getTime());
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String json = ow.writeValueAsString(doc);
		//System.out.println(json);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(json.getBytes());
		ObjectMetadata omd = new ObjectMetadata();
		//omd.setContentType("text/html");
		//omd.setContentEncoding("base64");
		omd.setContentLength(json.getBytes().length);
		PutObjectRequest request = new PutObjectRequest(s3.bucketName, key,
				inputStream, omd);
		s3.s3client.putObject(request);
		s3.dynamo.putItem("documentUrl", doc.getDocumentId(), "s3Key", key);
		System.out.println(key);
		
		String key2 = s3.dynamo.getValue("documentUrl", wiki, "s3Key");
		System.out.println(key2);
		GetObjectRequest req = new GetObjectRequest(s3.bucketName, key2);
		S3Object s3Object = s3.s3client.getObject(req);
		InputStream objectData = s3Object.getObjectContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(objectData));
        StringBuilder s3Content = new StringBuilder();
        String line;
		while((line=reader.readLine())!=null)
		{
			s3Content.append(line+"\r\n");
		}
		out.write(s3Content.toString().getBytes());
		objectData.close();
		out.write("\n++++++++++++++++++++++++++++\n".getBytes());
		
		doc = mapper.readValue(s3Content.toString(), DocumentRecord.class);
	
		out.flush();
		out.close();
		
		s3.putDocument(doc);
		
		System.out.println(s3.getDocument(wiki));
		//System.out.println(value);
		//DocumentRecord d2 = mapper.readValue(value, DocumentRecord.class);
		//System.out.println(d2.getDocumentId());
		//System.out.println(d2.getDocumentString());
		ByteArrayInputStream inputStream = new ByteArrayInputStream(
				json.getBytes("UTF-8"));
		byte[] bytes = new byte[json.length()];
		inputStream.read(bytes);
		String temp = new String(bytes);
		System.out.println(temp);
		// System.out.println(json);
		ObjectMapper m2 = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		DocumentRecord d2 = m2.readValue(temp, DocumentRecord.class);
		// System.out.println(d2);
		// s3.putDocument(new DocumentRecord(wiki, doc, 1111111));
		// System.out.println(s3.getDocument(wiki));
		// System.out.println(doc);
		// Object to JSON in file
	}*/
}