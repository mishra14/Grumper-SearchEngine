package edu.upenn.cis455.project.emr;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.core.JsonProcessingException;

public class IndexerEmrController
{
	private static AmazonS3Client s3Client = new AmazonS3Client();
	private static String projectDocumentBucket = "edu.upenn.cis455.project.documents";
	private static String indexerDocumentBucket = "edu.upenn.cis455.project.indexer.documents.large";
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
		System.out.println(new Date());
		List<String> documentsToMerge = getObjectNamesForBucket(projectDocumentBucket);
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
		List<String> docs = controller
				.getObjectNamesForBucket(projectDocumentBucket);
		System.out.println(docs.size());
		System.out.println("Indexer Terminated");
		System.out.println(new Date());
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

}
