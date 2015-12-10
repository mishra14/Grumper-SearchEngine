package edu.upenn.cis455.project.emr;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.upenn.cis455.project.bean.DocumentRecord;

// TODO: Auto-generated Javadoc
/**
 * The Class IndexerEmrController.
 */
public class IndexerEmrController
{

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws JsonProcessingException the json processing exception
	 * @throws InterruptedException the interrupted exception
	 */
	public static void main(String[] args) throws NoSuchAlgorithmException,
			JsonProcessingException, InterruptedException
	{
		String documentBucketName = "edu.upenn.cis455.project.documents";
		String emrInputPath = "s3://test-indexer/";
		String emrOutputBucketName = "edu.upenn.cis455.project.indexer";
		String emrOutputPrefix = "output";
		String clusterLogPath = "s3://edu.upenn.cis455.project.indexer/log";
		String emrJarPath = "s3://edu.upenn.cis455.project.indexer/code/indexerUnigram.jar";
		String emrStepName = "indexer";
		String clusterId = "j-Q5KFD4DZEMVI";
		String clusterName = "indexer cluster";
		String ec2AccessKeyName = "test";
		String tableName = "IndexerTest";
		String primaryKeyName = "Word";
		String rangeKeyName = "Range";
		String valueKeyName = "Postings";
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
		/*controller.mergeCrawledDocuments(
				controller.getObjectNamesForBucket(documentBucketName),
				"edu.upenn.cis455.project.indexer.documents");*/
		controller.setIterative(false);
		// controller.runJob();
		controller.s3ToDynamoPostings(controller.getObjectNamesForBucket());
		/*List<String> docs = controller
				.getObjectNamesForBucket("edu.upenn.cis455.project.documents");
		System.out.println(docs.size());*/
		System.out.println("Indexer Terminated");
		System.out.println(new Date());
	}

}
