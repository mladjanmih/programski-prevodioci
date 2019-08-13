package rs.ac.bg.etf.pp1;

import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.AssignmentVisitor.ArrayUsageVisitor;
import rs.ac.bg.etf.pp1.ast.*;

public class SemanticAnalyzer extends VisitorAdaptor {
	
	public static final Struct boolType = new Struct(Struct.Bool);
	public static final Struct enumType = new Struct(Struct.Int);
	
	Logger log = Logger.getLogger(getClass());

	Obj currentEnum = null;
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
	
	
	//==========================PROGRAM===================================
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
	
	
	//===========================CONSTS====================================
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
				Obj constNode = Tab.find(numConstDecl.getConstName());
				if (constNode == Tab.noObj) {
					constNode = Tab.insert(Obj.Con, numConstDecl.getConstName(), constDeclsType.struct);
				}
				else {
					report_info("Greska na liniji " + numConstDecl.getLine() + " : identifikator " + numConstDecl.getConstName() + " je vec deklarisan u okruzujucem opsegu!", null);
				}
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
				Obj constNode = Tab.find(charConstDecl.getConstName());
				if (constNode == Tab.noObj)
				{
					Tab.insert(Obj.Con, charConstDecl.getConstName(), constDeclsType.struct);
				}
				else {
					report_info("Greska na liniji " + charConstDecl.getLine() + " : identifikator " + charConstDecl.getConstName() + " je vec deklarisan u okruzujucem opsegu!", null);
				}
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
				Obj constNode = Tab.find(boolConstDecl.getConstName());
				if (constNode == Tab.noObj) {
					Tab.insert(Obj.Con, boolConstDecl.getConstName(), constDeclsType.struct);
				}
				else {
					report_info("Greska na liniji " + boolConstDecl.getLine() + " : identifikator " + boolConstDecl.getConstName() + " je vec deklarisan u okruzujucem opsegu!", null);
				}
			}
		}
	}
	
	//============================VARS====================================
	Type varDeclsType = null;
	public void visit(VarType varType) {
		varDeclsType = varType.getType();
	}
	
	public void visit(VarDeclarationTypeList varDeclarationTypeList) {
		varDeclsType = null;
	}
	
	public void visit(SingleVarDecl singleVarDecl) {
		Obj varNode = findObjInCurrentScope(singleVarDecl.getVarName());
		if (varNode == Tab.noObj) 
		{
			report_info("Deklarisana promenljiva " + singleVarDecl.getVarName(), singleVarDecl);
			varNode = Tab.insert(Obj.Var, singleVarDecl.getVarName(), varDeclsType.struct);
			nVars++;
		}
		else {
			report_info("Greska na liniji " + singleVarDecl.getLine() + " : identifikator " + singleVarDecl.getVarName() + " je vec deklarisan u okruzujucem opsegu!", null);	
		}
		
	}
	
	public void visit(VarDeclArray varDeclArray) { 
		Obj varNode = findObjInCurrentScope(varDeclArray.getVarName());
		if (varNode == Tab.noObj) 
		{
			report_info("Deklarisana promenljiva " + varDeclArray.getVarName(), varDeclArray);
			varNode = Tab.insert(Obj.Elem, varDeclArray.getVarName(), new Struct(Struct.Array, varDeclsType.struct));
			nVars++;
		}
		else {
			report_info("Greska na liniji " + varDeclArray.getLine() + " : identifikator " + varDeclArray.getVarName() + " je vec deklarisan u okruzujucem opsegu!", null);
		}
		
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
	
	//=============================ENUMS==================================
	int enumDeclCounter = 0;
	public void visit(EnumName enumName) {
		Obj defined = Tab.find(enumName.getEnumName());
		if (defined != Tab.noObj) {
			report_error("Greska na liniji " + enumName.getLine() + " : Identifikator " + enumName.getEnumName() + " je vec definisan u okruzujucem scope-u!", null);
		}
		else  {
			currentEnum = Tab.insert(Obj.Elem, enumName.getEnumName() , enumType);
			enumName.obj = currentEnum;
			enumDeclCounter = 0;
			Tab.openScope();
		}
	}
	
	public void visit(EnumDeclaration enumDeclaration) {
		if (currentEnum != null) {
			Tab.chainLocalSymbols(enumDeclaration.getEnumName().obj);
			Tab.closeScope();
		}
		currentEnum = null;
	}
	
	public void visit(SingleEnumDecl singleEnumDecl) {
		if (currentEnum == null) {
			report_error("Greska na liniji " + singleEnumDecl.getLine() + " : Enum literal ne moze biti definisan izvan enuma!", null);
			return;
		}
		
		Obj enumLiteral = Tab.find(singleEnumDecl.getLiteralName());
		if (enumLiteral != Tab.noObj) {
			report_error("Greska na liniji " + singleEnumDecl.getLine() + " : Enum literal " + singleEnumDecl.getLiteralName() + " je vec definisan u enumu " + currentEnum.getName() + "!", null);
			return;
		}
		
		Obj obj = Tab.insert(Obj.Con, singleEnumDecl.getLiteralName(), Tab.intType);
		obj.setAdr(enumDeclCounter++);
	}
	
	public void visit(EnumDeclEqual enumDeclEqual) {
		if (currentEnum == null) {
			report_error("Greska na liniji " + enumDeclEqual.getLine() + " : Enum literal ne moze biti definisan izvan enuma!", null);
		}
		else  {
			Obj enumLiteral = Tab.find(enumDeclEqual.getLiteralName());
			if (enumLiteral != Tab.noObj) {
				report_error("Greska na liniji " + enumDeclEqual.getLine() + " : Enum literal " + enumDeclEqual.getLiteralName() + " je vec definisan u enumu " + currentEnum.getName() + "!", null);
			}
			
			if (enumDeclEqual.getN1() <= enumDeclCounter) {
				report_error("Greska na liniji " + enumDeclEqual.getLine() + " : Enum literal " + enumDeclEqual.getLiteralName() + " ne moze imati vrednost " + enumDeclEqual.getN1() + "!", null);
			}
			
			enumDeclCounter = enumDeclEqual.getN1();
			Obj obj = Tab.insert(Obj.Con, enumDeclEqual.getLiteralName(), Tab.intType);
			obj.setAdr(enumDeclCounter++);
		}
	}
	
	//==========================FUNCTIONS=================================	
	HashMap<String, ArrayList<Obj>> methodFormalPars = new HashMap<>();
	Obj currentMethod = null;
	Obj currentMethodCall = null;
	int actParamNo = 0;
	
	public void visit(MethodVoidTypeName methodVoidTypeName) {
		Obj obj = Tab.find(methodVoidTypeName.getMethName());
		if (obj != Tab.noObj) {
			report_error("Greska na liniji " + methodVoidTypeName.getLine() + " : ime " + methodVoidTypeName.getMethName() + " je vec deklarisano!", null);
		}
		else {
			currentMethod = Tab.insert(Obj.Meth,  methodVoidTypeName.getMethName(), Tab.noType);
			methodVoidTypeName.obj = currentMethod;
			Tab.openScope();
			report_info("Obradjuje se funkcija " + methodVoidTypeName.getMethName(), methodVoidTypeName);
			methodFormalPars.put(methodVoidTypeName.getMethName(), new ArrayList<Obj>());
		}
	}
	
	public void visit(MethodNoVoidTypeName methodNoVoidTypeName) {
		Obj obj = Tab.find(methodNoVoidTypeName.getMethName());
		if (obj != Tab.noObj) {
			report_error("Greska na liniji " + methodNoVoidTypeName.getLine() + " : ime " + methodNoVoidTypeName.getMethName() + " je vec deklarisano!", null);
		}
		else {
			currentMethod = Tab.insert(Obj.Meth,  methodNoVoidTypeName.getMethName(), methodNoVoidTypeName.getType().struct);
			methodNoVoidTypeName.obj = currentMethod;
			Tab.openScope();
			report_info("Obradjuje se funkcija " + methodNoVoidTypeName.getMethName(), methodNoVoidTypeName);
			methodFormalPars.put(methodNoVoidTypeName.getMethName(), new ArrayList<Obj>());
		}
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
	
	public void visit(FuncName funcName) {
		actParamNo = 0;
		currentMethodCall = Tab.find(funcName.getDesignator().obj.getName());
	}

	public void visit(ParamDecl paramDecl) {
		Obj obj = Tab.insert(Obj.Var, paramDecl.getName(), paramDecl.getType().struct);
		if (obj == Tab.noObj) {
			report_error("Greska na liniji " + paramDecl.getLine() + " : identifikator " + paramDecl.getName() + " je vec deklarisan u okruzujucem opsegu!", null);
		}
		else {
			methodFormalPars.get(currentMethod.getName()).add(obj);
		}
	}
	
	public void visit(ArrayParamDecl arrayParamDecl) {
		Obj obj = Tab.insert(Obj.Var, arrayParamDecl.getName(), arrayParamDecl.getType().struct);
		if (obj == Tab.noObj) {
			report_error("Greska na liniji " + arrayParamDecl.getLine() + " : identifikator " + arrayParamDecl.getName() + " je vec deklarisan u okruzujucem opsegu!", null);
		}
		else {
			methodFormalPars.get(currentMethod.getName()).add(obj);
		}
	}
	
	public void visit(ActualParams actualParams) {
		if (currentMethodCall == null) {
			return;
		}
		
		ArrayList<Obj> pars = methodFormalPars.get(currentMethodCall.getName());
		
		if (actParamNo == pars.size()) {
			report_error("Greska na liniji " + actualParams.getLine() + " : Pozivu funkcije je prosledjeno vise paramatera od potrebnog broja!", null);
			return;
		}
		
		Obj par = pars.get(actParamNo++);
		if (!par.getType().compatibleWith(actualParams.getExpr().struct)) {
			report_error("Greska na liniji " + actualParams.getLine() + " : Tip prosledjenog parametra nije kompatibilan sa tipom parametra funkcije!", null);
		}
	}
	
	public void visit(ActualParam actualParam) {
		if (currentMethodCall == null) {
			return;
		}
		
		ArrayList<Obj> pars = methodFormalPars.get(currentMethodCall.getName());
		
		if (actParamNo == pars.size()) {
			report_error("Greska na liniji " + actualParam.getLine() + " : Pozivu funkcije je prosledjeno vise paramatera od potrebnog broja!", null);
			return;
		}
		
		Obj par = pars.get(actParamNo++);
		if (!par.getType().compatibleWith(actualParam.getExpr().struct) && !enumAssignable(par.getType(), actualParam.getExpr().struct)) {
			report_error("Greska na liniji " + actualParam.getLine() + " : Tip prosledjenog parametra nije kompatibilan sa tipom parametra funkcije!", null);
		}
	}
	
	public void visit(NoActuals noActuals) {
		if (currentMethodCall == null) {
			return;
		}
		
		if (methodFormalPars.get(currentMethodCall.getName()).size() != 0) {
			report_error("Greska na liniji " + noActuals.getLine() + " : metoda " + currentMethodCall.getName() + " zahteva parametre!", null);
		}
	}
	
	
	//======================== DESIGNATOR============================
	public void visit (IdentDesign designator) {
		Obj obj = Tab.find(designator.getName());
		if (obj == Tab.noObj) {
			report_error("Greska na liniji " + designator.getLine() + " : ime " + designator.getName() + " nije deklarisano!", null);
			designator.obj = new Obj(Obj.Meth, designator.getName(), Tab.noType);
		}
		else {
			if (obj.getType().getKind() == Struct.Array) {
				SyntaxNode parent = designator.getParent();
				if (parent.getClass() == Increment.class || 
						parent.getClass() == Decrement.class ||
						parent.getClass() == ReadStmt.class) {
					report_error("Greska na liniji " + designator.getLine() + " : referenca na niz se ne moze koristiti u ovom izrazu!", null);
				}
			}
			designator.obj = obj;
		}
	}
	
	public void visit (SubIdendDesign designator) {
		Obj obj = Tab.find(designator.getName());
		if (obj == Tab.noObj) {
			report_error("Greska na liniji " + designator.getLine() + " : ime " + designator.getName() + " nije deklarisano!", null);
		}
		
		if (!obj.getType().equals(enumType)) {
			report_error("Greska na liniji " + designator.getLine() + " : ime " + designator.getName() + " nije izraz enumeracije!", null);
		}
		
		Collection<Obj> locals = obj.getLocalSymbols();
		Obj enumerator = null;
		for(Obj local: locals) {
			if (local.getName().equals(designator.getSubName())) {
				enumerator = local;
				break;
			}
		}
	
		if (enumerator == null) {
			report_error("Greska na liniji " + designator.getLine() + " : enumerator " + designator.getSubName() + " nije deklarisan!", null);
		}
		designator.obj = obj;
	}
	
	public void visit(DesignatorName designatorName) {
		Obj obj = Tab.find(designatorName.getName());
		designatorName.obj = obj;
	}
	
	public void visit (ArrIdentDesign designator) {
		Obj obj = Tab.find(designator.getDesignatorName().obj.getName());
		if (obj == Tab.noObj) {
			report_error("Greska na liniji " + designator.getLine() + " : ime " + designator.getDesignatorName().obj.getName() + " nije deklarisano!", null);
		}
		
		designator.obj = obj;
		
		if (!designator.getExpr().struct.compatibleWith(Tab.intType)) {
			report_error("Greska na liniji " + designator.getLine() + " : nekompatibilan tip u izrazu za indeksiranje!", null);
		}
	}
		
	
	//===========================EXPR================================
	public void visit(PosExpr posExpr) {
		ArrayUsageVisitor auv = new ArrayUsageVisitor();
		posExpr.traverseTopDown(auv);
		
		if (!auv.validExpression()) {
			report_error("Greska na liniji " + posExpr.getLine() + " : nije moguce koristiti referncu na niz u ovom izrazu!", null);
		}
		
		if (posExpr.getParent().getClass() == PrintStmt.class || posExpr.getParent().getClass() == PrintNumStmt.class) {
			if (auv.isArrayReference()) {
				report_error("Greska na liniji " + posExpr.getLine() + " : nije moguce koristiti referncu na niz u ovom izrazu!", null);
			}
		}
		
		
		posExpr.struct = posExpr.getSignedExpr().struct;
	}
	
	public void visit(NegExpr negExpr) {
		ArrayUsageVisitor auv = new ArrayUsageVisitor();
		negExpr.traverseTopDown(auv);
		
//		if (!auv.validExpression()) {
//			report_error("Greska na liniji " + negExpr.getLine() + " : nije moguce koristiti referncu na niz u ovom izrazu!", null);
//		}
//		
//		if (negExpr.getParent().getClass() == PrintStmt.class || negExpr.getParent().getClass() == PrintNumStmt.class) {
//			if (auv.isArrayReference()) {
//				report_error("Greska na liniji " + negExpr.getLine() + " : nije moguce koristiti referncu na niz u ovom izrazu!", null);
//			}
//		}
		
		negExpr.struct = negExpr.getSignedExpr().struct;
	}
	
	
	//========================SIGNED EXPR============================
	public void visit(TermExpr termExpr) {
		termExpr.struct = termExpr.getTerm().struct;
	}

	public void visit(AddopExpr addopExpr) {
		Struct te = addopExpr.getSignedExpr().struct;
		Struct t = addopExpr.getTerm().struct;

		if (te.isRefType()) {
			te = te.getElemType();
		}
		if (t.isRefType()) {
			t = t.getElemType();
		}
	
		if ((te.equals(t) && ((te == Tab.intType) || (te == Tab.charType) || (te == boolType))) || enumCompatible(t, te)) {
			if (te == enumType || t == enumType)
				addopExpr.struct = Tab.intType;
			else	
				addopExpr.struct = te;
		}
		else {
			report_error("Greska na liniji " + addopExpr.getLine() + " : nekompatibilni tipovi u izrazu za sabiranje.", null);
			addopExpr.struct = Tab.noType;
		}	
	}
	
	
	//===========================TERM================================
	public void visit(FactorTerm factorTerm) {
		factorTerm.struct = factorTerm.getFactor().struct;
	}

	public void visit(MulopTerm mulopTerm) {
		Struct mte = mulopTerm.getTerm().struct;
		Struct mt = mulopTerm.getFactor().struct;
		if (mte.isRefType()) {
			mte = mte.getElemType();
		}
		else if (mt.isRefType()) {
			mt = mt.getElemType();
		}
	
		if ((mte.equals(mt) && ((mte == Tab.intType) || (mte == Tab.charType) || (mte == boolType))) || enumCompatible(mt, mte)) {
			if (mt == enumType || mte == enumType)
				mulopTerm.struct = Tab.intType;
			else
				mulopTerm.struct = mte;
		}
		else {
			report_error("Greska na liniji " + mulopTerm.getLine() + " : nekompatibilni tipovi u izrazu za operacije mnozenja i deljenja.", null);
			mulopTerm.struct = Tab.noType;
		}
	}
	
	
	//==========================FACTOR===============================	
	public void visit(NumConstFact numConstFact) {
		numConstFact.struct = Tab.intType;
	}
	
	public void visit(CharConstFact charConstFact) {
		charConstFact.struct = Tab.charType;
	}
	
	public void visit(BoolConstFact boolConstFact) {
		boolConstFact.struct = boolType;
	}
	
	public void visit(NewExprFact newExprFact) {
		if (!newExprFact.getExpr().struct.assignableTo(Tab.intType) && !enumAssignable(Tab.intType, newExprFact.getExpr().struct)) {
			report_error("Greska na liniji " + newExprFact.getLine() + " : Izraz u naredbi kreiranja niza mora da bude tipa int!", null);
			newExprFact.struct = Tab.noType;
		}
		else {
			newExprFact.struct = new Struct(Struct.Array, newExprFact.getType().struct);
		}
	}
	
	public void visit(NewFact newFact) {
		newFact.struct = newFact.getType().struct;	
	}
	
	public void visit(Var var) {
		Obj obj = Tab.find(var.getDesignator().obj.getName());
		if (obj == Tab.noObj) {
			report_error("Greska na liniji " + var.getLine() + " : Promenljiva " + var.getDesignator().obj.getName() + " nije definisana!", null);
		}
		
		var.struct = var.getDesignator().obj.getType();
	}
	
	public void visit(ExprFact exprFact) {
		exprFact.struct = exprFact.getExpr().struct;
	}
	
	public void visit(FuncCall funcCall) {
		Obj func = funcCall.getFuncName().getDesignator().obj;
		if (Obj.Meth == func.getKind()) {
			
			if (Tab.noType == func.getType()) {
				report_error("Greska na liniji " + funcCall.getLine() + " : Funkcija bez povratne vrednosti se ne moze koristiti u izrazima!", null);
			}
			
			report_info("Pronadjen poziv funkcije " + funcCall.getLine() + " na liniji " + funcCall.getLine(), null);
			funcCall.struct = func.getType();
			
			if (currentMethodCall != null) {
				if (methodFormalPars.get(currentMethodCall.getName()).size() != actParamNo) {
					report_error("Greska na liniji " + funcCall.getLine() + " nije prosledjen dovoljan broj argumenata pozivu funkcije!", null);
				}
			}
			
			currentMethodCall = null;
			actParamNo = 0;
		} 
		else {
			//TODO: Dobijes noObj kao ime funkcije kada funkcija nije deklarisana
			report_error("Greska na liniji " + funcCall.getLine() + " : ime " + func.getName() + " nije funkcija!", null);
			funcCall.struct = Tab.noType;
		}
	}
	
	
	//======================DESIGNATOR STATEMENTS===================
	public void visit(Assignment assignment) {
		
		if (assignment.getDesignator().obj.getKind() == Obj.Con) {
			report_error("Greska na liniji " + assignment.getLine() + " : Vrednost konstanti se ne moze menjati!", null);
		}
		Struct exprType = assignment.getExpr().struct;
		Struct destType = assignment.getDesignator().obj.getType();
		
		if (!(exprType.isRefType() ^ destType.isRefType())) {
			if (!exprType.assignableTo(destType) && !enumAssignable(destType, exprType)
			) {
				report_error("Greska na liniji " + assignment.getLine() + " : " + "Nekomaptibilni tipovi u dodeli vrednosti!", null);
			}
		}
		else if (exprType.isRefType()){
			if (!exprType.getElemType().assignableTo(destType)) {
				report_error("Greska na liniji " + assignment.getLine() + " : " + "Nekomaptibilni tipovi u dodeli vrednosti!", null);
			}
		}
		else {
			if (!exprType.assignableTo(destType.getElemType()) && !enumAssignable(destType.getElemType(), exprType)) {
				report_error("Greska na liniji " + assignment.getLine() + " : " + "Nekomaptibilni tipovi u dodeli vrednosti!", null);
			}
		}
		
	}
	
	public void visit(Increment increment) {
		Obj obj = Tab.find(increment.getDesignator().obj.getName());
		if (obj == Tab.noObj) {
			report_error("Greska na liniji " + increment.getLine() + " : Promenljiva " + increment.getDesignator().obj.getName() + " nije deklarisana!", null);
		}
		else if (!obj.getType().equals(Tab.intType) || (obj.getKind() != Obj.Var)) {
			report_error("Greska na liniji " + increment.getLine() + " : Nedozvoljena operacija!", null);
		}
		
	//	increment.struct = increment.getDesignator().obj.getType();
	}
	
	public void visit(Decrement decrement) {
		Obj obj = Tab.find(decrement.getDesignator().obj.getName());
		if (obj == Tab.noObj) {
			report_error("Greska na liniji " + decrement.getLine() + " : Promenljiva " + decrement.getDesignator().obj.getName() + " nije deklarisana!", null);
		}
		else if (!obj.getType().equals(Tab.intType) || (obj.getKind() != Obj.Var)) {
			report_error("Greska na liniji " + decrement.getLine() + " : Nekompatibilni tipovi!", null);
		}
		
	//	decrement.struct = decrement.getDesignator().obj.getType();
	}
	
	public void visit(ProcCall procCall) {
		Obj func = procCall.getFuncName().getDesignator().obj;
		if (Obj.Meth == func.getKind()) {
			report_info("Pronadjen poziv procedure " + procCall.getLine() + " na liniji " + procCall.getLine(), null);
			procCall.struct = func.getType();
			
			if (currentMethodCall != null) {
				if (methodFormalPars.get(currentMethodCall.getName()).size() != actParamNo) {
					report_error("Greska na liniji " + procCall.getLine() + " nije prosledjen dovoljan broj argumenata pozivu metode!", null);
				}
			}
		} 
		else {
			//TODO: Dobijes noObj kao ime funkcije kada funkcija nije deklarisana
			report_error("Greska na liniji " + procCall.getLine() + " : ime " + procCall.getFuncName().getDesignator().obj.getName() + " nije funkcija!", null);
			procCall.struct = Tab.noType;
		}
	}
	
	
	//===========================STATEMENTS=========================	
	public void visit(ReadStmt readStmt) {
		String objName = readStmt.getDesignator().obj.getName();
		Obj obj = Tab.find(objName);
		if (obj.equals(Tab.noObj)) {
			report_error("Greska na liniji " + readStmt.getLine() + " : Promenljiva sa imenom " + objName + " nije definisana!", null);
		}
		
		if (obj.getKind() != Obj.Var) {
			report_error("Greska na liniji " + readStmt.getLine() + " : " + objName + " nije promenljiva!", null);
		}
	}
	
	public void visit(ReturnStmt returnStmt) {
		returnFound = true;
		Struct currMethType = currentMethod.getType();
		if (!currMethType.compatibleWith(returnStmt.getExpr().struct) && !enumAssignable(currMethType, returnStmt.getExpr().struct)) {
			report_error("Greska na liniji " + returnStmt.getLine() + " : " + "tip izraza u return naredbi ne slaze se sa tipom povratne vrednosti funkcije " + currentMethod.getName(), null);
		}
	}
	
	public void visit(PrintNumStmt printNumStmt) {
		Struct t = printNumStmt.getExpr().struct;
		if (t != Tab.intType && t != Tab.charType) report_error("Greska na liniji " + printNumStmt.getLine() + " : Operant instruckije PRINT mora biti INT ili CHAR tipa!", null); 
	}
	
	public void visit(PrintStmt printStmt) {
		Struct t = printStmt.getExpr().struct;
		//Tab.
		if (t.getKind() == Struct.Array) {
			t = t.getElemType();
		}
		
		if (t != Tab.intType && t != Tab.charType) 
			report_error("Greska na liniji " + printStmt.getLine() + " : Operant instruckije PRINT mora biti INT ili CHAR tipa!", null); 
	}
	
	
	//============================UTILS=============================
	public boolean passed() {
		return !errorDetected;
	}
	
	private boolean enumCompatible(Struct s1, Struct s2) {
		return enumAssignable(s1, s2) || enumAssignable(s2, s1);
	}
	
	private boolean enumAssignable(Struct dest, Struct src) {
		return dest.equals(Tab.intType) 
				&& src.equals(enumType);
	}
	
	private Obj findObjInCurrentScope(String name) {
		Scope s = Tab.currentScope();
		if (s == null) {
			return Tab.noObj;
		}
		
		if (s.getLocals() == null) {
			return Tab.noObj;
		}
		
		Obj obj = s.getLocals().searchKey(name);
		return obj != null ? obj : Tab.noObj;
	}
}
