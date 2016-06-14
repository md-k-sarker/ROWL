package edu.wsu.dase.view;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.semanticweb.owlapi.model.OWLOntology;
import org.swrlapi.exceptions.SWRLAPIException;
import org.swrlapi.ui.dialog.SWRLRuleEngineDialogManager;
import org.swrlapi.ui.model.SWRLRuleEngineModel;
import org.swrlapi.ui.view.SWRLAPIView;
import org.swrlapi.ui.view.SWRLRulesTableView;
import org.swrlapi.ui.view.queries.SQWRLQueriesView;


/**
 * Component that presents a SWRL editor and rule execution graphical interface.
 * It can be used to embed SWRL rule editing and execution into an application.
 *
 * @see SQWRLQueriesView
 */
public class SWRLRulesViewExtended extends JSplitPane implements SWRLAPIView {
	private static final long serialVersionUID = 1L;

	private static final double SPLIT_PANE_RESIZE_WEIGHT = 0.6;

	@NonNull
	private final SWRLRulesTableView ruleTablesView;
	// @NonNull private final SWRLRuleExecutionView ruleExecutionView;
	@NonNull
	private JPanel topPnl;
	//private JButton integrateWithOntologybtn;
	//private IntegrateWithOntologyAction actionlistener;
	private OWLOntology activeOntology;

	public SWRLRulesViewExtended(@NonNull SWRLRuleEngineModel ruleEngineModel,
			@NonNull SWRLRuleEngineDialogManager dialogManager, OWLOntology activeOntology, JTabbedPane tabbedPane) throws SWRLAPIException {
		this.ruleTablesView = new SWRLRulesTableView(ruleEngineModel, dialogManager);
		this.activeOntology = activeOntology;
		
		topPnl = new JPanel();
		topPnl.setLayout(new BorderLayout());
		//integrateWithOntologybtn = new JButton("Convert to OWL Axioms.");
		//actionlistener = new IntegrateWithOntologyAction();
		//integrateWithOntologybtn.addActionListener(actionlistener);
		topPnl.add(new DaseRuleEditorPanel(ruleEngineModel,this.activeOntology,dialogManager, tabbedPane),BorderLayout.CENTER);
		//topPnl.add(integrateWithOntologybtn,BorderLayout.PAGE_END);
		// this.ruleExecutionView = new SWRLRuleExecutionView(ruleEngineModel);
	}

	@Override
	public void initialize() {
		this.ruleTablesView.initialize();
		// this.ruleExecutionView.initialize();

		setOrientation(JSplitPane.VERTICAL_SPLIT);
		setResizeWeight(SPLIT_PANE_RESIZE_WEIGHT);
		setTopComponent(this.topPnl);
		setBottomComponent(this.ruleTablesView);
	}

	@Override
	public void update() {
		this.ruleTablesView.update();
		// this.ruleExecutionView.update();
	}
}
