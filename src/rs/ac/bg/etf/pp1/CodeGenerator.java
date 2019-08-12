package rs.ac.bg.etf.pp1;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.AssignmentVisitor.NewArrayExprVisitor;
import rs.ac.bg.etf.pp1.CounterVisitor.FormParamCounter;
import rs.ac.bg.etf.pp1.CounterVisitor.VarCounter;
import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class CodeGenerator extends VisitorAdaptor {
	private int mainPc;
	
	public int getMainPc() {
		return mainPc;
	}
	
	Logger log = Logger.getLogger(getClass());
	
	public void report_info(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message); 
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" na liniji ").append(line);
		log.info(msg.toString());
	}
	
	public void visit(VarDeclArray varDeclArray) { 
		
	}
	
	//===================METHODS===================
	public void visit(MethodVoidTypeName methodVoidTypeName) {
		methodVoidTypeName.obj.setAdr(Code.pc);
		
		if ("main".equalsIgnoreCase(methodVoidTypeName.obj.getName())) {
			mainPc = Code.pc;
		}
		
		// Collect arguments and locals
		SyntaxNode methodNode = methodVoidTypeName.getParent();
		
		VarCounter varCounter = new VarCounter();
		methodNode.traverseTopDown(varCounter);
		
		FormParamCounter formParamCounter = new FormParamCounter();
		methodNode.traverseTopDown(formParamCounter);
		
		// Generate method entry
		Code.put(Code.enter);
		Code.put(formParamCounter.getCount());
		Code.put(formParamCounter.getCount() + varCounter.getCount());
	}
	
	public void visit(MethodNoVoidTypeName methodNoVoidTypeName) {
		methodNoVoidTypeName.obj.setAdr(Code.pc);
		
		if ("main".equalsIgnoreCase(methodNoVoidTypeName.obj.getName())) {
			mainPc = Code.pc;
		}
		
		// Collect arguments and locals
		SyntaxNode methodNode = methodNoVoidTypeName.getParent();
		
		VarCounter varCounter = new VarCounter();
		methodNode.traverseTopDown(varCounter);
		
		FormParamCounter formParamCounter = new FormParamCounter();
		methodNode.traverseTopDown(formParamCounter);
		
		// Generate method entry
		Code.put(Code.enter);
		Code.put(formParamCounter.getCount());
		report_info("Pronadjeno " + varCounter.getCount() + " promenljivih i " + formParamCounter.getCount() + " formalnih parametara!", null );
		Code.put(formParamCounter.getCount() + varCounter.getCount());
	}
	
	public void visit(MethodDecl methodDecl) {
//		Code.put(Code.exit);
//		Code.put(Code.return_);
	}
	
	

	//=================STATEMENTS==================
	//U expr si izracunao vec vrednost izraza
	public void visit(PrintStmt printStmt) {
		Struct struct = printStmt.getExpr().struct;
		
		if (struct.getKind() == Struct.Array) {
			struct = struct.getElemType();
		}

		if (struct == Tab.intType) {
			Code.loadConst(5);
			Code.put(Code.print);
		}
		else {
			Code.loadConst(1);
			Code.put(Code.bprint);
		}
		
	}
	
	public void visit(PrintNumStmt printNumStmt) {
		if (printNumStmt.getExpr().struct == Tab.intType) {
			
		}
		else {
			
		}
	}
	
	public void visit(ReturnNoExpr returnNoExpr) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	public void visit(ReturnStmt returnStmt) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	public void visit(MulopTerm mulopTerm) {
		if (MulMulop.class == mulopTerm.getMulop().getClass()) {
			Code.put(Code.mul);
		}
		else if (DivMulop.class == mulopTerm.getMulop().getClass()) {
			Code.put(Code.div);
		}
		else {
			Code.put(Code.rem);
		}
	}
	
	
	//============DESIGNATOR STATEMENTS============
	public void visit(Assignment assignment) {
		NewArrayExprVisitor visitor = new NewArrayExprVisitor();
		assignment.traverseTopDown(visitor);
		
		if (visitor.isNewArrayExpr()) {
			Code.put(Code.putstatic); Code.put2(assignment.getDesignator().obj.getAdr()); 	
		}
		else
		{
			Code.store(assignment.getDesignator().obj);
		}
	}
	
	public void visit(ProcCall procCall) {
		Obj functionObj = procCall.getFuncName().getDesignator().obj;
		int offset = functionObj.getAdr() - Code.pc;
		Code.put(Code.call);
		Code.put2(offset);
		
		if (procCall.getFuncName().getDesignator().obj.getType() != Tab.noType) {
			Code.put(Code.pop);
		}
	}
	
	public void visit(Increment increment) {
		Code.loadConst(1);
		Code.put(Code.add);
		Code.store(increment.getDesignator().obj);
		
	}
	
	public void visit(Decrement decrement) {
		Code.loadConst(1);
		Code.put(Code.sub);
		Code.store(decrement.getDesignator().obj);
	}
	
	//==================EXPR=====================
	public void visit(NegExpr negExpr) {
		Code.put(Code.neg);
	}
	
	
	//================SIGNED EXPR=================
	public void visit(AddopExpr addopExpr) {
		if (PlusAddop.class == addopExpr.getAddop().getClass())
			Code.put(Code.add);
		else 
			Code.put(Code.sub);
	}
	
	
	//=================DESIGNATORS================
	public void visit(IdentDesign designator) {
		SyntaxNode parent = designator.getParent();
		
		if ((Assignment.class != parent.getClass()) && 
				(FuncCall.class != parent.getClass()) && 
				
				(designator.obj.getKind() != Obj.Meth)) {
			Code.load(designator.obj);
		}
		
	}
	
	public void visit(DesignatorName designatorName) {
		Obj o = designatorName.obj;
        if (o.getLevel()==0) { // global variable 
      	  	Code.put(Code.getstatic); Code.put2(o.getAdr()); 
	      }
        else {
	      // local variable
	      if (0 <= o.getAdr() && o.getAdr() <= 3) 
	          Code.put(Code.load_n + o.getAdr());
	      else { 
	      	 Code.put(Code.load); Code.put(o.getAdr()); 
	      } 
        }
	}
	
	public void visit(ArrIdentDesign arrIdentDesign) {
		SyntaxNode parent = arrIdentDesign.getParent();
		if (parent.getClass() != Assignment.class) {
			Code.load(arrIdentDesign.obj);
		}
	}
	
	
	//==================FACTORS====================
	public void visit(FuncCall funcCall) {
		Obj functionObj = funcCall.getFuncName().getDesignator().obj;
		int offset = functionObj.getAdr() - Code.pc;
		Code.put(Code.call);
		Code.put2(offset);
	}
	
	public void visit(NumConstFact numConstFact) {
		Obj con = Tab.insert(Obj.Con, "$", numConstFact.struct);
		con.setLevel(0);
		con.setAdr(numConstFact.getN1());
		
		Code.load(con);
	}
	
	public void visit(NewExprFact newExprFact) {
		Code.put(Code.newarray);
		if (newExprFact.getType().struct.getElemType() == Tab.charType) {
			Code.put(0);
		}
		else {
			Code.put(1);
		}
		

	}
	
	public void visit(CharConstFact charConstFact) {
		Obj con = Tab.insert(Obj.Con, "$", charConstFact.struct);
		con.setLevel(0);
		con.setAdr(charConstFact.getC1().charAt(1));
		Code.load(con);
	}
} 
