package rs.ac.bg.etf.pp1;

import rs.ac.bg.etf.pp1.ast.AddopExpr;
import rs.ac.bg.etf.pp1.ast.ArrIdentDesign;
import rs.ac.bg.etf.pp1.ast.BoolConstFact;
import rs.ac.bg.etf.pp1.ast.CharConstFact;
import rs.ac.bg.etf.pp1.ast.FuncCall;
import rs.ac.bg.etf.pp1.ast.IdentDesign;
import rs.ac.bg.etf.pp1.ast.MulopTerm;
import rs.ac.bg.etf.pp1.ast.NewExprFact;
import rs.ac.bg.etf.pp1.ast.NumConstFact;
import rs.ac.bg.etf.pp1.ast.VisitorAdaptor;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class AssignmentVisitor extends VisitorAdaptor {
	protected boolean newArrayExpr;
	
	public boolean isNewArrayExpr() {
		return newArrayExpr;
	}
	
	
	protected boolean hasOtherReference;
	protected boolean hasArrayReference;
	
	public boolean validExpression() {
		return (hasArrayReference && !hasOtherReference) || (hasOtherReference && !hasArrayReference) || (!hasArrayReference && !hasOtherReference);
	}
	public boolean isArrayReference() {
		return hasArrayReference;
	}
	
	public static class NewArrayExprVisitor extends AssignmentVisitor{
		public NewArrayExprVisitor() {
			newArrayExpr = false;
		}
		
		public void visit(NewExprFact newExprFact) {
			newArrayExpr = true;
		}
		
		public void visit(IdentDesign identDesign) {
			if (identDesign.obj.getType().getKind() == Struct.Array) {
				newArrayExpr = true;
			}
			
		}
	}
	
	public static class ArrayUsageVisitor extends AssignmentVisitor {
		public ArrayUsageVisitor() {
			hasArrayReference = false;
			hasOtherReference = false;
		}
		
		public void visit(AddopExpr addopExpr) {
			hasOtherReference = true;
		}
		
		public void visit(MulopTerm mulopTerm) {
			hasOtherReference = true;
		}
		
		public void visit(IdentDesign identDesign) {
			if (identDesign.obj.getType().getKind() == Struct.Array)
				hasArrayReference = true;
		}
		
		public void visit(NewExprFact newExprFact) {
			hasArrayReference = true;
		}
		
		public void visit(NumConstFact numConstFact) {
			if (!hasArrayReference)
				hasOtherReference = true;
		}
		
		public void visit(BoolConstFact numConstFact) {
			hasOtherReference = true;
		}
		
		public void visit(CharConstFact numConstFact) {
			hasOtherReference = true;
		}
		
		public void visit(FuncCall funcCall) {
			hasOtherReference = true;
		}
	}
}