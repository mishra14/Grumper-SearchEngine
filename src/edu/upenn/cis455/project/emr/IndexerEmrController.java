package edu.upenn.cis455.project.emr;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import edu.upenn.cis455.project.bean.DocumentRecord;

public class IndexerEmrController
{

	public static void main(String[] args) throws NoSuchAlgorithmException,
			JsonProcessingException, InterruptedException
	{
		String documentBucketName = "edu.upenn.cis455.project.documents";
		String emrInputPath = "s3://edu.upenn.cis455.project.indexer.documents";
		String emrOutputBucketName = "edu.upenn.cis455.project.indexer";
		String emrOutputPrefix = "output";
		String clusterLogPath = "s3://edu.upenn.cis455.project.indexer/log";
		String emrJarPath = "s3://edu.upenn.cis455.project.indexer/code/indexerdynamo.jar";
		String emrStepName = "indexer";
		String clusterId = "j-2NGSEPQ0WS3S8";
		String clusterName = "indexer cluster";
		String ec2AccessKeyName = "test";
		String tableName = "";
		String primaryKeyName = "";
		String valueKeyName = "";
		EmrController controller = new EmrController(emrInputPath,
				emrOutputBucketName, emrOutputPrefix, emrJarPath, emrStepName,
				clusterId, tableName, primaryKeyName, valueKeyName);
		/*EmrController controller = new EmrController(emrInputPath,
				emrOutputBucketName, emrOutputPrefix, clusterLogPath,
				emrJarPath, emrStepName, clusterName, ec2AccessKeyName,
				tableName, primaryKeyName, valueKeyName);
		controller.createCluster();*/
		controller.mergeCrawledDocuments(
				controller.getObjectNamesForBucket(documentBucketName),
				"edu.upenn.cis455.project.indexer.documents");
		//controller.runJob();
		List<String> docs = controller
				.getObjectNamesForBucket("edu.upenn.cis455.project.documents");
		System.out.println(docs.size());
		System.out.println("Indexer Terminated");
	}

}
