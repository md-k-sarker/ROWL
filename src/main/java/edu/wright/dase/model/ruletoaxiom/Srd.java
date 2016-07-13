package edu.wright.dase.model.ruletoaxiom;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLBuiltInAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLDataRangeAtom;
import org.semanticweb.owlapi.model.SWRLDifferentIndividualsAtom;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLSameIndividualAtom;
import org.semanticweb.owlapi.model.SWRLVariable;

public class Srd {

	public static OWLDataFactory factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();
	public static int freshCounter = 0;

	// SWRL Helper Methods

	// Return a set with all atoms of a certain kind

	public static Set<SWRLAtom> getAtomsWithArg(Set<SWRLAtom> atoms, SWRLArgument arg) {
		Set<SWRLAtom> atomsWithArg = new HashSet<SWRLAtom>();
		for (SWRLAtom atom : atoms)
			if (atom.getAllArguments().contains(arg))
				atomsWithArg.add(atom);
		return atomsWithArg;
	}

	public static Set<SWRLAtom> getAtomsWithoutArg(Set<SWRLAtom> atoms, SWRLArgument arg) {
		Set<SWRLAtom> atomsWithoutArg = new HashSet<SWRLAtom>();
		for (SWRLAtom atom : atoms)
			if (!atom.getAllArguments().contains(arg))
				atomsWithoutArg.add(atom);
		return atomsWithoutArg;
	}

	public static Set<SWRLClassAtom> getClassAtoms(Set<SWRLAtom> atoms) {
		Set<SWRLClassAtom> classAtoms = new HashSet<SWRLClassAtom>();
		for (SWRLAtom atom : atoms)
			if (atom instanceof SWRLClassAtom)
				classAtoms.add((SWRLClassAtom) atom);
		return classAtoms;
	}

	public static Set<SWRLClassAtom> getClassAtomsWithArg(Set<SWRLAtom> atoms, SWRLArgument var) {
		Set<SWRLClassAtom> classAtomsWithVar = new HashSet<SWRLClassAtom>();
		for (SWRLClassAtom classAtom : getClassAtoms(atoms))
			if (classAtom.getAllArguments().contains(var))
				classAtomsWithVar.add(classAtom);
		return classAtomsWithVar;
	}

	public static Set<SWRLObjectPropertyAtom> getObjPropAtoms(Set<SWRLAtom> atoms) {
		Set<SWRLObjectPropertyAtom> objPropAtomsWithVar = new HashSet<SWRLObjectPropertyAtom>();
		for (SWRLAtom atom : atoms)
			if (atom instanceof SWRLObjectPropertyAtom)
				objPropAtomsWithVar.add((SWRLObjectPropertyAtom) atom);
		return objPropAtomsWithVar;
	}

	public static Set<SWRLObjectPropertyAtom> getObjPropAtomsWithArg(Set<SWRLAtom> atoms, SWRLArgument arg) {
		Set<SWRLObjectPropertyAtom> objPropAtomsWithVar = new HashSet<SWRLObjectPropertyAtom>();
		for (SWRLObjectPropertyAtom objPropAtom : getObjPropAtoms(atoms))
			if (objPropAtom.getAllArguments().contains(arg))
				objPropAtomsWithVar.add(objPropAtom);
		return objPropAtomsWithVar;
	}

	public static Set<SWRLDataPropertyAtom> getDataPropAtoms(Set<SWRLAtom> atoms) {
		Set<SWRLDataPropertyAtom> dataPropAtoms = new HashSet<SWRLDataPropertyAtom>();
		for (SWRLAtom atom : atoms)
			if (atom instanceof SWRLDataPropertyAtom)
				dataPropAtoms.add((SWRLDataPropertyAtom) atom);
		return dataPropAtoms;
	}

	public static Set<SWRLDataPropertyAtom> getDataPropAtomsWithArg(Set<SWRLAtom> atoms, SWRLArgument arg) {
		Set<SWRLDataPropertyAtom> dataPropAtomsWithArg = new HashSet<SWRLDataPropertyAtom>();
		for (SWRLDataPropertyAtom dataPropAtom : getDataPropAtoms(atoms))
			if (dataPropAtom.getAllArguments().contains(arg))
				dataPropAtomsWithArg.add(dataPropAtom);
		return dataPropAtomsWithArg;
	}

	public static Set<SWRLDataRangeAtom> getDataRangeAtoms(Set<SWRLAtom> atoms) {
		Set<SWRLDataRangeAtom> dataRangeAtoms = new HashSet<SWRLDataRangeAtom>();
		for (SWRLAtom atom : atoms)
			if (atom instanceof SWRLDataRangeAtom)
				dataRangeAtoms.add((SWRLDataRangeAtom) atom);
		return dataRangeAtoms;
	}

	public static Set<SWRLDataRangeAtom> getDataRangeAtomsWithArg(Set<SWRLAtom> atoms, SWRLArgument arg) {
		Set<SWRLDataRangeAtom> dataRangeAtomsWithArg = new HashSet<SWRLDataRangeAtom>();
		for (SWRLDataRangeAtom dataRangeAtom : getDataRangeAtoms(atoms))
			if (dataRangeAtom.getAllArguments().contains(arg))
				dataRangeAtomsWithArg.add((SWRLDataRangeAtom) dataRangeAtom);
		return dataRangeAtomsWithArg;
	}

	public static Set<SWRLSameIndividualAtom> getSameIndAtoms(Set<SWRLAtom> atoms) {
		Set<SWRLSameIndividualAtom> sameIndividualAtoms = new HashSet<SWRLSameIndividualAtom>();
		for (SWRLAtom atom : atoms)
			if (atom instanceof SWRLSameIndividualAtom)
				sameIndividualAtoms.add((SWRLSameIndividualAtom) atom);
		return sameIndividualAtoms;
	}

	public static Set<SWRLDifferentIndividualsAtom> getDifferentIndsAtoms(Set<SWRLAtom> atoms) {
		Set<SWRLDifferentIndividualsAtom> differentIndsAtoms = new HashSet<SWRLDifferentIndividualsAtom>();
		for (SWRLAtom atom : atoms)
			if (atom instanceof SWRLDifferentIndividualsAtom)
				differentIndsAtoms.add((SWRLDifferentIndividualsAtom) atom);
		return differentIndsAtoms;
	}

	public static Set<SWRLBuiltInAtom> getBuiltInAtoms(Set<SWRLAtom> atoms) {
		Set<SWRLBuiltInAtom> builtInAtoms = new HashSet<SWRLBuiltInAtom>();
		for (SWRLAtom atom : atoms)
			if (atom instanceof SWRLBuiltInAtom)
				builtInAtoms.add((SWRLBuiltInAtom) atom);
		return builtInAtoms;
	}

	// Return a set with the arguments, variables, individuals and literals in an atom or a set of atoms

	public static Set<SWRLArgument> getArgsToSet(Set<SWRLAtom> atoms) {
		Set<SWRLArgument> args = new HashSet<SWRLArgument>();
		for (SWRLAtom atom : atoms)
			args.addAll(atom.getAllArguments());
		return args;
	}

	private static Set<SWRLIArgument> getObjArgs(Set<SWRLAtom> atoms) {
		Set<SWRLIArgument> objArgs = new HashSet<SWRLIArgument>();
		for (SWRLVariable objVar : getObjVarsToSet(atoms))
			objArgs.add((SWRLIArgument) objVar);
		objArgs.addAll(getIndsToSet(atoms));
		return objArgs;
	}

	public static Set<SWRLVariable> getVarsToSet(SWRLAtom atom) {
		Set<SWRLVariable> vars = new HashSet<SWRLVariable>();
		for (SWRLArgument arg : atom.getAllArguments()) {
			if (arg instanceof SWRLVariable)
				vars.add((SWRLVariable) arg);
		}
		return vars;
	}

	public static Set<SWRLVariable> getVarsToSet(Set<SWRLAtom> atoms) {
		Set<SWRLVariable> vars = new HashSet<SWRLVariable>();
		for (SWRLAtom atom : atoms)
			vars.addAll(getVarsToSet(atom));
		return vars;
	}

	public static Set<SWRLVariable> getObjVarsToSet(SWRLAtom atom) {
		Set<SWRLVariable> vars = new HashSet<SWRLVariable>();
		for (SWRLArgument arg : atom.getAllArguments())
			if (arg instanceof SWRLVariable)
				if (arg instanceof SWRLIArgument)
					vars.add((SWRLVariable) arg);
		return vars;
	}

	public static Set<SWRLVariable> getObjVarsToSet(Set<SWRLAtom> atoms) {
		Set<SWRLVariable> vars = new HashSet<SWRLVariable>();
		for (SWRLAtom atom : atoms)
			vars.addAll(getObjVarsToSet(atom));
		return vars;
	}

	public static Set<SWRLVariable> getDataVarsToSet(SWRLAtom atom) {
		Set<SWRLVariable> vars = new HashSet<SWRLVariable>();
		for (SWRLArgument arg : atom.getAllArguments())
			if (arg instanceof SWRLVariable)
				if (arg instanceof SWRLDArgument)
					vars.add((SWRLVariable) arg);
		return vars;
	}

	public static Set<SWRLVariable> getDataVarsToSet(Set<SWRLAtom> atoms) {
		Set<SWRLVariable> vars = new HashSet<SWRLVariable>();
		for (SWRLAtom atom : atoms)
			vars.addAll(getObjVarsToSet(atom));
		return vars;
	}

	public static Set<SWRLIndividualArgument> getIndsToSet(SWRLAtom atom) {
		Set<SWRLIndividualArgument> inds = new HashSet<SWRLIndividualArgument>();
		for (SWRLArgument arg : atom.getAllArguments())
			if (arg instanceof SWRLIndividualArgument)
				inds.add((SWRLIndividualArgument) arg);
		return inds;
	}

	public static Set<SWRLIndividualArgument> getIndsToSet(Set<SWRLAtom> atoms) {
		Set<SWRLIndividualArgument> inds = new HashSet<SWRLIndividualArgument>();
		for (SWRLAtom atom : atoms)
			inds.addAll(getIndsToSet(atom));
		return inds;
	}

	public static Set<SWRLLiteralArgument> getLitsToSet(SWRLAtom atom) {
		Set<SWRLLiteralArgument> lits = new HashSet<SWRLLiteralArgument>();
		for (SWRLArgument arg : atom.getAllArguments())
			if (arg instanceof SWRLLiteralArgument)
				lits.add((SWRLLiteralArgument) arg);
		return lits;
	}

	public static Set<SWRLLiteralArgument> getLitsToSet(Set<SWRLAtom> atoms) {
		Set<SWRLLiteralArgument> lits = new HashSet<SWRLLiteralArgument>();
		for (SWRLAtom atom : atoms)
			lits.addAll(getLitsToSet(atom));
		return lits;
	}

	// Checkers for different types of SWRLAtom objects

	public static boolean isClassAtomWithInd(SWRLAtom atom) {
		if (atom instanceof SWRLClassAtom)
			if (((SWRLClassAtom) atom).getArgument() instanceof SWRLIndividualArgument)
				return true;
		return false;
	}

	public static boolean isObjPropAtomWith2Inds(SWRLAtom atom) {
		if (atom instanceof SWRLObjectPropertyAtom) {
			SWRLObjectPropertyAtom objPropAtom = (SWRLObjectPropertyAtom) atom;
			if (objPropAtom.getFirstArgument() instanceof SWRLIndividualArgument && objPropAtom.getSecondArgument() instanceof SWRLIndividualArgument)
				return true;
		}
		return false;
	}

	public static boolean isObjPropAtomWith2DiffVars(SWRLAtom atom) {
		if (atom instanceof SWRLObjectPropertyAtom) {
			SWRLObjectPropertyAtom objPropAtom = (SWRLObjectPropertyAtom) atom;
			if (objPropAtom.getFirstArgument() instanceof SWRLVariable)
				if (objPropAtom.getSecondArgument() instanceof SWRLVariable)
					if (!objPropAtom.getFirstArgument().equals(objPropAtom.getSecondArgument()))
						return true;
		}
		return false;
	}

	public static boolean isDataPropAtomWith2DiffVars(SWRLAtom atom) {
		if (atom instanceof SWRLDataPropertyAtom) {
			SWRLDataPropertyAtom dataPropAtom = (SWRLDataPropertyAtom) atom;
			if (dataPropAtom.getFirstArgument() instanceof SWRLVariable)
				if (dataPropAtom.getSecondArgument() instanceof SWRLVariable)
					if (!dataPropAtom.getFirstArgument().equals(dataPropAtom.getSecondArgument()))
						return true;
		}
		return false;
	}

	// Checker for object variables

	public static boolean isObjVar(SWRLArgument arg, Set<SWRLAtom> atoms) {
		if (!(arg instanceof SWRLVariable))
			return false;

		for (SWRLAtom atom : atoms)
			if (atom.getAllArguments().contains(arg))
				if (atom instanceof SWRLClassAtom || atom instanceof SWRLObjectPropertyAtom) {
					return true;
				} else if (atom instanceof SWRLDataPropertyAtom) {
					SWRLDataPropertyAtom dataPropAtom = (SWRLDataPropertyAtom) atom;
					if (dataPropAtom.getFirstArgument().equals(arg))
						return true;
					if (dataPropAtom.getSecondArgument().equals(arg))
						return false;
				} else if (atom instanceof SWRLDataRangeAtom)
					return false;

		System.out.println("WARNING!!! at isObjVariable at S.java." + "\n");
		return false;

	}

	// Inverse Role Related Methods

	public static boolean isInverse(OWLObjectPropertyExpression role) {
		return (isInverse(role.toString()));
	}

	protected static boolean isInverse(OWLDataPropertyExpression role) {
		return (isInverse(role.toString()));
	}

	protected static boolean isInverse(String role) {
		return (role.contains("InverseOf"));
	}

	public static OWLObjectPropertyExpression invert(OWLObjectPropertyExpression role) {
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

	// To Set

	public static Set<SWRLAtom> toSet(SWRLAtom objPropAtom) {
		Set<SWRLAtom> set = new HashSet<SWRLAtom>();
		set.add(objPropAtom);
		return set;
	}

	// Class Expression Builders

	public static OWLDataRange buildDataRangeIntersection(Set<OWLDataRange> fillerBConjuncts) {
		switch (fillerBConjuncts.size()) {
		case 0:
			return factory.getTopDatatype();
		case 1:
			return fillerBConjuncts.iterator().next();
		default:
			return factory.getOWLDataIntersectionOf(fillerBConjuncts);
		}
	}

	public static OWLClassExpression buildClassExpressionIntersection(Set<OWLClassExpression> conjuncts) {
		switch (conjuncts.size()) {
		case 0:
			return Srd.factory.getOWLThing();
		case 1:
			return conjuncts.iterator().next();
		default:
			return Srd.factory.getOWLObjectIntersectionOf(conjuncts);
		}
	}

	public static OWLObjectOneOf buildNominal(SWRLIndividualArgument ind) {
		Set<OWLIndividual> set = new HashSet<OWLIndividual>();
		set.add((OWLIndividual) ind);
		return factory.getOWLObjectOneOf(set);
	}

	// Choose Root Method

	public static SWRLVariable chooseRoot(Set<SWRLAtom> bodyAtoms) {
		Set<SWRLVariable> objVars = Srd.getObjVarsToSet(bodyAtoms);
		if (objVars.isEmpty())
			return Srd.factory.getSWRLVariable(IRI.create("x"));
		for (SWRLVariable potentialRootVar : objVars)
			if (!requiresInverseInTranslation(potentialRootVar, bodyAtoms))
				return potentialRootVar;
		return objVars.iterator().next();
	}

	private static boolean requiresInverseInTranslation(SWRLArgument potentialRoot, Set<SWRLAtom> atoms) {
		for (SWRLObjectPropertyAtom objPropAtom : Srd.getObjPropAtoms(atoms))
			if ((objPropAtom.getFirstArgument().equals(potentialRoot) && Srd.isInverse(objPropAtom.getPredicate()))
					|| (objPropAtom.getSecondArgument().equals(potentialRoot) && !Srd.isInverse(objPropAtom.getPredicate())))
				return true;
		return false;
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
		if (argStr.contains("<") && argStr.contains(">"))
			return argStr.substring(argStr.indexOf("<"), argStr.indexOf(">") + 1);
		else
			return argStr;
	}

	public static String toString(SWRLRule testRule) {
		return toString(testRule.getBody()) + " -> " + toString(testRule.getHead()) + ".";
	}

	public static String toString(Entry<SWRLVariable, Set<SWRLAtom>> singleVarSplit) {
		String splitStr = new String(singleVarSplit.getKey().toString());
		for (SWRLAtom atom : singleVarSplit.getValue())
			splitStr += atom.toString() + ", ";

		return splitStr.substring(splitStr.length() - 2);
	}

	public static String shortStr(String atomsStr) {
		atomsStr = atomsStr.replace("ClassAtom", "");
		atomsStr = atomsStr.replace("DLSafeRule", "Rule");
		atomsStr = atomsStr.replace("DataRangeAtom", "");
		atomsStr = atomsStr.replace("InverseOf", "Inv");
		atomsStr = atomsStr.replace("ObjectIntersectionOf", "Int");
		atomsStr = atomsStr.replace("ObjectOneOf", "Nom");
		atomsStr = atomsStr.replace("ObjectPropertyAtom", "");
		atomsStr = atomsStr.replace("DataPropertyAtom", "");
		atomsStr = atomsStr.replace("ObjectSomeValuesFrom", "ObjSome");
		atomsStr = atomsStr.replace("Variable", "");
		return atomsStr;
	}

	// Not currently Using

	//	public static OWLClassExpression createFreshUniqueClass() {
	//		return factory.getOWLClass(IRI.create("pluginFreshClass" + freshCounter++));
	//	}
	//
	//	public static SWRLVariable getHeadVariable(Set<SWRLAtom> head) {
	//		SWRLVariable headVariable = null;
	//		for (SWRLAtom headAtom : head)
	//			for (SWRLArgument headArgument : headAtom.getAllArguments())
	//				if (headArgument.toString().contains("Variable"))
	//					if (headVariable == null)
	//						headVariable = (SWRLVariable) headArgument;
	//					else if (!headArgument.toString().equals(headVariable.toString()))
	//						System.out.println("WARNING!!! More than 1 different head variable at getHeadVariable");
	//		if (headVariable == null)
	//			System.out.println("WARNING!!! No head variables at getHeadVariable");
	//		return headVariable;
	//	}

	//	public static int getDiffVariableCount(SWRLAtom atom) {
	//		Set<SWRLVariable> variables = new HashSet<SWRLVariable>();
	//		for (SWRLArgument argument : atom.getAllArguments())
	//			if (argument instanceof SWRLVariable)
	//				variables.add((SWRLVariable) argument);
	//		return variables.size();
	//	}

	//	protected static boolean isVariable(SWRLArgument argument) {
	//		return argument.toString().contains("Variable");
	//	}
	//
	//	public static OWLClassExpression predicateToClass(SWRLPredicate predicate) {
	//		return factory.getOWLClass(IRI.create(predicate.toString().replace("<", "").replace(">", "")));
	//	}
	//
	//	public static OWLObjectProperty predicateToRole(SWRLPredicate predicate) {
	//		return factory.getOWLObjectProperty(IRI.create(predicate.toString().replace("<", "").replace(">", "")));
	//	}
	//
	//	public static OWLIndividual argumentToIndividual(SWRLArgument root) {
	//		return factory.getOWLNamedIndividual(IRI.create(root.toString().replace("<", "").replace(">", "")));
	//	}

	//	public static Set<SWRLAtom> getAtomsContainingIndividualsToSet(Set<SWRLAtom> atoms) {
	//		Set<SWRLAtom> atomsWithIndividuals = new HashSet<SWRLAtom>();
	//		for (SWRLAtom atom : atoms)
	//			for (SWRLArgument arg : atom.getAllArguments())
	//				if (arg instanceof SWRLIndividualArgument)
	//					atomsWithIndividuals.add(atom);
	//
	//		return atomsWithIndividuals;
	//	}

	//	public static ArrayList<SWRLVariable> getDiffVariablesToArrayList(Set<SWRLAtom> atoms) {
	//		ArrayList<SWRLVariable> variables = new ArrayList<SWRLVariable>();
	//		for (SWRLAtom atom : atoms)
	//			for (SWRLArgument arg : atom.getAllArguments())
	//				if (arg instanceof SWRLVariable)
	//					if (!variables.contains(arg))
	//						variables.add((SWRLVariable) arg);
	//		return variables;
	//	}

	//	public static Set<SWRLDataRangeAtom> getDataRangeAtomsToSet(Set<SWRLAtom> atoms) {
	//		Set<SWRLDataRangeAtom> dataRangeAtoms = new HashSet<SWRLDataRangeAtom>();
	//		for (SWRLAtom atom : atoms)
	//			if (atom instanceof SWRLDataRangeAtom)
	//				dataRangeAtoms.add((SWRLDataRangeAtom) atom);
	//		return dataRangeAtoms;
	//	}

	//	public static Set<SWRLDataPropertyAtom> getDataPropertyAtomsToSet(Set<SWRLAtom> atoms) {
	//		Set<SWRLDataPropertyAtom> dataPropAtoms = new HashSet<SWRLDataPropertyAtom>();
	//		for (SWRLAtom atom : atoms)
	//			if (atom instanceof SWRLDataPropertyAtom)
	//				dataPropAtoms.add((SWRLDataPropertyAtom) atom);
	//		return dataPropAtoms;
	//	}

	//	public static boolean isDataPropAtomWith2EqVars(SWRLAtom atom) {
	//		if (atom instanceof SWRLDataPropertyAtom) {
	//			SWRLDataPropertyAtom dataPropAtom = (SWRLDataPropertyAtom) atom;
	//			if (dataPropAtom.getFirstArgument() instanceof SWRLVariable)
	//				if (dataPropAtom.getSecondArgument() instanceof SWRLVariable)
	//					if (dataPropAtom.getFirstArgument().equals(dataPropAtom.getSecondArgument()))
	//						return true;
	//		}
	//		return false;
	//	}
}
