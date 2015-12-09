package edu.upenn.cis455.project.indexer.bigram;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.upenn.cis455.project.bean.DocumentRecord;
//import edu.upenn.cis455.project.indexer.Stemmer;
import edu.upenn.cis455.project.scoring.Stemmer;

public class Map extends Mapper<NullWritable, BytesWritable, Text, Text>
{

	private final Text url = new Text();
	private ArrayList<String> allWords = new ArrayList<String>();
	private final String splitOn = " ,.?\"!-[({\r\t\"\'\\_";

	@Override
	public void map(NullWritable key, BytesWritable value, Context context)
			throws IOException, InterruptedException
	{
		Text bigram = new Text();
		List<DocumentRecord> docList = getDocument(value);
		for (DocumentRecord doc : docList)
		{
			if (doc != null)
			{
				String line = doc.getDocumentString();
				//sanitize the url
				String sanitizedUrl = doc.getDocumentId().trim();
				if (sanitizedUrl.contains(" "))
				{
					sanitizedUrl.replaceAll(" ", "%20");
				}
				url.set(sanitizedUrl);

				String rawContent = getHtmlText(line);
				getAllWords(rawContent);
				
				int numWords = allWords.size();
				for (int i = 0; i < numWords - 1; i++)
				{
					bigram.set(allWords.get(i) + " " + allWords.get(i + 1));
					context.write(bigram, url);
				}
			}
		}
	}

	public void getAllWords(String content)
	{
		allWords.clear();
		StringTokenizer tokenizer = new StringTokenizer(content, splitOn);
		String word;
		while (tokenizer.hasMoreTokens())
		{
			word = tokenizer.nextToken();
			word = word.trim().toLowerCase().replaceAll("[^a-z0-9]", "");
			if (!word.matches("[0-9]+") && !stopwords.contains(word)
					&& !word.isEmpty())
			{
				allWords.add(stem(word));
			}
		}
	}

	public String getHtmlText(String html)
	{
		Document doc = Jsoup
				.parse(html.replaceAll("(?i)<br[^>]*>", "<pre>\n</pre>"));
		String textContent = doc.select("body").text();
		return textContent;
	}

	private List<DocumentRecord> getDocument(BytesWritable value)
	{
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		List<DocumentRecord> docList = new ArrayList<DocumentRecord>();
		try
		{
			docList = mapper.readValue(new String(value.getBytes()),
					new TypeReference<List<DocumentRecord>>()
					{
					});
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return docList;
	}

	public String stem(String word)
	{
		Stemmer stemmer = new Stemmer();
		char[] charArray = word.toCharArray();
		stemmer.add(charArray, word.length());
		stemmer.stem();
		String stemmedWord = stemmer.toString();
		return stemmedWord;
	}

	private static ArrayList<String> stopwords = new ArrayList<String>(
			Arrays.asList(("a,about,above,"
					+ "after,again,against,all,am,an,and,any,are,"
					+ "aren't,as,at,be,because,been,before,being,"
					+ "below,between,both,but,by,could,"
					+ "couldn't,did,didn't,do,does,doesn't,doing,don't,"
					+ "down,during,each,few,for,from,further,had,hadn't,"
					+ "has,hasn't,have,haven't,having,he,he'd,he'll,he's,"
					+ "her,here,here's,hers,herself,him,himself,his,"
					+ "how's,i,i'd,i'll,i'm,i've,if,in,into,is,isn't,it,"
					+ "it's,its,itself,let's,me,more,mustn't,my,myself,"
					+ "no,nor,of,off,on,once,only,or,other,ought,our,ours,"
					+ "ourselves,out,over,own,shan't,she,she'd,she'll,"
					+ "she's,should,shouldn't,so,some,such,than,that,that's,"
					+ "the,their,theirs,them,themselves,then,there,there's,"
					+ "these,they,they'd,they'll,they're,they've,this,those,"
					+ "through,to,too,under,until,up,very,was,wasn't,we,we'd,"
					+ "we'll,we're,we've,were,weren't,what's,when's,"
					+ "where's,while,who's,why's,with,"
					+ "won't,would,wouldn't,you,you'd,you'll,you're,you've,your,"
					+ "yours,yourself,yourselves,").split(",")));
}