package edu.upenn.cis455.project.indexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.upenn.cis455.project.bean.DocumentRecord;
import edu.upenn.cis455.project.scoring.Stemmer;

public class Map extends Mapper<Text, BytesWritable, Text, Text>
{

	private final Text url = new Text();

	@Override
	public void map(Text key, BytesWritable value, Context context)
			throws IOException, InterruptedException
	{
		Text word = new Text();
		List<DocumentRecord> docList = getDocument(value);
		for (DocumentRecord doc : docList)
		{
			if (doc != null)
			{
				String line = doc.getDocumentString();
				url.set(doc.getDocumentId().trim());

				String rawContent = getHtmlText(line);
				StringTokenizer tokenizer = new StringTokenizer(rawContent, " ,.?\"!-");
				while (tokenizer.hasMoreTokens())
				{
					String currWord = tokenizer.nextToken();
					currWord = currWord.toLowerCase()
							.replaceAll("[^a-z0-9 ]", "").trim();
					// String stemmedWord = stem(currWord);
					if (!currWord.isEmpty())
					{
						word.set(stem(currWord) + " " + key);
						context.write(word, url);
					}

				}
			}
		}
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
	private String getHtmlText(String html)
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

}
