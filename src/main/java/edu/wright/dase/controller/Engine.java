package edu.wright.dase.controller;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import org.protege.editor.owl.ui.prefix.PrefixUtilities;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.swrlapi.core.IRIResolver;

import edu.wright.dase.model.Constants;
import edu.wright.dase.model.RuleModel;
import edu.wright.dase.model.RuleTableModel;
import edu.wright.dase.view.RuleEditorPanel;
import edu.wright.dase.view.RuleTablePanel;

public class Engine {

	private OWLOntology activeOntology;
	private TreeMap<String, Set<OWLAxiom>> axiomsWithID;
	private TreeMap<String, RuleModel> rulesWithID;
	private PrefixManager prefixManager;
	private OWLAnnotationProperty fixedAnnotationProperty;

	private RuleTableModel ruleTableModel;

	private RuleTablePanel ruleTablePanel;

	private RuleEditorPanel ruleEditorPanel;

	private IRIResolver iriResolver;
	
	private String defaultPrefix;

	/**
	 * @return the defaultPrefix
	 */
	public String getDefaultPrefix() {
		return defaultPrefix;
	}

	/**
	 * @param defaultPrefix the defaultPrefix to set
	 */
	public void setDefaultPrefix(String defaultPrefix) {
		this.defaultPrefix = defaultPrefix;
	}

	/**
	 * @return the prefixManager
	 */
	public PrefixManager getPrefixManager() {
		return prefixManager;
	}

	/**
	 * @param prefixManager
	 *            the prefixManager to set
	 */
	public void setPrefixManager(PrefixManager prefixManager) {
		this.prefixManager = prefixManager;
	}

	private OWLOntologyManager owlOntologyManager;

	/**
	 * @return the activeOntology
	 */
	public OWLOntology getActiveOntology() {
		return activeOntology;
	}

	/**
	 * @param activeOntology
	 *            the activeOntology to set
	 */
	public void setActiveOntology(OWLOntology activeOntology) {
		this.activeOntology = activeOntology;
	}

	/**
	 * @return the owlOntologyManager
	 */
	public OWLOntologyManager getOwlOntologyManager() {
		return owlOntologyManager;
	}

	/**
	 * @param owlOntologyManager
	 *            the owlOntologyManager to set
	 */
	public void setOwlOntologyManager(OWLOntologyManager owlOntologyManager) {
		this.owlOntologyManager = owlOntologyManager;
	}

	private OWLDataFactory owlDataFactory;

	/**
	 * @return the owlDataFactory
	 */
	public OWLDataFactory getOwlDataFactory() {
		return owlDataFactory;
	}

	/**
	 * @param owlDataFactory
	 *            the owlDataFactory to set
	 */
	public void setOwlDataFactory(OWLDataFactory owlDataFactory) {
		this.owlDataFactory = owlDataFactory;
	}

	/**
	 * @return the iriResolver
	 */
	public IRIResolver getIriResolver() {
		return iriResolver;
	}

	/**
	 * @param iriResolver
	 *            the iriResolver to set
	 */
	public void setIriResolver(IRIResolver iriResolver) {
		this.iriResolver = iriResolver;
	}

	/**
	 * @return the ruleEditorPanel
	 */
	public RuleEditorPanel getRuleEditorPanel() {
		return ruleEditorPanel;
	}

	/**
	 * @param ruleEditorPanel
	 *            the ruleEditorPanel to set
	 */
	public void setRuleEditorPanel(RuleEditorPanel ruleEditorPanel) {
		this.ruleEditorPanel = ruleEditorPanel;
	}

	/**
	 * @return the ruleTablePanel
	 */
	public RuleTablePanel getRuleTablePanel() {
		return ruleTablePanel;
	}

	/**
	 * @param ruleTablePanel
	 *            the ruleTablePanel to set
	 */
	public void setRuleTablePanel(RuleTablePanel ruleTablePanel) {
		this.ruleTablePanel = ruleTablePanel;
	}

	/**
	 * @return the ruleTableModel
	 */
	public RuleTableModel getRuleTableModel() {
		return ruleTableModel;
	}

	/**
	 * @param ruleTableModel
	 *            the ruleTableModel to set
	 */
	public void setRuleTableModel(RuleTableModel ruleTableModel) {
		this.ruleTableModel = ruleTableModel;
	}

	public Engine(OWLOntology activeOntology, IRIResolver iriResolver) {
		this.activeOntology = activeOntology;
		this.owlOntologyManager = this.activeOntology.getOWLOntologyManager();
		this.owlDataFactory = this.owlOntologyManager.getOWLDataFactory();
		this.iriResolver = iriResolver;

		this.prefixManager = PrefixUtilities.getPrefixOWLOntologyFormat(activeOntology);
		
		if(! addPrefix()){
			defaultPrefix = "";
		}
		
		fixedAnnotationProperty = activeOntology.getOWLOntologyManager().getOWLDataFactory()
				.getOWLAnnotationProperty(Constants.FIXED_ANNOTATION_NAME, prefixManager);

		initializeDataStructure();

		reloadRulesAndAxiomsFromOntology();

	}


	public boolean addPrefix() {
		try {
			
			OWLOntologyID ontoID = activeOntology.getOntologyID();

			if (ontoID == null) {
				return false;
			}
			// ontoID can contain anonymous.
			// need more checking

			com.google.common.base.Optional<IRI> iri = ontoID.getDefaultDocumentIRI();
			if (!iri.isPresent()) {
				return false;
			}

			String uriString = iri.get().toString();
			if (uriString == null) {
				return false;
			}
			String prefix;
			if (uriString.endsWith("/")) {
				String sub = uriString.substring(0, uriString.length() - 1);
				prefix = sub.substring(sub.lastIndexOf("/") + 1, sub.length());
			} else {
				prefix = uriString.substring(uriString.lastIndexOf('/') + 1, uriString.length());
			}
			if (prefix.endsWith(".owl")) {
				prefix = prefix.substring(0, prefix.length() - 4);
			}
			prefix = prefix.toLowerCase();
			if (!uriString.endsWith("#") && !uriString.endsWith("/")) {
				uriString = uriString + "#";
			}

			if (prefix.length() < 1) {

				return false;
			}
			prefixManager.setPrefix(prefix, uriString);
			defaultPrefix = prefix + ":";
			return true;
		} catch (IllegalStateException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	
	private void initializeDataStructure() {
		rulesWithID = new TreeMap<String, RuleModel>();
		axiomsWithID = new TreeMap<String, Set<OWLAxiom>>();
	}

	public TreeMap<String, RuleModel> getRules() {

		return this.rulesWithID;
	}

	public RuleModel getRulebyID(String ruleName) {
		if (rulesWithID.containsKey(ruleName)) {
			return rulesWithID.get(ruleName);
		}
		return null;
	}

	public Set<OWLAxiom> getAxiomsbyID(String ruleName) {

		if (axiomsWithID.containsKey(ruleName)) {
			return axiomsWithID.get(ruleName);
		}
		return null;
	}

	public void addARulesWithID(String ruleName, RuleModel rule) {
		rulesWithID.put(ruleName, rule);
	}

	public void addAxiomsWithID(String ruleName, Set<OWLAxiom> axiomSet) {
		axiomsWithID.put(ruleName, axiomSet);
	}

	/**
	 * Delete Rule from the table.
	 * 
	 * 
	 * has to be sure from Adila.
	 * 
	 * Possible option 1. also remove corresponding axioms from ontology or 2.
	 * only remove corresponding annotations from those axioms
	 * 
	 * Current implementation remove corresponding axioms from ontology
	 * 
	 * @param ruleName
	 */
	public void deleteRule(String ruleName) {
		if (rulesWithID.containsKey(ruleName)) {
			rulesWithID.remove(ruleName);
		}

		if (axiomsWithID.containsKey(ruleName)) {
			owlOntologyManager.removeAxioms(activeOntology, axiomsWithID.get(ruleName));
			axiomsWithID.remove(ruleName);
		}
	}

	public void OntologyChanged() {
		// reloadRulesAndAxiomsFromOntology();
	}

	public boolean checkDuplicateRuleName(String RuleName) {
		if (rulesWithID.containsKey(RuleName))
			return true;
		else
			return false;
	}

	public boolean checkDuplicateRuleText(String RuleText) {
		if (rulesWithID.containsValue(RuleText))
			return true;
		else
			return false;
	}

	public String getAutogeneratedNextRuleName() {
		int size = rulesWithID.size() + 1;
		return "R" + size;
	}

	private void reloadRulesAndAxiomsFromOntology() {

		// JPopupMenu
		if (rulesWithID != null)
			rulesWithID.clear();
		if (axiomsWithID != null)
			axiomsWithID.clear();

		Set<OWLAxiom> tmpAxioms = new HashSet<OWLAxiom>();
		String tmpRuleID = "";
		int i = 0;

		/**
		 * when converting rule to owl, single rule can generate multiple
		 * axioms. That means multiple axioms need to be binded for a single
		 * rule-id
		 */
		for (OWLAxiom ax : activeOntology.getAxioms()) {
			for (OWLAnnotation ann : ax.getAnnotations()) {
				for (OWLAnnotationProperty anp : ann.getAnnotationPropertiesInSignature()) {
					if (anp.equals(fixedAnnotationProperty)) {
						// System.out.println("\n\naxiom before parse: " +
						// ax.toString() + "\n\n");
						String val = ann.getValue().asLiteral().get().getLiteral();
						String[] values = val.split("___", 3);

						if (values.length == 3) {
							String ruleID = values[0];
							String ruleText = values[1];
							String ruleComment = values[2];
							if (ruleID.length() > 0 && ruleText.length() > 0) {

								// add to rulewith ID
								rulesWithID.put(ruleID, new RuleModel(ruleID, ruleText, ruleComment));

								// System.out.println("axiomsWithID length
								// before: " + axiomsWithID.size());
								// add to axioms with ID
								// System.out.println("equal or not: " +
								// tmpRuleID + " " + ruleID);

								if (axiomsWithID.containsKey(ruleID)) {
									axiomsWithID.get(ruleID).add(ax);
								} else {
									tmpAxioms = new HashSet<OWLAxiom>();
									tmpAxioms.add(ax);
									axiomsWithID.put(ruleID, tmpAxioms);
								}

							}
						} else {
							System.out.println("Cannot retrieve annotation. Annotation doesn't have 3 parts");
						}
					}
				}
			}
		}
	}

}