package edu.upenn.cis455.project.indexer.metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import edu.upenn.cis455.project.dynamoDA.DynamoIndexerDA;
import edu.upenn.cis455.project.storage.Postings;

// TODO: Auto-generated Javadoc
/**
 * The Class Reduce.
 */
public class Reduce extends Reducer<Text, Text, Text, Text>
{
	
	/** The tf. */
	private HashMap<String, Integer> tf = null;
	
	/** The Constant MAX_LIST. */
	private final static int MAX_LIST = 2000;
	
	/** The Constant tablename. */
	private static final String tablename = "Metadata";

	/* (non-Javadoc)
	 * @see org.apache.hadoop.mapreduce.Reducer#reduce(KEYIN, java.lang.Iterable, org.apache.hadoop.mapreduce.Reducer.Context)
	 */
	@Override
	protected void reduce(Text key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException
	{
		computeFrequecy(values);
		String postingsList = createPostingsList();
		DynamoIndexerDA dynamo = new DynamoIndexerDA(tablename);
		dynamo.save(key.toString(), postingsList);
	}

	/**
	 * Compute frequecy.
	 *
	 * @param docIDs the doc i ds
	 */
	private void computeFrequecy(Iterable<Text> docIDs)
	{
		Set<String> docIDset = new HashSet<>();
		tf = new HashMap<>();
		for (Text id : docIDs)
		{
			String docID = id.toString();
			docIDset.add(docID);
			// add to tf dictionary
			if (tf.containsKey(docID))
			{
				int count = tf.get(docID);
				tf.put(docID, count + 1);
			}
			else
			{
				tf.put(docID, 1);
			}
		}
	}

	/**
	 * Sort postings.
	 *
	 * @return the array list
	 */
	private ArrayList<Postings> sortPostings()
	{

		ArrayList<Postings> postingsList = new ArrayList<Postings>();
		for (String docID : tf.keySet())
		{
			Postings newPostings = new Postings(docID, tf.get(docID), 0);
			postingsList.add(newPostings);
		}
		Collections.sort(postingsList);
		return postingsList;
	}

	/**
	 * Creates the postings list.
	 *
	 * @return the string
	 */
	private String createPostingsList()
	{
		StringBuilder postings = new StringBuilder();
		ArrayList<Postings> postingsList = sortPostings();
		int size = postingsList.size() - 1;
		int i = 0;
		for (Postings posting : postingsList)
		{
			if (i < size)
				postings.append(posting.getPosting() + " " + posting.getTfidf()
						+ " " + posting.getIdf() + "\t");
			else
				postings.append(posting.getPosting() + " " + posting.getTfidf()
						+ " " + posting.getIdf());
			i++;
			if (i > MAX_LIST)
				break;
		}
		return postings.toString();
	}

}