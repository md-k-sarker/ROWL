package edu.wright.dase.model.ruletoaxiom;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLDataRangeAtom;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLVariable;

import uk.ac.manchester.cs.owl.owlapi.SWRLClassAtomImpl;

public class RollUp {

	// Public Methods

	public static void rollUpLiterals(SWRLIArgument root, Set<SWRLAtom> atoms) {
		Set<SWRLArgument> roots = new HashSet<SWRLArgument>();
		roots.add(root);
		rollUpLiterals(roots, atoms);
	}

	public static void rollUpLiterals(SWRLVariable root1, SWRLVariable root2, Set<SWRLAtom> atoms) {
		Set<SWRLArgument> roots = new HashSet<SWRLArgument>();
		roots.add(root1);
		roots.add(root2);
		rollUpLiterals(roots, atoms);
	}

	public static void rollUpIndsAndVars(SWRLVariable root, Set<SWRLAtom> atoms) {
		Set<SWRLArgument> roots = new HashSet<SWRLArgument>();
		roots.add(root);
		rollUpIndsAndVars(roots, atoms);
	}

	public static void rollUpIndsAndVars(SWRLIArgument root1, SWRLVariable root2, Set<SWRLAtom> atoms) {
		Set<SWRLArgument> roots = new HashSet<SWRLArgument>();
		roots.add(root1);
		roots.add(root2);
		rollUpIndsAndVars(roots, atoms);
	}

	public static void rollUpIndsAndVars(SWRLVariable root1, SWRLVariable root2, SWRLVariable root3, Set<SWRLAtom> atoms) {
		Set<SWRLArgument> roots = new HashSet<SWRLArgument>();
		roots.add(root1);
		roots.add(root2);
		roots.add(root3);
		rollUpIndsAndVars(roots, atoms);
	}

	// Private Methods

	private static void rollUpLiterals(Set<SWRLArgument> roots, Set<SWRLAtom> atoms) {
		Set<SWRLLiteralArgument> lits = Srd.getLitsToSet(atoms);
		for (SWRLLiteralArgument lit : lits)
			rollUpDataArg(lit, atoms);
	}

	private static void rollUpIndsAndVars(Set<SWRLArgument> roots, Set<SWRLAtom> atoms) {
		SWRLArgument rollUpArg = selectArgToRollUp(roots, atoms);
		while (rollUpArg != null) {
			rollUpArg(rollUpArg, atoms);
			rollUpArg = selectArgToRollUp(roots, atoms);
		}
	}

	private static SWRLArgument selectArgToRollUp(Set<SWRLArgument> roots, Set<SWRLAtom> atoms) {

		for (SWRLVariable var : Srd.getVarsToSet(atoms))
			if (!roots.contains(var)) {
				int counter = 0;

				Set<SWRLDataPropertyAtom> dataPropAtoms = Srd.getDataPropAtomsWithArg(atoms, var);
				for (SWRLDataPropertyAtom dataPropAtom : dataPropAtoms)
					if (dataPropAtom.getFirstArgument().equals(var))
						counter = 2;
					else
						counter++;

				Set<SWRLObjectPropertyAtom> objPropAtoms = Srd.getObjPropAtomsWithArg(atoms, var);
				for (SWRLObjectPropertyAtom objPropAtom : objPropAtoms)
					if (Srd.isObjPropAtomWith2DiffVars(objPropAtom))
						counter++;

				if (counter == 1)
					return var;
			}

		Set<SWRLIndividualArgument> inds = Srd.getIndsToSet(atoms);
		for (SWRLIndividualArgument ind : inds)
			if (Srd.getDataPropAtomsWithArg(atoms, ind).isEmpty())
				if (Connector.isConnected(Srd.getAtomsWithoutArg(atoms, ind)))
					return ind;

		if (!inds.isEmpty())
			return inds.iterator().next();

		return null;
	}

	private static void rollUpArg(SWRLArgument rollUpArg, Set<SWRLAtom> atoms) {
		if (rollUpArg instanceof SWRLVariable) {
			if (Srd.isObjVar((SWRLVariable) rollUpArg, atoms))
				rollUpObjVar((SWRLVariable) rollUpArg, atoms);
			else
				rollUpDataArg(rollUpArg, atoms);
		} else if (rollUpArg instanceof SWRLLiteralArgument)
			rollUpDataArg(rollUpArg, atoms);
		else if (rollUpArg instanceof SWRLIndividualArgument)
			rollUpInd((SWRLIndividualArgument) rollUpArg, atoms);
	}

	private static void rollUpObjVar(SWRLVariable rollUpObjVar, Set<SWRLAtom> atoms) {
		Set<SWRLClassAtom> classAtomsWithRollUpObjVar = Srd.getClassAtomsWithArg(atoms, rollUpObjVar);
		atoms.removeAll(classAtomsWithRollUpObjVar);
		Set<SWRLObjectPropertyAtom> objPropAtomsWithRollUpObjVar = Srd.getObjPropAtomsWithArg(atoms, rollUpObjVar);
		atoms.removeAll(objPropAtomsWithRollUpObjVar);

		Set<OWLClassExpression> fillerConjuncts = new HashSet<OWLClassExpression>();
		for (SWRLClassAtom classAtomWithRollUpObjVar : classAtomsWithRollUpObjVar)
			fillerConjuncts.add(classAtomWithRollUpObjVar.getPredicate());
		for (SWRLObjectPropertyAtom objPropAtomWithRollUpObjVar : objPropAtomsWithRollUpObjVar)
			if (objPropAtomWithRollUpObjVar.getFirstArgument().equals(objPropAtomWithRollUpObjVar.getSecondArgument()))
				fillerConjuncts.add(Srd.factory.getOWLObjectHasSelf(objPropAtomWithRollUpObjVar.getPredicate()));

		OWLObjectPropertyExpression objProp = null;
		SWRLArgument hookArg = null;
		for (SWRLObjectPropertyAtom objPropAtomWithRollUpObjVar : objPropAtomsWithRollUpObjVar)
			if (!objPropAtomWithRollUpObjVar.getFirstArgument().equals(objPropAtomWithRollUpObjVar.getSecondArgument()))
				if (objPropAtomWithRollUpObjVar.getSecondArgument().equals(rollUpObjVar)) {
					objProp = objPropAtomWithRollUpObjVar.getPredicate();
					hookArg = objPropAtomWithRollUpObjVar.getFirstArgument();
				} else {
					objProp = Srd.invert(objPropAtomWithRollUpObjVar.getPredicate());
					hookArg = objPropAtomWithRollUpObjVar.getSecondArgument();
				}

		atoms.add(new SWRLClassAtomImpl(Srd.factory.getOWLObjectSomeValuesFrom(objProp, Srd.buildClassExpressionIntersection(fillerConjuncts)), (SWRLIArgument) hookArg));
	}

	private static void rollUpInd(SWRLIndividualArgument rollUpInd, Set<SWRLAtom> atoms) {

		Set<SWRLClassAtom> classAtomsWithRollUpInd = Srd.getClassAtomsWithArg(atoms, rollUpInd);
		atoms.removeAll(classAtomsWithRollUpInd);
		Set<SWRLObjectPropertyAtom> objPropAtomsWithRollUpInd = Srd.getObjPropAtomsWithArg(atoms, rollUpInd);
		atoms.removeAll(objPropAtomsWithRollUpInd);

		Set<OWLClassExpression> fillerConjuncts = new HashSet<OWLClassExpression>();
		Set<OWLIndividual> individualSet = new HashSet<OWLIndividual>();
		individualSet.add(rollUpInd.getIndividual());
		OWLClassExpression nominal = Srd.factory.getOWLObjectOneOf(individualSet);
		fillerConjuncts.add(nominal);
		for (SWRLClassAtom classAtomWithRollUpInd : classAtomsWithRollUpInd)
			fillerConjuncts.add(classAtomWithRollUpInd.getPredicate());
		for (SWRLObjectPropertyAtom objPropAtom : objPropAtomsWithRollUpInd)
			if (objPropAtom.getFirstArgument().equals(objPropAtom.getSecondArgument()))
				fillerConjuncts.add(Srd.factory.getOWLObjectHasSelf(objPropAtom.getPredicate()));

		OWLClassExpression fillerWithConjuncts = Srd.buildClassExpressionIntersection(fillerConjuncts);
		boolean addedFillerWithConjuncts = false;

		for (SWRLObjectPropertyAtom objPropAtomWithRollUpInd : objPropAtomsWithRollUpInd)
			if (!objPropAtomWithRollUpInd.getFirstArgument().equals(objPropAtomWithRollUpInd.getSecondArgument())) {
				OWLObjectPropertyExpression objProp = null;
				SWRLIArgument hookArg = null;
				if (objPropAtomWithRollUpInd.getSecondArgument().equals(rollUpInd)) {
					objProp = objPropAtomWithRollUpInd.getPredicate();
					hookArg = (SWRLIArgument) objPropAtomWithRollUpInd.getFirstArgument();
				} else {
					objProp = Srd.invert(objPropAtomWithRollUpInd.getPredicate());
					hookArg = (SWRLIArgument) objPropAtomWithRollUpInd.getSecondArgument();
				}

				if (!addedFillerWithConjuncts) {
					atoms.add(new SWRLClassAtomImpl(Srd.factory.getOWLObjectSomeValuesFrom(objProp, fillerWithConjuncts), hookArg));
					addedFillerWithConjuncts = true;
				} else
					atoms.add(new SWRLClassAtomImpl(Srd.factory.getOWLObjectSomeValuesFrom(objProp, nominal), hookArg));
			}
	}

	private static void rollUpDataArg(SWRLArgument rollUpDataArg, Set<SWRLAtom> atoms) {
		Set<SWRLDataPropertyAtom> dataPropAtomsWithRollUpDataVar = Srd.getDataPropAtomsWithArg(atoms, rollUpDataArg);
		atoms.removeAll(dataPropAtomsWithRollUpDataVar);
		Set<SWRLDataRangeAtom> dataRangeAtomsWithRollUpDataVar = Srd.getDataRangeAtomsWithArg(atoms, rollUpDataArg);
		atoms.removeAll(dataRangeAtomsWithRollUpDataVar);

		Set<OWLDataRange> dataRangeConjuncts = new HashSet<OWLDataRange>();
		for (SWRLDataRangeAtom dataRangeAtomWithRollUpDataVar : dataRangeAtomsWithRollUpDataVar)
			dataRangeConjuncts.add(dataRangeAtomWithRollUpDataVar.getPredicate());

		if (rollUpDataArg instanceof SWRLLiteralArgument) {
			SWRLLiteralArgument literalDataArg = (SWRLLiteralArgument) rollUpDataArg;
			Set<OWLLiteral> nomLiteral = new HashSet<OWLLiteral>();
			nomLiteral.add(literalDataArg.getLiteral());
			dataRangeConjuncts.add(Srd.factory.getOWLDataOneOf(nomLiteral));
		}

		for (SWRLDataPropertyAtom dataPropAtom : dataPropAtomsWithRollUpDataVar)
			atoms.add(new SWRLClassAtomImpl(Srd.factory.getOWLDataSomeValuesFrom(dataPropAtom.getPredicate(), Srd.buildDataRangeIntersection(dataRangeConjuncts)),
					dataPropAtom.getFirstArgument()));
	}
}
