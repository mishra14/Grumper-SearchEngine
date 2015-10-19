package edu.upenn.cis455.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Httpresponse class to hold http response data
 * 
 * @author cis455
 *
 */
public class HttpResponse {
	private String protocol;
	private String version;
	private String responseCode;
	private String responseCodeString;
	private Map<String, List<String>> headers;
	private String data;

	public HttpResponse(String protocol, String version, String responseCode,
			String responseCodeString, Map<String, List<String>> headers,
			String data) {
		super();
		this.protocol = protocol;
		this.version = version;
		this.responseCode = responseCode;
		this.responseCodeString = responseCodeString;
		this.headers = headers;
		this.data = data;
	}

	public HttpResponse(String protocol, String version, String responseCode,
			String responseCodeString, Map<String, List<String>> headers) {
		super();
		this.protocol = protocol;
		this.version = version;
		this.responseCode = responseCode;
		this.responseCodeString = responseCodeString;
		this.headers = headers;
	}

	public HttpResponse() {
		this.protocol = "";
		this.version = "";
		this.responseCode = "";
		this.responseCodeString = "";
		this.headers = new HashMap<String, List<String>>();
		this.data = "";
	}

	public String getResponseString() {
		StringBuilder response = new StringBuilder();
		response.append(protocol + "/" + version + " " + responseCode + " "
				+ responseCodeString + "\r\n");
		if (headers != null) {
			for (Map.Entry<String, List<String>> header : headers
					.entrySet()) {
				StringBuilder headerString = new StringBuilder();
				for (int i = 0; i < header.getValue().size(); i++) {
					headerString.append(header.getValue().get(i));
					if (i < header.getValue().size() - 1) {
						headerString.append(", ");
					}
				}
				response.append(header.getKey() + ":" + headerString.toString()
						+ "\r\n");
			}
		}
		response.append("\r\n");
		if (data != null) {
			response.append(data);
		}
		return response.toString();
	}

	public String getResponseStringHeadersOnly() {
		StringBuilder response = new StringBuilder();
		response.append(protocol + "/" + version + " " + responseCode + " "
				+ responseCodeString + "\r\n");
		if (headers != null) {
			for (Map.Entry<String, List<String>> header : headers
					.entrySet()) {
				StringBuilder headerString = new StringBuilder();
				for (int i = 0; i < header.getValue().size(); i++) {
					headerString.append(header.getValue().get(i));
					if (i < header.getValue().size() - 1) {
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

	@Override
	public String toString() {
		return "HttpResponse [protocol=" + protocol + ", version=" + version
				+ ", responseCode=" + responseCode + ", responseCodeString="
				+ responseCodeString + ", headers=" + headers + ", \ndata="
				+ data + "]";
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public String getResponseCodeString() {
		return responseCodeString;
	}

	public void setResponseCodeString(String responseCodeString) {
		this.responseCodeString = responseCodeString;
	}

	public Map<String, List<String>> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, List<String>> map) {
		this.headers = map;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public void reset() {
		this.responseCode = "";
		this.responseCodeString = "";
		this.headers.clear();
		this.data = "";
	}

}