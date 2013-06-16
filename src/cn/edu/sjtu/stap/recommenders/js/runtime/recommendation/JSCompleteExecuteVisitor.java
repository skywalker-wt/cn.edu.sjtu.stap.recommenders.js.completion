package cn.edu.sjtu.stap.recommenders.js.runtime.recommendation;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.wst.jsdt.core.ast.ASTVisitor;
import org.eclipse.wst.jsdt.core.ast.IAND_AND_Expression;
import org.eclipse.wst.jsdt.core.ast.IASTNode;
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
import org.eclipse.wst.jsdt.core.ast.ILabeledStatement;
import org.eclipse.wst.jsdt.core.ast.IListExpression;
import org.eclipse.wst.jsdt.core.ast.ILocalDeclaration;
import org.eclipse.wst.jsdt.core.ast.INullLiteral;
import org.eclipse.wst.jsdt.core.ast.IOR_OR_Expression;
import org.eclipse.wst.jsdt.core.ast.IObjectLiteral;
import org.eclipse.wst.jsdt.core.ast.IObjectLiteralField;
import org.eclipse.wst.jsdt.core.ast.IPostfixExpression;
import org.eclipse.wst.jsdt.core.ast.IPrefixExpression;
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
import org.eclipse.wst.jsdt.core.infer.InferredAttribute;
import org.eclipse.wst.jsdt.core.infer.InferredMethod;
import org.eclipse.wst.jsdt.core.infer.InferredType;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import cn.edu.sjtu.stap.recommenders.js.model.JSEngine;
import cn.edu.sjtu.stap.recommenders.js.model.JSObjectGenerator;

public class JSCompleteExecuteVisitor extends ASTVisitor {
	public static final String EXECUTE_SCOPE_NAME = "EXECUTE_SCOPE_NAME";
	public static final int DEFAULT_REPEAT_TIME = 50;
	
	final private Set<IASTNode> parentNodesOfCompleteSite;
	final private JSEngine engine;
	final private IASTNode topAstNode;
	
	private Scriptable executeScope;
	private IASTNode nextASTNode;
	private List<RuntimeException> evaluatorExceptions = new LinkedList<RuntimeException>();
	
	public JSCompleteExecuteVisitor(final Set<IASTNode> parentNodesOfCompleteSite,
						  final JSEngine engine,
						  final Scriptable parentScope,
						  final IASTNode topAstNode) {
		this.parentNodesOfCompleteSite = parentNodesOfCompleteSite;
		this.engine = engine;
		this.topAstNode = topAstNode;
		
		parentScope.delete(EXECUTE_SCOPE_NAME);
		executeScope = new ScriptableObject() {
			@Override
			public String getClassName() {
				return EXECUTE_SCOPE_NAME;
			}
		};
		executeScope.setParentScope(parentScope);
	}

	public Scriptable getExecuteScope() {
		return executeScope;
	}

	public IASTNode getNextASTNode() {
		return nextASTNode;
	}

	public void setNextASTNode(IASTNode nextASTNode) {
		this.nextASTNode = nextASTNode;
	}
	
	public List<RuntimeException> getEvaluatorExceptions() {
		return evaluatorExceptions;
	}

	protected boolean checkAndExecute(IASTNode astNode) {
		if (astNode == topAstNode) return true;
		
		if (isParentOfCompleteSite(astNode)) {
			nextASTNode = astNode;
		} else {
			if (needExecute()) {
				executeAstNode(astNode, DEFAULT_REPEAT_TIME);
			}
		}
		
		return false;
	}
	
	private void executeAstNode(final IASTNode astNode, final int repeatTime) {
		try {
			engine.execute(astNode.toString(), executeScope);
		} 
		catch (EcmaError ecmaError) {
			handleEcmaError(astNode, ecmaError, repeatTime);
		}
		catch (RuntimeException evaluatorException) {
			evaluatorExceptions.add(evaluatorException);
		}
	}
	
	private void handleEcmaError(final IASTNode astNode, final EcmaError ecmaError, final int repeatTime) {
		
//		
//		
//		executeAstNode(astNode, repeatTime - 1);
//		
//		evaluatorExceptions.add(ecmaError);
	}
	
	private boolean isParentOfCompleteSite(final IASTNode astNode) {
		return parentNodesOfCompleteSite.contains(astNode);
	}
	
	private boolean needExecute() {
		// TODO: add logic of astnode like Object Literal
		if (nextASTNode != null) return false;
		
		return true;
	}
	
	public boolean visit(IAND_AND_Expression and_and_Expression) {
		return checkAndExecute(and_and_Expression); 
	}
	public boolean visit(IArgument argument) {
		return checkAndExecute(argument); 
	}

	public boolean visit(IArrayAllocationExpression arrayAllocationExpression) {
		return checkAndExecute(arrayAllocationExpression); 
	}
	public boolean visit(IArrayInitializer arrayInitializer) {
		return checkAndExecute(arrayInitializer); 
	}
	public boolean visit(IArrayQualifiedTypeReference arrayQualifiedTypeReference) {
		return checkAndExecute(arrayQualifiedTypeReference); 
	}

	public boolean visit(IArrayReference arrayReference) {
		return checkAndExecute(arrayReference); 
	}
	public boolean visit(IArrayTypeReference arrayTypeReference) {
		return checkAndExecute(arrayTypeReference); 
	}
	public boolean visit(IAssignment assignment) {
		return checkAndExecute(assignment); 
	}
	public boolean visit(IBinaryExpression binaryExpression) {
		return checkAndExecute(binaryExpression); 
	}
	public boolean visit(IBlock block) {
		return checkAndExecute(block); 
	}
	public boolean visit(IBreakStatement breakStatement) {
		return checkAndExecute(breakStatement); 
	}
	public boolean visit(ICaseStatement caseStatement) {
		return checkAndExecute(caseStatement); 
	}
	public boolean visit(IScriptFileDeclaration compilationUnitDeclaration) {
		return checkAndExecute(compilationUnitDeclaration); 
	}
	public boolean visit(ICompoundAssignment compoundAssignment) {
		return checkAndExecute(compoundAssignment); 
	}
	public boolean visit(IConditionalExpression conditionalExpression) {
		return checkAndExecute(conditionalExpression); 
	}
	public boolean visit(IConstructorDeclaration constructorDeclaration) {
		return checkAndExecute(constructorDeclaration); 
	}
	public boolean visit(IContinueStatement continueStatement) {
		return checkAndExecute(continueStatement); 
	}
	public boolean visit(IDoStatement doStatement) {
		return checkAndExecute(doStatement); 
	}
	public boolean visit(IDoubleLiteral doubleLiteral) {
		return checkAndExecute(doubleLiteral); 
	}
	public boolean visit(IEmptyStatement emptyStatement) {
		return checkAndExecute(emptyStatement); 
	}
	public boolean visit(IEqualExpression equalExpression) {
		return checkAndExecute(equalExpression); 
	}
	public boolean visit(IExplicitConstructorCall explicitConstructor) {
		return checkAndExecute(explicitConstructor); 
	}
	public boolean visit(IExtendedStringLiteral extendedStringLiteral) {
		return checkAndExecute(extendedStringLiteral); 
	}
	public boolean visit(IFalseLiteral falseLiteral) {
		return checkAndExecute(falseLiteral); 
	}
	public boolean visit(IFieldDeclaration fieldDeclaration) {
		return checkAndExecute(fieldDeclaration); 
	}
	public boolean visit(IFieldReference fieldReference) {
		return checkAndExecute(fieldReference); 
	}
	public boolean visit(IForeachStatement forStatement) {
		return checkAndExecute(forStatement); 
	}
	public boolean visit(IForInStatement forInStatement) {
		return checkAndExecute(forInStatement); 
	}
	public boolean visit(IForStatement forStatement) {
		return checkAndExecute(forStatement); 
	}
	public boolean visit(IFunctionExpression functionExpression) {
		return checkAndExecute(functionExpression); 
	}
	public boolean visit(IIfStatement ifStatement) {
		return checkAndExecute(ifStatement); 
	}
	public boolean visit(IImportReference importRef) {
		return checkAndExecute(importRef); 
	}

	public boolean visit(InferredType inferredType) {
		return checkAndExecute(inferredType); 
	}

	public boolean visit(InferredMethod inferredMethod) {
		return checkAndExecute(inferredMethod); 
	}

	public boolean visit(InferredAttribute inferredField) {
		return checkAndExecute(inferredField); 
	}
	public boolean visit(IInitializer initializer) {
		return checkAndExecute(initializer); 
	}
	public boolean visit(IInstanceOfExpression instanceOfExpression) {
		return checkAndExecute(instanceOfExpression); 
	}
	public boolean visit(IIntLiteral intLiteral) {
		return checkAndExecute(intLiteral); 
	}
	public boolean visit(IJsDoc javadoc) {
		return checkAndExecute(javadoc); 
	}
	public boolean visit(ILabeledStatement labeledStatement) {
		return checkAndExecute(labeledStatement); 
	}
	public boolean visit(ILocalDeclaration localDeclaration) {
		return checkAndExecute(localDeclaration); 
	}
	public boolean visit(IListExpression listDeclaration) {
		return checkAndExecute(listDeclaration); 
	}
	public boolean visit(IFunctionCall functionCall) {
		return checkAndExecute(functionCall); 
	}
	public boolean visit(IFunctionDeclaration functionDeclaration) {
		return checkAndExecute(functionDeclaration); 
	}
	public boolean visit(IStringLiteralConcatenation literal) {
		return checkAndExecute(literal); 
	}
	public boolean visit(INullLiteral nullLiteral) {
		return checkAndExecute(nullLiteral); 
	}
	public boolean visit(IOR_OR_Expression or_or_Expression) {
		return checkAndExecute(or_or_Expression); 
	}
	public boolean visit(IPostfixExpression postfixExpression) {
		return checkAndExecute(postfixExpression); 
	}
	public boolean visit(IPrefixExpression prefixExpression) {
		return checkAndExecute(prefixExpression); 
	}
	public boolean visit(IQualifiedNameReference qualifiedNameReference) {
		return checkAndExecute(qualifiedNameReference); 
	}
	public boolean visit(IQualifiedThisReference qualifiedThisReference) {
		return checkAndExecute(qualifiedThisReference); 
	}

	public boolean visit(IQualifiedTypeReference qualifiedTypeReference) {
		return checkAndExecute(qualifiedTypeReference); 
	}

	public boolean visit(IRegExLiteral stringLiteral) {
		return checkAndExecute(stringLiteral); 
	}
	public boolean visit(IReturnStatement returnStatement) {
		return checkAndExecute(returnStatement); 
	}
	public boolean visit(ISingleNameReference singleNameReference) {
		return checkAndExecute(singleNameReference); 
	}

	public boolean visit(ISingleTypeReference singleTypeReference) {
		return checkAndExecute(singleTypeReference); 
	}

	public boolean visit(IStringLiteral stringLiteral) {
		return checkAndExecute(stringLiteral); 
	}
	public boolean visit(ISuperReference superReference) {
		return checkAndExecute(superReference); 
	}
	public boolean visit(ISwitchStatement switchStatement) {
		return checkAndExecute(switchStatement); 
	}

	public boolean visit(IThisReference thisReference) {
		return checkAndExecute(thisReference); 
	}

	public boolean visit(IThrowStatement throwStatement) {
		return checkAndExecute(throwStatement); 
	}
	public boolean visit(ITrueLiteral trueLiteral) {
		return checkAndExecute(trueLiteral); 
	}
	public boolean visit(ITryStatement tryStatement) {
		return checkAndExecute(tryStatement); 
	}
	public boolean visit(ITypeDeclaration localTypeDeclaration) {
		return checkAndExecute(localTypeDeclaration); 
	}

	public boolean visit(IUnaryExpression unaryExpression) {
		return checkAndExecute(unaryExpression); 
	}
	public boolean visit(IUndefinedLiteral undefined) {
		return checkAndExecute(undefined); 
	}
	public boolean visit(IWhileStatement whileStatement) {
		return checkAndExecute(whileStatement); 
	}
	public boolean visit(IWithStatement whileStatement) {
		return checkAndExecute(whileStatement); 
	}
	public boolean visit(IObjectLiteral literal) {
		return checkAndExecute(literal); 
	}
	public boolean visit(IObjectLiteralField field) {
		return checkAndExecute(field); 
	}
}
