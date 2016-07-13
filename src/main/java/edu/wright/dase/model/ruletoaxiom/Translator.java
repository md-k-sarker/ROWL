package edu.wright.dase.model.ruletoaxiom;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLSameIndividualAtom;
import org.semanticweb.owlapi.model.SWRLVariable;

import uk.ac.manchester.cs.owl.owlapi.SWRLClassAtomImpl;
import uk.ac.manchester.cs.owl.owlapi.SWRLDataPropertyAtomImpl;
import uk.ac.manchester.cs.owl.owlapi.SWRLObjectPropertyAtomImpl;
import uk.ac.manchester.cs.owl.owlapi.SWRLSameIndividualAtomImpl;

public class Translator {

	public SWRLRule rule;
	public Set<OWLAxiom> resultingAxioms;

	public Translator(SWRLRule r) {
		rule = r;
		resultingAxioms = new HashSet<OWLAxiom>();
	}

	public void ruleToAxioms() {
		Set<SWRLAtom> head = rule.getHead();
		Set<SWRLAtom> body = rule.getBody();
		Set<SWRLAtom> ruleAtoms = new HashSet<SWRLAtom>();
		ruleAtoms.addAll(head);
		ruleAtoms.addAll(body);

		// 1. Check whether the rule contains SWRLBuiltInAtom objects
		if (!Srd.getBuiltInAtoms(ruleAtoms).isEmpty())
			return;

		// 2. Check whether the rule contains SWRLDifferentIndividualsAtom objects
		if (!Srd.getDifferentIndsAtoms(ruleAtoms).isEmpty())
			return;

		// 3. Normalize binary atoms of the form R(t, t)
		normalizeBinaryAtomsWith1Term(body);
		normalizeBinaryAtomsWith1Term(head);

		// 3. Normalize Away Equality
		normalizeEquality(body, head);

		// 4. Compute Resulting Axioms

		Set<SWRLVariable> headVars = Srd.getVarsToSet(head);

		switch (headVars.size()) {
		case 0:
			resultingAxioms.addAll(TranslatorFor0VarHeadSplits.translate(head, body));
			SWRLVariable root = Srd.chooseRoot(body);
			head.add(new SWRLClassAtomImpl(Srd.factory.getOWLThing(), root));
			resultingAxioms.addAll(TranslatorFor1ObjVarHeadSplits.translate(root, head, body));
			break;
		case 1:
			SWRLVariable rootVar = headVars.iterator().next();
			if (Srd.isObjVar(rootVar, head))
				resultingAxioms.addAll(TranslatorFor1ObjVarHeadSplits.translate(rootVar, head, body));
			else
				resultingAxioms.addAll(TranslatorFor1DataVarHeadSplits.translate(rootVar, head, body));
			break;
		case 2:
			SWRLAtom singleHeadAtom = head.iterator().next();
			if (singleHeadAtom instanceof SWRLObjectPropertyAtom) {
				SWRLObjectPropertyAtom headObjPropAtom = (SWRLObjectPropertyAtom) head.iterator().next();
				SWRLVariable firstObjVar = (SWRLVariable) headObjPropAtom.getFirstArgument();
				SWRLVariable secondObjVar = (SWRLVariable) headObjPropAtom.getSecondArgument();
				resultingAxioms.addAll(TranslatorForObjPropAtomSplits.translate(firstObjVar, secondObjVar, headObjPropAtom, body));
			} else if (singleHeadAtom instanceof SWRLDataPropertyAtom) {
				SWRLDataPropertyAtom headDataPropAtom = (SWRLDataPropertyAtom) head.iterator().next();
				SWRLVariable firstDataVar = (SWRLVariable) headDataPropAtom.getFirstArgument();
				SWRLVariable secondDataVar = (SWRLVariable) headDataPropAtom.getSecondArgument();
				resultingAxioms.addAll(TranslatorForDataPropAtomHead.translate(firstDataVar, secondDataVar, headDataPropAtom, body));
			} else {
				//				SWRLSameIndividualAtom sameIndAtom = (SWRLSameIndividualAtom) head.iterator().next();
				//				SWRLVariable firstSameArg = (SWRLVariable) sameIndAtom.getFirstArgument();
				//				SWRLVariable secondSameArg = (SWRLVariable) sameIndAtom.getSecondArgument();
			}
		}
	}

	private void normalizeBinaryAtomsWith1Term(Set<SWRLAtom> atoms) {
		for (SWRLObjectPropertyAtom objPropAtom : Srd.getObjPropAtoms(atoms))
			if (objPropAtom.getFirstArgument().equals(objPropAtom.getSecondArgument())) {
				atoms.add(new SWRLClassAtomImpl(Srd.factory.getOWLObjectHasSelf(objPropAtom.getPredicate()), objPropAtom.getFirstArgument()));
				atoms.remove(objPropAtom);
			}

		for (SWRLSameIndividualAtom sameIndsAtom : Srd.getSameIndAtoms(atoms))
			if (sameIndsAtom.getFirstArgument().equals(sameIndsAtom.getSecondArgument()))
				atoms.remove(sameIndsAtom);

	}

	public static void normalizeEquality(Set<SWRLAtom> body, Set<SWRLAtom> head) {
		Set<SWRLSameIndividualAtom> sameIndBodyAtoms = Srd.getSameIndAtoms(body);
		body.removeAll(sameIndBodyAtoms);

		for (SWRLSameIndividualAtom sameIndBodyAtom : sameIndBodyAtoms) {
			SWRLIArgument firstArg = sameIndBodyAtom.getFirstArgument();
			SWRLIArgument secondArg = sameIndBodyAtom.getSecondArgument();
			if (firstArg instanceof SWRLVariable)
				if (secondArg instanceof SWRLVariable) {
					replaceWith((SWRLVariable) firstArg, (SWRLVariable) secondArg, body);
					replaceWith((SWRLVariable) firstArg, (SWRLVariable) secondArg, head);
				} else
					body.add(new SWRLClassAtomImpl(Srd.buildNominal((SWRLIndividualArgument) secondArg), firstArg));
			else
				body.add(new SWRLClassAtomImpl(Srd.buildNominal((SWRLIndividualArgument) firstArg), secondArg));
		}

		Set<SWRLSameIndividualAtom> sameIndHeadAtoms = Srd.getSameIndAtoms(head);
		for (SWRLSameIndividualAtom sameIndHeadAtom : sameIndHeadAtoms) {
			SWRLIArgument firstArg = sameIndHeadAtom.getFirstArgument();
			SWRLIArgument secondArg = sameIndHeadAtom.getSecondArgument();
			if (firstArg instanceof SWRLIndividualArgument) {
				head.remove(sameIndHeadAtom);
				head.add(new SWRLClassAtomImpl(Srd.buildNominal((SWRLIndividualArgument) firstArg), secondArg));
			} else if (secondArg instanceof SWRLIndividualArgument) {
				head.remove(sameIndHeadAtom);
				head.add(new SWRLClassAtomImpl(Srd.buildNominal((SWRLIndividualArgument) secondArg), firstArg));
			}
		}
	}

	private static void replaceWith(SWRLIArgument argToReplace, SWRLIArgument replacerArg, Set<SWRLAtom> atoms) {
		Set<SWRLAtom> atomsWithArgToReplace = Srd.getAtomsWithArg(atoms, argToReplace);
		atoms.removeAll(atomsWithArgToReplace);
		for (SWRLAtom atomWithFirstArg : atomsWithArgToReplace) {
			SWRLAtom replacedAtom = null;
			if (atomWithFirstArg instanceof SWRLClassAtom) {
				replacedAtom = new SWRLClassAtomImpl(((SWRLClassAtom) atomWithFirstArg).getPredicate(), replacerArg);
			} else if (atomWithFirstArg instanceof SWRLObjectPropertyAtom) {
				SWRLObjectPropertyAtom objPropAtom = (SWRLObjectPropertyAtom) atomWithFirstArg;
				if (objPropAtom.getFirstArgument().equals(argToReplace) && !objPropAtom.getSecondArgument().equals(argToReplace))
					replacedAtom = new SWRLObjectPropertyAtomImpl(objPropAtom.getPredicate(), replacerArg, objPropAtom.getSecondArgument());
				else if (!objPropAtom.getFirstArgument().equals(argToReplace) && objPropAtom.getSecondArgument().equals(argToReplace))
					replacedAtom = new SWRLObjectPropertyAtomImpl(objPropAtom.getPredicate(), objPropAtom.getFirstArgument(), replacerArg);
			} else if (atomWithFirstArg instanceof SWRLDataPropertyAtom) {
				SWRLDataPropertyAtom dataPropAtom = (SWRLDataPropertyAtom) atomWithFirstArg;
				replacedAtom = new SWRLDataPropertyAtomImpl(dataPropAtom.getPredicate(), replacerArg, dataPropAtom.getSecondArgument());
			} else if (atomWithFirstArg instanceof SWRLSameIndividualAtom) {
				SWRLSameIndividualAtom sameIndAtom = (SWRLSameIndividualAtom) atomWithFirstArg;
				if (sameIndAtom.getFirstArgument().equals(argToReplace) && !sameIndAtom.getSecondArgument().equals(argToReplace))
					replacedAtom = new SWRLSameIndividualAtomImpl(Srd.factory.getOWLTopObjectProperty(), replacerArg, sameIndAtom.getSecondArgument());
				else if (!sameIndAtom.getFirstArgument().equals(argToReplace) && sameIndAtom.getSecondArgument().equals(argToReplace))
					replacedAtom = new SWRLSameIndividualAtomImpl(Srd.factory.getOWLTopObjectProperty(), sameIndAtom.getFirstArgument(), replacerArg);
			}
			atoms.add(replacedAtom);
		}
	}

	private int returnHeadType(Set<SWRLAtom> headAtoms) {

		if (headAtoms.size() == 1) {
			SWRLAtom headAtom = headAtoms.iterator().next();
			if (headAtom instanceof SWRLObjectPropertyAtom) {
				SWRLObjectPropertyAtom objPropHeadAtom = (SWRLObjectPropertyAtom) headAtom;
				if (objPropHeadAtom.getFirstArgument() instanceof SWRLVariable && objPropHeadAtom.getSecondArgument() instanceof SWRLVariable)
					return 2;
				else
					return 1;
			} else if (headAtom instanceof SWRLDataPropertyAtom) {
				SWRLDataPropertyAtom dataPropAtom = (SWRLDataPropertyAtom) headAtom;
				if (!dataPropAtom.getFirstArgument().equals(dataPropAtom.getSecondArgument()))
					return 3;
			} else if (headAtom instanceof SWRLSameIndividualAtom) {
				SWRLSameIndividualAtom sameIndAtom = (SWRLSameIndividualAtom) headAtom;
				if (!sameIndAtom.getFirstArgument().equals(sameIndAtom.getSecondArgument()))
					return 4;
			}
		}

		return 1;
	}

}

//	private boolean updateAxioms(Set<OWLAxiom> newAxioms) {
//		if (newAxioms == null) {
//			axioms.clear();
//			axioms.add(rule);
//			System.out.println("   o The rule cannot be translated" + "\n\n");
//			return true;
//		}
//
//		for (OWLAxiom newAxiom : newAxioms)
//			System.out.println("   o " + S.shortStr(newAxiom.toString()));
//
//		axioms.addAll(newAxioms);
//		return false;
//	}
//	private static Set<SWRLAtom> initializeBody(SWRLVariable root, SWRLVariable root2, Set<SWRLAtom> bodyAtomsWODataProp) {
//		Set<SWRLAtom> bodySplit = new HashSet<SWRLAtom>();
//		bodySplit.addAll(bodyAtomsWODataProp);
//		if (!S.getAllVarsToSet(bodySplit).contains(root))
//			bodySplit.add(new SWRLClassAtomImpl(S.factory.getOWLThing(), root));
//		if (!S.getAllVarsToSet(bodySplit).contains(root2))
//			bodySplit.add(new SWRLClassAtomImpl(S.factory.getOWLThing(), root2));
//		return bodySplit;
//	}
//
//	private static Set<SWRLAtom> initializeBody(SWRLVariable root, Set<SWRLAtom> bodyAtomsWODataProp) {
//		Set<SWRLAtom> bodySplit = new HashSet<SWRLAtom>();
//		bodySplit.addAll(bodyAtomsWODataProp);
//		if (!S.getAllVarsToSet(bodySplit).contains(root))
//			bodySplit.add(new SWRLClassAtomImpl(S.factory.getOWLThing(), root));
//		return bodySplit;
//	}

//		// 2. Normalize away literals in the body and head
//		Set<SWRLAtom> bodyAtomsWOLiterals = BDataPropNormalizer.rollUpLiterals(rule.getBody());
//		Set<SWRLAtom> headAtomsWOLiterals = BDataPropNormalizer.rollUpLiterals(rule.getHead());

//		for (SWRLAtom headAtom : headAtoms)
//			if (S.isDataPropAtomWithIndAndVar(headAtom)) {
//				System.out.println("Cannot Transform: The head of the rule contains an atom of the form S(a, y)." + "\n" + " -> " + rule + "\n");
//				return false;
//			}

//		for (SWRLAtom atom : bodyAtoms)
//			for (SWRLAtom atom2 : bodyAtoms)
//				if (!atom.equals(atom2))
//					if (S.isDataPropAtomWithVarOnSecond(atom) && S.isDataPropAtomWithVarOnSecond(atom2))
//						if (((SWRLDataPropertyAtom) atom).getSecondArgument().equals(((SWRLDataPropertyAtom) atom2).getSecondArgument())) {
//							System.out.println("Cannot Transform: The body of the rule contains two atoms of the form S(x, y) and S(w, y)." + "\n"
//									+ " -> " + rule + "\n");
//							return false;
//						}