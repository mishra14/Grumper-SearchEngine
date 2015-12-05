package edu.upenn.cis455.project.pagerank;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class PageRankTool extends Configured implements Tool
{

	private static final int INTERATION_COUNT = 10;

	public static void main(String[] args) throws Exception
	{
		int res = 1;
		for (int i = 0; i < INTERATION_COUNT; i++)
		{
			String input = args[0];
			String output = args[1];
			String[] jobArgs = { input, output + i };
			res = ToolRunner.run(new Configuration(), new PageRankTool(),
					jobArgs);
			if (res == 0)
			{
				break;
			}
			System.out.println("Done with job " + (i + 1) + " successfully");
		}
		System.exit(res);
	}

	@Override
	public int run(String[] args) throws Exception
	{

		// When implementing tool
		// Configuration conf = this.getConf();

		// Create job
		// Job job = new Job(conf, "Tool Job");
		Job job = Job.getInstance(new Configuration(),
				PageRankTool.class.getCanonicalName());
		job.setJarByClass(PageRankTool.class);

		// Setup MapReduce job
		// Do not specify the number of Reducer
		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);

		// Specify key / value
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		// Input
		FileInputFormat.addInputPath(job, new Path(args[0]));
		job.setInputFormatClass(WholeFileInputFormat.class);

		// Output
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.setOutputFormatClass(TextOutputFormat.class);

		// Execute job and return status
		return job.waitForCompletion(true) ? 0 : 1;
	}
}
