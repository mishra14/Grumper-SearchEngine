package edu.upenn.cis455.project.emr;

import java.security.NoSuchAlgorithmException;
import com.fasterxml.jackson.core.JsonProcessingException;

// TODO: Auto-generated Javadoc
/**
 * The Class PageRankEmrController.
 */
public class PageRankEmrController
{
	
	/** The project url bucket. */
	private static String projectUrlBucket = "edu.upenn.cis455.project.urls";
	
	/** The page rank url bucket. */
	private static String pageRankUrlBucket = "edu.upenn.cis455.project.pagerank.urls";
	
	/** The page rank bucket. */
	private static String pageRankBucket = "edu.upenn.cis455.project.pagerank";
	
	/** The emr input path. */
	private static String emrInputPath = "s3://" + pageRankUrlBucket + "/";
	
	/** The emr output bucket name. */
	private static String emrOutputBucketName = pageRankBucket;
	
	/** The emr output prefix. */
	private static String emrOutputPrefix = "output";
	
	/** The cluster log path. */
	private static String clusterLogPath = "s3://" + pageRankBucket + "/log";
	
	/** The emr jar path. */
	private static String emrJarPath = "s3://" + pageRankBucket
			+ "/jar/pagerank.jar";
	
	/** The emr step name. */
	private static String emrStepName = "pagerank";
	
	/** The cluster id. */
	private static String clusterId = "j-115AWJCL45F9H";
	
	/** The cluster name. */
	private static String clusterName = "page rank cluster";
	
	/** The ec2 access key name. */
	private static String ec2AccessKeyName = "test";
	
	/** The table name. */
	private static String tableName = pageRankBucket;
	
	/** The primary key name. */
	private static String primaryKeyName = "hostName";
	
	/** The value key name. */
	private static String valueKeyName = "rank";

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws InterruptedException the interrupted exception
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws JsonProcessingException the json processing exception
	 */
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
		// start the 3 threads
		controllerThread.start();
		s3Thread.start();
		dynamoThread.start();
		// wait for the 3 threads to finish
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
		// controller.terminateCluster();
		System.out.println("PageRank Controller : PageRank finished");
	}

}
