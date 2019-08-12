package rs.ac.bg.etf.pp1;

import rs.ac.bg.etf.pp1.ast.NewExprFact;
import rs.ac.bg.etf.pp1.ast.VisitorAdaptor;

public class AssignmentVisitor extends VisitorAdaptor {
	protected boolean newArrayExpr;
	
	public boolean isNewArrayExpr() {
		return newArrayExpr;
	}
	
	public static class NewArrayExprVisitor extends AssignmentVisitor{
		public NewArrayExprVisitor() {
			newArrayExpr = false;
		}
		
		public void visit(NewExprFact newExprFact) {
			newArrayExpr = true;
		}
	}
}
