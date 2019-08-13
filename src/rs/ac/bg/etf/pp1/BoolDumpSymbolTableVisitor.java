package rs.ac.bg.etf.pp1;

import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;
import rs.etf.pp1.symboltable.visitors.DumpSymbolTableVisitor;

public class BoolDumpSymbolTableVisitor extends DumpSymbolTableVisitor {
	@Override
	public void visitStructNode(Struct structToVisit) {
		super.visitStructNode(structToVisit);
		switch (structToVisit.getKind()) {
		case Struct.Bool:
			output.append("bool");
			break;
			case Struct.Array:
				switch (structToVisit.getElemType().getKind()) {
				case Struct.Bool:
					output.append("bool");
					break;
				}
				break;
		}
	}
}
