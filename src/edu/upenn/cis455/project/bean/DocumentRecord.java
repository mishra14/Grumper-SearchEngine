package edu.upenn.cis455.project.bean;

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

// TODO: Auto-generated Javadoc
/**
 * Entity class to hold document records.
 *
 * @author cis455
 */
@Entity
public class DocumentRecord
{

	/** The document id. */
	@PrimaryKey
	private String documentId;

	/** The document string. */
	private String documentString;

	/** The html. */
	private boolean html;

	/** The xml. */
	private boolean xml;

	/** The last crawled. */
	private long lastCrawled;

	/** The Constant DEFAULT_ENCODING. */
	private static final String DEFAULT_ENCODING = "utf-8";

	/**
	 * Instantiates a new document record.
	 */
	public DocumentRecord()
	{

	}

	/**
	 * Instantiates a new document record.
	 *
	 * @param documentId
	 *            the document id
	 * @param documentString
	 *            the document string
	 * @param html
	 *            the html
	 * @param xml
	 *            the xml
	 * @param lastCrawled
	 *            the last crawled
	 */
	public DocumentRecord(String documentId, String documentString,
			boolean html, boolean xml, long lastCrawled)
	{
		super();
		this.documentId = documentId;
		this.documentString = documentString;
		this.html = html;
		this.xml = xml;
		this.lastCrawled = lastCrawled;
	}

	/**
	 * Instantiates a new document record.
	 *
	 * @param documentId
	 *            the document id
	 * @param documentString
	 *            the document string
	 * @param lastCrawled
	 *            the last crawled
	 */
	public DocumentRecord(String documentId, String documentString,
			long lastCrawled)
	{
		super();
		this.documentId = documentId;
		this.documentString = documentString;
		this.html = true;
		this.lastCrawled = lastCrawled;
	}

	/**
	 * Gets the document id.
	 *
	 * @return the document id
	 */
	public String getDocumentId()
	{
		return documentId;
	}

	/**
	 * Sets the document id.
	 *
	 * @param documentId
	 *            the new document id
	 */
	public void setDocumentId(String documentId)
	{
		this.documentId = documentId;
	}

	/**
	 * Gets the document string.
	 *
	 * @return the document string
	 */
	public String getDocumentString()
	{
		return documentString;
	}

	/**
	 * Sets the document.
	 *
	 * @param documentString
	 *            the new document
	 */
	public void setDocument(String documentString)
	{
		this.documentString = documentString;
	}

	/**
	 * Checks if is html.
	 *
	 * @return true, if is html
	 */
	public boolean isHtml()
	{
		return html;
	}

	/**
	 * Sets the html.
	 *
	 * @param html
	 *            the new html
	 */
	public void setHtml(boolean html)
	{
		this.html = html;
	}

	/**
	 * Checks if is xml.
	 *
	 * @return true, if is xml
	 */
	public boolean isXml()
	{
		return xml;
	}

	/**
	 * Gets the last crawled.
	 *
	 * @return the last crawled
	 */
	public long getLastCrawled()
	{
		return lastCrawled;
	}

	/**
	 * Sets the last crawled.
	 *
	 * @param lastCrawled
	 *            the new last crawled
	 */
	public void setLastCrawled(long lastCrawled)
	{
		this.lastCrawled = lastCrawled;
	}

	/**
	 * Gets the date.
	 *
	 * @param dateString
	 *            the date string
	 * @return the date
	 */
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

	/**
	 * Gets the document.
	 *
	 * @return the document
	 * @throws ParserConfigurationException
	 *             the parser configuration exception
	 * @throws SAXException
	 *             the SAX exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public Document getDocument() throws ParserConfigurationException,
			SAXException, IOException
	{
		Document document = null;
		if (isHtml() && documentString != null)
		{
			Tidy tidy = new Tidy();
			tidy.setQuiet(true);
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "DocumentRecord [documentId=" + documentId + ", documentString="
				+ documentString + ", html=" + html + ", xml=" + xml
				+ ", lastCrawled=" + lastCrawled + "]";
	}

}
