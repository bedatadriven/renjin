package org.renjin.gcc.gimple;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleNull;
import org.renjin.gcc.gimple.expr.GimpleVar;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.gimple.type.PrimitiveType;

import java.util.List;
import java.util.Map;

public class GimpleFunction {
	private String name;
	private List<GimpleBasicBlock> basicBlocks = Lists.newArrayList();
	private List<GimpleParameter> parameters = Lists.newArrayList();
	private List<GimpleVarDecl> variableDeclarations = Lists.newArrayList();
	private Map<String, GimpleType> typeMap = Maps.newHashMap();

  GimpleFunction(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void addParameter(GimpleParameter parameter) {
		parameters.add(parameter);
		typeMap.put(parameter.getName(), parameter.getType());
	}
	
	public GimpleBasicBlock addBasicBlock(String label) {
		if(!label.toLowerCase().startsWith("bb ")) {
			throw new IllegalArgumentException("Expected label in the form 'BB 999', got: '"  + label + "'");
		}
		int number = Integer.parseInt(label.substring(3));
		GimpleBasicBlock bb = new GimpleBasicBlock(number);
		basicBlocks.add(bb);
		return bb;
	}

	public void addVarDecl(GimpleVarDecl decl) {
		variableDeclarations.add(decl);
		typeMap.put(decl.getName(), decl.getType());
	}

	public GimpleType getVariableType(String name) {
    if(typeMap.containsKey(name)) {
      return typeMap.get(name);
    }
		throw new IllegalArgumentException(name);
	}

  public List<GimpleVarDecl> getVariableDeclarations() {
    return variableDeclarations;
  }

  public GimpleType getType(GimpleExpr expr) {
		if(expr instanceof GimpleVar) {
			return getVariableType(((GimpleVar) expr).getName());
    } else if(expr == GimpleNull.INSTANCE) {
      return PrimitiveType.VOID_TYPE;
		} else {
			throw new UnsupportedOperationException("don't know how to deduce type for '" + expr + "'");
		}
	}
	
	public GimpleType returnType() {
		for(GimpleBasicBlock bb : basicBlocks) {
			for(GimpleIns ins : bb.getInstructions()) {
				if(ins instanceof GimpleReturn) {
					return getType( ((GimpleReturn)ins).getValue() );
				}
			}
		}
		return PrimitiveType.VOID_TYPE;
	}
	
	public List<GimpleParameter> getParameters() {
		return parameters;
	}

  public void visitIns(GimpleVisitor visitor) {
    for(GimpleBasicBlock bb : basicBlocks) {
      visitor.blockStart(bb);
      for(GimpleIns ins : bb.getInstructions()) {
        ins.visit(visitor);
      }
    }
  }
	
	public List<GimpleBasicBlock> getBasicBlocks() {
		return basicBlocks;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append(" (");
		Joiner.on(", ").appendTo(sb, parameters);
		sb.append(")\n");
		sb.append("{\n");
		for(GimpleVarDecl decl : variableDeclarations) {
			sb.append(decl).append("\n");
		}
		for(GimpleBasicBlock bb : basicBlocks) {
			sb.append(bb.toString());
		}
		sb.append("}\n");
		return sb.toString();
	}

}
