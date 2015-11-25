package edu.upenn.cis455.project.pagerank;

import java.io.IOException;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class Reduce extends Reducer<Text, Text, Text, Text>
{
	
	@Override
	protected void reduce(Text key, Iterable<Text> values, Context context) 
			throws IOException, InterruptedException {
		Float rankSum = new Float(0);
		for(Text rankVariableString : values)
		{
			Float rankVariable = Float.parseFloat(rankVariableString.toString());
			rankSum+=rankVariable;
		}
		String rank = rankSum.toString();
		context.write(key, new Text(rank));
    }
	
}