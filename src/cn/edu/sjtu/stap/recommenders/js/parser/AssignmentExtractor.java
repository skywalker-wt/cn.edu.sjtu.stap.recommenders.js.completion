package cn.edu.sjtu.stap.recommenders.js.parser;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.wst.jsdt.core.ast.ASTVisitor;
import org.eclipse.wst.jsdt.core.ast.IAssignment;
import org.eclipse.wst.jsdt.core.ast.IExpression;
import org.eclipse.wst.jsdt.core.ast.ILocalDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.ast.SingleNameReference;

public class AssignmentExtractor extends ASTVisitor{
	private String variable;
	private List<IExpression> rightHandExpressions = new LinkedList<IExpression>();
	
	public AssignmentExtractor(String variable) {
		this.variable = variable;
	}
	
	public String getVariable() {
		return variable;
	}

	public void setVariable(String variable) {
		this.variable = variable;
	}

	public List<IExpression> getRightHandExpressions() {
		return rightHandExpressions;
	}

	public void setRightHandExpressions(List<IExpression> rightHandExpressions) {
		this.rightHandExpressions = rightHandExpressions;
	}
	
	public boolean visit(ILocalDeclaration declaration) {
		IAssignment assignment = declaration.getAssignment();
		if (assignment == null) return super.visit(declaration);
		
		IExpression left = assignment.getLeftHandSide();
		if (left instanceof SingleNameReference)
			if (!variable.equals(new String(((SingleNameReference) left).getToken())))
					return super.visit(assignment);
		IExpression right = assignment.getExpression();
		rightHandExpressions.add(right);
		
		return super.visit(declaration);
	}
	
	public boolean visit(IAssignment assignment) {
		IExpression left = assignment.getLeftHandSide();
		if (left instanceof SingleNameReference)
			if (!variable.equals(new String(((SingleNameReference) left).getToken())))
					return super.visit(assignment);
		IExpression right = assignment.getExpression();
		rightHandExpressions.add(right);
		
		return super.visit(assignment);
	}
}
