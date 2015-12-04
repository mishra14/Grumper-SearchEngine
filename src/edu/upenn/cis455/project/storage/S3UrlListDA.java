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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
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

import edu.upenn.cis455.project.bean.UrlList;
import edu.upenn.cis455.project.crawler.Hash;

public class S3UrlListDA
{
	private String bucketName;
	private AmazonS3 s3client;

	public S3UrlListDA()
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
					.println("S3UrlListDA : reading from local credential file failed");
			e.printStackTrace();
		}
		catch (IOException e)
		{
			System.out
					.println("S3UrlListDA : reading from local credential file failed");
			e.printStackTrace();
		}

		this.bucketName = "edu.upenn.cis455.project.urls";
		BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey,
				secretKey);
		this.s3client = new AmazonS3Client(awsCreds);
	}

	public boolean urlListExists(UrlList urlList)
	{
		boolean result = true;
		try
		{
			String key = Hash.hashKey(urlList.getParentUrl());
			if (key == null)
			{
				System.out.println("Key is null");
				result = false;
			}
		}
		catch (AmazonS3Exception ase)
		{
			// if (ase.getStatusCode() == 404) {
			result = false;
			// }
		}
		catch (NoSuchAlgorithmException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public UrlList getUrlList(String url)
	{
		UrlList urlList = null;
		try
		{
			String key = Hash.hashKey(url);
			GetObjectRequest req = new GetObjectRequest(bucketName, key);
			S3Object s3Object = s3client.getObject(req);
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
			urlList = mapper.readValue(s3Content.toString(), UrlList.class);
		}
		catch (AmazonS3Exception ase)
		{
			System.out.println("S3UrlListDA : document does not exist");
//			ase.printStackTrace();
		}
		catch (IOException e)
		{
			System.out
					.println("S3UrlListDA : IOException while fetching document from S3");
			e.printStackTrace();
		}
		catch (NoSuchAlgorithmException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return urlList;
	}

	public void putUrlList(UrlList urlList)
	{
		try
		{
			// hash url to get key
			String s3Key = Hash.hashKey(urlList.getParentUrl());

			// put document into s3
			ObjectMapper mapper = new ObjectMapper();
			mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
			mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
			ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(urlList);
			ByteArrayInputStream inputStream = new ByteArrayInputStream(
					json.getBytes());
			ObjectMetadata omd = new ObjectMetadata();
			omd.setContentLength(json.getBytes().length);
			PutObjectRequest request = new PutObjectRequest(bucketName, s3Key,
					inputStream, omd);
			s3client.putObject(request);

			// No need to put into dynamo as it should already be there

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
		catch (JsonProcessingException e)
		{
			System.out
					.println("S3UrlListDA : json processing excecption exception");
			e.printStackTrace();
		}
		catch (NoSuchAlgorithmException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void deleteUrlList(UrlList urlList)
	{

		// first get hash key from dynamo db
		try
		{
			String s3Key = Hash.hashKey(urlList.getParentUrl());
			s3client.deleteObject(bucketName, s3Key);
		}
		catch (AmazonClientException e)
		{
			System.out
					.println("S3UrlListDA : Exception while deleting document with id - "
							+ urlList.getParentUrl());
			e.printStackTrace();
		}
		catch (NoSuchAlgorithmException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException,
			NoSuchAlgorithmException
	{
		S3UrlListDA s3 = new S3UrlListDA();
		String parentUrl = "money.usnews.com";
		Set<String> urls = new HashSet<String>();
		urls.add("www.google.com");
		urls.add("www.amazon.com");
		UrlList urlList = new UrlList(parentUrl, urls, true,
				new Date().getTime());
//		s3.putUrlList(urlList);
//		System.out.println(s3.getUrlList(parentUrl));
		
		System.out.println(s3.urlListExists(urlList));
	}
}
