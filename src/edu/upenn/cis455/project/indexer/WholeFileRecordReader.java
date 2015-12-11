package edu.upenn.cis455.project.indexer;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

// TODO: Auto-generated Javadoc
/**
 * The Class WholeFileRecordReader.
 */
public class WholeFileRecordReader  extends RecordReader<NullWritable, BytesWritable> {
	
	/** The split. */
	private FileSplit split;
	
	/** The conf. */
	private Configuration conf;
	
	/** The fileread. */
	private boolean fileread = false;
	
	/** The value. */
	private BytesWritable value = new BytesWritable();
	
	/** The bucket size. */
	private int bucketSize;
	
	/** The Constant crawlerBucket. */
	private static final String crawlerBucket = "test-indexer";//"edu.upenn.cis455.project.indexer.documents";
	
	/* (non-Javadoc)
	 * @see org.apache.hadoop.mapreduce.RecordReader#getCurrentKey()
	 */
	@Override
	public NullWritable getCurrentKey() throws IOException, InterruptedException {
		return NullWritable.get();
	}
	  
	  
	/* (non-Javadoc)
	 * @see org.apache.hadoop.mapreduce.RecordReader#getProgress()
	 */
	@Override
	public float getProgress() throws IOException, InterruptedException {
        return 0;
	}
	
	
	/* (non-Javadoc)
	 * @see org.apache.hadoop.mapreduce.RecordReader#close()
	 */
	@Override
	public void close() throws IOException {}

	/* (non-Javadoc)
	 * @see org.apache.hadoop.mapreduce.RecordReader#initialize(org.apache.hadoop.mapreduce.InputSplit, org.apache.hadoop.mapreduce.TaskAttemptContext)
	 */
	@Override
    public void initialize(InputSplit split, TaskAttemptContext context)
                     throws IOException, InterruptedException {
             this.split = (FileSplit)split;
             this.conf = context.getConfiguration();
             //this.crawlerBucket = "emr-job-aayushi/input"; //TODO Use crawler bucket
             //setBucketSize();
             
     }
	
	/* (non-Javadoc)
	 * @see org.apache.hadoop.mapreduce.RecordReader#nextKeyValue()
	 */
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

	/* (non-Javadoc)
	 * @see org.apache.hadoop.mapreduce.RecordReader#getCurrentValue()
	 */
	@Override
	public BytesWritable getCurrentValue() throws IOException, InterruptedException {
		return value;
	}
	
//	public void setBucketSize()
//	{
//		AmazonS3 s3client = new AmazonS3Client(); //TODO Need to add aws credentials here
//	    ObjectListing listing = s3client.listObjects(crawlerBucket);
//	    List<S3ObjectSummary> summaries = listing.getObjectSummaries();
//	
//	    while (listing.isTruncated()) {
//	       listing = s3client.listNextBatchOfObjects (listing);
//	       summaries.addAll (listing.getObjectSummaries());
//	    }
//	    
//	    bucketSize = summaries.size();
//	}
}
