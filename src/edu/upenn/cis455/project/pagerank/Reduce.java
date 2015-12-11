package edu.upenn.cis455.project.pagerank;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import edu.upenn.cis455.project.storage.DynamoDA;

// TODO: Auto-generated Javadoc
/**
 * The Class Reduce.
 */
public class Reduce extends Reducer<Text, Text, Text, Text>
{

	/** The Constant damper. */
	private static final Float damper = (float) 0.6;

	/** The dynamo. */
	private DynamoDA<Float> dynamo = new DynamoDA<Float>(
			"edu.upenn.cis455.project.pagerank", Float.class);

	/* (non-Javadoc)
	 * @see org.apache.hadoop.mapreduce.Reducer#reduce(KEYIN, java.lang.Iterable, org.apache.hadoop.mapreduce.Reducer.Context)
	 */
	@Override
	protected void reduce(Text key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException
	{
		Float rankSum = new Float(0);
		for (Text rankVariableString : values)
		{
			Float rankVariable = Float
					.parseFloat(rankVariableString.toString());
			rankSum += rankVariable;
		}
		Float rank = (1 - damper) + damper * rankSum;
		String rankString = rank.toString();
		dynamo.putItem("hostName", key.toString(), "rank", rank);
		// context.write(key, new Text(rankString));
	}

}