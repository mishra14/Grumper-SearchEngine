package edu.upenn.cis455.project.indexer;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class Reduce extends Reducer<Text, Text, Text, String>
{
	
	@Override
	protected void reduce(Text key, Iterable<Text> values, Context context) 
			throws IOException, InterruptedException {
//        int sum = 0;
//	    for (Text value : values) {
//	        sum += value.get();
//        }
//	    context.write(key, new IntWritable(sum));
		
		int df = computeDF(values);
		  String postings = computePostings(values);
		  
		  
		  String value = String.valueOf(df) + ":{" + postings + "}" ;
		  context.write(key, value);
    }
	
	private int computeDF( Iterable<Text> docIDs){
		 // Set<String> docIDset = new HashSet<Text>(Arrays.asList(docIDs));
		  Set<Text> docIDset = new HashSet<>();
		  
		  for (Text docId : docIDs){
			  docIDset.add(docId);
		  }
		  return docIDset.size();
	  }
	  
	  private String  computePostings( Iterable<Text> docIDs){
		  HashMap<Text, Integer> tf = new HashMap<>();
		 
		  for (Text docID : docIDs){
			  if (tf.containsKey(docID)){
				  int count = tf.get(docID);
				  tf.put(docID, count + 1);
			  }
			  else {
				  tf.put(docID, 1);
			  }
		  }
		  
		  StringBuilder postings = new StringBuilder();
		  for (Text docID: tf.keySet()){
			  postings.append(docID+":" + tf.get(docID)+ " ");
		  }
		  return postings.toString();
		  
	  }
}