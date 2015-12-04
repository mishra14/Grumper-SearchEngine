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
				StringTokenizer tokenizer = new StringTokenizer(rawContent, " ,.?\"!-\t");
				while (tokenizer.hasMoreTokens())
				{
					String currWord = tokenizer.nextToken();
					currWord = currWord.toLowerCase()
							.replaceAll("[^a-z]", "").trim();
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
	
//	public static void main(String[] args){
//		String html = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n<html>\n<head>\n<title>Find Out About IP Search With Hostip.info</title>\n\n<meta name=\"description\" content=\" Hostip.info gives you all the info you need on how to find your IP address or geolocation, as well as on the benefits of using this extensive database.\" />\n\n<meta name=\"keywords\" content=\"IP search, find IP address, search IP address, my computer IP address\" />\n\n <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n    <script type=\"text/javascript\">\n//<![CDATA[\ntry{if (!window.CloudFlare) {var CloudFlare=[{verbose:0,p:0,byc:0,owlid:\"cf\",bag2:1,mirage2:0,oracle:0,paths:{cloudflare:\"/cdn-cgi/nexp/dok3v=1613a3a185/\"},atok:\"5ac921f801587ba797dc2b3681af31c0\",petok:\"f0c2e69b02620faca3cf3628bc080ce65672a42b-1448343220-1800\",zone:\"hostip.info\",rocket:\"0\",apps:{\"ga_key\":{\"ua\":\"UA-6801622-16\",\"ga_bs\":\"2\"}},sha2test:0}];!function(a,b){a=document.createElement(\"script\"),b=document.getElementsByTagName(\"script\")[0],a.async=!0,a.src=\"//ajax.cloudflare.com/cdn-cgi/nexp/dok3v=6f4db11806/cloudflare.min.js\",b.parentNode.insertBefore(a,b)}()}}catch(e){};\n//]]>\n</script>\n<link rel=\"stylesheet\" type=\"text/css\" href=\"css/global.css?1448343220\" media=\"all\">\n\n <!-- Google Analytics -->\n<script type=\"text/javascript\">\n\n  var _gaq = _gaq || [];\n  _gaq.push(['_setAccount', 'UA-6801622-16']);\n  _gaq.push(['_setDomainName', 'hostip.info']);\n  _gaq.push(['_trackPageview']);\n\n  (function() {\n    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;\n    ga.src = ('https:' == document.location.protocol ? 'https://' : 'http://') + 'stats.g.doubleclick.net/dc.js';\n    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);\n  })();\n\n</script>\n\n<script type=\"text/javascript\">\n/* <![CDATA[ */\nvar _gaq = _gaq || [];\n_gaq.push(['_setAccount', 'UA-6801622-16']);\n_gaq.push(['_trackPageview']);\n\n(function() {\nvar ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;\nga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';\nvar s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);\n})();\n\n(function(b){(function(a){\"__CF\"in b&&\"DJS\"in b.__CF?b.__CF.DJS.push(a):\"addEventListener\"in b?b.addEventListener(\"load\",a,!1):b.attachEvent(\"onload\",a)})(function(){\"FB\"in b&&\"Event\"in FB&&\"subscribe\"in FB.Event&&(FB.Event.subscribe(\"edge.create\",function(a){_gaq.push([\"_trackSocial\",\"facebook\",\"like\",a])}),FB.Event.subscribe(\"edge.remove\",function(a){_gaq.push([\"_trackSocial\",\"facebook\",\"unlike\",a])}),FB.Event.subscribe(\"message.send\",function(a){_gaq.push([\"_trackSocial\",\"facebook\",\"send\",a])}));\"twttr\"in b&&\"events\"in twttr&&\"bind\"in twttr.events&&twttr.events.bind(\"tweet\",function(a){if(a){var b;if(a.target&&a.target.nodeName==\"IFRAME\")a:{if(a=a.target.src){a=a.split(\"#\")[0].match(/[^?=&]+=([^&]*)?/g);b=0;for(var c;c=a[b];++b)if(c.indexOf(\"url\")===0){b=unescape(c.split(\"=\")[1]);break a}}b=void 0}_gaq.push([\"_trackSocial\",\"twitter\",\"tweet\",b])}})})})(window);\n/* ]]> */\n</script>\n</head>\n<body>\n<center>\n<meta name=\"viewport\" content=\"minimal-ui, width=device-width, initial-scale=1, maximum-scale=1, user-scalable=yes\">\n\n<script src=\"//code.jquery.com/jquery-1.11.2.min.js\"></script>\n<script src=\"//code.jquery.com/jquery-migrate-1.2.1.min.js\"></script>\n\n<!-- Latest compiled and minified JavaScript -->\n<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js\"></script>\n<!-- Latest compiled and minified CSS -->\n<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css\">\n\n\n<div style=\"margin:0; width:100%\">\n\n        <div id=\"header\" style=\"width:320px; height:135px;\">\n            <div style=\"width:320px; height:55px;\"></div>\n            <img src=\"/images/frontbanner2.png\"><br>\n            <h1 style=\"color: rgb(90, 90, 90); font-size: 10px; padding: 0; text-align: left; margin: 0; top:-10px; left:0px; position:relative;\">My IP Address Lookup and GeoTargeting Community<br>Geotarget IP Project &ndash; what country, city IP addresses map to</h1>\n        </div>\n\n<nav class=\"navbar navbar-default navbar-fixed-top\">\n    <div class=\"container-fluid\">\n        <!-- Brand and toggle get grouped for better mobile display -->\n        <div class=\"navbar-header\">\n            <button type=\"button\" class=\"navbar-toggle collapsed\" data-toggle=\"collapse\" data-target=\"#bs-example-navbar-collapse-1\">\n                <span class=\"sr-only\">Toggle navigation</span>\n                <span class=\"icon-bar\"></span>\n                <span class=\"icon-bar\"></span>\n                <span class=\"icon-bar\"></span>\n            </button>\n            <a class=\"navbar-brand\" style=\"height:36px; padding:5px;\" href=\"#\"><img src=\"/images/brand.png\"></a>\n        </div>\n        <div class=\"collapse navbar-collapse\" id=\"bs-example-navbar-collapse-1\">\n            <ul class=\"nav navbar-nav\" role=\"menu\" style=\"text-align: left;\">\n\n                <li><a href=\"/\">IP Address Lookup</a></li>\n                <li><a href=\"/use.html\">API</a></li>\n                <li><a href=\"/dl/index.html\">Data</a></li>\n                <li><a href=\"/contrib/index.html\">Contribute</a></li>\n                <li><a href=\"/ipaddress_forum\">Forum</a></li>\n                <li><a href=\"/faq.html\">FAQ</a></li>\n                <li><a href=\"/about.html\">About</a></li>\n                <li><a href=\"http://ecommerce.hostip.info/\">Ecommerce</a></li>\n\n            </ul>\n\n        </div>\n\n\n    </div><!-- /.container-fluid -->\n</nav>\n\n</div><table id=\"content\">\n<tr>\n\t<td valign=\"top\" align=\"left\" width=\"700\">\n\t<h1>Should I be worried about being located like this ?</h1>\n\t<div class=\"info\">\n\tWell, there are two schools of thought on that one. \"Yes\", and \"No\" :-) I \n\ttend to fall into the \"No\" camp, and I'll try and explain why on this page.\n\t<p>\n\tThere are two possible sources of worry over being located. Either you've \n\tgot 'Authority' (in whatever manifestation) after you or you're worried about\n\tspammers (sorry - Internet direct-marketeers. Spit.)\n\t<p>\n\tIf you've got 'Authority' on your case, there's nothing here that they don't\n\talready have access to, and in fact they'll know a lot more. They'll know\n\texactly when you logged on (if you're DHCP or fixed-ip, modem or leased-line),\n\tand probably even have a packet-log for recent stuff. Face it. They've got you.\n\t<p>\n\tSo, ignoring 'Authority', we're left with spammers. There are a few points I\n\twould make as to why the site isn't the panacea for spammers that some would\n\tbelieve...\n\t<ul>\n\n\t<li>Spammers / Internet marketers already have access to far more-detailed \n\tcommercial databases than I could realistically provide. There are a few \n\tcompanies (quova,maxmind,others) who will sell (at not a huge cost) their \n\tDB to anyone. They have hardware at the major internet routing nodes doing \n\tthis automatically using statistical analysis of traffic between already-known \n\tplaces. Hostip was an attempt to level the playing field, not provide a\n\tvehicle for spammers.\n\n\t<li>Spam by its nature is (currently, at least) a mass-delivery \"shotgun\"-type\n\tapproach. They don't care where you are, what sex you are (which is surprising\n\tconsidering most of the products!), what age you are, etc. etc.\n\t\n\t<li>Should spammers ever move to a more 'sniper'-style delivery using hostip\n\tto locate localised businesses, I would shut the site down. I consider them \n\tto be the scum of the Earth, too. The database is surprisingly fluid, within \n\ta few months it would be pretty much useless apart from huge institutions, \n\twhich can probably fend for themselves. For a 'sniper'-style approach to work,\n\tI'd expect them to need more info than what geographic area you live in, \n\tas well...\n\n\t<li>Hostip only locates you to your city. You are still reasonably anonymous\n\twithin a city of 100,000 people! If you're in a small township, you're almost\n\tcertainly on a DHCP circuit, and the system won't cope well, anyway. At\n\tthe moment, hostip will just respond with the last location to register \n\tany given IP, which on a widely-distributed DHCP network could be hundreds \n\tor thousands of miles away! I think both large and small are covered here...\n\n\t<li>Hostip doesn't allow reverse lookups (what IP addresses does this city \n\thave allocated to it) even though it'd be a trivial operation. This is\n\tan attempt to block abusers, since I can't think of any use for that\n\tinformation.\n\t</ul>\n\t<p>\n\tI made this site up, mainly to see if I could, and to see if people\n\twould band together to collectively create something like this. It's looking \n\tgood so far :-) One of my goals is a graphical 'traceroute' program, and a \n\twebstats program that lets me see where my visitors came from on a globe... \n\tjust because I'm curious.\n\t<p>\n\tIf you feel you have to pollute the DB, then by all means do what you feel\n\tis right. It's sort of disappointing given the work I've put into the site, \n\tbut it'd be naive to think that everyone has put in their correct location. \n\t<p>\n\tFor what it's worth as a final thought, I don't think hostip contributes in any \n\tway to spammers targetting you. I think other agencies (govt,police,your ISP)\n\thave far easier and more-effective methods to track you, so it's no use to \n\tthem either. It's just a \"cool tool\" for geeks like me :-)<br/>\n\t</div>\n\n\t<h1>Who owns Hostip.info?</h1>\n\t<div class=\"info\">\n\tHostip.info is an open project.  There are many mashups, plugins, and APIs to choose from, all contributed by the community as part of an ongoing open-source effort.  The most important resource, obviously, is the data, which is completely free and also owned by, and contributed to by the community.  The Hostip.info operations (hosting and bandwidth costs, etc) are sponsored by <a href=\"http://www.netindustries.us/\">Net Industries, LLC</a>.\n\t<br/><br/>\n\t</div>\n\t</td>\n</tr>\n</table>\n<br/>\n\n\n\n<div id=\"footer\">Licensed under the <a href=\"dl/COPYING\" rel=\"nofollow\">GPL</a></div>\n\n\n</center>\n</html>\n\n";
//		Document doc = Jsoup
//				.parse(html.replaceAll("(?i)<br[^>]*>", "<pre>\n</pre>"));
//		String textContent = doc.select("title").text();
//		System.out.println(textContent);
//	}

}
