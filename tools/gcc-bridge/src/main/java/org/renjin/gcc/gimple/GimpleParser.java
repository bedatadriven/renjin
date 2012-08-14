package org.renjin.gcc.gimple;

import com.google.common.collect.Lists;
import org.renjin.gcc.gimple.expr.*;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.gimple.type.PointerType;
import org.renjin.gcc.gimple.type.PrimitiveType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

public class GimpleParser {

	
	private GimpleFunction currentFunction;

	public List<GimpleFunction> parse(Reader reader) throws IOException {
		return parse(new BufferedReader(reader));
	}
	
	public List<GimpleFunction> parse(BufferedReader reader) throws IOException {
		
		List<GimpleFunction> functions = Lists.newArrayList();
		
		String line;
		currentFunction = null;
		GimpleBasicBlock currentBB = null;
		
		while((line = reader.readLine()) != null) {
			line = line.trim();
			if(line.isEmpty() || isComment(line)) {
				continue;
			}
			if(currentFunction == null) {
				// expect function declaration
				currentFunction = parseFunctionDeclaration(line);
				functions.add(currentFunction);
				assertNextLine(reader, "{");
			} else if(line.equals("}")) {
				currentFunction = null;
			} else {
				// function body
				if(isLabel(line)) {
					currentBB = currentFunction.addBasicBlock(parseLabel(line));
				} else if(isVarDecl(line)) {
					currentFunction.addVarDecl(parseVarDecl(line));
				} else if(line.startsWith("gimple_assign")) {
					currentBB.addIns(parseAssign(line));
					//  gimple_assign <indirect_ref, D.6853_34, *n_29(D), NULL>
				} else if(line.startsWith("goto")) {
					currentBB.addIns(parseGoto(line));
				} else if(line.startsWith("gimple_cond")) {
					currentBB.addIns(parseCond(line, reader));
				} else if(line.startsWith("gimple_call")) {
					currentBB.addIns(parseCall(line));
				} else if(line.startsWith("gimple_return")) {
					currentBB.addIns(parseReturn(line));
				}
				
			}
		}
		return functions;
	}

	private GimpleVarDecl parseVarDecl(String line) {
		// strip final ;
		int terminatingSemi = line.indexOf(';');
		line = line.substring(0, terminatingSemi);
		
		// split into tokens
    int identifierStart = line.lastIndexOf(' ')+1;
		String identifier = line.substring(identifierStart);
    String typeDecl = line.substring(0, identifierStart-1).trim();
    return new GimpleVarDecl(parseType(typeDecl), identifier);
	}

	private boolean isVarDecl(String line) {
		return line.indexOf('<') == -1;
	}

	private GimpleIns parseReturn(String line) {
		GimpleExpr value = parseExpr(parseInsArguments(line)[0]);
		return new GimpleReturn(value);
	}

	protected GimpleConditional parseCond(String line, BufferedReader reader)
			throws IOException {
//		gimple_cond <lt_expr, D.6854_37, nn2_35, NULL, NULL>
//	      goto <bb 7>;	
//	    else
//	      goto <bb 8>;
		
		String trueBranch = reader.readLine();
		String elseKeyword = reader.readLine().trim();
		String falseBranch = reader.readLine();
		
		String[] arguments = parseInsArguments(line);
		
		GimpleConditional cond = new GimpleConditional();
		cond.setOperator(parseOp(arguments[0]));
		cond.setOperands( parseOperands(arguments, 1 ));
		cond.setTrueTarget( parseGoto(trueBranch).getTarget() );
		cond.setFalseTarget( parseGoto(falseBranch).getTarget() );
		return cond;
	}

	private GimpleAssign parseAssign(String line) {
		//  gimple_assign <indirect_ref, D.6853_34, *n_29(D), NULL>
		String[] arguments = parseInsArguments(line);
		List<GimpleExpr> operands = parseOperands(arguments, 2);
		return new GimpleAssign(parseOp(arguments[0]), parseVar(arguments[1]), operands);
	}

	private GimpleOp parseOp(String string) {
		return GimpleOp.valueOf(string.toUpperCase());
	}

	private List<GimpleExpr> parseOperands(String[] arguments, int start) {
		List<GimpleExpr> operands = Lists.newArrayList();
		for(int i=start;i<arguments.length;++i) {
			operands.add(parseExpr(arguments[i]));
		}
		return operands;
	}

	private String[] parseInsArguments(String line) {
		int start = line.indexOf('<')+1;
		int end = line.lastIndexOf('>');
		String[] arguments = line.substring(start,end).split("\\s*,\\s*");
		return arguments;
	}
	
	private Goto parseGoto(String line) {
		GimpleLabel target = new GimpleLabel(parseInsArguments(line)[0]);
		return new Goto(target);
	}
	
	private GimpleExpr parseExpr(String text) {
    if(text.equals("NULL")) {
      return GimpleNull.INSTANCE;
    } else if(Character.isDigit(text.charAt(0)) || text.charAt(0)=='-') {
			return parseDoubleConstant(text);
		} else {
			return parseName(text);
		}
	}
	
	private GimpleConstant parseDoubleConstant(String text) {
    if(text.equals("0B")) {
      // TODO: not sure if this is correct
      return new GimpleConstant(false);
    } else {
		  return new GimpleConstant(Double.parseDouble(text));
    }
	}

	private GimpleExpr parseName(String name) {
    name = stripNameDecorators(name);

    if(name.matches(".+_\\d+")) {
      return parseVar(name);
    } else {
      return new GimpleExternal(name);
    }
	}

  private String stripNameDecorators(String name) {
    int originStart = name.indexOf('(');
    if(originStart != -1) {
      name = name.substring(0, originStart);
    }
    while(name.startsWith("*")) {
      name = name.substring(1);
    }
    return name;
  }

  private GimpleVar parseVar(String text) {
    String ssaName = stripNameDecorators(text);
    int underscore = ssaName.lastIndexOf('_');
    if(underscore == -1) {
      throw new AssertionError("Expecting ssa name: " + text);
    }
    String name = ssaName.substring(0, underscore);
    int version = Integer.parseInt(ssaName.substring(underscore+1));
    return new GimpleVar(name, version);
  }

  private GimpleCall parseCall(String line) {
    String[] arguments = parseInsArguments(line);
    List<GimpleExpr> operands = parseOperands(arguments, 2);
    if(arguments[1].equals("NULL")) {
      return new GimpleCall(arguments[0], null, operands);
    } else {
      return new GimpleCall(arguments[0], parseVar(arguments[1]), operands);
    }
  }

	private void assertNextLine(BufferedReader reader, String expected) throws IOException {
		String line = reader.readLine();
		if(line == null) {
			throw new GimpleParseException("Unexpected EOF, expected '" + expected + "'");
		}
	}

	private GimpleFunction parseFunctionDeclaration(String line) {
		int paramListStart = line.indexOf('(');
		String name = line.substring(0, paramListStart);
		GimpleFunction fn = new GimpleFunction(name);
		
		int paramListEnd = line.lastIndexOf(')');
		String params[] = line.substring(paramListStart+1, paramListEnd).split("\\s*,\\s*");
		
		for(String param : params) {
			fn.addParameter(parseParameter(param));
		}
		
		return fn;
	}

	private GimpleParameter parseParameter(String paramDecl) {
		int nameStart = paramDecl.lastIndexOf(' ');
		String typeDecl = paramDecl.substring(0, nameStart).trim();
		String name = paramDecl.substring(nameStart+1);
		return new GimpleParameter(parseType(typeDecl), name);
	}

	private GimpleType parseType(String typeDecl) {
		if(typeDecl.endsWith("*")) {
      return new PointerType(parseType(typeDecl.substring(0, typeDecl.length()-1).trim()));
    } else if(typeDecl.equals("double")) {
			return PrimitiveType.DOUBLE_TYPE;
    } else if(typeDecl.equals("float")) {
      return PrimitiveType.FLOAT_TYPE;
    } else if(typeDecl.equals("int") || typeDecl.equals("long unsigned int")) {
      return PrimitiveType.INT_TYPE;
    } else if(typeDecl.equals("_Bool")) {
      return PrimitiveType.BOOLEAN;
		} else {
			throw new UnsupportedOperationException("cannot parse '" + typeDecl + "' type declaration yet");
		}
	}

	private boolean isComment(String line) {
		return line.startsWith(";;");
	}
	
	private boolean isLabel(String line) {
		// labels take the form " <bb 33>: "
		return line.matches("<bb \\d+>:");
	}
	
	private String parseLabel(String line) {
		if(!isLabel(line)) {
			throw new GimpleParseException("Expected label declaration, found: '" + line + "'");
		}
		return line.substring(1, line.length()-2);
	}
}
