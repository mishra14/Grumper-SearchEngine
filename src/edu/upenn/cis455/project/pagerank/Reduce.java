package edu.upenn.cis455.project.pagerank;

import java.io.IOException;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class Reduce extends Reducer<Text, Text, Text, Text>
{
	private static final Float damper = (float) 0.5;
	@Override
	protected void reduce(Text key, Iterable<Text> values, Context context) 
			throws IOException, InterruptedException {
		Float rankSum = new Float(0);
		for(Text rankVariableString : values)
		{
			Float rankVariable = Float.parseFloat(rankVariableString.toString());
			rankSum+=rankVariable;
		}
		Float rank = (1-damper)+damper*rankSum;
		String rankString = rank.toString();
		context.write(key, new Text(rankString));
    }
	
}