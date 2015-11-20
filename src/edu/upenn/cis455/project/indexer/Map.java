package edu.upenn.cis455.project.indexer;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class Map extends Mapper<Text, Text, Text, Text> {
    
	@Override
    public void map(Text key, Text value, Context context) 
    		throws IOException, InterruptedException {
	    Text word = new Text();
	    String line = value.toString();
	    StringTokenizer tokenizer = new StringTokenizer(line, " ,.?\"");
	    while (tokenizer.hasMoreTokens()) {
	        word.set(tokenizer.nextToken().toLowerCase());
	        context.write(word, key);
        }
    }
	
	
}
