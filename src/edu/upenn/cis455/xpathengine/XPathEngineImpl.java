package edu.upenn.cis455.xpathengine;

import java.io.InputStream;

import org.w3c.dom.Document;
import org.xml.sax.helpers.DefaultHandler;

import edu.upenn.cis455.xpath.DomParser;
import edu.upenn.cis455.xpath.XPath;
import edu.upenn.cis455.xpath.XPathParser;

public class XPathEngineImpl implements XPathEngine {

	private String[] xPathStrings;
	private XPath[] xPaths;

	public XPathEngineImpl() {
		// Do NOT add arguments to the constructor!!
	}

	public void setXPaths(String[] xPathArray) {
		xPathStrings = xPathArray;
		xPaths = new XPath[xPathStrings.length];
		for (int i = 0; i < xPathStrings.length; i++) {
			xPaths[i] = new XPath(xPathStrings[i]);
			xPaths[i].tokenize();
			XPathParser.parseXPath(xPaths[i]);
		}
	}

	public boolean isValid(int i) {
		if (i >= xPaths.length || i < 0 || !xPaths[i].isValid()) {
			return false;
		} else {
			return true;
		}
	}

	public boolean[] evaluate(Document document) {
		if (xPaths == null || xPaths.length == 0) {
			return null;
		}
		boolean[] result = new boolean[xPaths.length];
		for (int i = 0; i < result.length; i++) {
			if (xPaths[i].isValid()) {
				result[i] = DomParser.parseDom(document, xPaths[i]);
			} else {
				result[i] = false;
			}
		}
		return result;
	}

	@Override
	public boolean isSAX() {
		return false;
	}

	@Override
	public boolean[] evaluateSAX(InputStream document, DefaultHandler handler) {
		return null;
	}

}
