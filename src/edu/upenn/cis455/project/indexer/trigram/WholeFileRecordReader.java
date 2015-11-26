package edu.upenn.cis455.project.indexer.trigram;
import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class WholeFileRecordReader  extends RecordReader<Text, BytesWritable> {
	private FileSplit split;
	private Configuration conf;
	private boolean fileread = false;
	private BytesWritable value = new BytesWritable();
	private int bucketSize;
	private String crawlerBucket;

	
	@Override
	public Text getCurrentKey() throws IOException, InterruptedException {
		return new Text(String.valueOf(bucketSize));
	}
	  
	@Override
	public float getProgress() throws IOException, InterruptedException {
        return 0;
	}

	@Override
	public void close() throws IOException {}

	@Override
    public void initialize(InputSplit split, TaskAttemptContext context)
                     throws IOException, InterruptedException {
             this.split = (FileSplit)split;
             this.conf = context.getConfiguration();
             this.crawlerBucket = "pdeepti-test-bucket"; //TODO Use crawler bucket
             setBucketSize();
     }
	
	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException{
		if (fileread){
			return false;
		}
		
		int filesize = (int) split.getLength();
		byte[] filecontent = new byte[filesize];
		
		final Path file = split.getPath();		
		FileSystem fs = file.getFileSystem(conf);
		FSDataInputStream inputStream = fs.open(file);
		try{
			IOUtils.readFully(inputStream, filecontent, 0, filesize);
			value.set(filecontent, 0, filesize);
			this.fileread = true;
		} finally {
			IOUtils.closeStream(inputStream);
		} 
		
		return true;
	}

	@Override
	public BytesWritable getCurrentValue() throws IOException, InterruptedException {
		return value;
	}
	
	public void setBucketSize()
	{
		AmazonS3 s3client = new AmazonS3Client(); //TODO Need to add aws credentials here
	    ObjectListing listing = s3client.listObjects(crawlerBucket);
	    List<S3ObjectSummary> summaries = listing.getObjectSummaries();

	    while (listing.isTruncated()) {
	       listing = s3client.listNextBatchOfObjects (listing);
	       summaries.addAll (listing.getObjectSummaries());
	    }
	    
	    bucketSize = summaries.size();
	}
}