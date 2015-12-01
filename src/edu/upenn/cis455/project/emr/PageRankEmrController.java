package edu.upenn.cis455.project.emr;


public class PageRankEmrController
{

	public static void main(String[] args) throws InterruptedException
	{
		String emrInputPath = "s3://edu.upenn.cis455.project.url.test/";
		String emrOutputBucketName = "edu.upenn.cis455.project.pagerank";
		String emrOutputPrefix = "output";
		String clusterLogPath = "s3://edu.upenn.cis455.project.pagerank/log";
		String emrJarPath = "s3://edu.upenn.cis455.project.pagerank/jar/pagerank.jar";
		String emrStepName = "pagerank";
		String clusterId = "j-1QOPEV7EB7JFE";
		String clusterName = "page rank cluster";
		String ec2AccessKeyName = "test";
		String tableName = "edu.upenn.cis455.project.pagerank";
		String primaryKeyName = "hostName";
		String valueKeyName = "rank";
		/*EmrController controller = new EmrController(emrInputPath,
				emrOutputBucketName, emrOutputPrefix, emrJarPath, emrStepName,
				clusterId, tableName, "hostName", "rank");*/
		EmrController controller = new EmrController(emrInputPath,
				emrOutputBucketName, emrOutputPrefix, clusterLogPath,
				emrJarPath, emrStepName, clusterName, ec2AccessKeyName,
				tableName, primaryKeyName, valueKeyName);
		controller.createCluster();
		controller.setIterative(true);
		int i = 0;
		do
		{
			controller.setDone(true);
			controller.runJob();
			controller.s3ToDynamo(controller.getObjectNamesForBucket());
			System.out.println("Status after iterations " + (++i) + " - "
					+ controller.isDone());
		}
		while (!controller.isDone());
		controller.terminateCluster();
		System.out.println("Page rank terminated");
	}

}
