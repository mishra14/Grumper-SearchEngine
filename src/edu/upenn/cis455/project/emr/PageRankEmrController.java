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

public class PageRankEmrController
{
	private static final int URLS_TO_MERGE = 100;
	private static AmazonS3Client s3Client = new AmazonS3Client();

	public static void main(String[] args) throws InterruptedException,
			NoSuchAlgorithmException, JsonProcessingException
	{
		String emrInputPath = "s3://edu.upenn.cis455.project.pagerank.urls/";
		String emrOutputBucketName = "edu.upenn.cis455.project.pagerank";
		String emrOutputPrefix = "output";
		String clusterLogPath = "s3://edu.upenn.cis455.project.pagerank/log";
		String emrJarPath = "s3://edu.upenn.cis455.project.pagerank/jar/pagerank.jar";
		String emrStepName = "pagerank";
		String clusterId = "j-115AWJCL45F9H";
		String clusterName = "page rank cluster";
		String ec2AccessKeyName = "test";
		String tableName = "edu.upenn.cis455.project.pagerank";
		String primaryKeyName = "hostName";
		String valueKeyName = "rank";
		EmrController controller = new EmrController(emrInputPath,
				emrOutputBucketName, emrOutputPrefix, emrJarPath, emrStepName,
				clusterId, tableName, primaryKeyName, valueKeyName);

		/*mergeUrlLists("edu.upenn.cis455.project.urls",
				"edu.upenn.cis455.project.pagerank.urls");
		EmrController controller = new EmrController(emrInputPath,
				emrOutputBucketName, emrOutputPrefix, clusterLogPath,
				emrJarPath, emrStepName, clusterName, ec2AccessKeyName,
				tableName, primaryKeyName, valueKeyName);
		controller.createCluster();*/
		controller.runJob();

		// controller.terminateCluster();
		List<String> docs = controller
				.getObjectNamesForBucket("edu.upenn.cis455.project.url");
		System.out.println(docs.size());
		System.out.println("Page rank terminated");
	}

	public static void mergeUrlLists(String inputBucketName,
			String outputBucketName) throws NoSuchAlgorithmException,
			JsonProcessingException
	{

		List<UrlList> mergedUrlLists = new ArrayList<UrlList>();
		int i = 0;
		for (String object : getObjectNamesForBucket(inputBucketName))
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

	public static String writeUrlLists(List<UrlList> mergedUrlLists,
			String bucketName) throws NoSuchAlgorithmException,
			JsonProcessingException
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

	public static UrlList getUrlList(String bucketName, String key)
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
			System.out.println("S3UrlListDA : document does not exist");
			ase.printStackTrace();
		}
		catch (IOException e)
		{
			System.out
					.println("S3UrlListDA : IOException while fetching document from S3");
			e.printStackTrace();
		}
		return urlList;
	}

	public static List<String> getObjectNamesForBucket(String bucketName)
	{
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
				.withBucketName(bucketName);
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

}
