package edu.wsu.dase.ui;

import java.awt.BorderLayout;

import javax.swing.JTabbedPane;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swrlapi.core.IRIResolver;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrlapi.ui.dialog.SWRLRuleEngineDialogManager;
import org.swrlapi.ui.model.SWRLRuleEngineModel;
import org.swrlapi.ui.view.rules.SWRLRulesView;
import org.swrltab.ui.ProtegeIRIResolver;

import edu.wsu.dase.controller.Engine;
import edu.wsu.dase.model.RuleTableModel;
import edu.wsu.dase.view.RulesViewMain;

public class ViewAsTab extends OWLWorkspaceViewsTab {

	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(ViewAsTab.class);

	private SWRLRuleEngineModel swrlRuleEngineModel;
	private Engine engine;
	private RulesViewMain rulesView;
	private SWRLRuleEngineDialogManager dialogManager;
	private SWRLRulesView swrlRulesView;
	private IRIResolver iriResolver;
	private OWLOntology activeOntology;

	private JTabbedPane tabbedPane;

	private final ViewAsTabListener listener = new ViewAsTabListener();

	private boolean updating = false;

	@Override
	public void initialise() {
		super.initialise();

		setToolTipText("ROWL");

		if (getOWLModelManager() != null) {
			getOWLModelManager().addListener(this.listener);

			setLayout(new BorderLayout());

			if (getOWLModelManager().getActiveOntology() != null)
				update();
		} else
			log.warn("SWRLTab initialization failed - no model manager");
	}

	@Override
	public void dispose() {
		super.dispose();
		getOWLModelManager().removeListener(this.listener);
		this.swrlRuleEngineModel.unregisterOntologyListener();
	}

	private void update() {
		this.updating = true;
		try {

			this.activeOntology = getOWLModelManager().getActiveOntology();

			if (this.activeOntology != null) {
				// first initilize the tabbedPane
				this.tabbedPane = new JTabbedPane();

				// Create an IRI resolver using Protege's entity finder and
				// entity renderer
				this.iriResolver = new ProtegeIRIResolver(getOWLModelManager().getOWLEntityFinder(),
						getOWLModelManager().getOWLEntityRenderer());

				updateSWRLTab();

				updateROWLTab();

				// remove tab view if existing
				if (this.tabbedPane != null) {
					remove(this.tabbedPane);
				}

				this.tabbedPane.addTab("ROWL", this.rulesView);
				this.tabbedPane.addTab("SWRL", this.swrlRulesView);

				add(this.tabbedPane, java.awt.BorderLayout.CENTER);

			} else {
				log.warn("SWRLTab update failed - no active OWL ontology");
				System.out.println("ROWL plugin update failed - no active OWL ontology");
			}
		} catch (RuntimeException e) {
			log.error("Error updating SWRLTab", e);
			System.out.println("Error updating ROWL" + e);
			e.printStackTrace();
		}
		this.updating = false;
	}

	private void updateSWRLTab() {
		// Create a rule engine
		SWRLRuleEngine ruleEngine = SWRLAPIFactory.createSWRLRuleEngine(this.activeOntology, this.iriResolver);

		// Create a rule engine model. This is the core plugin model.
		this.swrlRuleEngineModel = SWRLAPIFactory.createSWRLRuleEngineModel(ruleEngine);

		// Create the rule engine dialog manager
		this.dialogManager = SWRLAPIFactory.createSWRLRuleEngineDialogManager(this.swrlRuleEngineModel);

		// Create the existing SWRL tab View
		this.swrlRulesView = new SWRLRulesView(this.swrlRuleEngineModel, this.dialogManager);
		this.swrlRulesView.initialize();

		this.swrlRuleEngineModel.registerOntologyListener();
	}

	private void updateROWLTab() {
		// Create the custom tab View
		this.engine = new Engine(this.activeOntology);

		this.rulesView = new RulesViewMain(this.swrlRuleEngineModel, this.engine, this.dialogManager,
				this.activeOntology, tabbedPane);
		this.rulesView.initialize();
	}

	public class ViewAsTabListener implements OWLModelManagerListener {
		@Override
		public void handleChange(@NonNull OWLModelManagerChangeEvent event) {
			if (!ViewAsTab.this.updating) {
				if (event.getType() == EventType.ACTIVE_ONTOLOGY_CHANGED) {
					update();
				}
			} else
				log.warn("SWRLTab ignoring ontology change - still processing old change");
		}
	}

}
