package cn.edu.sjtu.stap.recommenders.js.runtime.recommendation;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.wst.jsdt.core.ast.ASTVisitor;
import org.eclipse.wst.jsdt.core.ast.IAND_AND_Expression;
import org.eclipse.wst.jsdt.core.ast.IASTNode;
import org.eclipse.wst.jsdt.core.ast.IAllocationExpression;
import org.eclipse.wst.jsdt.core.ast.IArgument;
import org.eclipse.wst.jsdt.core.ast.IArrayAllocationExpression;
import org.eclipse.wst.jsdt.core.ast.IArrayInitializer;
import org.eclipse.wst.jsdt.core.ast.IArrayQualifiedTypeReference;
import org.eclipse.wst.jsdt.core.ast.IArrayReference;
import org.eclipse.wst.jsdt.core.ast.IArrayTypeReference;
import org.eclipse.wst.jsdt.core.ast.IAssignment;
import org.eclipse.wst.jsdt.core.ast.IBinaryExpression;
import org.eclipse.wst.jsdt.core.ast.IBlock;
import org.eclipse.wst.jsdt.core.ast.IBreakStatement;
import org.eclipse.wst.jsdt.core.ast.ICaseStatement;
import org.eclipse.wst.jsdt.core.ast.ICompoundAssignment;
import org.eclipse.wst.jsdt.core.ast.IConditionalExpression;
import org.eclipse.wst.jsdt.core.ast.IConstructorDeclaration;
import org.eclipse.wst.jsdt.core.ast.IContinueStatement;
import org.eclipse.wst.jsdt.core.ast.IDoStatement;
import org.eclipse.wst.jsdt.core.ast.IDoubleLiteral;
import org.eclipse.wst.jsdt.core.ast.IEmptyStatement;
import org.eclipse.wst.jsdt.core.ast.IEqualExpression;
import org.eclipse.wst.jsdt.core.ast.IExplicitConstructorCall;
import org.eclipse.wst.jsdt.core.ast.IExtendedStringLiteral;
import org.eclipse.wst.jsdt.core.ast.IFalseLiteral;
import org.eclipse.wst.jsdt.core.ast.IFieldDeclaration;
import org.eclipse.wst.jsdt.core.ast.IFieldReference;
import org.eclipse.wst.jsdt.core.ast.IForInStatement;
import org.eclipse.wst.jsdt.core.ast.IForStatement;
import org.eclipse.wst.jsdt.core.ast.IForeachStatement;
import org.eclipse.wst.jsdt.core.ast.IFunctionCall;
import org.eclipse.wst.jsdt.core.ast.IFunctionDeclaration;
import org.eclipse.wst.jsdt.core.ast.IFunctionExpression;
import org.eclipse.wst.jsdt.core.ast.IIfStatement;
import org.eclipse.wst.jsdt.core.ast.IImportReference;
import org.eclipse.wst.jsdt.core.ast.IInitializer;
import org.eclipse.wst.jsdt.core.ast.IInstanceOfExpression;
import org.eclipse.wst.jsdt.core.ast.IIntLiteral;
import org.eclipse.wst.jsdt.core.ast.IJsDoc;
import org.eclipse.wst.jsdt.core.ast.IJsDocAllocationExpression;
import org.eclipse.wst.jsdt.core.ast.IJsDocArgumentExpression;
import org.eclipse.wst.jsdt.core.ast.IJsDocArrayQualifiedTypeReference;
import org.eclipse.wst.jsdt.core.ast.IJsDocArraySingleTypeReference;
import org.eclipse.wst.jsdt.core.ast.IJsDocFieldReference;
import org.eclipse.wst.jsdt.core.ast.IJsDocImplicitTypeReference;
import org.eclipse.wst.jsdt.core.ast.IJsDocMessageSend;
import org.eclipse.wst.jsdt.core.ast.IJsDocQualifiedTypeReference;
import org.eclipse.wst.jsdt.core.ast.IJsDocReturnStatement;
import org.eclipse.wst.jsdt.core.ast.IJsDocSingleNameReference;
import org.eclipse.wst.jsdt.core.ast.IJsDocSingleTypeReference;
import org.eclipse.wst.jsdt.core.ast.ILabeledStatement;
import org.eclipse.wst.jsdt.core.ast.IListExpression;
import org.eclipse.wst.jsdt.core.ast.ILocalDeclaration;
import org.eclipse.wst.jsdt.core.ast.INullLiteral;
import org.eclipse.wst.jsdt.core.ast.IOR_OR_Expression;
import org.eclipse.wst.jsdt.core.ast.IObjectLiteral;
import org.eclipse.wst.jsdt.core.ast.IObjectLiteralField;
import org.eclipse.wst.jsdt.core.ast.IPostfixExpression;
import org.eclipse.wst.jsdt.core.ast.IPrefixExpression;
import org.eclipse.wst.jsdt.core.ast.IQualifiedAllocationExpression;
import org.eclipse.wst.jsdt.core.ast.IQualifiedNameReference;
import org.eclipse.wst.jsdt.core.ast.IQualifiedThisReference;
import org.eclipse.wst.jsdt.core.ast.IQualifiedTypeReference;
import org.eclipse.wst.jsdt.core.ast.IRegExLiteral;
import org.eclipse.wst.jsdt.core.ast.IReturnStatement;
import org.eclipse.wst.jsdt.core.ast.IScriptFileDeclaration;
import org.eclipse.wst.jsdt.core.ast.ISingleNameReference;
import org.eclipse.wst.jsdt.core.ast.ISingleTypeReference;
import org.eclipse.wst.jsdt.core.ast.IStringLiteral;
import org.eclipse.wst.jsdt.core.ast.IStringLiteralConcatenation;
import org.eclipse.wst.jsdt.core.ast.ISuperReference;
import org.eclipse.wst.jsdt.core.ast.ISwitchStatement;
import org.eclipse.wst.jsdt.core.ast.IThisReference;
import org.eclipse.wst.jsdt.core.ast.IThrowStatement;
import org.eclipse.wst.jsdt.core.ast.ITrueLiteral;
import org.eclipse.wst.jsdt.core.ast.ITryStatement;
import org.eclipse.wst.jsdt.core.ast.ITypeDeclaration;
import org.eclipse.wst.jsdt.core.ast.IUnaryExpression;
import org.eclipse.wst.jsdt.core.ast.IUndefinedLiteral;
import org.eclipse.wst.jsdt.core.ast.IWhileStatement;
import org.eclipse.wst.jsdt.core.ast.IWithStatement;
import org.eclipse.wst.jsdt.core.infer.InferredType;
import org.eclipse.wst.jsdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.wst.jsdt.internal.codeassist.complete.CompletionOnSingleNameReference;

public class MarkParentNodeOfCompletionSiteVisitor extends ASTVisitor{
	private int completeStart = -1;
	private int completeEnd = -1;
	private Set<IASTNode> parentNodes = new HashSet<IASTNode>();
	
	private void checkAndRecordParentNode(IASTNode astNode) {
		if (astNode.sourceStart() <= completeStart &&
			astNode.sourceEnd() >= completeEnd) {
			parentNodes.add(astNode);
		}
	}
	
	public boolean visit(IArgument argument) {
		if (NeedCompleteJudger.needComplete(argument)) {
			completeStart = argument.sourceStart();
			completeEnd = argument.sourceEnd();
		}
		
		return super.visit(argument);
	}
	
	public boolean visit(IFieldReference fieldReference){
		if (NeedCompleteJudger.needComplete(fieldReference)) {
			completeStart = fieldReference.sourceStart();
			completeEnd = fieldReference.sourceEnd();
		}
		
		return super.visit(fieldReference);
	}
	
	public void endVisit(IAND_AND_Expression and_and_Expression) {
		checkAndRecordParentNode(and_and_Expression);
	}
	public void endVisit(IArgument argument) {
		checkAndRecordParentNode(argument);
	}

	public void endVisit(IArrayAllocationExpression arrayAllocationExpression) {
		checkAndRecordParentNode(arrayAllocationExpression);
	}
	
	public void endVisit(IArrayInitializer arrayInitializer) {
		checkAndRecordParentNode(arrayInitializer);
	}
	
	public void endVisit(IArrayQualifiedTypeReference arrayQualifiedTypeReference) {
		checkAndRecordParentNode(arrayQualifiedTypeReference);
	}
	
	public void endVisit(IArrayReference arrayReference) {
		checkAndRecordParentNode(arrayReference);
	}
	
	public void endVisit(IArrayTypeReference arrayTypeReference) {
		checkAndRecordParentNode(arrayTypeReference);
	}
	public void endVisit(IAssignment assignment) {
		checkAndRecordParentNode(assignment);
	}
	public void endVisit(IBinaryExpression binaryExpression) {
		checkAndRecordParentNode(binaryExpression);
	}
	public void endVisit(IBlock block) {
		checkAndRecordParentNode(block);
	}
	public void endVisit(IBreakStatement breakStatement) {
		checkAndRecordParentNode(breakStatement);
	}
	public void endVisit(ICaseStatement caseStatement) {
		checkAndRecordParentNode(caseStatement);
	}
	public void endVisit(IScriptFileDeclaration scriptFileDeclaration) {		
		checkAndRecordParentNode(scriptFileDeclaration);
	}
	public void endVisit(ICompoundAssignment compoundAssignment) {
		checkAndRecordParentNode(compoundAssignment);
	}
	public void endVisit(IConditionalExpression conditionalExpression) {
		checkAndRecordParentNode(conditionalExpression);
	}
	public void endVisit(IConstructorDeclaration constructorDeclaration) {
		checkAndRecordParentNode(constructorDeclaration);
	}
	public void endVisit(IContinueStatement continueStatement) {
		checkAndRecordParentNode(continueStatement);
	}
	public void endVisit(IDoStatement doStatement) {
		checkAndRecordParentNode(doStatement);
	}
	public void endVisit(IDoubleLiteral doubleLiteral) {
		checkAndRecordParentNode(doubleLiteral);
	}
	public void endVisit(IEmptyStatement emptyStatement) {
		checkAndRecordParentNode(emptyStatement);
	}
	public void endVisit(IEqualExpression equalExpression) {
		checkAndRecordParentNode(equalExpression);
	}
	public void endVisit(IExplicitConstructorCall explicitConstructor) {
		checkAndRecordParentNode(explicitConstructor);
	}
	public void endVisit(IExtendedStringLiteral extendedStringLiteral) {
		checkAndRecordParentNode(extendedStringLiteral);
	}
	public void endVisit(IFalseLiteral falseLiteral) {
		checkAndRecordParentNode(falseLiteral);
	}
	public void endVisit(IFieldDeclaration fieldDeclaration) {
		checkAndRecordParentNode(fieldDeclaration);
	}
	
	public void endVisit(IFieldReference fieldDeclaration) {
		checkAndRecordParentNode(fieldDeclaration);
	}
	
	public void endVisit(IForeachStatement forStatement) {
		checkAndRecordParentNode(forStatement);
	}
	public void endVisit(IForStatement forStatement) {
		checkAndRecordParentNode(forStatement);
	}
	public void endVisit(IForInStatement forInStatement) {
		checkAndRecordParentNode(forInStatement);
	}

	public void endVisit(IFunctionExpression functionExpression) {
		checkAndRecordParentNode(functionExpression);
	}

	public void endVisit(IIfStatement ifStatement) {
		checkAndRecordParentNode(ifStatement);
	}
	public void endVisit(IImportReference importRef) {
		checkAndRecordParentNode(importRef);
	}
	public void endVisit(InferredType inferredType) {
		checkAndRecordParentNode(inferredType);
	}

	public void endVisit(IInitializer initializer) {
		checkAndRecordParentNode(initializer);
	}
	public void endVisit(IInstanceOfExpression instanceOfExpression) {
		checkAndRecordParentNode(instanceOfExpression);
	}
	public void endVisit(IIntLiteral intLiteral) {
		checkAndRecordParentNode(intLiteral);
	}
	public void endVisit(IJsDoc javadoc) {
		checkAndRecordParentNode(javadoc);
	}
	public void endVisit(ILabeledStatement labeledStatement) {
		checkAndRecordParentNode(labeledStatement);
	}
	public void endVisit(ILocalDeclaration localDeclaration) {
		checkAndRecordParentNode(localDeclaration);
	}
	public void endVisit(IListExpression listDeclaration) {
		checkAndRecordParentNode(listDeclaration);
	}
	public void endVisit(IFunctionCall messageSend) {
		checkAndRecordParentNode(messageSend);
	}
	public void endVisit(IFunctionDeclaration methodDeclaration) {
		checkAndRecordParentNode(methodDeclaration);
	}
	public void endVisit(IStringLiteralConcatenation literal) {
		checkAndRecordParentNode(literal);
	}
	public void endVisit(INullLiteral nullLiteral) {
		checkAndRecordParentNode(nullLiteral);
	}
	public void endVisit(IOR_OR_Expression or_or_Expression) {
		checkAndRecordParentNode(or_or_Expression);
	}
	public void endVisit(IPostfixExpression postfixExpression) {
		checkAndRecordParentNode(postfixExpression);
	}
	public void endVisit(IPrefixExpression prefixExpression) {
		checkAndRecordParentNode(prefixExpression);
	}
	public void endVisit(IQualifiedAllocationExpression qualifiedAllocationExpression) {
		checkAndRecordParentNode(qualifiedAllocationExpression.getMember());
	}
	public void endVisit(IQualifiedNameReference qualifiedNameReference) {
		checkAndRecordParentNode(qualifiedNameReference);
	}
	public void endVisit(IQualifiedThisReference qualifiedThisReference) {
		checkAndRecordParentNode(qualifiedThisReference);
	}
	public void endVisit(IQualifiedTypeReference qualifiedTypeReference) {
		checkAndRecordParentNode(qualifiedTypeReference);
	}
	public void endVisit(IRegExLiteral stringLiteral) {
		checkAndRecordParentNode(stringLiteral);
	}
	public void endVisit(IReturnStatement returnStatement) {
		checkAndRecordParentNode(returnStatement);
	}
	public void endVisit(ISingleNameReference singleNameReference) {
		checkAndRecordParentNode(singleNameReference);
	}
	
	public void endVisit(ISingleTypeReference singleTypeReference) {
		checkAndRecordParentNode(singleTypeReference);
	}
	public void endVisit(IStringLiteral stringLiteral) {
		checkAndRecordParentNode(stringLiteral);
	}
	public void endVisit(ISuperReference superReference) {
		checkAndRecordParentNode(superReference);
	}
	public void endVisit(ISwitchStatement switchStatement) {
		checkAndRecordParentNode(switchStatement);
	}
	public void endVisit(IThisReference thisReference) {
		checkAndRecordParentNode(thisReference);
	}
	public void endVisit(IThrowStatement throwStatement) {
		checkAndRecordParentNode(throwStatement);
	}
	public void endVisit(ITrueLiteral trueLiteral) {
		checkAndRecordParentNode(trueLiteral);
	}
	public void endVisit(ITryStatement tryStatement) {
		checkAndRecordParentNode(tryStatement);
	}
	public void endVisit(ITypeDeclaration memberTypeDeclaration) {
		checkAndRecordParentNode(memberTypeDeclaration);
	}
	public void endVisit(IUnaryExpression unaryExpression) {
		checkAndRecordParentNode(unaryExpression);
	}
	public void endVisit(IUndefinedLiteral undefinedLiteral) {
		checkAndRecordParentNode(undefinedLiteral);
	}
	public void endVisit(IWhileStatement whileStatement) {
		checkAndRecordParentNode(whileStatement);
	}
	public void endVisit(IWithStatement whileStatement) {
		checkAndRecordParentNode(whileStatement);
	}
	public void endVisit(IObjectLiteral literal) {
		checkAndRecordParentNode(literal);
	}
	public void endVisit(IObjectLiteralField field) {
		checkAndRecordParentNode(field);
	}

	public Set<IASTNode> getParentNodes() {
		return parentNodes;
	}

	public void setParentNodes(Set<IASTNode> parentNodes) {
		this.parentNodes = parentNodes;
	}
}
