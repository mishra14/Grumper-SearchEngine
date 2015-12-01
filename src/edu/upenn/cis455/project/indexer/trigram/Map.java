package edu.upenn.cis455.project.indexer.trigram;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.upenn.cis455.project.bean.DocumentRecord;
//import edu.upenn.cis455.project.indexer.Stemmer;

public class Map extends Mapper<Text, BytesWritable, Text, Text> {
    
	private final Text url = new Text();
	private ArrayList<String>allWords;
	@Override
    public void map(Text key, BytesWritable value, Context context) 
    		throws IOException, InterruptedException {
	    Text bigram = new Text();
	    DocumentRecord doc = getDocument(value);
	    String line = doc.getDocumentString();
	    url.set(doc.getDocumentId().trim());
	    
	    String rawContent = getHtmlText(line);
	    getAllWords(rawContent);
	    int numWords = allWords.size();
	    
	    for (int i = 0; i < numWords - 2; i++){
	    	bigram.set(allWords.get(i) 
	    			+ " " + allWords.get(i + 1)
	    			+ " " + allWords.get(i + 2)
	    			+ ";" + key);
	    	context.write( new Text(bigram), url);
	    }  
	    
    }
	
	public void setUrl(String content){
		this.url.set(content.trim());
		
	}
	
	public void getAllWords(String content){
		allWords = new ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer(content, " ,.?\"!-");
		String word;
		while (tokenizer.hasMoreTokens()) {
			word = tokenizer.nextToken();
	    	word = word.trim().toLowerCase().replaceAll("[^a-z0-9 ]", "");
	    	if (!stopwords.contains(word) && !word.equals("")){
	    		allWords.add(word);
	    	}
	    }
	}
	
	public String getHtmlText(String html)
	{
		Document doc = Jsoup.parse(html.replaceAll("(?i)<br[^>]*>", "<pre>\n</pre>"));
		String textContent = doc.select("body").text();
		return textContent;
	}
	private DocumentRecord getDocument(BytesWritable value) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		DocumentRecord doc=null;
		try {
			doc = mapper.readValue(new String(value.getBytes()), DocumentRecord.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return doc;
	}
	
//	public String stem(String word)
//	{
//		System.out.println("received word: " + word);
//		Stemmer stemmer = new Stemmer();
//		char[] charArray = word.toCharArray();
//		stemmer.add(charArray, word.length());
//		stemmer.stem();
//		String stemmedWord = stemmer.toString();
//		return stemmedWord;
//	}
	
	private static ArrayList<String> stopwords =
			new ArrayList<String> (Arrays.asList(("a,about,above,"
					+ "after,again,against,all,am,an,and,any,are,"
					+ "aren't,as,at,be,because,been,before,being,"
					+ "below,between,both,but,by,can't,cannot,could,"
					+ "couldn't,did,didn't,do,does,doesn't,doing,don't,"
					+ "down,during,each,few,for,from,further,had,hadn't,"
					+ "has,hasn't,have,haven't,having,he,he'd,he'll,he's,"
					+ "her,here,here's,hers,herself,him,himself,his,how,"
					+ "how's,i,i'd,i'll,i'm,i've,if,in,into,is,isn't,it,"
					+ "it's,its,itself,let's,me,more,most,mustn't,my,myself,"
					+ "no,nor,not,of,off,on,once,only,or,other,ought,our,ours,"
					+ "ourselves,out,over,own,same,shan't,she,she'd,she'll,"
					+ "she's,should,shouldn't,so,some,such,than,that,that's,"
					+ "the,their,theirs,them,themselves,then,there,there's,"
					+ "these,they,they'd,they'll,they're,they've,this,those,"
					+ "through,to,too,under,until,up,very,was,wasn't,we,we'd,"
					+ "we'll,we're,we've,were,weren't,what,what's,when,when's,"
					+ "where,where's,which,while,who,who's,whom,why,why's,with,"
					+ "won't,would,wouldn't,you,you'd,you'll,you're,you've,your,"
					+ "yours,yourself,yourselves,").split(",")));
}