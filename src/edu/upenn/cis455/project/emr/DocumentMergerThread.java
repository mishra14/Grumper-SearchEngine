package edu.upenn.cis455.project.emr;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import edu.upenn.cis455.project.bean.DocumentRecord;
import edu.upenn.cis455.project.crawler.Hash;

public class DocumentMergerThread extends Thread
{

	private static final int DOCUMENTS_TO_MERGE = 150;
	private static final long MAX_DOCUMENT_SIZE = 60000000;
	private int id;
	private String projectDocumentBucket;
	private String mergedDocumentBucket;
	private List<String> objectNames;
	private AmazonS3Client s3Client;

	public DocumentMergerThread(String projectDocumentBucket,
			String mergedDocumentBucket, List<String> objectNames, int id)
	{
		super();
		this.projectDocumentBucket = projectDocumentBucket;
		this.mergedDocumentBucket = mergedDocumentBucket;
		this.objectNames = objectNames;
		this.s3Client = new AmazonS3Client(
				IndexerEmrController.getCredentials());
		this.id = id;
	}

	public void run()
	{
		System.out.println("Merger Thread " + id + " : started");
		try
		{
			mergeCrawledDocuments();
		}
		catch (NoSuchAlgorithmException e)
		{
			System.out.println("Merger Thread " + id + " : Exception");
			e.printStackTrace();
		}
		catch (JsonProcessingException e)
		{
			System.out.println("Merger Thread " + id + " : Exception");
			e.printStackTrace();
		}
		System.out.println("Merger Thread " + id + " : done");
	}

	public void mergeCrawledDocuments() throws NoSuchAlgorithmException,
			JsonProcessingException
	{
		long size = 0;
		List<DocumentRecord> mergedDocuments = new ArrayList<DocumentRecord>();
		for (String object : objectNames)
		{
			DocumentRecord doc = getDocument(projectDocumentBucket, object);
			size += doc.toString().length();
			mergedDocuments.add(doc);
			if (size > MAX_DOCUMENT_SIZE)
			{
				System.out.println("Merger Thread " + id + " : Merged "
						+ mergedDocuments.size() + " documents with size - "
						+ size);
				// merge the list into 1 s3 object
				writeDocuments(mergedDocuments, mergedDocumentBucket);
				// clear the list
				mergedDocuments.clear();
				// clear size
				size = 0;
			}
		}
		if (mergedDocuments.size() > 0)
		{
			// merge the list into 1 s3 object
			writeDocuments(mergedDocuments, mergedDocumentBucket);
			// clear the list
			mergedDocuments.clear();
		}
	}

	public DocumentRecord getDocument(String bucketName, String prefix)
	{
		DocumentRecord doc = null;
		try
		{
			GetObjectRequest req = new GetObjectRequest(bucketName, prefix);
			S3Object s3Object = s3Client.getObject(req);
			InputStream objectData = s3Object.getObjectContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					objectData));
			StringBuilder s3Content = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null)
			{
				s3Content.append(line + "\r\n");
			}
			reader.close();
			objectData.close();
			ObjectMapper mapper = new ObjectMapper();
			mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
			mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
			doc = mapper.readValue(s3Content.toString(), DocumentRecord.class);
		}
		catch (AmazonS3Exception ase)
		{
			System.out.println("Merger Thread " + id
					+ " : document does not exist");
			ase.printStackTrace();
		}
		catch (IOException e)
		{
			System.out.println("Merger Thread " + id
					+ " : IOException while fetching document from S3");
			e.printStackTrace();
		}
		return doc;
	}

	public String writeDocuments(List<DocumentRecord> mergedDocuments,
			String bucketName) throws NoSuchAlgorithmException,
			JsonProcessingException
	{

		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String json = ow.writeValueAsString(mergedDocuments);
		System.out.println("json size - " + json.length());
		String s3Key = Hash.hashKey(json);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(
				json.getBytes());
		ObjectMetadata omd = new ObjectMetadata();
		omd.setContentLength(json.getBytes().length);
		PutObjectRequest request = new PutObjectRequest(bucketName, s3Key,
				inputStream, omd);
		s3Client.putObject(request);
		return s3Key;
	}

}
