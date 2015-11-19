import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class Map extends Mapper<LongWritable, Text, Text, IntWritable> {
    
	@Override
    public void map(LongWritable key, Text value, Context context) 
    		throws IOException, InterruptedException {
        IntWritable one = new IntWritable(1);
	    Text word = new Text();
	    String line = value.toString();
	    StringTokenizer tokenizer = new StringTokenizer(line);
	    while (tokenizer.hasMoreTokens()) {
	        word.set(tokenizer.nextToken());
	        context.write(word, one);
        }
    }
}
