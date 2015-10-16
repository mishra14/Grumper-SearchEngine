package edu.upenn.cis455.xpath;

import java.util.ArrayList;

/**
 * This class is used to tokenize and parse the xpath expressions based on the
 * following grammar -
 * 
 * XPath -> axis step axis -> / step -> nodename ([ test ])* (axis step)? test
 * -> step -> text() = "..." -> contains(text(), "...") -> @attname = "..."
 * 
 * @author Ankit
 *
 */
public class XPath {

	private String xPath;
	private ArrayList<String> tokens;
	private XPathStep rootStep;
	private boolean isValid;

	/*
	 * Unique symbols in an xPath - /, (, ), [, ], =, @, ",
	 */
	public XPath(String xPath) {
		StringBuilder xPathBuilder = new StringBuilder();
		int quotes = 0;
		for (int i = 0; i < xPath.length(); i++) {
			if (xPath.charAt(i) == '\"') {
				quotes++;
			} else if (xPath.charAt(i) == ' ' && quotes % 2 == 0) {
				continue;
			}
			xPathBuilder.append(xPath.charAt(i));
		}
		this.xPath = xPathBuilder.toString();// xPath.replace(" ", "");
		System.out.println(this.xPath);
	}

	public void tokenize() {
		int last = 0;
		tokens = new ArrayList<String>();
		boolean quote = false;
		int bracket = 0;
		int paran = 0;
		for (int i = 0; i < xPath.length(); i++) {
			char ch = xPath.charAt(i);
			if (ch == '/' || ch == '(' || ch == ')' || ch == '[' || ch == ']'
					|| ch == '=' || ch == '@' || ch == '"') {
				/*
				 * System.out.println("char - "+ch);
				 * System.out.println("Quote - "+quote);
				 * System.out.println("Bracket - "+bracket);
				 * System.out.println("Paran - "+ paran);
				 */
				if (!quote) {
					if (last != i) {
						tokens.add(xPath.substring(last, i));
					}
					tokens.add(xPath.substring(i, i + 1));
					last = i + 1;
				}
				if (!quote && ch == '"') {
					quote = true;
				} else if (!quote && ch == '(') {
					paran++;
				} else if (!quote && ch == '[') {
					bracket++;
				} else if (!quote && ch == ')') {
					paran--;
				} else if (!quote && ch == ']') {
					bracket--;
				} else if (quote
						&& ((bracket > 0 && ch == ']') || (paran > 0 && ch == ')'))) {
					// System.out.println("inside with - "+ch);
					if (last != i) {
						tokens.add(xPath.substring(last, i - 1));
					}
					tokens.add(xPath.substring(i - 1, i));
					tokens.add(xPath.substring(i, i + 1));
					last = i + 1;
					quote = false;
					if (paran > 0 && ch == ')') {
						paran--;
					} else if (bracket > 0 && ch == ']') {
						bracket--;
					}
				}

			}
		}
		if (last != xPath.length()) {
			tokens.add(xPath.substring(last));
		}
		System.out.println("Tokens - \n" + tokens);
	}

	public String getxPath() {
		return xPath;
	}

	public ArrayList<String> getTokens() {
		return tokens;
	}

	public XPathStep getRootStep() {
		return rootStep;
	}

	public void setRootStep(XPathStep rootStep) {
		this.rootStep = rootStep;
	}

	public void setxPath(String xPath) {
		this.xPath = xPath;
	}

	public void setTokens(ArrayList<String> tokens) {
		this.tokens = tokens;
	}

	public boolean isValid() {
		return isValid;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

}
