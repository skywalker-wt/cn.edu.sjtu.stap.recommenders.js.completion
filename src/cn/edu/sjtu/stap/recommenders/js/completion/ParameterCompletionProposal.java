/**
 * Copyright (c) 2011, 2012 Shanghai Jiao Tong University.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *    Tong Wu - initial API and implementation.
 */
package cn.edu.sjtu.stap.recommenders.js.completion;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension3;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.wst.jsdt.ui.text.java.IJavaCompletionProposal;

/**
 * This class is the entity of completion proposal. It is only called
 * by ParameterCompletionProposalComputer. And stores the information of
 * completion proposal. 
 * 
 * @author TongWu
 */
public class ParameterCompletionProposal implements IJavaCompletionProposal, ICompletionProposalExtension2,
	ICompletionProposalExtension3, ICompletionProposalExtension4 {
	
	/**
	 * The field stores the proposal candidate.
	 */
	private final TemplateProposal completion;
	
	/**
	 * Create the instance by its proposal information.
	 * @param compl the proposal create by the ProposalComputer.
	 */
	public ParameterCompletionProposal(final TemplateProposal compl) {
		this.completion = compl;
	}
	
	@Override
    public final void apply(final IDocument document) {
        throw new IllegalStateException("Applying proposals to documents is deprecated");
    }

    @Override
    public final void apply(final ITextViewer viewer, final char trigger, final int stateMask, final int offset) {
    	IDocument document = viewer.getDocument();
    	Point p = viewer.getSelectedRange();
    	try {
			document.replace(p.x, p.y, "");
		} catch (BadLocationException e) {
			System.err.println("ERROR: BadLocationException" + e.getMessage());
		}
    	completion.apply(viewer, trigger, stateMask, offset);
    }

    @Override
    public final String getAdditionalProposalInfo() {
        return completion.getAdditionalProposalInfo();
    }

    @Override
    public final String getDisplayString() {
        return completion.getDisplayString();
    }

    @Override
    public final IContextInformation getContextInformation() {
        return completion.getContextInformation();
    }

    @Override
    public final int getRelevance() {
        return completion.getRelevance();
    }

    @Override
    public final Point getSelection(final IDocument document) {
        return completion.getSelection(document);
    }

    @Override
    public final Image getImage() {
        return completion.getImage();
    }

    @Override
    public final void selected(final ITextViewer viewer, final boolean smartToggle) {
        completion.selected(viewer, smartToggle);
    }
    
    @Override
    public final void unselected(final ITextViewer viewer) {
        completion.unselected(viewer);
    }

    @Override
    public final boolean validate(final IDocument document, final int offset, final DocumentEvent event) {
        return completion.validate(document, offset, event);
    }

    @Override
    public final String toString() {
        return completion.getDisplayString();
    }

    @Override
    public final boolean isAutoInsertable() {
        return true;
    }

    @Override
    public final IInformationControlCreator getInformationControlCreator() {
        return completion.getInformationControlCreator();
    }

    @Override
    public final CharSequence getPrefixCompletionText(final IDocument document, final int completionOffset) {
        return completion.getPrefixCompletionText(document, completionOffset);
    }

    @Override
    public final int getPrefixCompletionStart(final IDocument document, final int completionOffset) {
        return completion.getPrefixCompletionStart(document, completionOffset);
    }
}
