package edu.wright.dase.model;

import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.swrlapi.ui.model.SWRLRuleEngineModel;

import edu.wright.dase.controller.Engine;

public class Constants {

	public final static String FIXED_ANNOTATION_NAME = "DASE_RULE";
	public final static String FRESH_PROP_NAME = "freshObjectProperty";
	public final static String ANONYMOUS_DEFUALT_ID = "http://www.co-ode.org/ontologies/ont.owl#";
	
	public static SWRLRuleEngineModel swrlRuleEngineModelAsStaticReference;

	public static Engine engineAsStaticReference;

	public static OWLOntology activeOntologyAsStaticReference;
	public static OWLDataFactory owlDataFactoryAsStaticReference;
	public static OWLOntologyManager owlOntologyManagerAsStaticReference;

}
