package edu.upenn.cis455.project.emr;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.BatchGetItemOutcome;
import com.amazonaws.services.dynamodbv2.document.BatchWriteItemOutcome;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.TableKeysAndAttributes;
import com.amazonaws.services.dynamodbv2.document.TableWriteItems;
import com.amazonaws.services.dynamodbv2.model.KeysAndAttributes;
import com.amazonaws.services.dynamodbv2.model.WriteRequest;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClient;
import com.amazonaws.services.elasticmapreduce.model.ActionOnFailure;
import com.amazonaws.services.elasticmapreduce.model.AddJobFlowStepsRequest;
import com.amazonaws.services.elasticmapreduce.model.AddJobFlowStepsResult;
import com.amazonaws.services.elasticmapreduce.model.DescribeClusterRequest;
import com.amazonaws.services.elasticmapreduce.model.DescribeClusterResult;
import com.amazonaws.services.elasticmapreduce.model.DescribeStepRequest;
import com.amazonaws.services.elasticmapreduce.model.DescribeStepResult;
import com.amazonaws.services.elasticmapreduce.model.HadoopJarStepConfig;
import com.amazonaws.services.elasticmapreduce.model.JobFlowInstancesConfig;
import com.amazonaws.services.elasticmapreduce.model.RunJobFlowRequest;
import com.amazonaws.services.elasticmapreduce.model.RunJobFlowResult;
import com.amazonaws.services.elasticmapreduce.model.StepConfig;
import com.amazonaws.services.elasticmapreduce.model.TerminateJobFlowsRequest;
import com.amazonaws.services.elasticmapreduce.util.StepFactory;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.MultiObjectDeleteException;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.MultiObjectDeleteException.DeleteError;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import edu.upenn.cis455.project.bean.DocumentRecord;
import edu.upenn.cis455.project.bean.EmrResult;
import edu.upenn.cis455.project.crawler.Hash;
import edu.upenn.cis455.project.storage.S3EmrDA;

// TODO: Auto-generated Javadoc
/**
 * The Class EmrController.
 */
public class EmrController
{
	
	/** The Constant MAX_LIST_SIZE. */
	private static final int MAX_LIST_SIZE = 25;
	
	/** The Constant DELTA. */
	private static final Double DELTA = 0.00001;
	
	/** The Constant DOCUMENTS_TO_MERGE. */
	private static final int DOCUMENTS_TO_MERGE = 50;
	
	/** The Constant MAX_POSTINGS_SIZE. */
	private static final int MAX_POSTINGS_SIZE = 80;
	
	/** The emr input path. */
	private String emrInputPath;
	
	/** The emr output bucket name. */
	private String emrOutputBucketName;
	
	/** The emr output prefix. */
	private String emrOutputPrefix;
	
	/** The cluster log path. */
	private String clusterLogPath;
	
	/** The emr jar path. */
	private String emrJarPath;
	
	/** The emr step name. */
	private String emrStepName;
	
	/** The cluster id. */
	private String clusterId;
	
	/** The cluster name. */
	private String clusterName;
	
	/** The ec2 access key name. */
	private String ec2AccessKeyName;
	
	/** The s3 client. */
	private AmazonS3Client s3Client;
	
	/** The table name. */
	private String tableName;
	
	/** The primary key name. */
	private String primaryKeyName;
	
	/** The value key name. */
	private String valueKeyName;
	
	/** The range key name. */
	private String rangeKeyName;
	
	/** The dynamo. */
	private DynamoDB dynamo;
	
	/** The iterative. */
	private boolean iterative;
	
	/** The done. */
	private boolean done;

	// this.dynamo = new DynamoDA<>(tableName, dynamoReturnClass);

	/**
	 * Instantiates a new emr controller.
	 *
	 * @param emrInputPath the emr input path
	 * @param emrOutputBucketName the emr output bucket name
	 * @param emrOutputPrefix the emr output prefix
	 * @param clusterLogPath the cluster log path
	 * @param emrJarPath the emr jar path
	 * @param emrStepName the emr step name
	 * @param clusterName the cluster name
	 * @param ec2AccessKeyName the ec2 access key name
	 * @param tableName the table name
	 * @param primaryKeyName the primary key name
	 * @param valueKeyName the value key name
	 */
	public EmrController(String emrInputPath, String emrOutputBucketName,
			String emrOutputPrefix, String clusterLogPath, String emrJarPath,
			String emrStepName, String clusterName, String ec2AccessKeyName,
			String tableName, String primaryKeyName, String valueKeyName)
	{
		super();
		this.emrInputPath = emrInputPath;
		this.emrOutputBucketName = emrOutputBucketName;
		this.emrOutputPrefix = emrOutputPrefix;
		this.clusterLogPath = clusterLogPath;
		this.emrJarPath = emrJarPath;
		this.emrStepName = emrStepName;
		this.clusterName = clusterName;
		this.ec2AccessKeyName = ec2AccessKeyName;
		this.tableName = tableName;
		this.s3Client = new AmazonS3Client();
		this.primaryKeyName = primaryKeyName;
		this.valueKeyName = valueKeyName;
		this.dynamo = new DynamoDB(new AmazonDynamoDBClient());
		this.iterative = false;
		this.done = true;
	}

	/**
	 * Instantiates a new emr controller.
	 *
	 * @param emrInputPath the emr input path
	 * @param emrOutputBucketName the emr output bucket name
	 * @param emrOutputPrefix the emr output prefix
	 * @param emrJarPath the emr jar path
	 * @param emrStepName the emr step name
	 * @param clusterId the cluster id
	 * @param tableName the table name
	 * @param primaryKeyName the primary key name
	 * @param valueKeyName the value key name
	 */
	public EmrController(String emrInputPath, String emrOutputBucketName,
			String emrOutputPrefix, String emrJarPath, String emrStepName,
			String clusterId, String tableName, String primaryKeyName,
			String valueKeyName)
	{
		super();
		this.emrInputPath = emrInputPath;
		this.emrOutputBucketName = emrOutputBucketName;
		this.emrOutputPrefix = emrOutputPrefix;
		this.emrJarPath = emrJarPath;
		this.emrStepName = emrStepName;
		this.clusterId = clusterId;
		this.tableName = tableName;
		this.s3Client = new AmazonS3Client();
		this.primaryKeyName = primaryKeyName;
		this.valueKeyName = valueKeyName;
		this.dynamo = new DynamoDB(new AmazonDynamoDBClient());
		this.iterative = false;
		this.done = true;
	}

	/**
	 * Instantiates a new emr controller.
	 *
	 * @param emrInputPath the emr input path
	 * @param emrOutputBucketName the emr output bucket name
	 * @param emrOutputPrefix the emr output prefix
	 * @param clusterLogPath the cluster log path
	 * @param emrJarPath the emr jar path
	 * @param emrStepName the emr step name
	 * @param clusterName the cluster name
	 * @param ec2AccessKeyName the ec2 access key name
	 * @param tableName the table name
	 * @param primaryKeyName the primary key name
	 * @param valueKeyName the value key name
	 * @param rangeKeyName the range key name
	 */
	public EmrController(String emrInputPath, String emrOutputBucketName,
			String emrOutputPrefix, String clusterLogPath, String emrJarPath,
			String emrStepName, String clusterName, String ec2AccessKeyName,
			String tableName, String primaryKeyName, String valueKeyName,
			String rangeKeyName)
	{
		super();
		this.emrInputPath = emrInputPath;
		this.emrOutputBucketName = emrOutputBucketName;
		this.emrOutputPrefix = emrOutputPrefix;
		this.clusterLogPath = clusterLogPath;
		this.emrJarPath = emrJarPath;
		this.emrStepName = emrStepName;
		this.clusterName = clusterName;
		this.ec2AccessKeyName = ec2AccessKeyName;
		this.tableName = tableName;
		this.s3Client = new AmazonS3Client();
		this.primaryKeyName = primaryKeyName;
		this.valueKeyName = valueKeyName;
		this.dynamo = new DynamoDB(new AmazonDynamoDBClient());
		this.iterative = false;
		this.done = true;
		this.rangeKeyName = rangeKeyName;
	}

	/**
	 * Instantiates a new emr controller.
	 *
	 * @param emrInputPath the emr input path
	 * @param emrOutputBucketName the emr output bucket name
	 * @param emrOutputPrefix the emr output prefix
	 * @param emrJarPath the emr jar path
	 * @param emrStepName the emr step name
	 * @param clusterId the cluster id
	 * @param tableName the table name
	 * @param primaryKeyName the primary key name
	 * @param valueKeyName the value key name
	 * @param rangeKeyName the range key name
	 */
	public EmrController(String emrInputPath, String emrOutputBucketName,
			String emrOutputPrefix, String emrJarPath, String emrStepName,
			String clusterId, String tableName, String primaryKeyName,
			String valueKeyName, String rangeKeyName)
	{
		super();
		this.emrInputPath = emrInputPath;
		this.emrOutputBucketName = emrOutputBucketName;
		this.emrOutputPrefix = emrOutputPrefix;
		this.emrJarPath = emrJarPath;
		this.emrStepName = emrStepName;
		this.clusterId = clusterId;
		this.tableName = tableName;
		this.s3Client = new AmazonS3Client();
		this.primaryKeyName = primaryKeyName;
		this.valueKeyName = valueKeyName;
		this.dynamo = new DynamoDB(new AmazonDynamoDBClient());
		this.iterative = false;
		this.done = true;
		this.rangeKeyName = rangeKeyName;
	}

	/**
	 * Run job.
	 *
	 * @return true, if successful
	 * @throws InterruptedException the interrupted exception
	 */
	public boolean runJob() throws InterruptedException
	{

		boolean success = true;
		AWSCredentials awsCredentais = getCredentials();

		AmazonElasticMapReduce client = new AmazonElasticMapReduceClient(
				awsCredentais);
		List<String> args = new ArrayList<String>();
		args.add(emrInputPath);
		args.add("s3://" + emrOutputBucketName + "/" + emrOutputPrefix);
		HadoopJarStepConfig pageRankConfig = new HadoopJarStepConfig().withJar(
				emrJarPath).withArgs(args);
		StepConfig customStep = new StepConfig(emrStepName, pageRankConfig)
				.withActionOnFailure(ActionOnFailure.CANCEL_AND_WAIT);

		AddJobFlowStepsResult result = client
				.addJobFlowSteps(new AddJobFlowStepsRequest().withJobFlowId(
						clusterId).withSteps(customStep));
		System.out.println(result.getStepIds());
		System.out.println("waiting on job completion...");
		DescribeStepRequest stepRequest = new DescribeStepRequest();
		stepRequest.setClusterId(clusterId);
		stepRequest.setStepId(result.getStepIds().get(0));
		DescribeStepResult stepResult;
		while (true)
		{
			stepResult = client.describeStep(stepRequest);
			System.out.println(stepResult.getStep().getStatus());
			if (stepResult.getStep().getStatus().getState().equals("PENDING")
					|| stepResult.getStep().getStatus().getState()
							.equals("RUNNING"))
			{
				Thread.sleep(10000);
			}
			else
			{
				if (stepResult.getStep().getStatus().getState()
						.equals("FAILED"))
				{
					success = false;
				}
				break;
			}
		}
		System.out.println("Step completed with success = " + success);
		return success;
	}

	/**
	 * Gets the credentials.
	 *
	 * @return the credentials
	 */
	private AWSCredentials getCredentials()
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

	/**
	 * Gets the object names for bucket.
	 *
	 * @return the object names for bucket
	 */
	public List<String> getObjectNamesForBucket()
	{
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
				.withBucketName(emrOutputBucketName)
				.withPrefix(emrOutputPrefix);
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
		// System.out.println(objectNames);
		return objectNames;
	}

	/**
	 * Gets the object names for bucket.
	 *
	 * @param bucketName the bucket name
	 * @return the object names for bucket
	 */
	public List<String> getObjectNamesForBucket(String bucketName)
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

	/**
	 * S3 to dynamo.
	 *
	 * @param objectNames the object names
	 * @return the list
	 */
	public List<EmrResult> s3ToDynamo(List<String> objectNames)
	{
		List<EmrResult> results = new ArrayList<EmrResult>();
		S3EmrDA s3 = new S3EmrDA(emrOutputBucketName);
		for (String object : objectNames)
		{
			List<EmrResult> resultsInDocument = s3.getEmrResult(object);
			results.addAll(resultsInDocument);
			if (results.size() > MAX_LIST_SIZE)
			{
				// System.out.println(results);
				List<EmrResult> resultsToBeWritten = results.subList(0, 24);
				batchWriteEmrResults(resultsToBeWritten);
				results.removeAll(resultsToBeWritten);
			}
			// remove that document from s3
			s3.deleteEmrResult(object);
		}
		if (results.size() > 0)
		{
			// System.out.println(results);
			batchWriteEmrResults(results);
		}
		// delete the output folder now
		s3.deleteEmrResult(emrOutputPrefix);
		return results;
	}

	/**
	 * S3 to dynamo postings.
	 *
	 * @param objectNames the object names
	 * @return the list
	 */
	public List<EmrResult> s3ToDynamoPostings(List<String> objectNames)
	{
		List<EmrResult> results = new ArrayList<EmrResult>();
		S3EmrDA s3 = new S3EmrDA(emrOutputBucketName);
		for (String object : objectNames)
		{
			List<EmrResult> resultsInDocument = s3.getEmrResult(object);
			HashSet<EmrResult> resultSet = new HashSet<>(resultsInDocument);
			List<EmrResult> newResults = new ArrayList<EmrResult>();
			for (EmrResult result : resultSet)
			{
				if (result.getKey().equals("category"))
				{
					System.out.println("result - " + result);

				}
				String[] postings = result.getValue().split("\t");
				int size = postings.length;
				StringBuilder newValue = new StringBuilder();
				int count = 0;
				for (int i = 0; i < size; i++)
				{
					newValue.append(postings[i]);
					count++;
					if (count >= MAX_POSTINGS_SIZE || i == size - 1)
					{
						count = 0;
						EmrResult newResult = new EmrResult(result.getKey(),
								newValue.toString());
						if (result.getKey().equals("category"))
						{
							System.out.println("new result - " + newResult);

						}
						newResults.add(newResult);
					}
					else
					{
						newValue.append("\t");
					}
				}
			}

			/*for (EmrResult result : resultsInDocument)
			{
				int count = 0;
				int prev = 0;
				for (int i = 0; i < result.getValue().length(); i++)
				{
					if (result.getValue().charAt(i) == '\t')
					{
						count++;
						if (count % MAX_POSTINGS_SIZE == 0 && count > 1)
						{
							String bunchOfPostings = result.getValue()
									.substring(prev, i);
							EmrResult newResult = new EmrResult(
									result.getKey(), bunchOfPostings);
							newResults.add(newResult);
							prev = i+1;
						}
					}
				}
				String bunchOfPostings = result.getValue().substring(prev,
						result.getValue().length() - 1);
				EmrResult newResult = new EmrResult(result.getKey(),
						bunchOfPostings);
				newResults.add(newResult);

			}*/
			results.addAll(newResults);
			while (results.size() > MAX_LIST_SIZE)
			{
				// System.out.println(results);
				List<EmrResult> resultsToBeWritten = results.subList(0, 24);
				batchWriteEmrResults(resultsToBeWritten);
				results.removeAll(resultsToBeWritten);
			}
			// remove that document from s3
			s3.deleteEmrResult(object);
		}
		if (results.size() > 0)
		{
			// System.out.println(results);
			batchWriteEmrResults(results);
		}
		// delete the output folder now
		s3.deleteEmrResult(emrOutputPrefix);
		return results;
	}

	/**
	 * Merge crawled documents.
	 *
	 * @param objectNames the object names
	 * @param outputBucketName the output bucket name
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws JsonProcessingException the json processing exception
	 */
	public void mergeCrawledDocuments(List<String> objectNames,
			String outputBucketName) throws NoSuchAlgorithmException,
			JsonProcessingException
	{
		List<DocumentRecord> mergedDocuments = new ArrayList<DocumentRecord>();
		for (String object : objectNames)
		{
			DocumentRecord doc = getDocument(
					"edu.upenn.cis455.project.documents", object);
			mergedDocuments.add(doc);
			if (mergedDocuments.size() > DOCUMENTS_TO_MERGE)
			{
				// merge the list into 1 s3 object
				writeDocuments(mergedDocuments, outputBucketName);
				// clear the list
				mergedDocuments.clear();
			}
		}
		if (mergedDocuments.size() > 0)
		{
			// merge the list into 1 s3 object
			writeDocuments(mergedDocuments, outputBucketName);
			// clear the list
			mergedDocuments.clear();
		}
	}

	/**
	 * Write documents.
	 *
	 * @param mergedDocuments the merged documents
	 * @param bucketName the bucket name
	 * @return the string
	 * @throws NoSuchAlgorithmException the no such algorithm exception
	 * @throws JsonProcessingException the json processing exception
	 */
	public String writeDocuments(List<DocumentRecord> mergedDocuments,
			String bucketName) throws NoSuchAlgorithmException,
			JsonProcessingException
	{

		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
		String json = ow.writeValueAsString(mergedDocuments);
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

	/**
	 * Creates the cluster.
	 *
	 * @return the string
	 * @throws InterruptedException the interrupted exception
	 */
	public String createCluster() throws InterruptedException
	{
		AWSCredentials awsCredentais = getCredentials();
		AmazonElasticMapReduce emr = new AmazonElasticMapReduceClient(
				awsCredentais);
		StepFactory stepFactory = new StepFactory();

		StepConfig enabledebugging = new StepConfig()
				.withName("Enable debugging")
				.withActionOnFailure(ActionOnFailure.CONTINUE)
				.withHadoopJarStep(stepFactory.newEnableDebuggingStep());

		StepConfig installHive = new StepConfig().withName("Install Hive")
				.withActionOnFailure(ActionOnFailure.TERMINATE_CLUSTER)
				.withHadoopJarStep(stepFactory.newInstallHiveStep());

		RunJobFlowRequest request = new RunJobFlowRequest()
				.withName(clusterName)
				.withAmiVersion("3.8")
				.withSteps(enabledebugging, installHive)
				.withLogUri(clusterLogPath)
				.withServiceRole("EMR_DefaultRole")
				.withJobFlowRole("EMR_EC2_DefaultRole")
				.withInstances(
						new JobFlowInstancesConfig()
								.withEc2KeyName(ec2AccessKeyName)
								.withInstanceCount(5)
								.withKeepJobFlowAliveWhenNoSteps(true)
								.withMasterInstanceType("m3.xlarge")
								.withSlaveInstanceType("m1.large"));

		RunJobFlowResult flowResult = emr.runJobFlow(request);
		DescribeClusterRequest clusterRequest = new DescribeClusterRequest();
		clusterRequest.setClusterId(flowResult.getJobFlowId());
		DescribeClusterResult clusterResult;
		while (true)
		{
			clusterResult = emr.describeCluster(clusterRequest);
			System.out.println(clusterResult.getCluster().getStatus());
			if (clusterResult.getCluster().getStatus().getState()
					.equals("STARTING"))
			{
				Thread.sleep(30000);
			}
			else
			{
				if (clusterResult.getCluster().getStatus().getState()
						.equals("RUNNING"))
				{
					// cluster Id is updated internally if the cluster starts
					// properly
					clusterId = flowResult.getJobFlowId();
				}
				break;
			}
		}
		return flowResult.getJobFlowId();
	}

	/**
	 * Terminate cluster.
	 *
	 * @throws InterruptedException the interrupted exception
	 */
	public void terminateCluster() throws InterruptedException
	{
		TerminateJobFlowsRequest terminateRequest = new TerminateJobFlowsRequest()
				.withJobFlowIds(clusterId);
		AmazonElasticMapReduce emr = new AmazonElasticMapReduceClient();
		emr.terminateJobFlows(terminateRequest);

		DescribeClusterRequest clusterRequest = new DescribeClusterRequest();
		clusterRequest.setClusterId(clusterId);
		DescribeClusterResult clusterResult;
		while (true)
		{
			clusterResult = emr.describeCluster(clusterRequest);
			System.out.println(clusterResult.getCluster().getStatus());
			if (!clusterResult.getCluster().getStatus().getState()
					.equals("TERMINATING"))
			{
				Thread.sleep(30000);
			}
			else
			{
				break;
			}
		}

	}

	/**
	 * Batch write emr results.
	 *
	 * @param results the results
	 */
	public void batchWriteEmrResults(List<EmrResult> results)
	{
		try
		{
			if (iterative && done)
			{
				batchGetandValidate(results);
			}
			TableWriteItems writeItems = new TableWriteItems(tableName);
			for (EmrResult result : results)
			{
				// System.out.println(result);

				PrimaryKey primaryKey;
				if (!isIterative())
				{
					primaryKey = new PrimaryKey(primaryKeyName,
							result.getKey(), rangeKeyName, Hash.hashKey(result
									.getValue()));
				}
				else
				{
					primaryKey = new PrimaryKey(primaryKeyName, result.getKey());
				}
				Item item = new Item().withPrimaryKey(primaryKey).with(
						valueKeyName, result.getValue());
				// System.out.println(item);
				writeItems.addItemToPut(item);
			}
			try
			{
				BatchWriteItemOutcome outcome = dynamo
						.batchWriteItem(writeItems);
				do
				{

					// Check for unprocessed keys
					Map<String, List<WriteRequest>> unprocessedItems = outcome
							.getUnprocessedItems();

					if (outcome.getUnprocessedItems().size() > 0)
					{
						outcome = dynamo
								.batchWriteItemUnprocessed(unprocessedItems);
					}

				}
				while (outcome.getUnprocessedItems().size() > 0);
			}
			catch (Exception e)
			{
				System.out.println(writeItems.getItemsToPut());
				e.printStackTrace();
			}

		}
		catch (Exception e)
		{
			System.out
					.println("EMR Controller : Failed to batch write items - ");
			e.printStackTrace();
		}
	}

	/**
	 * Batch getand validate.
	 *
	 * @param results the results
	 */
	public void batchGetandValidate(List<EmrResult> results)
	{
		try
		{
			System.out.println("validating");
			TableKeysAndAttributes tableKeysAndAttributes = new TableKeysAndAttributes(
					tableName);
			Map<String, Float> resultsMap = new HashMap<String, Float>();
			for (EmrResult result : results)
			{
				tableKeysAndAttributes.addPrimaryKey(new PrimaryKey(
						primaryKeyName, result.getKey()));
				resultsMap.put(result.getKey(),
						Float.valueOf(result.getValue()));
			}

			Map<String, TableKeysAndAttributes> requestItems = new HashMap<String, TableKeysAndAttributes>();
			requestItems.put(tableName, tableKeysAndAttributes);

			BatchGetItemOutcome outcome = dynamo
					.batchGetItem(tableKeysAndAttributes);
			do
			{
				List<Item> items = outcome.getTableItems().get(tableName);
				Map<String, Float> tableMap = new HashMap<String, Float>();
				for (Item item : items)
				{
					tableMap.put(item.getString(primaryKeyName),
							Float.valueOf(item.getFloat(valueKeyName)));
				}
				// System.out.println("comparing - ");
				// System.out.println(tableMap);
				// System.out.println(resultsMap);
				for (Map.Entry<String, Float> result : resultsMap.entrySet())
				{
					Float newRank = result.getValue();
					Float oldRank;
					if (tableMap.containsKey(result.getKey()))
					{
						oldRank = tableMap.get(result.getKey());
					}
					else
					{
						oldRank = new Float(1.0);
					}
					if (Math.abs(newRank - oldRank) > DELTA)
					{
						System.out.println("Not done as - ");
						System.out
								.println(tableMap.containsKey(result.getKey()) ? tableMap
										.get(result.getKey()) : "Not in table");
						System.out.println("compares with ");
						System.out.println(result);
						done = false;
						return;
					}
				}
				// Check for unprocessed keys which could happen if you exceed
				// provisioned
				// throughput or reach the limit on response size.

				Map<String, KeysAndAttributes> unprocessedKeys = outcome
						.getUnprocessedKeys();

				if (outcome.getUnprocessedKeys().size() > 0)
				{
					outcome = dynamo.batchGetItemUnprocessed(unprocessedKeys);
				}

			}
			while (outcome.getUnprocessedKeys().size() > 0);

		}
		catch (Exception e)
		{
			System.out.println("Failed to retrieve items.");
			e.printStackTrace();
		}
	}

	/**
	 * Batch delete emr results.
	 *
	 * @param results the results
	 */
	public void batchDeleteEmrResults(List<EmrResult> results)
	{
		// Multi-object delete by specifying only keys (no version ID).
		DeleteObjectsRequest multiObjectDeleteRequest = new DeleteObjectsRequest(
				emrOutputBucketName).withQuiet(false);

		// Create request that include only object key names.
		List<KeyVersion> justKeys = new ArrayList<KeyVersion>();
		for (EmrResult result : results)
		{
			System.out.println("To delete - " + emrOutputBucketName + "/"
					+ result.getKey());
			justKeys.add(new KeyVersion(result.getKey()));
		}
		multiObjectDeleteRequest.setKeys(justKeys);

		DeleteObjectsResult delObjRes = null;
		try
		{
			delObjRes = s3Client.deleteObjects(multiObjectDeleteRequest);
			System.out.format("Successfully deleted all the %s items.\n",
					delObjRes.getDeletedObjects().size());
		}
		catch (MultiObjectDeleteException mode)
		{
			System.out.format("%s \n", mode.getMessage());
			System.out.format("No. of objects successfully deleted = %s\n",
					mode.getDeletedObjects().size());
			System.out.format("No. of objects failed to delete = %s\n", mode
					.getErrors().size());
			System.out.format("Printing error data...\n");
			for (DeleteError deleteError : mode.getErrors())
			{
				System.out.format("Object Key: %s\t%s\t%s\n",
						deleteError.getKey(), deleteError.getCode(),
						deleteError.getMessage());
			}

		}
	}

	/**
	 * Gets the document.
	 *
	 * @param bucketName the bucket name
	 * @param prefix the prefix
	 * @return the document
	 */
	public DocumentRecord getDocument(String bucketName, String prefix)
	{
		DocumentRecord doc = null;
		try
		{
			GetObjectRequest req = new GetObjectRequest(bucketName, prefix);
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
			doc = mapper.readValue(s3Content.toString(), DocumentRecord.class);
		}
		catch (AmazonS3Exception ase)
		{
			System.out.println("S3EmrDA : document does not exist");
			ase.printStackTrace();
		}
		catch (IOException e)
		{
			System.out
					.println("S3EmrDA : IOException while fetching document from S3");
			e.printStackTrace();
		}
		return doc;
	}

	/**
	 * Gets the documents.
	 *
	 * @param bucketName the bucket name
	 * @param prefix the prefix
	 * @return the documents
	 */
	public List<DocumentRecord> getDocuments(String bucketName, String prefix)
	{
		List<DocumentRecord> documentList = new ArrayList<DocumentRecord>();
		try
		{
			GetObjectRequest req = new GetObjectRequest(bucketName, prefix);
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
			documentList = mapper.readValue(s3Content.toString(),
					new TypeReference<List<DocumentRecord>>()
					{
					});
		}
		catch (AmazonS3Exception ase)
		{
			System.out.println("S3EmrDA : document does not exist");
			ase.printStackTrace();
		}
		catch (IOException e)
		{
			System.out
					.println("S3EmrDA : IOException while fetching document from S3");
			e.printStackTrace();
		}
		return documentList;
	}

	/**
	 * Checks if is iterative.
	 *
	 * @return true, if is iterative
	 */
	public boolean isIterative()
	{
		return iterative;
	}

	/**
	 * Sets the iterative.
	 *
	 * @param iterative the new iterative
	 */
	public void setIterative(boolean iterative)
	{
		this.iterative = iterative;
	}

	/**
	 * Checks if is done.
	 *
	 * @return true, if is done
	 */
	public boolean isDone()
	{
		return done;
	}

	/**
	 * Sets the done.
	 *
	 * @param done the new done
	 */
	public void setDone(boolean done)
	{
		this.done = done;
	}

}