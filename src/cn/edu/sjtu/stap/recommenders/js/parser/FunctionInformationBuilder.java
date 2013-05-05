package cn.edu.sjtu.stap.recommenders.js.parser;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.wst.jsdt.core.ast.ASTVisitor;
import org.eclipse.wst.jsdt.core.ast.IAssignment;
import org.eclipse.wst.jsdt.core.ast.IBlock;
import org.eclipse.wst.jsdt.core.ast.IExpression;
import org.eclipse.wst.jsdt.core.ast.IFieldDeclaration;
import org.eclipse.wst.jsdt.core.ast.IFieldReference;
import org.eclipse.wst.jsdt.core.ast.IFunctionDeclaration;
import org.eclipse.wst.jsdt.core.ast.IObjectLiteralField;
import org.eclipse.wst.jsdt.internal.compiler.ast.FunctionExpression;
import org.eclipse.wst.jsdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.ast.SingleNameReference;

import cn.edu.sjtu.stap.recommenders.js.model.JSFunctionObject;
import cn.edu.sjtu.stap.recommenders.js.model.JSObject;
import cn.edu.sjtu.stap.recommenders.js.model.JSObjectModel;
import cn.edu.sjtu.stap.recommenders.js.model.JSParameter;

public class FunctionInformationBuilder extends ASTVisitor {
	private JSObjectModel model;
	
	public FunctionInformationBuilder(JSObjectModel model) {
		this.model = model;
	}
	
	public boolean visit(IFunctionDeclaration f) {
		
		MethodDeclaration d;
		
//		Object o = f.getName();
//		Set<JSObject> jsObjects = model.getJSObjectByProperty(new String(f.getName()));
		return super.visit(f);
	}
	
	public boolean visit(IFieldDeclaration de) {
		
		return super.visit(de);
	}
	
	private void collectFunctionInfomation(FunctionExpression function, String name) {
		Set<JSObject> objects = model.getJSObjectByName(name);
		
		ParameterExtractor extractor = new ParameterExtractor();
		function.traverse(extractor);
		
		VariableExtractor pExtractor = new VariableExtractor(new HashSet<String>(extractor.getArguments()));
		function.traverse(pExtractor);
		
		if (objects == null) return;
		for (JSObject obj : objects) {
			if (!(obj instanceof JSFunctionObject)) continue;
			JSFunctionObject fObj = (JSFunctionObject)obj;
			
			for (int i = 0; i < extractor.getArguments().size(); ++i) {
				Set<JSParameter> paraSet = fObj.getParameters().get(i);
				if (paraSet == null) {
					paraSet = new HashSet<JSParameter>();
					fObj.getParameters().put(i, paraSet);
				}
				String pName = extractor.getArguments().get(i);
				JSObject pObj = JSObject.createJSObject(JSObject.NULL_TYPE);
				
				for (String property : pExtractor.getProperties().get(pName))
					pObj.addProperty(property, JSObject.createJSObject(JSObject.NULL_TYPE));
				
				for (String f : pExtractor.getFunctions().get(pName))
					pObj.addProperty(f, JSObject.createJSObject(JSObject.FUNCTION_TYPE));
				
				JSParameter parameter = new JSParameter(pObj);
				parameter.setIndex(i);
				parameter.setName(pName);
				paraSet.add(parameter);
				
				Set<Integer> vCalls = pExtractor.getVariableCalls().get(pName);
				for (int pCount : vCalls) {
					pObj = JSObject.createJSObject(JSObject.FUNCTION_TYPE);
					((JSFunctionObject)pObj).setArgsCount(pCount);
					parameter = new JSParameter(pObj);
					parameter.setIndex(i);
					parameter.setName(pName);
					paraSet.add(parameter);
				}
			}
		}
	}
	
	public boolean visit(IObjectLiteralField field) {
		if (!(field.getInitializer() instanceof FunctionExpression)) return super.visit(field);
		collectFunctionInfomation((FunctionExpression)field.getInitializer(), new String(field.getFieldName().toString()));
		return true;
	}
	
	public boolean visit(IBlock block){
		return super.visit(block);
	}
	
	public boolean visit(IAssignment assignment) {
		System.out.print(assignment.getExpression().getClass());
		if (assignment.getExpression() instanceof FunctionExpression) {
			collectFunctionInfomation((FunctionExpression)assignment.getExpression(), getReceiverToken(assignment.getLeftHandSide()));
		}
		
		return super.visit(assignment);
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
