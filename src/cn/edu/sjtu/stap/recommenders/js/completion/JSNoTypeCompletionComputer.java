package cn.edu.sjtu.stap.recommenders.js.completion;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.jsdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.wst.jsdt.ui.text.java.JavaContentAssistInvocationContext;

import cn.edu.sjtu.stap.recommenders.js.model.JSEngine;
import cn.edu.sjtu.stap.recommenders.js.model.JSObjectModel;

public class JSNoTypeCompletionComputer implements
		IJavaCompletionProposalComputer {

	@Override
	public void sessionStarted() {
		// TODO Auto-generated method stub

	}

	@Override
	public List computeCompletionProposals(
			ContentAssistInvocationContext context, IProgressMonitor monitor) {
		if (context instanceof JavaContentAssistInvocationContext) {
			JavaContentAssistInvocationContext javaContext= (JavaContentAssistInvocationContext) context;
			JSObjectModel model = JSEngine.getDefault().getJsObjectModel();
			if (model == null) {
				JSEngine.getDefault().initEngine();
				model = JSEngine.getDefault().getJsObjectModel();
			}
				
			JSNoTypeCompletionEngine completionEngine = new JSNoTypeCompletionEngine(model);
			return completionEngine.getProposals(context.getInvocationOffset(), javaContext, monitor);
		}
		
		return Collections.EMPTY_LIST;
	}

	@Override
	public List computeContextInformation(
			ContentAssistInvocationContext context, IProgressMonitor monitor) {
		
		return Collections.EMPTY_LIST;
	}

	@Override
	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sessionEnded() {
		// TODO Auto-generated method stub

	}

}
