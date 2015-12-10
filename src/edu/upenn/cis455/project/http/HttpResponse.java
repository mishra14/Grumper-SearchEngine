package edu.upenn.cis455.project.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * Httpresponse class to hold http response data.
 *
 * @author cis455
 */
public class HttpResponse
{

	/** The protocol. */
	private String protocol;

	/** The version. */
	private String version;

	/** The response code. */
	private String responseCode;

	/** The response code string. */
	private String responseCodeString;

	/** The headers. */
	private Map<String, List<String>> headers;

	/** The data. */
	private String data;

	/**
	 * Instantiates a new http response.
	 *
	 * @param protocol
	 *            the protocol
	 * @param version
	 *            the version
	 * @param responseCode
	 *            the response code
	 * @param responseCodeString
	 *            the response code string
	 * @param headers
	 *            the headers
	 * @param data
	 *            the data
	 */
	public HttpResponse(String protocol, String version, String responseCode,
			String responseCodeString, Map<String, List<String>> headers,
			String data)
	{
		super();
		this.protocol = protocol;
		this.version = version;
		this.responseCode = responseCode;
		this.responseCodeString = responseCodeString;
		this.headers = headers;
		this.data = data;
	}

	/**
	 * Instantiates a new http response.
	 *
	 * @param protocol
	 *            the protocol
	 * @param version
	 *            the version
	 * @param responseCode
	 *            the response code
	 * @param responseCodeString
	 *            the response code string
	 * @param headers
	 *            the headers
	 */
	public HttpResponse(String protocol, String version, String responseCode,
			String responseCodeString, Map<String, List<String>> headers)
	{
		super();
		this.protocol = protocol;
		this.version = version;
		this.responseCode = responseCode;
		this.responseCodeString = responseCodeString;
		this.headers = headers;
	}

	/**
	 * Instantiates a new http response.
	 */
	public HttpResponse()
	{
		this.protocol = "";
		this.version = "";
		this.responseCode = "";
		this.responseCodeString = "";
		this.headers = new HashMap<String, List<String>>();
		this.data = "";
	}

	/**
	 * Gets the response string.
	 *
	 * @return the response string
	 */
	public String getResponseString()
	{
		StringBuilder response = new StringBuilder();
		response.append(protocol + "/" + version + " " + responseCode + " "
				+ responseCodeString + "\r\n");
		if (headers != null)
		{
			for (Map.Entry<String, List<String>> header : headers.entrySet())
			{
				StringBuilder headerString = new StringBuilder();
				for (int i = 0; i < header.getValue().size(); i++)
				{
					headerString.append(header.getValue().get(i));
					if (i < header.getValue().size() - 1)
					{
						headerString.append(", ");
					}
				}
				response.append(header.getKey() + ":" + headerString.toString()
						+ "\r\n");
			}
		}
		response.append("\r\n");
		if (data != null)
		{
			response.append(data);
		}
		return response.toString();
	}

	/**
	 * Gets the response string headers only.
	 *
	 * @return the response string headers only
	 */
	public String getResponseStringHeadersOnly()
	{
		StringBuilder response = new StringBuilder();
		response.append(protocol + "/" + version + " " + responseCode + " "
				+ responseCodeString + "\r\n");
		if (headers != null)
		{
			for (Map.Entry<String, List<String>> header : headers.entrySet())
			{
				StringBuilder headerString = new StringBuilder();
				for (int i = 0; i < header.getValue().size(); i++)
				{
					headerString.append(header.getValue().get(i));
					if (i < header.getValue().size() - 1)
					{
						headerString.append(", ");
					}
				}
				response.append(header.getKey() + ":" + headerString.toString()
						+ "\r\n");
			}
		}
		response.append("\r\n");
		return response.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "HttpResponse [protocol=" + protocol + ", version=" + version
				+ ", responseCode=" + responseCode + ", responseCodeString="
				+ responseCodeString + ", headers=" + headers + ", \ndata="
				+ data + "]";
	}

	/**
	 * Gets the protocol.
	 *
	 * @return the protocol
	 */
	public String getProtocol()
	{
		return protocol;
	}

	/**
	 * Sets the protocol.
	 *
	 * @param protocol
	 *            the new protocol
	 */
	public void setProtocol(String protocol)
	{
		this.protocol = protocol;
	}

	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	public String getVersion()
	{
		return version;
	}

	/**
	 * Sets the version.
	 *
	 * @param version
	 *            the new version
	 */
	public void setVersion(String version)
	{
		this.version = version;
	}

	/**
	 * Gets the response code.
	 *
	 * @return the response code
	 */
	public String getResponseCode()
	{
		return responseCode;
	}

	/**
	 * Sets the response code.
	 *
	 * @param responseCode
	 *            the new response code
	 */
	public void setResponseCode(String responseCode)
	{
		this.responseCode = responseCode;
	}

	/**
	 * Gets the response code string.
	 *
	 * @return the response code string
	 */
	public String getResponseCodeString()
	{
		return responseCodeString;
	}

	/**
	 * Sets the response code string.
	 *
	 * @param responseCodeString
	 *            the new response code string
	 */
	public void setResponseCodeString(String responseCodeString)
	{
		this.responseCodeString = responseCodeString;
	}

	/**
	 * Gets the headers.
	 *
	 * @return the headers
	 */
	public Map<String, List<String>> getHeaders()
	{
		return headers;
	}

	/**
	 * Sets the headers.
	 *
	 * @param map
	 *            the map
	 */
	public void setHeaders(Map<String, List<String>> map)
	{
		this.headers = map;
	}

	/**
	 * Gets the data.
	 *
	 * @return the data
	 */
	public String getData()
	{
		return data;
	}

	/**
	 * Sets the data.
	 *
	 * @param data
	 *            the new data
	 */
	public void setData(String data)
	{
		this.data = data;
	}

	/**
	 * Reset.
	 */
	public void reset()
	{
		this.responseCode = "";
		this.responseCodeString = "";
		this.headers.clear();
		this.data = "";
	}

}