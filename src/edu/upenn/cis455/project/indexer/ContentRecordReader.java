package edu.upenn.cis455.project.indexer;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;

public class ContentRecordReader extends RecordReader<Text, Text> {

	private LineRecordReader lineRecordReader;
	private Text key;
	private Text value;
	
	@Override
	public void close() throws IOException {
		lineRecordReader.close();
	}

	@Override
	public Text getCurrentKey() throws IOException, InterruptedException {
		return key;
	}

	@Override
	public Text getCurrentValue() throws IOException, InterruptedException {
		return value;
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
		return lineRecordReader.getProgress();
	}

	@Override
	public void initialize(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
		System.out.println("in intialize");
		lineRecordReader = new LineRecordReader();
        lineRecordReader.initialize(split, context);
		System.out.println("done initializing");
	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		if (!lineRecordReader.nextKeyValue()) {
            key = null;
            value = null;
            return false;
        }
		
		Text line = lineRecordReader.getCurrentValue();      
		String str = line.toString();
		char charVal = str.charAt(0);
        value = new Text(String.valueOf(charVal));
		return true;
	}	
}


