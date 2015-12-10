package edu.upenn.cis455.project.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import edu.upenn.cis455.project.bean.EmrResult;

// TODO: Auto-generated Javadoc
/**
 * The Class S3EmrDA.
 */
public class S3EmrDA
{
	
	/** The bucket name. */
	private String bucketName;
	
	/** The s3client. */
	private AmazonS3 s3client;

	/**
	 * Instantiates a new s3 emr da.
	 *
	 * @param bucketName the bucket name
	 */
	public S3EmrDA(String bucketName)
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
					.println("S3UrlListDA : reading from local credential file failed");
			e.printStackTrace();
		}
		catch (IOException e)
		{
			System.out
					.println("S3UrlListDA : reading from local credential file failed");
			e.printStackTrace();
		}

		this.bucketName = bucketName;
		BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey,
				secretKey);
		this.s3client = new AmazonS3Client(awsCreds);
	}

	/**
	 * Gets the emr result.
	 *
	 * @param prefix the prefix
	 * @return the emr result
	 */
	public List<EmrResult> getEmrResult(String prefix)
	{
		List<EmrResult> result = new ArrayList<EmrResult>();
		try
		{
			GetObjectRequest req = new GetObjectRequest(bucketName, prefix);
			S3Object s3Object = s3client.getObject(req);
			InputStream objectData = s3Object.getObjectContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					objectData));
			String line;
			while ((line = reader.readLine()) != null)
			{
				EmrResult emrResult = new EmrResult(line);
				if (emrResult.isValid())
				{
					result.add(emrResult);
				}
			}
			reader.close();
			objectData.close();
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
		return result;
	}

	/**
	 * Delete emr result.
	 *
	 * @param prefix the prefix
	 */
	public void deleteEmrResult(String prefix)
	{
		try
		{
			DeleteObjectRequest req = new DeleteObjectRequest(bucketName,
					prefix);
			s3client.deleteObject(req);
		}
		catch (AmazonS3Exception ase)
		{
			System.out.println("S3EmrDA : document does not exist");
			ase.printStackTrace();
		}
	}

}
