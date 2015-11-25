package edu.upenn.cis455.project.indexer;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Map extends Mapper<NullWritable, BytesWritable, Text, Text> {
	private final Text url = new Text();
	@Override
    public void map(NullWritable key, BytesWritable value, Context context) 
    		throws IOException, InterruptedException {
	    Text word = new Text();
	    String line = new String(value.getBytes());
	    
	    String[] urlContent = line.split("\n", 2);
	    setUrl(urlContent[0]);
	    line = urlContent[1];
	    String actualContent = getHtmlText(line);
	    StringTokenizer tokenizer = new StringTokenizer(actualContent, " ,.?\"");
	    while (tokenizer.hasMoreTokens()) {
	        word.set(tokenizer.nextToken().toLowerCase().replaceAll("[^A-Za-z0-9 ]", ""));
	        context.write(word, url);
        }
    }
	
	public void setUrl(String content){
		this.url.set(content.trim());
		
	}
	
	public String getHtmlText(String html)
	{
		Document doc = Jsoup.parse(html.replaceAll("(?i)<br[^>]*>", "<pre>\n</pre>"));
		String textContent = doc.select("body").text();
		return textContent;
	}
	
}
