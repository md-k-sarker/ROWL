package edu.wsu.dase.model.ruletoaxiom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLPredicate;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLVariable;

public class Srd {

	public static OWLDataFactory factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();
	public static int freshCounter = 0;

	// SWRL Related Methods

	public static OWLClassExpression createFreshUniqueClass() {
		return factory.getOWLClass(IRI.create("pluginFreshClass" + freshCounter++));
	}

	public static Set<SWRLIArgument> getArgumentsToSet(SWRLAtom atom) {
		Set<SWRLIArgument> arguments = new HashSet<SWRLIArgument>();
		for (SWRLArgument argument : atom.getAllArguments())
			arguments.add((SWRLIArgument) argument);
		return arguments;
	}

	public static Set<SWRLIArgument> getArgumentsToSet(Set<SWRLAtom> atoms) {
		Set<SWRLIArgument> arguments = new HashSet<SWRLIArgument>();
		for (SWRLAtom atom : atoms)
			arguments.addAll(getArgumentsToSet(atom));
		return arguments;
	}

	protected static ArrayList<SWRLIArgument> getArgumentsToArrayList(SWRLAtom atom) {
		ArrayList<SWRLIArgument> arguments = new ArrayList<SWRLIArgument>();
		Iterator<SWRLArgument> iterator = atom.getAllArguments().iterator();
		while (iterator.hasNext())
			arguments.add((SWRLIArgument) iterator.next());
		return arguments;
	}

	protected static ArrayList<SWRLVariable> getVariablesToArrayList(SWRLAtom atom) {
		ArrayList<SWRLVariable> variables = new ArrayList<SWRLVariable>();
		for (SWRLArgument argument : atom.getAllArguments())
			if (isVariable(argument))
				variables.add((SWRLVariable) argument);
		return variables;
	}

	protected static Set<SWRLVariable> getVariablesToSet(Set<SWRLAtom> atoms) {
		Set<SWRLVariable> variables = new HashSet<SWRLVariable>();
		for (SWRLAtom atom : atoms)
			variables.addAll(getVariablesToSet(atom));
		return variables;
	}

	protected static Set<SWRLVariable> getVariablesToSet(SWRLAtom atom) {
		Set<SWRLVariable> variables = new HashSet<SWRLVariable>();
		for (SWRLArgument argument : atom.getAllArguments())
			if (Srd.isVariable(argument))
				variables.add((SWRLVariable) argument);
		return variables;
	}

	public static Set<SWRLIArgument> getConstantsToSet(SWRLAtom atom) {
		Set<SWRLIArgument> constants = new HashSet<SWRLIArgument>();
		for (SWRLArgument argument : atom.getAllArguments())
			if (!isVariable(argument))
				constants.add((SWRLIArgument) argument);
		return constants;
	}

	public static Set<SWRLArgument> getConstantsToSet(Set<SWRLAtom> atoms) {
		Set<SWRLArgument> arguments = new HashSet<SWRLArgument>();
		for (SWRLAtom atom : atoms)
			arguments.addAll(getConstantsToSet(atom));
		return arguments;
	}

	public static SWRLVariable getHeadVariable(Set<SWRLAtom> head) {
		SWRLVariable headVariable = null;
		for (SWRLAtom headAtom : head)
			for (SWRLArgument headArgument : headAtom.getAllArguments())
				if (headArgument.toString().contains("Variable"))
					if (headVariable == null)
						headVariable = (SWRLVariable) headArgument;
					else if (!headArgument.toString().equals(headVariable.toString()))
						System.out.println("WARNING!!! More than 1 different head variable at getHeadVariable");
		if (headVariable == null)
			System.out.println("WARNING!!! No head variables at getHeadVariable");
		return headVariable;
	}

	public static int getVariableCount(SWRLAtom atom) {
		Set<String> variables = new HashSet<String>();
		for (SWRLArgument argument : atom.getAllArguments())
			if (argument.toString().contains("Variable"))
				variables.add(argument.toString());
		return variables.size();
	}

	protected static boolean isVariable(SWRLArgument argument) {
		return argument.toString().contains("Variable");
	}

	public static OWLClassExpression predicateToClass(SWRLPredicate predicate) {
		return factory.getOWLClass(IRI.create(predicate.toString().replace("<", "").replace(">", "")));
	}

	public static OWLObjectProperty predicateToRole(SWRLPredicate predicate) {
		return factory.getOWLObjectProperty(IRI.create(predicate.toString().replace("<", "").replace(">", "")));
	}

	public static OWLIndividual argumentToIndividual(SWRLArgument root) {
		return factory.getOWLNamedIndividual(IRI.create(root.toString().replace("<", "").replace(">", "")));
	}

	// Inverse Role Related Methods

	protected static boolean isInverse(OWLObjectPropertyExpression role) {
		return (isInverse(role.toString()));
	}

	protected static boolean isInverse(OWLDataPropertyExpression role) {
		return (isInverse(role.toString()));
	}

	protected static boolean isInverse(String role) {
		return (role.contains("InverseOf"));
	}

	protected static OWLObjectPropertyExpression invert(OWLObjectPropertyExpression role) {
		if (isInverse(role))
			return role.getNamedProperty();
		else
			return role.getInverseProperty();
	}

	protected static String invert(String role) {
		if (role.contains("InverseOf"))
			return role.substring(10, role.length() - 1);
		else
			return "InverseOf(" + role + ")";
	}

	// Visualization Methods

	public static String toString(Set<SWRLAtom> body) {
		String bodyString = new String();
		for (SWRLAtom bodyAtom : body)
			bodyString += toString(bodyAtom) + ", ";
		if (bodyString.equals(""))
			return bodyString;
		else
			return bodyString.substring(0, bodyString.length() - 2);
	}

	protected static String toString(SWRLAtom atom) {
		String predicateStr = new String(atom.getPredicate().toString());
		Collection<SWRLArgument> arguments = atom.getAllArguments();
		Iterator<SWRLArgument> iterator = arguments.iterator();
		SWRLArgument arg0 = iterator.next();

		if (arguments.size() == 1)
			return predicateStr + "(" + trim(arg0.toString()) + ")";
		else {
			SWRLArgument arg1 = iterator.next();
			return predicateStr + "(" + trim(arg0.toString()) + ", " + trim(arg1.toString()) + ")";
		}
	}

	private static String trim(String argStr) {
		return argStr.substring(argStr.indexOf("<"), argStr.indexOf(">") + 1);
	}

	public static String toString(SWRLRule testRule) {
		return toString(testRule.getBody()) + " -> " + toString(testRule.getHead()) ;
	}

}
