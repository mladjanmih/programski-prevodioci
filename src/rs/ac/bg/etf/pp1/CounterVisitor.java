package rs.ac.bg.etf.pp1;

import rs.ac.bg.etf.pp1.ast.ArrayParamDecl;
import rs.ac.bg.etf.pp1.ast.ParamDecl;
import rs.ac.bg.etf.pp1.ast.SingleVarDecl;
import rs.ac.bg.etf.pp1.ast.VarDeclArray;
import rs.ac.bg.etf.pp1.ast.VisitorAdaptor;

public class CounterVisitor extends VisitorAdaptor {
	protected int count;
	
	public int getCount() {
		return count;
	}
	
	public static class FormParamCounter extends CounterVisitor {
		public void visit(ParamDecl paramDecl) {
			count++;
		}
		
		public void visit(ArrayParamDecl arrayParamDecl) {
			count++;
		}
	}
	
	public static class VarCounter extends CounterVisitor {
		public void visit(SingleVarDecl singleVarDecl) {
			count++;
		}
		
		public void visit(VarDeclArray varDeclArray) {
			count++;
		}
	
	}
}
