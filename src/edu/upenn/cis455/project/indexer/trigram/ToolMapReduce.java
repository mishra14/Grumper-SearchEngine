package edu.upenn.cis455.project.indexer.trigram;

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
 
// TODO: Auto-generated Javadoc
/**
 * The Class ToolMapReduce.
 */
public class ToolMapReduce extends Configured implements Tool {
 
    /**
     * The main method.
     *
     * @param args the arguments
     * @throws Exception the exception
     */
    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new ToolMapReduce(), args);
        System.exit(res);
    }
 
    /* (non-Javadoc)
     * @see org.apache.hadoop.util.Tool#run(java.lang.String[])
     */
    @Override
    public int run(String[] args) throws Exception {
 
        // When implementing tool
        //Configuration conf = this.getConf();
 
        // Create job
        //Job job = new Job(conf, "Tool Job");
        Job job = Job.getInstance(new Configuration(), ToolMapReduce.class.getCanonicalName());
        job.setJarByClass(ToolMapReduce.class);
 
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
