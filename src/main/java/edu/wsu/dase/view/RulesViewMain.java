package edu.wsu.dase.view;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.table.DefaultTableModel;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.semanticweb.owlapi.model.OWLOntology;
import org.swrlapi.exceptions.SWRLAPIException;
import org.swrlapi.ui.dialog.SWRLRuleEngineDialogManager;
import org.swrlapi.ui.model.SWRLRuleEngineModel;
import org.swrlapi.ui.view.SWRLAPIView;
import org.swrlapi.ui.view.SWRLRulesTableView;
import org.swrlapi.ui.view.queries.SQWRLQueriesView;

import edu.wsu.dase.controller.Engine;
import edu.wsu.dase.model.RuleTableModel;

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
	private final Engine engine;

	@NonNull
	private RuleTableModel ruleTableModel;

	@NonNull
	private OWLOntology activeOntology;

	public RulesViewMain(@NonNull SWRLRuleEngineModel swrlRuleEngineModel, Engine engine,
			@NonNull SWRLRuleEngineDialogManager dialogManager, OWLOntology activeOntology, JTabbedPane tabbedPane)
			throws SWRLAPIException {

		this.activeOntology = activeOntology;
		this.engine = engine;

		this.ruleEditorView = new RuleEditorPanel(swrlRuleEngineModel, this.engine, this.activeOntology, dialogManager,
				tabbedPane);
		this.engine.setRuleEditorPanel(this.ruleEditorView);

		this.ruleTableModel = new RuleTableModel(this.engine);
		this.engine.setRuleTableModel(this.ruleTableModel);

		this.ruleTablesView = new RuleTablePanel(this.engine, this.ruleTableModel, ruleEditorView);
		this.engine.setRuleTablePanel(this.ruleTablesView);
		
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
