package edu.upenn.cis455.project.indexer.metadata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.upenn.cis455.project.bean.DocumentRecord;
import edu.upenn.cis455.project.scoring.Stemmer;

// TODO: Auto-generated Javadoc
/**
 * The Class Map.
 */
public class Map extends Mapper<LongWritable, Text, Text, Text>
{

	/** The url. */
	private final Text url = new Text();
	
	/** The split on. */
	private final String splitOn = "[ \t\n\r,\"\\_\\-?.:,;\'\\[\\(\\{\\!\\)\\}\\]]";

	/* (non-Javadoc)
	 * @see org.apache.hadoop.mapreduce.Mapper#map(KEYIN, VALUEIN, org.apache.hadoop.mapreduce.Mapper.Context)
	 */
	@Override
	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException
	{
		DocumentRecord doc = getDocument(value);
		if (doc != null)
		{
			String html = doc.getDocumentString();
			// sanitize the url
			String sanitizedUrl = doc.getDocumentId().trim();
			if (sanitizedUrl.contains(" "))
			{
				sanitizedUrl.replaceAll(" ", "%20");
			}
			url.set(sanitizedUrl);

			ArrayList<String> metadata = getMetadata(html);

			for (String data : metadata)
			{
				data = data.trim().toLowerCase();
				if (data.length() > 50)
					continue;
				if (!data.isEmpty() & !stopwords.contains(data))
				{
					context.write(new Text(stem(data)), url);
				}
			}

		}
	}

	/**
	 * Gets the metadata.
	 *
	 * @param html the html
	 * @return the metadata
	 */
	public ArrayList<String> getMetadata(String html)
	{
		Document doc = Jsoup.parse(html);

		String title = doc.title();
		String keywords = "";
		String description = "";
		if ( doc.getElementsByTag("meta").size() > 0){
			description = doc.getElementsByTag("meta").get(0)
				.attr("description");
			keywords = doc.getElementsByTag("meta").get(0).attr("content");
		}

		ArrayList<String> metadata = new ArrayList<String>();

		if (!title.isEmpty())
			metadata.addAll(Arrays.asList(title.split(splitOn)));
		if(!keywords.isEmpty())
			metadata.addAll(Arrays.asList(keywords.split(splitOn)));
		if (!description.isEmpty())
			metadata.addAll(Arrays.asList(description.split(splitOn)));

		return metadata;

	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args){
		String a = " this is a smaple_sting. \"Look\". What is it?! (assked) This.is.no.cpre";
		String[] b = a.split("[ \t\n\r,\"\\_\\-?.:,;\'\\[\\(\\{\\!\\)\\}\\]]");
		
		for(String x : b ){
			System.out.println(x);
		}
		
	}
	

	/**
	 * Gets the document.
	 *
	 * @param value the value
	 * @return the document
	 */
	private DocumentRecord getDocument(Text value)
	{
		ObjectMapper mapper = new ObjectMapper();
		DocumentRecord doc = null;
		mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		try
		{
			doc = mapper.readValue(value.toString(), DocumentRecord.class);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return doc;
	}

	/**
	 * Stem.
	 *
	 * @param word the word
	 * @return the string
	 */
	public String stem(String word)
	{
		Stemmer stemmer = new Stemmer();
		char[] charArray = word.toCharArray();
		stemmer.add(charArray, word.length());
		stemmer.stem();
		String stemmedWord = stemmer.toString();
		return stemmedWord;
	}

	/** The stopwords. */
	private static ArrayList<String> stopwords = new ArrayList<String>(
			Arrays.asList(("about,above,"
					+ "after,again,against,all,am,an,and,any,are,"
					+ "aren't,as,at,be,because,been,before,being,"
					+ "below,between,both,but,by,could,"
					+ "couldn't,did,didn't,do,does,doesn't,doing,don't,"
					+ "down,during,each,few,for,from,further,had,hadn't,"
					+ "he,he'd,he'll,he's,"
					+ "her,here,here's,hers,herself,him,himself,his,"
					+ "how's,i,i'd,i'll,i'm,i've,if,in,into,is,isn't,it,"
					+ "it's,its,itself,let's,me,mustn't,my,myself,"
					+ "no,nor,of,off,on,once,only,or,other,ought,our,ours,"
					+ "she's,should,shouldn't,so,some,such,than,that,that's,"
					+ "the,their,theirs,them,themselves,then,there,there's,"
					+ "these,they,they'd,they'll,they're,they've,this,those,"
					+ "through,to,too,under,until,up,very,was,wasn't,we,we'd,"
					+ "we'll,we're,we've,were,weren't,what's,when's,"
					+ "where's,while,who's,why's,with,"
					+ "yours,yourself,yourselves,").split(",")));
}