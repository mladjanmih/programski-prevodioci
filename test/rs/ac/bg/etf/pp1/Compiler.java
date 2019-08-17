package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import java_cup.runtime.Symbol;
import rs.ac.bg.etf.pp1.ast.Program;
import rs.ac.bg.etf.pp1.util.Log4JUtils;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.visitors.DumpSymbolTableVisitor;

public class Compiler {
	static {
		DOMConfigurator.configure(Log4JUtils.instance().findLoggerConfigFile());
		Log4JUtils.instance().prepareLogFile(Logger.getRootLogger());
	}
	
	public static void main(String[] args) throws Exception {
		Logger log = Logger.getLogger(MJParserTest.class);
		Reader br = null;
	
		try {
			File sourceCode = new File(args[0]);
			log.info("Compiling source file: " + sourceCode.getAbsolutePath());
			
			br = new BufferedReader(new FileReader(sourceCode));
			Yylex lexer = new Yylex(br);			
			MJParser p = new MJParser(lexer);
	        
			Symbol s = p.parse();  //pocetak parsiranja
	    
			if (p.errorDetected) {
				log.error("Parsiranje nije uspesno zavrseno.");
				return;
			}
			
			Program prog = (Program)(s.value); 
	        Tab.init();
	        log.info(prog.toString(""));
 
	        Tab.currentScope().addToLocals(new Obj(Obj.Type, "bool", SemanticAnalyzer.boolType));
			SemanticAnalyzer v = new SemanticAnalyzer();
			prog.traverseBottomUp(v); 
			
			tsdump();
			
			if (v.passed()) {
				File objFile = new File(args[1]);
				if (objFile.exists()) {
					objFile.delete();
				}
				
				CodeGenerator codeGenerator = new CodeGenerator();
				prog.traverseBottomUp(codeGenerator);
				Code.dataSize = v.nVars;
				Code.mainPc = codeGenerator.getMainPc();
				Code.write(new FileOutputStream(objFile));
				log.info("Parsiranje uspesno zavrseno.");
			}
		}
	
		finally {
			if (br != null) try { br.close(); } catch (IOException e1) { log.error(e1.getMessage(), e1); }
		}
	}
	
	public static void tsdump() {
		MyDumpSymbolTableVisitor dumpVisitor = new MyDumpSymbolTableVisitor();
		Tab.dump(dumpVisitor);
	}
}
