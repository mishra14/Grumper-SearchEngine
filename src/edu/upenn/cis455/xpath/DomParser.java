package edu.upenn.cis455.xpath;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is used to parse a document and validate it against an xpath
 * 
 * @author cis455
 *
 */
public class DomParser
{

	/**
	 * parse the document against the xpath
	 * 
	 * @param document
	 * @param xPath
	 * @return
	 */
	public static boolean parseDom(Document document, XPath xPath)
	{
		boolean result = false;
		if (!(xPath == null || document == null))
		{
			result = validateStep(document.getDocumentElement(),
					xPath.getRootStep());
		}

		return result;
	}

	/**
	 * validate an XPathStep object against the document node root
	 * 
	 * @param root
	 * @param rootStep
	 * @return
	 */
	private static boolean validateStep(Node root, XPathStep rootStep)
	{
		boolean result = false;
		//System.out.println("Root - " + root.getNodeName());
		if (root == null || rootStep == null)
		{
			return false;
		}
		if (root.getNodeName().equals(rootStep.getNodeName())) // node name
																// matches;
																// proceed
																// further
		{
			result = true;
			if (rootStep.getTests() != null)
			{
				for (XPathTest test : rootStep.getTests())
				{
					result = validateTest(root, test);
					if (!result)
					{
						break;
					}

				}
			}
			if (rootStep.getNextStep() != null)
			{
				result = false;
				NodeList children = root.getChildNodes();
				for (int i = 0; i < children.getLength(); i++)
				{
					if (validateStep(children.item(i), rootStep.getNextStep()))
					{
						result = true;
						break;
					}
				}
			}
		}
		return result;
	}

	/**
	 * validate the given XPathTest object against the document Node root
	 * 
	 * @param root
	 * @param test
	 * @return
	 */

	private static boolean validateTest(Node root, XPathTest test)
	{
		boolean result = false;

		// test can be a text()= ".."
		if (test.isTextFilter())
		{
			String textContent = root.getTextContent();
			if (textContent != null
					&& textContent.contains(test.getQueryString()))
			{
				result = true;
			}
		}
		// test can be a contains(text(), "..")
		else if (test.isContainsFilter())
		{
			String textContent = root.getTextContent();
			if (textContent != null
					&& textContent.equals(test.getQueryString()))
			{
				result = true;
			}
		}
		// test can be a @att= ".."
		else if (test.isAttFilter())
		{
			NamedNodeMap attributeMap = root.getAttributes();
			if (attributeMap.getNamedItem(test.getAttName()) != null)
			{
				//System.out.println(attributeMap.getNamedItem(test.getAttName()));
				String attributeValue = attributeMap.getNamedItem(
						test.getAttName()).getNodeValue();
				if (attributeValue != null
						&& attributeValue.equals(test.getQueryString()))
				{
					result = true;
				}
			}
		}
		// test can be a step
		else if (test.isStep())
		{
			result = false;
			NodeList children = root.getChildNodes();
			for (int i = 0; i < children.getLength(); i++)
			{
				if (validateStep(children.item(i), test.getStep()))
				{
					result = true;
					break;
				}
			}
		}
		return result;
	}

}
