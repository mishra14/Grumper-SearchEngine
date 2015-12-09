package edu.upenn.cis455.project.emr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.BatchWriteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.TableWriteItems;
import com.amazonaws.services.dynamodbv2.model.WriteRequest;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.core.JsonProcessingException;

import edu.upenn.cis455.project.bean.EmrResult;
import edu.upenn.cis455.project.crawler.Hash;
import edu.upenn.cis455.project.storage.S3EmrDA;

public class IndexerEmrController
{
	private static AmazonS3Client s3Client = new AmazonS3Client(
			getCredentials());
	private static String projectDocumentBucket = "edu.upenn.cis455.project.documents";
	private static String indexerDocumentBucket = "edu.upenn.cis455.project.indexer.documents.large2";
	private static String emrInputPath = "s3://test-indexer/";
	private static String emrOutputBucketName = "edu.upenn.cis455.project.indexer";
	private static String emrOutputPrefix = "output";
	private static String clusterLogPath = "s3://edu.upenn.cis455.project.indexer/log";
	private static String emrJarPath = "s3://edu.upenn.cis455.project.indexer/code/indexerUnigram.jar";
	private static String emrStepName = "indexer";
	private static String clusterId = "j-Q5KFD4DZEMVI";
	private static String clusterName = "indexer cluster";
	private static String ec2AccessKeyName = "test";
	private static String tableName = "IndexerTest";
	private static String primaryKeyName = "Word";
	private static String rangeKeyName = "Range";
	private static String valueKeyName = "Postings";

	public static void main(String[] args) throws NoSuchAlgorithmException,
			JsonProcessingException, InterruptedException
	{

		EmrController controller = new EmrController(emrInputPath,
				emrOutputBucketName, emrOutputPrefix, emrJarPath, emrStepName,
				clusterId, tableName, primaryKeyName, valueKeyName,
				rangeKeyName);
		/*EmrController controller = new EmrController(emrInputPath,
				emrOutputBucketName, emrOutputPrefix, clusterLogPath,
				emrJarPath, emrStepName, clusterName, ec2AccessKeyName,
				tableName, primaryKeyName, valueKeyName, rangeKeyName);
		controller.createCluster();*/
		List<String> docs = getObjectNamesForBucket(indexerDocumentBucket);
		System.out.println(docs.size());
		System.out.println(new Date());
		/*List<String> documentsToMerge = getObjectNamesForBucket(projectDocumentBucket);
		List<DocumentMergerThread> mergerThreads = new ArrayList<DocumentMergerThread>();
		int i = 0;
		final int DOCS_TO_MERGE = 30000;
		while ((i + DOCS_TO_MERGE) < documentsToMerge.size())
		{
			DocumentMergerThread mergerThread = new DocumentMergerThread(
					projectDocumentBucket, indexerDocumentBucket,
					documentsToMerge.subList(i, i + DOCS_TO_MERGE), i);
			mergerThreads.add(mergerThread);
			mergerThread.start();
			i = i + DOCS_TO_MERGE;
		}
		if (i < documentsToMerge.size())
		{
			DocumentMergerThread mergerThread = new DocumentMergerThread(
					projectDocumentBucket, indexerDocumentBucket,
					documentsToMerge.subList(i, documentsToMerge.size()), i);
			mergerThreads.add(mergerThread);
			mergerThread.start();
		}
		for (DocumentMergerThread mergerThread : mergerThreads)
		{
			mergerThread.join();
		}
		// controller.runJob();
		// controller.s3ToDynamoPostings(controller.getObjectNamesForBucket());
		//List<String> docs = getObjectNamesForBucket(projectDocumentBucket);
		System.out.println(docs.size());*/
		moveEmrResultToDynamo();
		System.out.println("Indexer Terminated");
		System.out.println(new Date());
	}

	public static void moveEmrResultToDynamo()
	{
		try
		{
			String folderPath = "./documents";
			String emrResultsBucket = "edu.upenn.cis455.project.indexer.indexed.large/unigram";
			int result = 0;// syncBucket(emrResultsBucket, folderPath);
			if (result == 0)
			{
				System.out.println("Indexer Controller : Syncing done");
				int i = 0;
				final int FILES_PER_THREAD = 20;
				List<ResultUploaderThread> uploaderThreads = new ArrayList<ResultUploaderThread>();
				File resultFolder = new File(folderPath);
				List<File> files = new ArrayList<File>();
				for (File file : resultFolder.listFiles())
				{
					files.add(file);

				}
				while ((i + FILES_PER_THREAD) < resultFolder.listFiles().length)
				{
					ResultUploaderThread uploaderThread = new ResultUploaderThread(
							tableName, primaryKeyName, rangeKeyName,
							valueKeyName, new ArrayList<File>(
									Arrays.asList(Arrays.copyOfRange(
											resultFolder.listFiles(), i, i
													+ FILES_PER_THREAD))), i);
					uploaderThreads.add(uploaderThread);
					uploaderThread.start();
					i = i + FILES_PER_THREAD;
				}
				if (i < resultFolder.listFiles().length)
				{
					ResultUploaderThread uploaderThread = new ResultUploaderThread(
							tableName, primaryKeyName, rangeKeyName,
							valueKeyName, new ArrayList<File>(
									Arrays.asList(Arrays.copyOfRange(
											resultFolder.listFiles(), i, i
													+ FILES_PER_THREAD))), i);
					uploaderThreads.add(uploaderThread);
					uploaderThread.start();
				}
				for (ResultUploaderThread uploaderThread : uploaderThreads)
				{
					uploaderThread.join();
				}

			}
			else
			{
				System.out.println("Indexer Controller : Syncing errors");
			}

		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	public static int syncBucket(String bucketName, String path)
			throws IOException, InterruptedException
	{
		resetDirectory(path);
		File script = new File(path + "/runsort.sh");
		if (!script.exists())
		{
			script.createNewFile();
			FileWriter writer = new FileWriter(script);
			writer.append("aws s3 sync s3://" + bucketName + "/ " + path
					+ " --region us-east-1");
			writer.close();
			script.setExecutable(true);
		}
		Runtime runtime = Runtime.getRuntime();
		Process p = runtime.exec(script.getAbsolutePath());// pb.start();
		int result = p.waitFor();
		return result;
	}

	public static List<String> getObjectNamesForBucket(String bucketName)
	{
		long count = 0;
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
				.withBucketName(bucketName);
		ObjectListing objects = s3Client.listObjects(listObjectsRequest);
		List<String> objectNames = new ArrayList<String>(objects
				.getObjectSummaries().size());
		Iterator<S3ObjectSummary> objectIter = objects.getObjectSummaries()
				.iterator();
		while (objectIter.hasNext())
		{
			count++;
			objectNames.add(objectIter.next().getKey());
		}
		while (objects.isTruncated())
		{
			objects = s3Client.listNextBatchOfObjects(objects);
			objectIter = objects.getObjectSummaries().iterator();
			while (objectIter.hasNext())
			{
				count++;
				objectNames.add(objectIter.next().getKey());
			}
		}
		System.out.println("count - " + count);
		return objectNames;
	}

	public static AWSCredentials getCredentials()
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
		BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey,
				secretKey);
		return awsCredentials;
	}

	public static void resetDirectory(String path)
	{
		File outputDir = new File(path);
		if (outputDir.exists())
		{
			if (outputDir.isDirectory())
			{
				for (File file : outputDir.listFiles())
				{
					file.delete();
				}
			}
		}
		else
		{
			if (!outputDir.exists() || !outputDir.isDirectory())
			{
				outputDir.mkdirs();
			}
		}
	}

}
