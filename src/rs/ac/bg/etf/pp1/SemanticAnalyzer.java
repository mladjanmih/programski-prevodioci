package rs.ac.bg.etf.pp1;

import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.*;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;

public class SemanticAnalyzer extends VisitorAdaptor {
	
	public static final Struct boolType = new Struct(Struct.Bool);
	
	Logger log = Logger.getLogger(getClass());
	Obj currentMethod = null;
	boolean returnFound = false;
	boolean errorDetected = false;
	int nVars;
	
	public void report_error(String message, SyntaxNode info) {
		errorDetected = true;
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" na liniji ").append(line);
		log.error(msg.toString());
	}

	public void report_info(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message); 
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" na liniji ").append(line);
		log.info(msg.toString());
	}
	
	public void visit(ProgName progName) {
		Tab.insert(Obj.Type, "bool", boolType);
		progName.obj = Tab.insert(Obj.Prog,  progName.getProgName(), Tab.noType);
		Tab.openScope();
	}
	
	public void visit(Program program) {
		nVars = Tab.currentScope.getnVars();
		Tab.chainLocalSymbols(program.getProgName().obj);
		Tab.closeScope();
	}
	
	
	//============================CONST declarations====================================
	
	Type constDeclsType = null;
	public void visit(ConstType constType) {
		constDeclsType = constType.getType();
	}
	
	public void visit(ConstDeclaration constDeclaration) {
		constDeclsType = null;
	}

	public void visit(NumConstDecl numConstDecl) {
		if (constDeclsType == null) {
			report_error("Greska na liniji " + numConstDecl.getLine() + " konstanta se ne moze definisati u ovom opsegu!", null);
		}
		else {
			if (constDeclsType.struct != Tab.intType)  {
				report_error("Greska na liniji " + numConstDecl.getLine() + " nekompatibilni tip konstante i inicijalizator!", null);
			}
			else {
				Tab.insert(Obj.Con, numConstDecl.getConstName(), constDeclsType.struct);
			}
		}
	}
	
	public void visit(CharConstDecl charConstDecl) {
		if (constDeclsType == null) {
			report_error("Greska na liniji " + charConstDecl.getLine() + " konstanta se ne moze definisati u ovom opsegu!", null);
		}
		else {
			if (constDeclsType.struct != Tab.charType)  {
				report_error("Greska na liniji " + charConstDecl.getLine() + " nekompatibilni tip konstante i inicijalizator!", null);
			}
			else {
				Tab.insert(Obj.Con, charConstDecl.getConstName(), constDeclsType.struct);
			}
		}
	}
	
	public void visit(BoolConstDecl boolConstDecl) {
		if (constDeclsType == null) {
			report_error("Greska na liniji " + boolConstDecl.getLine() + " konstanta se ne moze definisati u ovom opsegu!", null);
		}
		else {
			if (constDeclsType.struct != boolType)  {
				report_error("Greska na liniji " + boolConstDecl.getLine() + " nekompatibilni tip konstante i inicijalizator!", null);
			}
			else {
				Tab.insert(Obj.Con, boolConstDecl.getConstName(), constDeclsType.struct);
			}
		}
	}
	
	//============================Var declarations=======================================
	Type varDeclsType = null;
	public void visit(VarType varType) {
		varDeclsType = varType.getType();
	}
	
	public void visit(VarDeclarationTypeList varDeclarationTypeList) {
		varDeclsType = null;
	}
	
	public void visit(SingleVarDecl singleVarDecl) {
		
		report_info("Deklarisana promenljiva " + singleVarDecl.getVarName(), singleVarDecl);
		Obj varNode = Tab.insert(Obj.Var, singleVarDecl.getVarName(), varDeclsType.struct);
	}
	
	public void visit(VarDeclArray varDeclArray) { 
		report_info("Deklarisana promenljiva " + varDeclArray.getVarName(), varDeclArray);
		Obj varNode = Tab.insert(Obj.Var, varDeclArray.getVarName(), new Struct(Struct.Array, varDeclsType.struct));
	}
	
	public void visit(Type type) {
		Obj typeNode = Tab.find(type.getTypeName());
		if (typeNode == Tab.noObj) {
			report_error("Nije pronadjen tip " + type.getTypeName() + " u tabeli simbola!", null);
			type.struct = Tab.noType;
		}
		else { 
			if (Obj.Type == typeNode.getKind()) {
				type.struct = typeNode.getType();
			}
			else {
				report_error("Greska: Ime " + type.getTypeName() + " ne predstavlja tip!", type);
				type.struct = Tab.noType;
			}
		}
	}
	
	//===========================ENUM DECLARATIONS=================================
	
	//==========================OBRADA FUNKCIJA=======================================
	public void visit(MethodVoidTypeName methodVoidTypeName) {
		currentMethod = Tab.insert(Obj.Meth,  methodVoidTypeName.getMethName(), Tab.noType);
		methodVoidTypeName.obj = currentMethod;
		Tab.openScope();
		report_info("Obradjuje se funkcija " + methodVoidTypeName.getMethName(), methodVoidTypeName);
	}
	
	public void visit(MethodNoVoidTypeName methodNoVoidTypeName) {
		currentMethod = Tab.insert(Obj.Meth,  methodNoVoidTypeName.getMethName(), methodNoVoidTypeName.getType().struct);
		methodNoVoidTypeName.obj = currentMethod;
		Tab.openScope();
		report_info("Obradjuje se funkcija " + methodNoVoidTypeName.getMethName(), methodNoVoidTypeName);
	}
	
	public void visit(MethodDecl methodDecl) {
		if (!returnFound && (currentMethod.getType() != Tab.noType))  {
			report_error("Semanticka greska na liniji " + methodDecl.getLine() + " : funkcija " + currentMethod.getName() + " nema return iskaz.", null);
		}
	
		Tab.chainLocalSymbols(currentMethod);
		Tab.closeScope();
		returnFound = false;
		currentMethod = null;
	}
	
	//=============================Obrada designatora=============================================
	public void visit (IdentDesign designator) {
		Obj obj = Tab.find(designator.getName());
		if (obj == Tab.noObj) {
			report_error("Greska na liniji " + designator.getLine() + " : ime " + designator.getName() + " nije deklarisano!", null);
		}
		
		designator.obj = obj;
	}
	
	public void visit (SubIdendDesign designator) {
		Obj obj = Tab.find(designator.getName());
		if (obj == Tab.noObj) {
			report_error("Greska na liniji " + designator.getLine() + " : ime " + designator.getName() + " nije deklarisano!", null);
		}
		
		designator.obj = obj;
	}
	
	public void visit (ArrIdentDesign designator) {
		Obj obj = Tab.find(designator.getName());
		if (obj == Tab.noObj) {
			report_error("Greska na liniji " + designator.getLine() + " : ime " + designator.getName() + " nije deklarisano!", null);
		}
		
		designator.obj = obj;
	}
	
	public void visit(FuncCall funcCall) {
		Obj func = funcCall.getDesignator().obj;
		if (Obj.Meth == func.getKind()) {
			report_info("Pronadjen poziv funkcije " + funcCall.getLine() + " na liniji " + funcCall.getLine(), null);
			funcCall.struct = func.getType();
		} 
		else {
			report_error("Greska na liniji " + funcCall.getLine() + " : ime " + func.getName() + " nije funkcija!", null);
			funcCall.struct = Tab.noType;
		}
	}
	
	public void visit(ProcCall procCall) {
		Obj func = procCall.getDesignator().obj;
		if (Obj.Meth == func.getKind()) {
			report_info("Pronadjen poziv procedure " + procCall.getLine() + " na liniji " + procCall.getLine(), null);
			procCall.struct = func.getType();
		} 
		else {
			report_error("Greska na liniji " + procCall.getLine() + " : ime " + func.getName() + " nije funkcija!", null);
			procCall.struct = Tab.noType;
		}
	}
	

	
	public void visit(FactorTerm factorTerm) {
		factorTerm.struct = factorTerm.getFactor().struct;
	}
	
	public void visit(TermExpr termExpr) {
		termExpr.struct = termExpr.getTerm().struct;
	}
	
	public void visit(AddopExpr addopExpr) {
		Struct te = addopExpr.getSignedExpr().struct;
		Struct t = addopExpr.getTerm().struct;
		if (te.equals(t) && ((te == Tab.intType) || (te == Tab.charType) || (te == boolType))) {
			addopExpr.struct = te;
		}
		else {
			report_error("Greska na liniji " + addopExpr.getLine() + " : nekompatibilni tipovi u izrazu za sabiranje.", null);
			addopExpr.struct = Tab.noType;
		}	
	}
	
	public void visit(MulopTerm mulopTerm) {
		Struct mte = mulopTerm.getTerm().struct;
		Struct mt = mulopTerm.getFactor().struct;
		if (mte.equals(mt) && ((mte == Tab.intType) || (mte == Tab.charType) || (mte == boolType))) {
			mulopTerm.struct = mte;
		}
		else {
			report_error("Greska na liniji " + mulopTerm.getLine() + " : nekompatibilni tipovi u izrazu za operacije mnozenja i deljenja.", null);
			mulopTerm.struct = Tab.noType;
		}
	}
	
	public void visit(ReturnStmt returnStmt) {
		returnFound = true;
		Struct currMethType = currentMethod.getType();
		if (!currMethType.compatibleWith(returnStmt.getExpr().struct)) {
			report_error("Greska na liniji " + returnStmt.getLine() + " : " + "tip izraza u return naredbi ne slaze se sa tipom povratne vrednosti funkcije " + currentMethod.getName(), null);
		}
	}
	
	public void visit(PosExpr posExpr) {
		posExpr.struct = posExpr.getSignedExpr().struct;
	}
	
	public void visit(NegExpr negExpr) {
		negExpr.struct = negExpr.getSignedExpr().struct;
	}
	
	public void visit(NumConstFact numConstFact) {
		numConstFact.struct = Tab.intType;
	}
	
	public void visit(CharConstFact charConstFact) {
		charConstFact.struct = Tab.charType;
	}
	
	public void visit(BoolConstFact boolConstFact) {
		boolConstFact.struct = boolType;
	}
	
	public void visit(Var var) {
		var.struct = var.getDesignator().obj.getType();
	}
	
	public void visit(Assignment assignment) {
		if (!assignment.getExpr().struct.assignableTo(assignment.getDesignator().obj.getType())) {
			report_error("Greska na liniji " + assignment.getLine() + " : " + "nekomaptibilni tipovi u dodeli vrednosti!", null);
		}
	}
	
	public void visit(ExprFact exprFact) {
		exprFact.struct = exprFact.getExpr().struct;
	}
	
//	public void visit(BoolConstFact boolConstFact) {
//		boolConstFact = Tab.
//	}
	
	public boolean passed() {
		return !errorDetected;
	}
}
