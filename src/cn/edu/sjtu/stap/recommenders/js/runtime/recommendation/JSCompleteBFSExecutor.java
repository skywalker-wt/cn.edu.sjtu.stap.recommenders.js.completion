package cn.edu.sjtu.stap.recommenders.js.runtime.recommendation;

import java.util.Set;

import org.eclipse.wst.jsdt.core.ast.IASTNode;
import org.mozilla.javascript.Scriptable;

import cn.edu.sjtu.stap.recommenders.js.model.JSEngine;
import cn.edu.sjtu.stap.recommenders.js.model.JSObject;

public class JSCompleteBFSExecutor {
	final private IASTNode executableNode;
	final private JSEngine engine;
	
	public JSCompleteBFSExecutor(final IASTNode executableNode, final JSEngine engine) {
		this.executableNode = executableNode;
		this.engine = engine;
	}
	
	public Scriptable execute() {
		final Set<IASTNode> parentNodesOfCompleteSite = getParentNodesOfCompleteSite();
		IASTNode currentNode = executableNode;
		Scriptable currentParentScope = engine.getJsObjectModel().getGlobal();
		
		while (currentNode != null && 
			   !NeedCompleteJudger.needComplete(currentNode)) {
			JSCompleteExecuteVisitor executeVisitor = new JSCompleteExecuteVisitor(parentNodesOfCompleteSite, 
															   engine, 
															   currentParentScope, 
															   currentNode);
			
			currentNode.traverse(executeVisitor);
			currentNode = executeVisitor.getNextASTNode();
			currentParentScope = executeVisitor.getExecuteScope();
		}
		
		return currentParentScope;
	}
	
	private JSCompleteExecuteVisitor execute(final IASTNode astNode) {
		return null;
	}
	
	private Set<IASTNode> getParentNodesOfCompleteSite() {
		MarkParentNodeOfCompletionSiteVisitor markVisitor = new MarkParentNodeOfCompletionSiteVisitor();
		executableNode.traverse(markVisitor);
		
		return markVisitor.getParentNodes();
	}
	
	private class ExecuteResult {
		private Scriptable result;
		private IASTNode nextNode;
		
		public ExecuteResult(Scriptable result, IASTNode nextNode) {
			this.result = result;
			this.nextNode = nextNode;
		}
	}
}
