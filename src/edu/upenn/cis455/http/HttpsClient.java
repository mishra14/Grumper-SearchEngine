package edu.upenn.cis455.http;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.UnknownHostException;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

/**
 * This class creates an HttpsClient that can fetch documents from a given https url
 * 
 * @author cis455
 *
 */
public class HttpsClient {
	
	private URL sourceUrl;
	private static final String CONTENT_TYPE_HEADER = "Content-Type";
	private static final String XML = "xml";
	private static final String HTML = "html";
	private static final String DEFAULT_ENCODING = "utf-8";
	
	public HttpsClient(URL url) throws UnknownHostException, IOException {
		sourceUrl = url;
	}

	/**
	 * fetches the document from the given url
	 * 
	 * @return
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public Document getDocument() throws UnknownHostException, IOException,
			SAXException, ParserConfigurationException {
		Document document = null;
		sendRequest();
		HttpResponse response = parseResponse();
		//System.out.println(response);
		if (response.getResponseCode().equals("200")
				&& response.getHeaders().containsKey(CONTENT_TYPE_HEADER)) {
			if (response.getHeaders().get(CONTENT_TYPE_HEADER).get(0)
					.contains(HTML)) {
				String html = response.getData();
				Tidy tidy = new Tidy();
				tidy.setInputEncoding(DEFAULT_ENCODING);
				tidy.setOutputEncoding(DEFAULT_ENCODING);
				tidy.setWraplen(Integer.MAX_VALUE);
				tidy.setPrintBodyOnly(true);
				tidy.setXmlOut(true);
				tidy.setSmartIndent(true);
				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
						html.getBytes(DEFAULT_ENCODING));
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				document = tidy.parseDOM(byteArrayInputStream,
						byteArrayOutputStream);
			} else if (response.getHeaders().get(CONTENT_TYPE_HEADER).get(0)
					.contains(XML)) {
				InputStream documentInputStream = new ByteArrayInputStream(
						response.getData().getBytes(DEFAULT_ENCODING));
				DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder documentBuilder = documentBuilderFactory
						.newDocumentBuilder();
				document = documentBuilder.parse(documentInputStream);
				document.normalize();
			}
		}

		return document;
	}

	public void sendRequest() throws IOException {
		HttpsURLConnection connection = (HttpsURLConnection) sourceUrl
				.openConnection();
		connection.setDoOutput(true);
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());
		outputStreamWriter.write("GET " + sourceUrl + " HTTPS/1.0\r\n");
		outputStreamWriter.write("User-Agent: cis455crawler\r\n");
		outputStreamWriter.write("Accept: text/html,application/xml\r\n");
		outputStreamWriter.write("\r\n");
		outputStreamWriter.flush();
		outputStreamWriter.close();
	}

	public HttpResponse parseResponse() throws IOException {
		HttpsURLConnection connection = (HttpsURLConnection) sourceUrl
				.openConnection();
		connection.setDoInput(true);
		InputStreamReader socketInputStreamReader = new InputStreamReader(
				connection.getInputStream());
		BufferedReader socketBufferedReader = new BufferedReader(
				socketInputStreamReader);
		HttpResponse response = parseResponse(socketBufferedReader);
		response.setResponseCode(""+connection.getResponseCode());
		//get the first line - 
		if( connection.getHeaderFields()!=null && connection.getHeaderFields().get(null) !=null && connection.getHeaderFields().get(null).get(0)!=null)
		{
			String[] firstLineSplit = connection.getHeaderFields().get(null).get(0).split(" ");
			if (firstLineSplit.length < 3) {
				return null;
			}
			if (firstLineSplit[0].trim().split("/").length < 2) {
				return null;
			}
			response.setProtocol((firstLineSplit[0].trim().split("/")[0]));
			response.setVersion((firstLineSplit[0].trim().split("/")[1]));
			response.setResponseCode(firstLineSplit[1].trim());
			response.setResponseCodeString(firstLineSplit[2].trim());
		}

		response.setHeaders(connection.getHeaderFields());
		socketBufferedReader.close();
		socketInputStreamReader.close();
		return response;
	}

	/**
	 * parses the http response from the server into an HttpResponse object
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public HttpResponse parseResponse(BufferedReader in) throws IOException {
		HttpResponse response = new HttpResponse();
		String line;
		StringBuilder responseBody = new StringBuilder();
		while ((line = in.readLine()) != null) {
			responseBody.append(line + "\r\n");
		}
		response.setData(responseBody.toString());
		return response;
	}

}
