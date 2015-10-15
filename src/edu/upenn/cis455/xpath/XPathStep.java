package edu.upenn.cis455.xpath;

import java.util.ArrayList;

public class XPathStep {

	private String nodeName;
	private ArrayList<XPathTest> tests;
	private XPathStep nextStep;
	private int afterStep;
	
	public XPathStep()
	{
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public ArrayList<XPathTest> getTests() {
		return tests;
	}

	public void setTests(ArrayList<XPathTest> tests) {
		this.tests = tests;
	}

	public XPathStep getNextStep() {
		return nextStep;
	}

	public void setNextStep(XPathStep nextStep) {
		this.nextStep = nextStep;
	}

	
	public int getAfterStep() {
		return afterStep;
	}

	public void setAfterStep(int afterStep) {
		this.afterStep = afterStep;
	}

	@Override
	public String toString() {
		return "XPathStep [nodeName=" + nodeName + ", tests=" + tests
				+ ", \nnextStep=" + nextStep + ", afterStep=" + afterStep + "]";
	}

	
	
	
	
	
}
