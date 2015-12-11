package edu.upenn.cis455.project.indexer;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

// TODO: Auto-generated Javadoc
/**
 * The Class WholeFileInputFormat.
 */
public class WholeFileInputFormat extends FileInputFormat<NullWritable, BytesWritable> {

        /* (non-Javadoc)
         * @see org.apache.hadoop.mapreduce.lib.input.FileInputFormat#isSplitable(org.apache.hadoop.mapreduce.JobContext, org.apache.hadoop.fs.Path)
         */
        @Override
        protected boolean isSplitable(JobContext context, Path filename) {
                return false;
        }

        /* (non-Javadoc)
         * @see org.apache.hadoop.mapreduce.InputFormat#createRecordReader(org.apache.hadoop.mapreduce.InputSplit, org.apache.hadoop.mapreduce.TaskAttemptContext)
         */
        @Override
        public RecordReader<NullWritable, BytesWritable> createRecordReader(
                        InputSplit inputSplit, TaskAttemptContext context) throws IOException,
                        InterruptedException {
                WholeFileRecordReader reader = new WholeFileRecordReader();
                reader.initialize(inputSplit, context);
                return reader;
        }


}
