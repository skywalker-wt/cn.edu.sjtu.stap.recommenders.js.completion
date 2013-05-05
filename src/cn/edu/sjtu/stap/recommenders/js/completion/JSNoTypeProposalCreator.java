package cn.edu.sjtu.stap.recommenders.js.completion;

import org.eclipse.jface.text.templates.Template;
import org.eclipse.wst.jsdt.internal.corext.template.java.CompilationUnitContextType;
import org.eclipse.wst.jsdt.internal.corext.template.java.JavaContextType;
import org.eclipse.wst.jsdt.internal.ui.JavaScriptPlugin;
import org.eclipse.wst.jsdt.internal.ui.text.template.contentassist.TemplateProposal;



public class JSNoTypeProposalCreator {
	private TemplateProposal createProposal(
    		final String displayString, 
    		final String desc, 
    		final String replaceString, 
    		final int relevance) {
        
		Template template =
                new Template(displayString, desc, JavaContextType.NAME, replaceString, false);
		CompilationUnitContextType contextType= (CompilationUnitContextType) JavaScriptPlugin.getDefault().getTemplateContextRegistry().getContextType(JavaContextType.NAME);
		
		TemplateProposal p = new TemplateProposal(template, null, null, null);
        
        return p;
    }
}
