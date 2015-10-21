package edu.upenn.cis455.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import edu.upenn.cis455.bean.DocumentRecord;
import edu.upenn.cis455.crawler.info.RobotsTxtInfo;

/**
 * This class creates an HttpClient that can fetch documents from a given url
 * 
 * @author cis455
 *
 */
public class HttpClient {
	private URL sourceUrl;
	private int port;
	private String host;
	private Socket socket;
	private static final String CONTENT_TYPE_HEADER = "content-type";
	private static final String CONTENT_LENGTH_HEADER = "content-length";
	private static final String XML = "xml";
	private static final String HTML = "html";

	public HttpClient(URL url) throws UnknownHostException, IOException {
		sourceUrl = url;
		host = sourceUrl.getHost();
		port = sourceUrl.getPort() == -1 ? sourceUrl.getDefaultPort()
				: sourceUrl.getPort();
		socket = new Socket(host, port);
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
	public DocumentRecord getDocument() throws UnknownHostException,
			IOException, SAXException, ParserConfigurationException {
		boolean isHtml = false;
		;
		boolean isXml = false;
		DocumentRecord documentRecord = null;
		// first send head
		HttpResponse response = sendHead();
		// System.out.println(sourceUrl + " - " + response);
		if (response != null && response.getHeaders() != null
				&& response.getResponseCode().equals("200")
				&& response.getHeaders().containsKey(CONTENT_LENGTH_HEADER)
				&& response.getHeaders().containsKey(CONTENT_TYPE_HEADER)) {
			String contentType = response.getHeaders().get(CONTENT_TYPE_HEADER)
					.get(0);
			int contentLength = Integer.valueOf(response.getHeaders()
					.get(CONTENT_LENGTH_HEADER).get(0));

			if (!(contentType.contains(HTML) || contentType.contains(XML))
					|| contentLength > XPathCrawler.getMaxSize()) {
				// return if document is not xml || html or if length is larger
				// than max size
				System.out
						.println(sourceUrl
								+ " : Not fetching file due to type mis match or larger size");
				System.out.println("contentType - " + contentType);
				System.out.println("contentLength - " + contentLength);
				System.out.println("max size - " + XPathCrawler.getMaxSize());
				return documentRecord;
			}
		} else {
			return documentRecord;
		}
		socket = new Socket(host, port);
		// then send get
		response = sendFileRequest();
		// System.out.println(response);

		if (response != null && response.getResponseCode() != null
				&& response.getResponseCode().equals("200")
				&& response.getHeaders().containsKey(CONTENT_TYPE_HEADER)) {
			if (response.getHeaders().get(CONTENT_TYPE_HEADER).get(0)
					.contains(HTML)) {
				isHtml = true;
			} else if (response.getHeaders().get(CONTENT_TYPE_HEADER).get(0)
					.contains(XML)) {
				isXml = true;
			}
			long lastCrawled = (new Date()).getTime();
			if (response.getHeaders().containsKey("Date")) {
				String dateString = response.getHeaders().get("Date").get(0);
				Date date = DocumentRecord.getDate(dateString);
				if (date != null) {
					lastCrawled = date.getTime();
				}
			}
			documentRecord = new DocumentRecord(sourceUrl.toString(),
					response.getData(), isHtml, isXml, lastCrawled);
		}

		return documentRecord;
	}

	public RobotsTxtInfo getRobotsTxt() throws IOException {
		RobotsTxtInfo info = null;
		HttpResponse response = sendRobotsTxtRequest();
		System.out.println(sourceUrl + " - " + response);
		if (response != null && response.getData() != null
				&& response.getResponseCode().equals("200")) {
			info = RobotsTxtInfo.parseRobotsTxt(response.getData());
		}
		return info;
	}

	public HttpResponse sendRobotsTxtRequest() throws IOException {
		PrintWriter clientSocketOut = new PrintWriter(new OutputStreamWriter(
				socket.getOutputStream()));
		clientSocketOut.print("GET " + sourceUrl + " HTTP/1.0\r\n");
		clientSocketOut.print("User-Agent: cis455crawler\r\n");
		clientSocketOut.print("Accept: text/plain\r\n");
		clientSocketOut.print("\r\n");
		clientSocketOut.flush();
		return parseResponse();
	}

	private HttpResponse sendHead() throws IOException {
		PrintWriter clientSocketOut = new PrintWriter(new OutputStreamWriter(
				socket.getOutputStream()));
		clientSocketOut.print("HEAD " + sourceUrl + " HTTP/1.0\r\n");
		clientSocketOut.print("User-Agent: cis455crawler\r\n");
		clientSocketOut.print("\r\n");
		clientSocketOut.flush();
		return parseResponse();
	}

	public HttpResponse sendFileRequest() throws IOException {
		PrintWriter clientSocketOut = new PrintWriter(new OutputStreamWriter(
				socket.getOutputStream()));
		clientSocketOut.print("GET " + sourceUrl + " HTTP/1.0\r\n");
		clientSocketOut.print("User-Agent: cis455crawler\r\n");
		clientSocketOut
				.print("Accept: text/html,application/xml, text/xml, application/rdf+xml, application/xslt+xml, application/mathml+xml, application/xml-dtd,application/xml-external-parsed-entity, text/xml-external-parsed-entity,\r\n");
		clientSocketOut.print("\r\n");
		clientSocketOut.flush();
		return parseResponse();
	}

	public HttpResponse parseResponse() throws IOException {
		InputStream socketInputStream = socket.getInputStream();
		InputStreamReader socketInputStreamReader = new InputStreamReader(
				socketInputStream);
		BufferedReader socketBufferedReader = new BufferedReader(
				socketInputStreamReader);
		HttpResponse response = parseResponse(socketBufferedReader);
		socketBufferedReader.close();
		socketInputStreamReader.close();
		socketInputStream.close();
		socket.close();
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
		String line = in.readLine();
		if (line != null) {
			String[] firstLineSplit = line.trim().split(" ", 3);
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
			Map<String, List<String>> headers = new HashMap<String, List<String>>();
			while ((line = in.readLine()) != null) {
				if (line.equals("")) {
					break;
				}
				String[] lineSplit = line.trim().split(":", 2);
				if (lineSplit.length == 2) {
					if (headers.containsKey(lineSplit[0].toLowerCase().trim())) {
						headers.get(lineSplit[0]).add(lineSplit[1].trim());
					} else {
						ArrayList<String> values = new ArrayList<String>();
						values.add(lineSplit[1].trim());
						headers.put(lineSplit[0].toLowerCase().trim(), values);
					}

				}
			}
			StringBuilder responseBody = new StringBuilder();
			while ((line = in.readLine()) != null) {
				responseBody.append(line + "\r\n");
			}
			response.setHeaders(headers);
			response.setData(responseBody.toString());
		} else {
			return null;
		}
		return response;
	}
}
