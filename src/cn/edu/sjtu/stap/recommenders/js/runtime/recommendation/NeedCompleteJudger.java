package cn.edu.sjtu.stap.recommenders.js.runtime.recommendation;

import org.eclipse.wst.jsdt.core.ast.IASTNode;
import org.eclipse.wst.jsdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.wst.jsdt.internal.codeassist.complete.CompletionOnSingleNameReference;

public class NeedCompleteJudger {
	public static boolean needComplete(IASTNode astNode) {
		if (astNode instanceof CompletionOnSingleNameReference ||
			astNode instanceof CompletionOnMemberAccess)
			return true;
			
		return false;
	}
}
