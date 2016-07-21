package edu.wright.dase.view;

import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.semanticweb.owlapi.model.OWLOntology;
import org.swrlapi.exceptions.SWRLAPIException;
import org.swrlapi.ui.dialog.SWRLRuleEngineDialogManager;
import org.swrlapi.ui.model.SWRLRuleEngineModel;
import org.swrlapi.ui.view.SWRLAPIView;

import edu.wright.dase.controller.Engine;
import edu.wright.dase.model.Constants;
import edu.wright.dase.model.RuleTableModel;

/**
 * Component that presents a SWRL rule editor and rule table view graphical
 * interface.
 * 
 */
public class RulesViewMain extends JSplitPane implements SWRLAPIView {
	private static final long serialVersionUID = 1L;

	private static final double SPLIT_PANE_RESIZE_WEIGHT = 0.6;

	@NonNull
	private final RuleTablePanel ruleTablesView;

	@NonNull
	private final RuleEditorPanel ruleEditorView;

	@NonNull
	private RuleTableModel ruleTableModel;

	public RulesViewMain(@NonNull SWRLRuleEngineDialogManager dialogManager, JTabbedPane tabbedPane)
			throws SWRLAPIException {

		this.ruleEditorView = new RuleEditorPanel(dialogManager, tabbedPane);
		
		//Constants.engineAsStaticReference.setRuleEditorPanel(this.ruleEditorView);

		this.ruleTableModel = new RuleTableModel(Constants.engineAsStaticReference);
		Constants.engineAsStaticReference.setRuleTableModel(this.ruleTableModel);

		this.ruleTablesView = new RuleTablePanel(this.ruleTableModel, ruleEditorView);
		Constants.engineAsStaticReference.setRuleTablePanel(this.ruleTablesView);

		this.ruleTableModel.setView(this.ruleTablesView);

	}

	@Override
	public void initialize() {
		this.ruleTablesView.initialize();
		// this.ruleExecutionView.initialize();

		setOrientation(JSplitPane.VERTICAL_SPLIT);
		setResizeWeight(SPLIT_PANE_RESIZE_WEIGHT);
		setTopComponent(this.ruleEditorView);
		setBottomComponent(this.ruleTablesView);
	}

	@Override
	public void update() {
		this.ruleTablesView.update();
		// this.ruleExecutionView.update();
	}
}
