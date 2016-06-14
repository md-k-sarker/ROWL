package org.swrltab.util.parser;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLBuiltInAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLDifferentIndividualsAtom;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLSameIndividualAtom;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.semanticweb.owlapi.vocab.XSDVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swrlapi.builtins.arguments.SWRLAnnotationPropertyBuiltInArgument;
import org.swrlapi.builtins.arguments.SWRLClassBuiltInArgument;
import org.swrlapi.builtins.arguments.SWRLDataPropertyBuiltInArgument;
import org.swrlapi.builtins.arguments.SWRLDatatypeBuiltInArgument;
import org.swrlapi.builtins.arguments.SWRLNamedIndividualBuiltInArgument;
import org.swrlapi.builtins.arguments.SWRLObjectPropertyBuiltInArgument;
import org.swrlapi.core.SWRLAPIOWLOntology;
import org.swrlapi.factory.OWLLiteralFactory;
import org.swrlapi.factory.SWRLAPIOWLDataFactory;
import org.swrlapi.factory.SWRLBuiltInArgumentFactory;

/**
 * Provides support methods used by the {@link org.swrlapi.parser.SWRLParser}.
 *
 * @see org.swrlapi.parser.SWRLParser
 */
class SWRLParserSupport {
	private static final Logger log = LoggerFactory.getLogger(SWRLParserSupport.class);

	@NonNull
	private final SWRLAPIOWLOntology swrlapiOWLOntology;

	public SWRLParserSupport(@NonNull SWRLAPIOWLOntology swrlapiOWLOntology) {
		this.swrlapiOWLOntology = swrlapiOWLOntology;
	}

	public boolean isOWLEntity(@NonNull String shortName) {
		return isOWLClass(shortName) || isOWLNamedIndividual(shortName) || isOWLObjectProperty(shortName)
				|| isOWLDataProperty(shortName) || isOWLAnnotationProperty(shortName) || isOWLDatatype(shortName);
	}

	public boolean isOWLClass(@NonNull String shortName) {
		IRI classIRI = prefixedName2IRI(shortName);

		return getOWLOntology().containsClassInSignature(classIRI, Imports.INCLUDED)
				|| classIRI.equals(OWLRDFVocabulary.OWL_THING.getIRI())
				|| classIRI.equals(OWLRDFVocabulary.OWL_NOTHING.getIRI());
	}

	public boolean isOWLNamedIndividual(@NonNull String shortName) {
		IRI individualIRI = prefixedName2IRI(shortName);
		return getOWLOntology().containsIndividualInSignature(individualIRI, Imports.INCLUDED);
	}

	public boolean isOWLObjectProperty(@NonNull String shortName) {
		IRI propertyIRI = prefixedName2IRI(shortName);
		return getOWLOntology().containsObjectPropertyInSignature(propertyIRI, Imports.INCLUDED);
	}

	public boolean isOWLDataProperty(@NonNull String shortName) {
		IRI propertyIRI = prefixedName2IRI(shortName);
		return getOWLOntology().containsDataPropertyInSignature(propertyIRI, Imports.INCLUDED);
	}

	public boolean isOWLAnnotationProperty(@NonNull String shortName) {
		IRI propertyIRI = prefixedName2IRI(shortName);
		return getOWLOntology().containsAnnotationPropertyInSignature(propertyIRI, Imports.INCLUDED);
	}

	public boolean isOWLDatatype(@NonNull String shortName) {
		try {
			XSDVocabulary.parseShortName(shortName);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	public boolean isSWRLBuiltIn(@NonNull String shortName) {
		IRI builtInIRI = prefixedName2IRI(shortName);
		return getSWRLAPIOWLOntology().isSWRLBuiltIn(builtInIRI);
	}

	private boolean isValidSWRLVariableName(@NonNull String candidateVariableName) {
		if (candidateVariableName.length() == 0)
			return false;

		if (!Character.isJavaIdentifierStart(candidateVariableName.charAt(0)))
			return false;

		for (int i = 1; i < candidateVariableName.length(); i++) {
			char c = candidateVariableName.charAt(i);
			if (!(Character.isJavaIdentifierPart(c) || c == ':' || c == '-')) {
				return false;
			}
		}
		return true;
	}

	public void checkThatSWRLVariableNameIsValid(@NonNull String variableName) throws SWRLParseException {
		if (!isValidSWRLVariableName(variableName))
			throw new SWRLParseException("Invalid SWRL variable name " + variableName);

		if (isOWLEntity(variableName))
			throw new SWRLParseException("Invalid SWRL variable name " + variableName
					+ " - cannot use name of existing OWL class, individual, property, or datatype");
	}

	@NonNull
	public SWRLLiteralArgument createXSDStringSWRLLiteralArgument(@NonNull String lexicalValue) {
		OWLLiteral owlLiteral = getOWLLiteralFactory().getOWLLiteral(lexicalValue);
		return getOWLDataFactory().getSWRLLiteralArgument(owlLiteral);
	}

	@NonNull
	public SWRLLiteralArgument createXSDBooleanSWRLLiteralArgument(@NonNull String lexicalValue)
			throws SWRLParseException {
		try {
			Boolean value = Boolean.parseBoolean(lexicalValue);
			OWLLiteral owlLiteral = getOWLLiteralFactory().getOWLLiteral(value);
			return getOWLDataFactory().getSWRLLiteralArgument(owlLiteral);
		} catch (NumberFormatException e) {
			throw new SWRLParseException(lexicalValue + " is not a valid xsd:boolean");
		}
	}

	@NonNull
	public SWRLLiteralArgument createXSDIntSWRLLiteralArgument(@NonNull String lexicalValue) throws SWRLParseException {
		try {
			int value = Integer.parseInt(lexicalValue);
			OWLLiteral owlLiteral = getOWLLiteralFactory().getOWLLiteral(value);
			return getOWLDataFactory().getSWRLLiteralArgument(owlLiteral);
		} catch (NumberFormatException e) {
			throw new SWRLParseException(lexicalValue + " is not a valid xsd:int");
		}
	}

	@NonNull
	public SWRLLiteralArgument createXSDFloatSWRLLiteralArgument(@NonNull String lexicalValue)
			throws SWRLParseException {
		try {
			float value = Float.parseFloat(lexicalValue);
			OWLLiteral owlLiteral = getOWLLiteralFactory().getOWLLiteral(value);
			return getOWLDataFactory().getSWRLLiteralArgument(owlLiteral);
		} catch (NumberFormatException e) {
			throw new SWRLParseException(lexicalValue + " is not a valid xsd:float");
		}
	}

	@NonNull
	public SWRLVariable createSWRLVariable(@NonNull String variableName) throws SWRLParseException {
		System.out.println("sarker.3 createSWRLVariable before- variableName: " + variableName);
		IRI iri = prefixedName2IRI(variableName);
		System.out.println("sarker.3 createSWRLVariable after- iri: " + getOWLDataFactory().getSWRLVariable(iri));
		if (isOWLEntity(variableName))
			throw new SWRLParseException(variableName
					+ " cannot be used as a SWRL variable name because it refers to an existing OWL entity");
		return getOWLDataFactory().getSWRLVariable(iri);
	}

	@NonNull
	public SWRLLiteralArgument createSWRLLiteralArgument(@NonNull String lexicalValue,
			@NonNull String datatypeShortName) throws SWRLParseException {
		OWLDatatype owlDatatype = createOWLDatatype(datatypeShortName);

		try {
			OWLLiteral literal = getOWLLiteralFactory().getOWLLiteral(lexicalValue, owlDatatype);
			return getOWLDataFactory().getSWRLLiteralArgument(literal);
		} catch (RuntimeException e) {
			throw new SWRLParseException(e.getMessage() == null ? "" : e.getMessage());
		}
	}

	@NonNull
	public SWRLRule createSWRLRule(@NonNull String ruleName, @NonNull Set<@NonNull SWRLAtom> head,
			@NonNull Set<@NonNull SWRLAtom> body, @NonNull String comment, boolean isEnabled) {

		System.out.println("inside SWRL Parser 0. rule, body: " + body + " head:" + head);
		Set<@NonNull OWLAnnotation> annotations = this.swrlapiOWLOntology.generateRuleAnnotations(ruleName, comment,
				isEnabled);
		// SWRLRule tmprule = getOWLDataFactory().getSWRLRule(body, head,
		// annotations);
		return getOWLDataFactory().getSWRLRule(body, head, annotations);
	}

	@NonNull
	public Set<@NonNull SWRLAtom> createSWRLBodyAtomList() {
		return new LinkedHashSet<>();
	}

	@NonNull
	public Set<@NonNull SWRLAtom> createSWRLHeadAtomList() {
		return new LinkedHashSet<>();
	}

	@NonNull
	public SWRLClassAtom createSWRLClassAtom(@NonNull String classShortName, @NonNull SWRLIArgument iArgument)
			throws SWRLParseException {
		OWLClass cls = createOWLClass(classShortName);

		return getOWLDataFactory().getSWRLClassAtom(cls, iArgument);
	}

	@NonNull
	public SWRLObjectPropertyAtom createSWRLObjectPropertyAtom(@NonNull String objectPropertyShortName,
			@NonNull SWRLIArgument iArgument1, @NonNull SWRLIArgument iArgument2) throws SWRLParseException {
		OWLObjectProperty objectProperty = createOWLObjectProperty(objectPropertyShortName);

		return getOWLDataFactory().getSWRLObjectPropertyAtom(objectProperty, iArgument1, iArgument2);
	}

	@NonNull
	public SWRLDataPropertyAtom createSWRLDataPropertyAtom(@NonNull String dataPropertyShortName,
			@NonNull SWRLIArgument iArgument, @NonNull SWRLDArgument dArgument) throws SWRLParseException {
		OWLDataProperty dataProperty = createOWLDataProperty(dataPropertyShortName);

		return getOWLDataFactory().getSWRLDataPropertyAtom(dataProperty, iArgument, dArgument);
	}

	@NonNull
	public SWRLBuiltInAtom createSWRLBuiltInAtom(@NonNull String builtInPrefixedName,
			@NonNull List<@NonNull SWRLDArgument> arguments) throws SWRLParseException {
		IRI builtInIRI = createSWRLBuiltInIRI(builtInPrefixedName);
		return getOWLDataFactory().getSWRLBuiltInAtom(builtInIRI, arguments);
	}

	@NonNull
	public SWRLSameIndividualAtom createSWRLSameIndividualAtom(@NonNull SWRLIArgument iArgument1,
			@NonNull SWRLIArgument iArgument2) {
		return getOWLDataFactory().getSWRLSameIndividualAtom(iArgument1, iArgument2);
	}

	@NonNull
	public SWRLDifferentIndividualsAtom createSWRLDifferentIndividualsAtom(@NonNull SWRLIArgument iArgument1,
			@NonNull SWRLIArgument iArgument2) {
		return getOWLDataFactory().getSWRLDifferentIndividualsAtom(iArgument1, iArgument2);
	}

	@NonNull
	public SWRLIndividualArgument createSWRLIndividualArgument(@NonNull String individualShortName)
			throws SWRLParseException {
		OWLNamedIndividual individual = createOWLNamedIndividual(individualShortName);
		return getOWLDataFactory().getSWRLIndividualArgument(individual);
	}

	@NonNull
	public SWRLClassBuiltInArgument createSWRLClassBuiltInArgument(@NonNull String classShortName)
			throws SWRLParseException {
		OWLClass cls = createOWLClass(classShortName);
		return getSWRLBuiltInArgumentFactory().getClassBuiltInArgument(cls);
	}

	@NonNull
	public SWRLNamedIndividualBuiltInArgument createSWRLNamedIndividualBuiltInArgument(
			@NonNull String individualShortName) throws SWRLParseException {
		OWLNamedIndividual individual = createOWLNamedIndividual(individualShortName);
		return getSWRLBuiltInArgumentFactory().getNamedIndividualBuiltInArgument(individual);
	}

	@NonNull
	public SWRLObjectPropertyBuiltInArgument createSWRLObjectPropertyBuiltInArgument(@NonNull String propertyShortName)
			throws SWRLParseException {
		OWLObjectProperty property = createOWLObjectProperty(propertyShortName);
		return getSWRLBuiltInArgumentFactory().getObjectPropertyBuiltInArgument(property);
	}

	@NonNull
	public SWRLDataPropertyBuiltInArgument createSWRLDataPropertyBuiltInArgument(@NonNull String propertyShortName)
			throws SWRLParseException {
		OWLDataProperty property = createOWLDataProperty(propertyShortName);
		return getSWRLBuiltInArgumentFactory().getDataPropertyBuiltInArgument(property);
	}

	@NonNull
	public SWRLAnnotationPropertyBuiltInArgument createSWRLAnnotationPropertyBuiltInArgument(
			@NonNull String propertyShortName) throws SWRLParseException {
		OWLAnnotationProperty property = createOWLAnnotationProperty(propertyShortName);
		return getSWRLBuiltInArgumentFactory().getAnnotationPropertyBuiltInArgument(property);
	}

	@NonNull
	public SWRLDatatypeBuiltInArgument createSWRLDatatypeBuiltInArgument(@NonNull String datatypeShortName)
			throws SWRLParseException {
		OWLDatatype datatype = createOWLDatatype(datatypeShortName);
		return getSWRLBuiltInArgumentFactory().getDatatypeBuiltInArgument(datatype);
	}

	@NonNull
	public String getShortNameFromIRI(@NonNull String iriString, boolean interactiveParseOnly)
			throws SWRLParseException {
		try {
			IRI iri = prefixedName2IRI(iriString);

			return iri2ShortForm(iri);
		} catch (RuntimeException e) {
			if (interactiveParseOnly)
				throw new SWRLIncompleteRuleException("IRI " + iriString + " does not refer to a valid OWL entity");
			else
				throw new SWRLParseException("IRI " + iriString + " does not refer to a valid OWL entity");
		}
	}

	@NonNull
	private OWLClass createOWLClass(@NonNull String classShortName) throws SWRLParseException {
		if (isOWLClass(classShortName)) {
			IRI classIRI = prefixedName2IRI(classShortName);
			return getOWLDataFactory().getOWLClass(classIRI);
		} else
			throw new SWRLParseException(classShortName + " is not an OWL class");
	}

	@NonNull
	private OWLNamedIndividual createOWLNamedIndividual(@NonNull String individualShortName) throws SWRLParseException {
		if (isOWLNamedIndividual(individualShortName)) {
			IRI individualIRI = prefixedName2IRI(individualShortName);
			return getOWLDataFactory().getOWLNamedIndividual(individualIRI);
		} else
			throw new SWRLParseException(individualShortName + " is not an OWL named individual");
	}

	@NonNull
	private OWLObjectProperty createOWLObjectProperty(@NonNull String objectPropertyShortName)
			throws SWRLParseException {
		if (isOWLObjectProperty(objectPropertyShortName)) {
			IRI propertyIRI = prefixedName2IRI(objectPropertyShortName);
			return getOWLDataFactory().getOWLObjectProperty(propertyIRI);
		} else
			throw new SWRLParseException(objectPropertyShortName + " is not an OWL object property");
	}

	@NonNull
	private OWLDataProperty createOWLDataProperty(@NonNull String dataPropertyShortName) throws SWRLParseException {
		if (isOWLDataProperty(dataPropertyShortName)) {
			IRI propertyIRI = prefixedName2IRI(dataPropertyShortName);
			return getOWLDataFactory().getOWLDataProperty(propertyIRI);
		} else
			throw new SWRLParseException(dataPropertyShortName + " is not an OWL data property");
	}

	@NonNull
	private OWLAnnotationProperty createOWLAnnotationProperty(@NonNull String annotationPropertyShortName)
			throws SWRLParseException {
		if (isOWLAnnotationProperty(annotationPropertyShortName)) {
			IRI propertyIRI = prefixedName2IRI(annotationPropertyShortName);
			return getOWLDataFactory().getOWLAnnotationProperty(propertyIRI);
		} else
			throw new SWRLParseException(annotationPropertyShortName + " is not an OWL annotation property");
	}

	@NonNull
	private OWLDatatype createOWLDatatype(@NonNull String datatypeShortName) throws SWRLParseException {
		if (isOWLDatatype(datatypeShortName)) {
			IRI datatypeIRI = prefixedName2IRI(datatypeShortName);
			return getOWLDataFactory().getOWLDatatype(datatypeIRI);
		} else
			throw new SWRLParseException(datatypeShortName + " is not a valid datatype");
	}

	@NonNull
	private IRI createSWRLBuiltInIRI(@NonNull String builtInPrefixedName) throws SWRLParseException {
		if (!isSWRLBuiltIn(builtInPrefixedName))
			throw new SWRLParseException(builtInPrefixedName + " is not a SWRL built-in");
		else
			return prefixedName2IRI(builtInPrefixedName);
	}

	@NonNull
	private SWRLAPIOWLOntology getSWRLAPIOWLOntology() {
		return this.swrlapiOWLOntology;
	}

	@NonNull
	private OWLOntology getOWLOntology() {
		return getSWRLAPIOWLOntology().getOWLOntology();
	}

	@NonNull
	private SWRLAPIOWLDataFactory getSWRLAPIOWLDataFactory() {
		return getSWRLAPIOWLOntology().getSWRLAPIOWLDataFactory();
	}

	@NonNull
	private SWRLBuiltInArgumentFactory getSWRLBuiltInArgumentFactory() {
		return getSWRLAPIOWLDataFactory().getSWRLBuiltInArgumentFactory();
	}

	@NonNull
	private OWLDataFactory getOWLDataFactory() {
		return getSWRLAPIOWLOntology().getSWRLAPIOWLDataFactory();
	}

	@NonNull
	private OWLLiteralFactory getOWLLiteralFactory() {
		return getSWRLAPIOWLOntology().getSWRLAPIOWLDataFactory().getOWLLiteralFactory();
	}

	@NonNull
	private String iri2ShortForm(IRI iri) {
		Optional<@NonNull String> shortForm = this.swrlapiOWLOntology.getIRIResolver().iri2ShortForm(iri);

		if (shortForm.isPresent())
			return shortForm.get();
		else
			throw new IllegalArgumentException("could not get short form for IRI " + iri);
	}

	//sarker.3 changed private to public
	@NonNull
	public IRI prefixedName2IRI(String prefixedName) {
		
		Optional<@NonNull IRI> iri = this.swrlapiOWLOntology.getIRIResolver().prefixedName2IRI(prefixedName);
		
		if (iri.isPresent())
			return iri.get();
		else {
			throw new IllegalArgumentException("could not find IRI for prefixed name " + prefixedName);
		}
	}
}
