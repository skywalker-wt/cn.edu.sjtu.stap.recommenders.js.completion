package cn.edu.sjtu.stap.recommenders.js.parser;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.wst.jsdt.core.ast.ASTVisitor;
import org.eclipse.wst.jsdt.core.ast.IArgument;

public class ParameterExtractor extends ASTVisitor{
	private List<String> arguments = new LinkedList<String>();
	
	public boolean visit(IArgument argument) {
		arguments.add(new String(argument.getName()));
		return super.visit(argument);
	}

	public List<String> getArguments() {
		return arguments;
	}

	public void setArguments(List<String> arguments) {
		this.arguments = arguments;
	}
}
