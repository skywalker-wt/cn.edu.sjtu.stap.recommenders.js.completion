package cn.edu.sjtu.stap.recommenders.js.build;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.wst.jsdt.core.dom.ASTVisitor;
import org.eclipse.wst.jsdt.core.dom.FunctionDeclaration;
import org.eclipse.wst.jsdt.core.dom.FunctionExpression;
import org.eclipse.wst.jsdt.core.dom.FunctionRefParameter;
import org.eclipse.wst.jsdt.core.dom.SingleVariableDeclaration;

public class DomParameterExtractor extends ASTVisitor{
	List<String> parameters = new LinkedList<String>();
	
	public List<String> getParameters() {
		return parameters;
	}

	public void setParameters(List<String> parameters) {
		this.parameters = parameters;
	}

	public boolean visit(FunctionDeclaration declaration) {
		final List nodes = declaration.parameters();
		for (Object node : nodes) {
			if (!(node instanceof SingleVariableDeclaration))
				parameters.add("");
			else {
				SingleVariableDeclaration s = (SingleVariableDeclaration)node;
				parameters.add(s.getName().toString());
			}
		}
		return super.visit(declaration);
	}
}
