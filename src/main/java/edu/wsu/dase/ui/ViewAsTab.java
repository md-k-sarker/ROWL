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

import edu.wsu.dase.view.SWRLRulesViewExtended;

public class ViewAsTab extends OWLWorkspaceViewsTab {

	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(ViewAsTab.class);

	private SWRLRuleEngineModel swrlRuleEngineModel;
	private SWRLRulesViewExtended rulesView;
	private SWRLRulesView swrlRulesView;

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
			//retrieveOWLAnnot();
			//createOWLAnnoT();
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
			//System.out.println("in update Rule to OWL");
			// Get the active OWL ontology
			OWLOntology activeOntology = getOWLModelManager().getActiveOntology();

			if (activeOntology != null) {
				// first initilize the tab
				this.tabbedPane = new JTabbedPane();

				// Create an IRI resolver using Protege's entity finder and
				// entity renderer
				IRIResolver iriResolver = new ProtegeIRIResolver(getOWLModelManager().getOWLEntityFinder(),
						getOWLModelManager().getOWLEntityRenderer());

				// Create a rule engine
				SWRLRuleEngine ruleEngine = SWRLAPIFactory.createSWRLRuleEngine(activeOntology, iriResolver);

				// Create a rule engine model. This is the core plugin model.
				this.swrlRuleEngineModel = SWRLAPIFactory.createSWRLRuleEngineModel(ruleEngine);

				// Create the rule engine dialog manager
				SWRLRuleEngineDialogManager dialogManager = SWRLAPIFactory
						.createSWRLRuleEngineDialogManager(swrlRuleEngineModel);

				// Create the custom tab View
				this.rulesView = new SWRLRulesViewExtended(swrlRuleEngineModel, dialogManager, activeOntology,
						tabbedPane);
				this.rulesView.initialize();

				// Create the existing SWRL tab View
				this.swrlRulesView = new SWRLRulesView(swrlRuleEngineModel, dialogManager);
				this.swrlRulesView.initialize();

				// create tab view
				if (this.tabbedPane != null) {
					remove(this.tabbedPane);
				}

				this.tabbedPane.addTab("Rule to OWL", this.rulesView);
				this.tabbedPane.addTab("SWRL", this.swrlRulesView);

				add(this.tabbedPane, java.awt.BorderLayout.CENTER);
				this.swrlRuleEngineModel.registerOntologyListener();

			} else {
				// log.warn("SWRLTab update failed - no active OWL ontology");
				System.out.println("SWRLTab update failed - no active OWL ontology");
			}
		} catch (RuntimeException e) {
			// log.error("Error updating SWRLTab", e);
			System.out.println("Error updating SWRLTab" + e);
		}
		this.updating = false;
		retrieveOWLAnnot();
	}

	public void createOWLAnnoT() {
		OWLOntology activeO = getOWLModelManager().getActiveOntology();
		OWLDataFactory df = getOWLModelManager().getOWLDataFactory();
		OWLAnnotationProperty p = df.getOWLAnnotationProperty(IRI.create("prefixP", "suffixP"));
		OWLAnnotationValue v ;
		
		OWLClass c = df.getOWLClass("owlliteral", new DefaultPrefixManager());
		//OWLAnnotation annot = df.getOWLAnnotation(p, "hellow world");
		//df.getOWLAnnotationAssertionAxiom(i, annot);
	}

	public void retrieveOWLAnnot() {
		OWLOntology activeO = getOWLModelManager().getActiveOntology();
		for (OWLAnnotation annot : activeO.getAnnotations()) {
			System.out.println(annot.getValue());
		}
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
