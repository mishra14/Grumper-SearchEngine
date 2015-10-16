package edu.upenn.cis455.xpath;

import java.util.ArrayList;
import java.util.EnumMap;

public class XPathParser {

	/*
	 * Unique symbols in an xPath - /, (, ), [, ], =, @, ",
	 */
	private static enum symbol {
		ForwardSlash, OpenParen, CloseParen, OpenBracket, CloseBracket, Equals, At, DoubleQuote, Text, Contains, Comma
	};

	private static EnumMap<symbol, String> symbolMap = new EnumMap<symbol, String>(
			symbol.class);

	static {
		symbolMap.put(symbol.ForwardSlash, "/");
		symbolMap.put(symbol.OpenParen, "(");
		symbolMap.put(symbol.CloseParen, ")");
		symbolMap.put(symbol.OpenBracket, "[");
		symbolMap.put(symbol.CloseBracket, "]");
		symbolMap.put(symbol.At, "@");
		symbolMap.put(symbol.DoubleQuote, "\"");
		symbolMap.put(symbol.Text, "text");
		symbolMap.put(symbol.Contains, "contains");
		symbolMap.put(symbol.Comma, ",");
		symbolMap.put(symbol.Equals, "=");

	}

	// method to match a token to a special symbol
	private static boolean matchToken(String token, symbol symbol) {
		// System.out.println("Matching token - "+token+" with "+symbolMap.get(symbol));
		return token.equalsIgnoreCase(symbolMap.get(symbol));
	}

	// parsing xpath to check for validity
	public static void parseXPath(XPath xPath) {
		XPathStep rootStep = xPath(xPath.getTokens(), 0);
		if (rootStep == null
				|| rootStep.getAfterStep() != xPath.getTokens().size()) {
			// System.out.println("Invalid path - "+xPath.getxPath());
			xPath.setValid(false);
		} else {
			// System.out.println("Valid path - "+xPath.getxPath());
			xPath.setRootStep(xPath(xPath.getTokens(), 0));
			xPath.setValid(true);
		}
	}

	// Start validation from root
	private static XPathStep xPath(ArrayList<String> tokens, int next) {
		XPathStep step = null;
		if ((axis(tokens, next++))) {
			step = step(tokens, next++);
			if (step != null) {
				int afterStep = step.getAfterStep();
				// System.out.println("Final pointer - "+afterStep);
				// System.out.println("Total tokens - "+tokens.size());
				if (afterStep == tokens.size()) {
					return step;
				}
				/*
				 * else { step = null; }
				 */
			}
		}
		return step;
	}

	// verify if an axis is correct
	private static boolean axis(ArrayList<String> tokens, int next) {
		if (tokens.size() <= next) {
			return false;
		}
		// System.out.println("In axis with - "+tokens.get(next));
		return matchToken(tokens.get(next++), symbol.ForwardSlash);
	}

	// verify if a step is correct
	private static XPathStep step(ArrayList<String> tokens, int next) {
		XPathStep step = null;
		// System.out.println("Begin step with - "+next);
		if (tokens.size() <= next || tokens.get(next).contains("::")
				|| !tokens.get(next).matches("[a-zA-Z0-9\\-]*")) {
			return step;
		}
		// check if the first token is a nodename - check if the first token is
		// not a special symbol
		step = new XPathStep();
		step.setNodeName(tokens.get(next));
		step.setTests(recursiveTest(tokens, next + 1));
		// System.out.println("Tests - "+step.getTests());
		// then check if there are 0 or more [test]
		// System.out.println("before recursive test - "+(next+1));
		if (step.getTests() == null) {
			next = next + 1;
		} else {
			next = step.getTests().get(step.getTests().size() - 1)
					.getAfterTest() + 1;
		}
		// then check if there are 0 or 1 axis step
		// System.out.println("After recursive test - "+next);
		// System.out.println("Before checking for axis step - "+next);
		int beforeAxisStep = next;
		if (axis(tokens, next++)) {
			XPathStep nextStep = step(tokens, next++);
			if (nextStep == null) {
				next = beforeAxisStep;
			} else {
				next = nextStep.getAfterStep();
				step.setNextStep(nextStep);
			}
		} else {
			next = beforeAxisStep;
		}
		// System.out.println("After axis step - "+next);
		/*
		 * if(next<=beforeStep+1 && next<tokens.size()) { next=beforeStep; }
		 */
		// System.out.println("End of step - "+next);
		step.setAfterStep(next);
		return step;
	}

	private static ArrayList<XPathTest> recursiveTest(ArrayList<String> tokens,
			int next) {
		ArrayList<XPathTest> tests = null;
		// System.out.println("In recursive Test with - "+next);
		int afterAllTests = next;
		if (tokens.size() <= next) {
			return tests;
		}
		if (matchToken(tokens.get(next), symbol.OpenBracket)) {
			// System.out.println("[");
			// System.out.println("Before test - "+next);
			XPathTest test = test(tokens, next + 1);
			if (test == null) {
				return tests;
			} else {
				tests = new ArrayList<XPathTest>();
				tests.add(test);
				// System.out.println("Test - "+test);
				if (matchToken(tokens.get(test.getAfterTest()),
						symbol.CloseBracket)) {
					// System.out.println("]");
					afterAllTests = test.getAfterTest() + 1;
					ArrayList<XPathTest> otherTests = recursiveTest(tokens,
							afterAllTests);
					if (otherTests != null) {
						tests.addAll(otherTests);
					}
				}
			}
			/*
			 * int afterTest = test(tokens, next+1);
			 * //System.out.println("After test - "+afterTest);
			 * if(afterTest>(next+1)) { if(tokens.size()<=afterTest) { return
			 * next; } if(matchToken(tokens.get(afterTest),
			 * symbol.CloseBracket)) { //System.out.println("]"); afterAllTests
			 * = afterTest+1; afterAllTests = recursiveTest(tokens,
			 * afterAllTests); } }
			 */
		}
		return tests;
	}

	private static XPathTest test(ArrayList<String> tokens, int next) {
		XPathTest test = null;
		if (tokens.size() <= next) {
			return test;
		}
		// test can be a step or a [text()=".."] or a [contains(text(),".."] or
		// [@attname = ".."]
		if (matchToken(tokens.get(next), symbol.Text)
				&& tokens.size() >= next + 7) {
			test = new XPathTest();
			test.setTextFilter(true);
			next++;
			if (matchToken(tokens.get(next++), symbol.OpenParen)
					&& matchToken(tokens.get(next++), symbol.CloseParen)
					&& matchToken(tokens.get(next++), symbol.Equals)
					&& matchToken(tokens.get(next++), symbol.DoubleQuote)) {
				test.setQueryString(tokens.get(next++));
				if (!matchToken(tokens.get(next++), symbol.DoubleQuote)) {
					test = null;
					// System.out.println("In Test, text case matching");
				} else {
					test.setAfterTest(next);
				}
			}
		} else if (matchToken(tokens.get(next), symbol.Contains)
				&& tokens.size() >= next + 10) {
			test = new XPathTest();
			test.setContainsFilter(true);
			next++;
			if (matchToken(tokens.get(next++), symbol.OpenParen)
					&& matchToken(tokens.get(next++), symbol.Text)
					&& matchToken(tokens.get(next++), symbol.OpenParen)
					&& matchToken(tokens.get(next++), symbol.CloseParen)
					&& matchToken(tokens.get(next++), symbol.Comma)
					&& matchToken(tokens.get(next++), symbol.DoubleQuote)) {
				test.setQueryString(tokens.get(next++));
				if (!matchToken(tokens.get(next++), symbol.DoubleQuote)) {
					test = null;
					// System.out.println("In Test, contains case matching");
				} else {
					test.setAfterTest(next);
				}
			}
		} else if (matchToken(tokens.get(next), symbol.At)) {
			test = new XPathTest();
			test.setAttFilter(true);
			next++;
			test.setAttName(tokens.get(next++));
			if (matchToken(tokens.get(next++), symbol.Equals)) {
				if (matchToken(tokens.get(next++), symbol.DoubleQuote)) {
					test.setQueryString(tokens.get(next++));
					if (!matchToken(tokens.get(next++), symbol.DoubleQuote)) {
						test = null;
						// System.out.println("In Test, At case matching");
					} else {
						test.setAfterTest(next);
					}
				}

			}
		} else {
			// System.out.println("In Test, checking step at - "+next+" with "+tokens.get(next));
			XPathStep step = step(tokens, next);
			if (step != null) {
				test = new XPathTest();
				test.setStep(true);
				test.setStep(step);
				test.setAfterTest(step.getAfterStep());
			}
			// System.out.println("In Test after step matching - "+tokens.get(next)+" "+matchToken(tokens.get(next),
			// symbol.Text));
		}
		// System.out.println("Test - "+test);
		return test;
	}
}
