package edu.upenn.cis455.project.emr;

import java.security.NoSuchAlgorithmException;
import com.fasterxml.jackson.core.JsonProcessingException;

public class PageRankEmrController
{
	private static String projectUrlBucket = "edu.upenn.cis455.project.urls";
	private static String pageRankUrlBucket = "edu.upenn.cis455.project.pagerank.urls";
	private static String pageRankBucket = "edu.upenn.cis455.project.pagerank";
	private static String emrInputPath = "s3://" + pageRankUrlBucket + "/";
	private static String emrOutputBucketName = pageRankBucket;
	private static String emrOutputPrefix = "output";
	private static String clusterLogPath = "s3://" + pageRankBucket + "/log";
	private static String emrJarPath = "s3://" + pageRankBucket
			+ "/jar/pagerank.jar";
	private static String emrStepName = "pagerank";
	private static String clusterId = "j-115AWJCL45F9H";
	private static String clusterName = "page rank cluster";
	private static String ec2AccessKeyName = "test";
	private static String tableName = pageRankBucket;
	private static String primaryKeyName = "hostName";
	private static String valueKeyName = "rank";

	public static void main(String[] args) throws InterruptedException,
			NoSuchAlgorithmException, JsonProcessingException
	{

		EmrController controller = new EmrController(emrInputPath,
				emrOutputBucketName, emrOutputPrefix, clusterLogPath,
				emrJarPath, emrStepName, clusterName, ec2AccessKeyName,
				tableName, primaryKeyName, valueKeyName);

		/*EmrController controller = new EmrController(emrInputPath,
				emrOutputBucketName, emrOutputPrefix, emrJarPath, emrStepName,
				clusterId, tableName, primaryKeyName, valueKeyName);*/

		PageRankControllerThread controllerThread = new PageRankControllerThread(
				controller);
		PageRankS3Thread s3Thread = new PageRankS3Thread(projectUrlBucket,
				pageRankUrlBucket, pageRankBucket, emrOutputBucketName,
				emrOutputPrefix);
		PageRankDynamoThread dynamoThread = new PageRankDynamoThread(tableName,
				10L, 500L);
		//start the 3 threads
		controllerThread.start();
		s3Thread.start();
		dynamoThread.start();
		//wait for the 3 threads to finish
		dynamoThread.join();
		System.out.println("PageRank Controller : Dynamo Thread done");
		s3Thread.join();
		System.out.println("PageRank Controller : S3 Thread done");
		controllerThread.join();
		System.out.println("PageRank Controller : Controller Thread done");

		// start job
		controller.runJob();
		dynamoThread.updateTableWriteCapacity(10L, 10L);
		System.out
				.println("PageRank Controller : pageRank table capacity updated");
		// terminate cluster
		//controller.terminateCluster();
		System.out.println("PageRank Controller : PageRank finished");
	}

}
