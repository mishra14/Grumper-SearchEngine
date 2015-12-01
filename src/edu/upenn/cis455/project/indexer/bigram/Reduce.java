package edu.upenn.cis455.project.indexer.bigram;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import edu.upenn.cis455.project.scoring.TFIDF;
import test.edu.upenn.cis455.project.DynamoIndexerDA;

public class Reduce extends Reducer<Text, Text, Text, Text>
{
	private Log log = LogFactory.getLog(Reduce.class);
	private HashMap<String, Integer> tf = null;
	private Text keyword;
	private int bucketSize, df;
	private static final String tablename = "BigramIndex";
	
	@Override
	protected void reduce(Text key, Iterable<Text> values, Context context) 
			throws IOException, InterruptedException {
		
		setFieldsFromKey(key);
		df = computeDF(values);
		String postings = computePostings();  
		String value = postings;
		context.write(keyword, new Text(value));
		DynamoIndexerDA dynamo = new DynamoIndexerDA(tablename);
		dynamo.saveIndex(keyword.toString(), value);
    }
	
	private int computeDF( Iterable<Text> docIDs){
		 Set<String> docIDset = new HashSet<>();
		 tf = new HashMap<>();
		 log.info("DOC IDS: " + docIDs.toString());
		 for (Text id : docIDs){
			 String docID = id.toString();
			 docIDset.add(docID);
			 //add to tf dictionary
			 if (tf.containsKey(docID)){
				  System.err.println("Compute Postings : Doc ID in tf" + docID.toString());
				  int count = tf.get(docID);
				  tf.put(docID, count + 1);
			  }
			  else {
				  System.err.println("Compute Postings : Doc ID new" + docID.toString());
				  tf.put(docID, 1);
			  }			 
		  }
		 
		  return docIDset.size();
	  }
	  
	  private String computePostings(){
		  
		  StringBuilder postings = new StringBuilder();
		  int size = tf.size() - 1;
		  int i = 0;
		  
		  for (String docID: tf.keySet()){
			  double tfidf = TFIDF.compute(tf.get(docID), df, bucketSize);
			  if (i < size )
				  postings.append(docID.toString()+" " + tfidf + ",");
			  else 
				  postings.append(docID.toString()+" " + tfidf);
			  i++;
		  }
		  return postings.toString();
		  
	  }
	  
	  private void setFieldsFromKey(Text key)
	  {
		  String[] keys = key.toString().split(";");
		  bucketSize = Integer.parseInt(keys[1]);
		  keyword = new Text(keys[0]);
	  }
}