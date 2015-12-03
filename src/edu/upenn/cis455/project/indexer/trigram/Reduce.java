package edu.upenn.cis455.project.indexer.trigram;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import edu.upenn.cis455.project.dynamoDA.DynamoIndexerDA;
import edu.upenn.cis455.project.scoring.URLTFIDF;

public class Reduce extends Reducer<Text, Text, Text, Text>
{
	private Log log = LogFactory.getLog(Reduce.class);
	private HashMap<String, Integer> tf = null;
	private Text keyword;
	private int bucketSize, df;
	private static final String tablename = "TrigramIndex";

	@Override
	protected void reduce(Text key, Iterable<Text> values, Context context) 
			throws IOException, InterruptedException {
		setFieldsFromKey(key);
		df = computeDF(values);
		String postingsList = createPostingsList();  
		//context.write(keyword, new Text(postingsList));
		DynamoIndexerDA dynamo = new DynamoIndexerDA(tablename);
		dynamo.saveIndex(keyword.toString(), postingsList);
    }
	
	private int computeDF( Iterable<Text> docIDs){
		 Set<String> docIDset = new HashSet<>();
		 tf = new HashMap<>();
		 log.info("DOC IDS: " + docIDs.toString());
		 for (Text id : docIDs){
			 String docID = id.toString();
			 docIDset.add(docID);
			 if (tf.containsKey(docID)){
				  int count = tf.get(docID);
				  tf.put(docID, count + 1);
			  }
			  else {
				  tf.put(docID, 1);
			  }			 
		  }
		 
		  return docIDset.size();
	  }
	  
	private ArrayList<URLTFIDF> sortPostings(){
		  ArrayList<URLTFIDF> postingsList = new ArrayList<URLTFIDF>();
		  
		  for(String docID: tf.keySet()){
			  float idf = (float) Math.log(3400/df);// replace later with bucketsize
			  float tfidf = tf.get(docID) * idf ;
			  URLTFIDF newPostings = new URLTFIDF(docID, tfidf, idf);
			  postingsList.add(newPostings);
		  }
		  Collections.sort(postingsList);
		  return postingsList; 
	  }
	
	 private String createPostingsList(){
		  StringBuilder postings = new StringBuilder();
		  ArrayList<URLTFIDF> postingsList = sortPostings();
		  int size = postingsList.size() - 1;
		  int i = 0;
		  for(URLTFIDF pair : postingsList){
			  if (i < size )
				  postings.append(pair.getURL()+ " " + pair.getTFIDF() + " "+ pair.getIDF() +"\t");
			  else 
				  postings.append(pair.getURL()+ " " + pair.getTFIDF() + " " + pair.getIDF());
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