package edu.wright.dase.ui;

import java.awt.BorderLayout;

import javax.swing.JTabbedPane;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.ui.OWLWorkspaceViewsTab;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swrlapi.core.IRIResolver;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrlapi.ui.dialog.SWRLRuleEngineDialogManager;
import org.swrlapi.ui.model.SWRLRuleEngineModel;
import org.swrlapi.ui.view.rules.SWRLRulesView;
import org.swrltab.ui.ProtegeIRIResolver;

import edu.wright.dase.controller.Engine;
import edu.wright.dase.model.Constants;
import edu.wright.dase.view.RulesViewMain;

public class ViewAsTab extends OWLWorkspaceViewsTab {

	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(ViewAsTab.class);

	private SWRLRuleEngineModel swrlRuleEngineModel;
	private RulesViewMain rulesView;
	private SWRLRuleEngineDialogManager dialogManager;
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
		} else
			log.warn("SWRLTab initialization failed - no model manager");
	}

	@Override
	public void dispose() {
		// super.dispose();
		getOWLModelManager().removeListener(this.listener);
		this.swrlRuleEngineModel.unregisterOntologyListener();

	}

	private void update() {
		this.updating = true;
		try {

			ProtegeIRIResolver iriResolver;
			OWLOntology activeOntology;

			activeOntology = getOWLModelManager().getActiveOntology();
			// System.out.println("inside ViewAsTab--ontology id:" +
			// this.activeOntology.getOntologyID().toString());

			if (activeOntology != null) {

				// Create an IRI resolver using Protege's entity finder and
				// entity renderer
				iriResolver = new ProtegeIRIResolver(getOWLModelManager().getOWLEntityFinder(),
						getOWLModelManager().getOWLEntityRenderer());

				// save the references
				// saveTheReferences(activeOntology);

				updateSWRLTab(activeOntology, iriResolver);

				updateROWLTab(activeOntology, iriResolver);

				// remove tab view if existing
				if (this.tabbedPane != null) {
					remove(this.tabbedPane);
				}

				//initilize the tabbedPane
				this.tabbedPane = new JTabbedPane();

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

	private void updateSWRLTab(OWLOntology activeOntology, IRIResolver iriResolver) {
		// Create a rule engine
		SWRLRuleEngine ruleEngine = SWRLAPIFactory.createSWRLRuleEngine(activeOntology, iriResolver);

		// Create a rule engine model. This is the core plugin model.
		this.swrlRuleEngineModel = SWRLAPIFactory.createSWRLRuleEngineModel(ruleEngine);
		// save the reference
		Constants.swrlRuleEngineModelAsStaticReference = this.swrlRuleEngineModel;

		// Create the rule engine dialog manager
		this.dialogManager = SWRLAPIFactory.createSWRLRuleEngineDialogManager(this.swrlRuleEngineModel);

		if (this.swrlRulesView != null)
			remove(this.swrlRulesView);

		// Create the existing SWRL tab View
		this.swrlRulesView = new SWRLRulesView(this.swrlRuleEngineModel, this.dialogManager);
		this.swrlRulesView.initialize();

		this.swrlRuleEngineModel.registerOntologyListener();
	}

	private void updateROWLTab(OWLOntology activeOntology, ProtegeIRIResolver iriResolver) {
		// Create the custom tab View
		Engine engine = new Engine(activeOntology, iriResolver);
		// Save the reference
		// Constants.engineAsStaticReference = engine;

		if (this.rulesView != null) {
			System.out.println("removing rulesView");
			remove(this.rulesView);
		}

		this.rulesView = new RulesViewMain(this.dialogManager, tabbedPane);
		this.rulesView.initialize();
	}

	private void saveTheReferences(OWLOntology activeOntology) {
		// Constants.activeOntologyAsStaticReference = activeOntology;
		// Constants.owlOntologyManagerAsStaticReference =
		// activeOntology.getOWLOntologyManager();
		// Constants.owlDataFactoryAsStaticReference =
		// activeOntology.getOWLOntologyManager().getOWLDataFactory();

	}

	public class ViewAsTabListener implements OWLModelManagerListener {

		@Override
		public void handleChange(@NonNull OWLModelManagerChangeEvent event) {
			if (!ViewAsTab.this.updating) {
				if ((event.getType() == EventType.ACTIVE_ONTOLOGY_CHANGED) || (event.isType(EventType.ONTOLOGY_LOADED))
						|| (event.isType(EventType.ONTOLOGY_RELOADED))) {

					// System.out.println("Ontology changed: " +
					// event.getType().name());

					update();
				}
			} else {
				// System.out.println("ROWLLTab ignoring ontology change - still
				// processing old change");
				log.warn("ROWLTab ignoring ontology change - still processing old change");
			}
		}
	}

}
