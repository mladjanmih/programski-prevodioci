package rs.ac.bg.etf.pp1;

import java.util.HashMap;
import java.util.LinkedList;

import rs.ac.bg.etf.pp1.ast.AddopExpr;
import rs.ac.bg.etf.pp1.ast.ArrIdentDesign;
import rs.ac.bg.etf.pp1.ast.Assignment;
import rs.ac.bg.etf.pp1.ast.BoolConstFact;
import rs.ac.bg.etf.pp1.ast.CharConstFact;
import rs.ac.bg.etf.pp1.ast.DesignatorName;
import rs.ac.bg.etf.pp1.ast.FactorTerm;
import rs.ac.bg.etf.pp1.ast.FuncCall;
import rs.ac.bg.etf.pp1.ast.IdentDesign;
import rs.ac.bg.etf.pp1.ast.InitList;
import rs.ac.bg.etf.pp1.ast.InitListStart;
import rs.ac.bg.etf.pp1.ast.Initializer;
import rs.ac.bg.etf.pp1.ast.InitializerListStart;
import rs.ac.bg.etf.pp1.ast.MinusTerm;
import rs.ac.bg.etf.pp1.ast.MulopTerm;
import rs.ac.bg.etf.pp1.ast.NewArrayExpr;
import rs.ac.bg.etf.pp1.ast.NewExprFact;
import rs.ac.bg.etf.pp1.ast.NewExprFactInit;
import rs.ac.bg.etf.pp1.ast.NewFact;
import rs.ac.bg.etf.pp1.ast.NumConstFact;
import rs.ac.bg.etf.pp1.ast.ProcCall;
import rs.ac.bg.etf.pp1.ast.ReadStmt;
import rs.ac.bg.etf.pp1.ast.SubIdendDesign;
import rs.ac.bg.etf.pp1.ast.SyntaxNode;
import rs.ac.bg.etf.pp1.ast.VisitorAdaptor;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class AssignmentVisitor extends VisitorAdaptor {
	protected boolean newArrayExpr;
	private static final String TRUE = "true";
	private static final String FALSE = "false";

	public boolean isNewArrayExpr() {
		return newArrayExpr;
	}

	protected boolean hasOtherReference;
	protected boolean hasArrayReference;

	public boolean validExpression() {
		return (hasArrayReference && !hasOtherReference) || (hasOtherReference && !hasArrayReference)
				|| (!hasArrayReference && !hasOtherReference);
	}

	public boolean isArrayReference() {
		return hasArrayReference;
	}

	public static class NewArrayExprVisitor extends AssignmentVisitor {
		public NewArrayExprVisitor() {
			newArrayExpr = false;
		}

		public void visit(NewArrayExpr newExprFact) {
			newArrayExpr = true;
		}

		public void visit(IdentDesign identDesign) {
			if (identDesign.obj.getType().getKind() == Struct.Array) {
				newArrayExpr = true;
			}

		}
	}

	public static class ArrayTermVisitor extends AssignmentVisitor {
		public void visit(ArrIdentDesign design) {
			hasArrayReference = true;
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

	protected boolean isValidInitializerListExpr;
	protected int elemNum = 0;

	public static class InitializerListExprVisitor extends AssignmentVisitor {
		public InitializerListExprVisitor() {
			isValidInitializerListExpr = true;
			elemNum = 0;
		}

		public void visit(AddopExpr expr) {
			isValidInitializerListExpr = false;
		}

		public void visit(MinusTerm expr) {
			isValidInitializerListExpr = false;
		}

		public void visit(MulopTerm expr) {
			isValidInitializerListExpr = false;
		}

		public void visit(FactorTerm expr) {
			if (expr.getFactor().getClass() != NumConstFact.class) {
				isValidInitializerListExpr = false;
			}
		}

		public void visit(NumConstFact expr) {
			elemNum = expr.getN1();
		}
	}

	protected boolean isValidInitializerListType;
	protected int initializersCount = 0;
	protected boolean initListHasUnallowedExpr;
	public static class InitializerListTypeCounterVisitor extends AssignmentVisitor {
		private Struct destType;
		HashMap<String, Struct> enumStructs;

		public InitializerListTypeCounterVisitor(Struct destType, HashMap<String, Struct> enumStructs) {
			if (destType == null || destType == Tab.noType) {
				isValidInitializerListType = false;
			} else
				isValidInitializerListType = true;
			initializersCount = 0;
			this.destType = destType;
			this.enumStructs = enumStructs;
			initListHasUnallowedExpr = false;
		}

		public void visit(Initializer init) {
			Struct exprType = init.struct;
			if (exprType == null || destType == null || exprType.getKind() == Struct.None) {
				isValidInitializerListType = false;
			}
			else if (!exprType.assignableTo(destType) && !enumAssignable(destType, exprType)) {
				isValidInitializerListType = false;
			}

			initializersCount++;
		}

		public void visit(InitList init) {
			Struct exprType = init.struct;
			if (exprType == null || destType == null || exprType.getKind() == Struct.None) {
				isValidInitializerListType = false;
				
			}
			else if (!exprType.assignableTo(destType) && !enumAssignable(destType, exprType)) {
				isValidInitializerListType = false;
				
			}

			initializersCount++;
		}

		
		public void visit(NewExprFact newExprFact) {
			initListHasUnallowedExpr = true;
		}
		
		public void visit(NewExprFactInit newExprFactInit) {
			initListHasUnallowedExpr = true;
		}
		
		public void visit(NewFact newFact) {
			initListHasUnallowedExpr = true;
		}
		
		private boolean enumAssignable(Struct dest, Struct src) {
			return dest.equals(Tab.intType) && isEnumType(src);
		}

		private boolean isEnumType(Struct str) {
			return enumStructs.containsValue(str);
		}
	}

	public static class InitializatorListCodeGenerator extends AssignmentVisitor {
		private boolean isNewArrayStatement;
		private Obj dest = null;
		private int i = 0;
		private int prevPc = 0;
		public InitializatorListCodeGenerator(Obj dest) {
			this.dest = dest;
			i = 0;
			prevPc = 0;
			isNewArrayStatement = true;
		}
		
		public void visit(NewArrayExpr newArrayExpr) {
			isNewArrayStatement = false;
			
		}
		
		public void visit(InitListStart start) {
			prevPc = Code.pc;
			Code.load(dest);
			Code.loadConst(i++);
		}
		
		
		public void visit(NewExprFactInit newExprFactInit) {
			//Code.put(Code.aload);
				Code.pc = prevPc;
		}	

		public void visit(FuncCall funcCall) {
			if (!isNewArrayStatement) {
				Obj functionObj = funcCall.getFuncName().getDesignator().obj;
				int offset = 0;
				if ("ord".equals(functionObj.getName()) || "chr".equals(functionObj.getName()))
					offset = 0 - Code.pc;
				else if ("len".equals(functionObj.getName())) {
					offset = 6 - Code.pc;
				}
				else
					offset = functionObj.getAdr() - Code.pc;
				Code.put(Code.call);
				Code.put2(offset);
			}
		}
		
		public void visit(NumConstFact numConstFact) {
			if (!isNewArrayStatement) {
				Obj con = Tab.insert(Obj.Con, "$", numConstFact.struct);
				con.setLevel(0);
				con.setAdr(numConstFact.getN1());
				
				Code.load(con);
			}
		}
		

		
		public void visit(CharConstFact charConstFact) {
			if (!isNewArrayStatement) {
				Obj con = Tab.insert(Obj.Con, "$", charConstFact.struct);
				con.setLevel(0);
				con.setAdr(charConstFact.getC1().charAt(1));
				Code.load(con);
			}
		}

		public void visit(BoolConstFact boolConstFact) {
			if (!isNewArrayStatement) {
				Obj con = Tab.insert(Obj.Con, "$", boolConstFact.struct);
				con.setLevel(0);
				if (TRUE.equals(boolConstFact.getB1())) {
					con.setAdr(1);
				}
				else {
					con.setAdr(0);
				}
				Code.load(con);
			}
		}

		public void visit(IdentDesign designator) {
			if (!isNewArrayStatement) {
				SyntaxNode parent = designator.getParent();
				
				if ((Assignment.class != parent.getClass()) && 
						(FuncCall.class != parent.getClass()) &&
						(ProcCall.class != parent.getClass()) && 
						(designator.obj.getKind() != Obj.Meth)) {
					
					Code.load(designator.obj);
				}
			}
		}
		
		int pcAfterGetStaticArray = 0;
		public void visit(DesignatorName designatorName) {
			if (!isNewArrayStatement) {
				Obj o = designatorName.obj;
		        Code.load(designatorName.obj);
			}
		}
		
		public void visit(ArrIdentDesign arrIdentDesign) {
			if (!isNewArrayStatement) {
				SyntaxNode parent = arrIdentDesign.getParent();
				if (parent.getClass() != Assignment.class
						&& parent.getClass() != ReadStmt.class) {
					Code.load(arrIdentDesign.obj);
				}
			}
		}
		
		public void visit (SubIdendDesign designator) {
			if (!isNewArrayStatement) {
				Code.load(designator.obj);
			}
		}
		
		public void visit(Initializer init) {
			if (dest.getType().getElemType() == Tab.charType) 
				Code.put(Code.bastore);
			else
				Code.put(Code.astore);
			prevPc = Code.pc;
			Code.load(dest);
			Code.loadConst(i++);
		}

		public void visit(InitList init) {
			if (dest.getType().getElemType() == Tab.charType) 
				Code.put(Code.bastore);
			else
				Code.put(Code.astore);
			prevPc = Code.pc;
			Code.load(dest);
			Code.loadConst(i++);
		}
	}
}