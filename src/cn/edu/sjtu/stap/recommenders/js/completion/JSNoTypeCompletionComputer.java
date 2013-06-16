package cn.edu.sjtu.stap.recommenders.js.completion;

import java.io.IOException;
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
			ContentAssistInvocationContext context, 
			IProgressMonitor monitor) {
		if (context instanceof JavaContentAssistInvocationContext) {
			JavaContentAssistInvocationContext javaContext= (JavaContentAssistInvocationContext) context;
			
			try {
				JSEngine jsExecuteEngine = getJSEngine(javaContext);
				JSNoTypeCompletionEngine completionEngine = new JSNoTypeCompletionEngine(jsExecuteEngine);
				return completionEngine.getProposals(context.getInvocationOffset(), javaContext, monitor);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
	
	private JSEngine getJSEngine(JavaContentAssistInvocationContext context) throws ClassNotFoundException, IOException {
		// TODO Choose the related Engine. Now just use default engine.
		JSEngine outcome = JSEngine.getJSEngine(null);
		if (outcome.getJsObjectModel() == null)
			outcome.initEngine();
		
		return outcome;
	}
}
