package edu.upenn.cis455.bean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class DocumentRecord
{

	@PrimaryKey
	private String documentId;
	private String documentString;
	private boolean html;
	private boolean xml;
	private long lastCrawled;

	private static final String DEFAULT_ENCODING = "utf-8";

	public DocumentRecord()
	{

	}

	public DocumentRecord(String documentId, String document, boolean html,
			boolean xml, long lastCrawled)
	{
		super();
		this.documentId = documentId;
		this.documentString = document;
		this.html = html;
		this.xml = xml;
		this.lastCrawled = lastCrawled;
	}

	public String getDocumentId()
	{
		return documentId;
	}

	public void setDocumentId(String documentId)
	{
		this.documentId = documentId;
	}

	public String getDocumentString()
	{
		return documentString;
	}

	public void setDocument(String documentString)
	{
		this.documentString = documentString;
	}

	public boolean isHtml()
	{
		return html;
	}

	public void setHtml(boolean html)
	{
		this.html = html;
	}

	public boolean isXml()
	{
		return xml;
	}

	public long getLastCrawled()
	{
		return lastCrawled;
	}

	public void setLastCrawled(long lastCrawled)
	{
		this.lastCrawled = lastCrawled;
	}

	public static Date getDate(String dateString)
	{
		Date date = new Date();
		ArrayList<SimpleDateFormat> httpDateFormats = new ArrayList<SimpleDateFormat>();
		httpDateFormats.add(new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",
				Locale.ENGLISH));
		httpDateFormats.add(new SimpleDateFormat(
				"EEEEEE, dd-MMM-yy HH:mm:ss z", Locale.ENGLISH));
		httpDateFormats.add(new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy",
				Locale.ENGLISH));
		for (SimpleDateFormat format : httpDateFormats)
		{
			try
			{
				date = format.parse(dateString);
				return date;
			}
			catch (ParseException e)
			{
			}
		}
		return null;
	}

	public Document getDocument() throws ParserConfigurationException,
			SAXException, IOException
	{
		Document document = null;
		if (isHtml() && documentString != null)
		{
			Tidy tidy = new Tidy();
			tidy.setInputEncoding(DEFAULT_ENCODING);
			tidy.setOutputEncoding(DEFAULT_ENCODING);
			tidy.setWraplen(Integer.MAX_VALUE);
			tidy.setPrintBodyOnly(true);
			tidy.setXmlOut(true);
			tidy.setSmartIndent(true);
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
					documentString.getBytes(DEFAULT_ENCODING));
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			document = tidy.parseDOM(byteArrayInputStream,
					byteArrayOutputStream);
		}
		else if (isXml() && documentString != null)
		{
			InputStream documentInputStream = new ByteArrayInputStream(
					documentString.getBytes(DEFAULT_ENCODING));
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory
					.newDocumentBuilder();
			document = documentBuilder.parse(documentInputStream);
			document.normalize();
		}

		return document;
	}

	@Override
	public String toString()
	{
		return "DocumentRecord [documentId=" + documentId + ", documentString="
				+ documentString + ", html=" + html + ", xml=" + xml
				+ ", lastCrawled=" + lastCrawled + "]";
	}

}
