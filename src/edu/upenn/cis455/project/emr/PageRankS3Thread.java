package edu.upenn.cis455.project.emr;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import edu.upenn.cis455.project.bean.UrlList;
import edu.upenn.cis455.project.crawler.Hash;

public class PageRankS3Thread extends Thread
{
	private static final int URLS_TO_MERGE = 150;
	private static AmazonS3Client s3Client = new AmazonS3Client();
	private String projectUrlBucket = "edu.upenn.cis455.project.urls";
	private String pageRankUrlBucket = "edu.upenn.cis455.project.pagerank.urls";
	private String pageRankBucket = "edu.upenn.cis455.project.pagerank";
	private String emrOutputBucketName = pageRankBucket;
	private String emrOutputPrefix = "output";

	public PageRankS3Thread(String projectUrlBucket, String pageRankUrlBucket,
			String pageRankBucket, String emrOutputBucketName,
			String emrOutputPrefix)
	{
		super();
		this.projectUrlBucket = projectUrlBucket;
		this.pageRankUrlBucket = pageRankUrlBucket;
		this.pageRankBucket = pageRankBucket;
		this.emrOutputBucketName = emrOutputBucketName;
		this.emrOutputPrefix = emrOutputPrefix;
	}

	public void run()
	{
		deleteEmrResults();
		deleteOldMergedUrls();
		try
		{
			mergeUrlLists(projectUrlBucket, pageRankUrlBucket);
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		catch (JsonProcessingException e)
		{
			e.printStackTrace();
		}
	}

	public List<String> getObjectNamesForBucket(String bucketName, String prefix)
	{
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
				.withBucketName(bucketName).withPrefix(prefix);
		ObjectListing objects = s3Client.listObjects(listObjectsRequest);
		List<String> objectNames = new ArrayList<String>(objects
				.getObjectSummaries().size());
		Iterator<S3ObjectSummary> objectIter = objects.getObjectSummaries()
				.iterator();
		while (objectIter.hasNext())
		{
			objectNames.add(objectIter.next().getKey());
		}
		while (objects.isTruncated())
		{
			objects = s3Client.listNextBatchOfObjects(objects);
			objectIter = objects.getObjectSummaries().iterator();
			while (objectIter.hasNext())
			{
				objectNames.add(objectIter.next().getKey());
			}
		}
		return objectNames;
	}

	public void deleteOldMergedUrls()
	{
		List<String> oldMergedUrls = getObjectNamesForBucket(pageRankUrlBucket,
				"");
		//System.out.println(oldMergedUrls);
		for (String mergedUrl : oldMergedUrls)
		{
			deleteObject(pageRankUrlBucket, mergedUrl);
		}
	}

	public void deleteEmrResults()
	{
		List<String> results = getObjectNamesForBucket(emrOutputBucketName,
				emrOutputPrefix);
		//System.out.println(results);
		for (String result : results)
		{
			deleteObject(emrOutputBucketName, result);
		}
	}

	public void deleteObject(String bucketName, String prefix)
	{
		try
		{
			DeleteObjectRequest req = new DeleteObjectRequest(bucketName,
					prefix);
			s3Client.deleteObject(req);
		}
		catch (AmazonS3Exception ase)
		{
			System.out.println("PageRankController : document does not exist");
			ase.printStackTrace();
		}
	}

	public void mergeUrlLists(String inputBucketName, String outputBucketName)
			throws NoSuchAlgorithmException, JsonProcessingException
	{

		List<UrlList> mergedUrlLists = new ArrayList<UrlList>();
		int i = 0;
		for (String object : getObjectNamesForBucket(inputBucketName, ""))
		{
			UrlList urlList = getUrlList(inputBucketName, object);

			mergedUrlLists.add(urlList);
			if (mergedUrlLists.size() > URLS_TO_MERGE)
			{
				// merge the list into 1 s3 object
				writeUrlLists(mergedUrlLists, outputBucketName);
				System.out.println("PageRankController : added " + (++i)
						+ " merged record");
				// clear the list
				mergedUrlLists.clear();
			}
		}
		if (mergedUrlLists.size() > 0)
		{
			// merge the list into 1 s3 object
			writeUrlLists(mergedUrlLists, outputBucketName);
			System.out.println("PageRankController : added " + (++i)
					+ " merged record");
			// clear the list
			mergedUrlLists.clear();
		}
	}

	public String writeUrlLists(List<UrlList> mergedUrlLists, String bucketName)
			throws NoSuchAlgorithmException, JsonProcessingException
	{

		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String json = ow.writeValueAsString(mergedUrlLists);
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

	public UrlList getUrlList(String bucketName, String key)
	{
		UrlList urlList = null;
		try
		{
			GetObjectRequest req = new GetObjectRequest(bucketName, key);
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
			urlList = mapper.readValue(s3Content.toString(), UrlList.class);
		}
		catch (AmazonS3Exception ase)
		{
			System.out.println("PageRankController : document does not exist");
			ase.printStackTrace();
		}
		catch (IOException e)
		{
			System.out
					.println("PageRankController : IOException while fetching document from S3");
			e.printStackTrace();
		}
		return urlList;
	}
}
