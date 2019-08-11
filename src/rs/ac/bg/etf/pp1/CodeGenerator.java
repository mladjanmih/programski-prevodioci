package rs.ac.bg.etf.pp1;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.CounterVisitor.FormParamCounter;
import rs.ac.bg.etf.pp1.CounterVisitor.VarCounter;
import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;

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
		if (printStmt.getExpr().struct == Tab.intType) {
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
	
	public void visit(AddopExpr addopExpr) {
		if (PlusAddop.class == addopExpr.getAddop().getClass())
			Code.put(Code.add);
		else 
			Code.put(Code.sub);
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
		Code.store(assignment.getDesignator().obj);
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
	
	
	//=================DESIGNATORS================
	public void visit(IdentDesign designator) {
		SyntaxNode parent = designator.getParent();
		
		if ((Assignment.class != parent.getClass()) && (FuncCall.class != parent.getClass())) {
			Code.load(designator.obj);
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
}
