package rs.ac.bg.etf.pp1;

import java.util.LinkedList;

public class ForContext {
	public LinkedList<Integer> conditionBackPatchAdresses = new LinkedList<Integer>();
	public LinkedList<Integer> trueConditionBackPatchAdresses = new LinkedList<Integer>();
	public LinkedList<Integer> falseConditionAdresses = new LinkedList<Integer>();
	public LinkedList<Integer> ifElseStatementExitBackPatchAddresses = new LinkedList<Integer>();
	//public LinkedList<Integer> afterForDesigStatementAddresses = new LinkedList<Integer>();
	//public int falseConditionAdress = 0;
	public int conditionStatementAddress = 0;
	public int afterForDesigStatementAddress= 0;
	
}
