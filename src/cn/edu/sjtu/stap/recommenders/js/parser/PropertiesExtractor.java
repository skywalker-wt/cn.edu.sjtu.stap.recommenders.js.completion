package cn.edu.sjtu.stap.recommenders.js.parser;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.wst.jsdt.core.ast.ASTVisitor;
import org.eclipse.wst.jsdt.core.ast.IExpression;
import org.eclipse.wst.jsdt.core.ast.IFieldReference;
import org.eclipse.wst.jsdt.core.ast.IFunctionCall;
import org.eclipse.wst.jsdt.internal.compiler.ast.SingleNameReference;

public class PropertiesExtractor extends ASTVisitor {
	private Set<String> properties = new HashSet<String>();
	private Set<String> functions = new HashSet<String>();
	private String variable;
	
	public PropertiesExtractor(String variable) {
		this.variable = variable;
	}
	
	public Set<String> getProperties() {
		return properties;
	}

	public void setProperties(Set<String> properties) {
		this.properties = properties;
	}

	public String getVariable() {
		return variable;
	}

	public void setVariable(String variable) {
		this.variable = variable;
	}
	
	public Set<String> getFunctions() {
		return functions;
	}

	public void setFunctions(Set<String> functions) {
		this.functions = functions;
	}

	public boolean visit(IFunctionCall function) {
		if (function.getReceiver() != null 
				&& getReceiverToken(function.getReceiver()) != null
				&& new String(getReceiverToken(function.getReceiver())).equals(variable) ) {
			String token = new String(function.getSelector());
			if (token .length() > 0)
				functions.add(token);
		}
		return super.visit(function);
	}

	public boolean visit(IFieldReference fieldReference){
		if (fieldReference.getReceiver() != null 
				&& getReceiverToken(fieldReference.getReceiver()) != null
				&& new String(getReceiverToken(fieldReference.getReceiver())).equals(variable) ) {
			String token = new String(fieldReference.getToken());
			if (token .length() > 0)
				properties.add(token);
		}
		
		return super.visit(fieldReference);
	}
	
	private String getReceiverToken(IExpression expression) {
		String outcome = null;
		if (expression == null) return null;
		if (expression instanceof IFieldReference) 
			outcome = new String(((IFieldReference) expression).getToken());
		else if (expression instanceof SingleNameReference) 
			outcome = new String(((SingleNameReference) expression).getToken());
		
		return outcome;
	}
}
