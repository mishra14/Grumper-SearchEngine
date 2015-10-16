package edu.upenn.cis455.http;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xml.sax.SAXException;

public class HttpClient 
{
	private URL sourceUrl;
	private int port;
	private String host;
	private Socket socket;
	
	public HttpClient(URL url) throws UnknownHostException, IOException
	{
		sourceUrl = url;
		host = sourceUrl.getHost();
		port = sourceUrl.getPort()==-1?sourceUrl.getDefaultPort():sourceUrl.getPort();
		socket = new Socket(host,port);
	}
	
	public Document getDocument() throws UnknownHostException, IOException, SAXException, ParserConfigurationException
	{
		Document document=null;
		sendRequest();
		HttpResponse response = parseResponse();
		System.out.println(response);
		if(response.getResponseCode().equals("200") && response.getHeaders().containsKey("content-type"))
		{
			if(response.getHeaders().get("content-type").get(0).contains("html"))
			{
				System.out.println("HTML detected");
				String html = response.getData();
				Tidy tidy = new Tidy();
			    tidy.setInputEncoding("UTF-8");
			    tidy.setOutputEncoding("UTF-8");
			    tidy.setWraplen(Integer.MAX_VALUE);
			    tidy.setPrintBodyOnly(true);
			    tidy.setXmlOut(true);
			    tidy.setSmartIndent(true);
			    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(html.getBytes("UTF-8"));
			    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			    document = tidy.parseDOM(byteArrayInputStream, byteArrayOutputStream);
			}
			else if(response.getHeaders().get("content-type").get(0).contains("xml"))
			{
				System.out.println("XML detected");
				InputStream documentInputStream = new ByteArrayInputStream( response.getData().getBytes( "UTF-8" ) );
				DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder(); 
				document = documentBuilder.parse(documentInputStream);
				document.normalize();
			}
		}

		return document;
	}
	public void sendRequest() throws IOException
	{
		PrintWriter clientSocketOut = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		clientSocketOut.print("GET "+sourceUrl+" HTTP/1.0\r\n");
		clientSocketOut.print("User-Agent: cis455crawler\r\n");
		clientSocketOut.print("Accept: text/html,application/xml\r\n");
		clientSocketOut.print("\r\n");
		clientSocketOut.flush();
	}
	public HttpResponse parseResponse() throws IOException
	{
		InputStream socketInputStream = socket.getInputStream();
		InputStreamReader socketInputStreamReader = new InputStreamReader(socketInputStream);
		BufferedReader socketBufferedReader = new BufferedReader(socketInputStreamReader);
		System.out.println("before reading");
	    HttpResponse response = parseResponse(socketBufferedReader);
		System.out.println("after reading");
	    socketBufferedReader.close();
	    socketInputStreamReader.close();
	    socketInputStream.close();
	    socket.close();
	    return response;
	}
	public HttpResponse parseResponse(BufferedReader in) throws IOException
	{
		HttpResponse response = new HttpResponse();
		String line = in.readLine();
		if(line!=null)
		{
			String[] firstLineSplit = line.trim().split(" ", 3);
			if(firstLineSplit.length<3)
			{
				return null;
			}
			if(firstLineSplit[0].trim().split("/").length < 2)
			{
				return null;
			}
			response.setProtocol((firstLineSplit[0].trim().split("/")[0]));
			response.setVersion((firstLineSplit[0].trim().split("/")[1]));
			response.setResponseCode(firstLineSplit[1].trim());
			response.setResponseCodeString(firstLineSplit[2].trim());
			Map<String, ArrayList<String>> headers = new HashMap<String, ArrayList<String>>();
			while((line = in.readLine())!=null)
			{
				if(line.equals(""))
				{
					break;
				}
				String[] lineSplit = line.trim().split(":",2);
				if(lineSplit.length==2)
				{
					if(headers.containsKey(lineSplit[0].toLowerCase().trim()))
					{
						headers.get(lineSplit[0]).add(lineSplit[1].trim());
					}
					else
					{
						ArrayList<String> values = new ArrayList<String>();
						values.add(lineSplit[1].trim());
						headers.put(lineSplit[0].toLowerCase().trim(),values);
					}

				}
			}
			StringBuilder responseBody = new StringBuilder();
			while((line = in.readLine())!=null)
			{
				responseBody.append(line+"\r\n");
			}		
			response.setHeaders(headers);
			response.setData(responseBody.toString());
		}
		else
		{
			return null;
		}
		return response;
	}
}
