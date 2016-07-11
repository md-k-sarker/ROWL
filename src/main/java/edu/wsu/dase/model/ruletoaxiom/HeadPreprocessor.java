package edu.wsu.dase.model.ruletoaxiom;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLSameIndividualAtom;
import org.semanticweb.owlapi.model.SWRLVariable;

import uk.ac.manchester.cs.owl.owlapi.SWRLClassAtomImpl;

public class HeadPreprocessor {

	public static OWLDataFactory factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();

	public static Set<SWRLRule> computeRuleHeadSplits(SWRLRule rule) {
		Set<SWRLRule> ruleSplits = new HashSet<SWRLRule>();
		Set<SWRLAtom> head = rule.getHead();
		Set<SWRLAtom> body = rule.getBody();

		if (head.isEmpty()) {
			head = new HashSet<SWRLAtom>();
			head.add(new SWRLClassAtomImpl(factory.getOWLNothing(), Srd.chooseRoot(body)));
		}

		for (SWRLObjectPropertyAtom objPropAtom : Srd.getObjPropAtoms(head))
			if (objPropAtom.getFirstArgument() instanceof SWRLVariable && objPropAtom.getSecondArgument() instanceof SWRLVariable
					&& !objPropAtom.getFirstArgument().equals(objPropAtom.getSecondArgument())) {
				ruleSplits.add(factory.getSWRLRule(body, Srd.toSet(objPropAtom)));
				head.remove(objPropAtom);
			}

		for (SWRLDataPropertyAtom dataPropAtom : Srd.getDataPropAtoms(head))
			if (dataPropAtom.getFirstArgument() instanceof SWRLVariable && dataPropAtom.getSecondArgument() instanceof SWRLVariable
					&& !dataPropAtom.getFirstArgument().equals(dataPropAtom.getSecondArgument())) {
				ruleSplits.add(factory.getSWRLRule(body, Srd.toSet(dataPropAtom)));
				head.remove(dataPropAtom);
			}

		Set<SWRLSameIndividualAtom> sameIndHeadAtoms = Srd.getSameIndAtoms(head);
		for (SWRLSameIndividualAtom sameIndHeadAtom : sameIndHeadAtoms)
			if (sameIndHeadAtom.getFirstArgument() instanceof SWRLVariable && sameIndHeadAtom.getSecondArgument() instanceof SWRLVariable
					&& !sameIndHeadAtom.getFirstArgument().equals(sameIndHeadAtom.getSecondArgument())) {
				ruleSplits.add(factory.getSWRLRule(body, Srd.toSet(sameIndHeadAtom)));
				head.remove(sameIndHeadAtom);
			}

		Set<SWRLVariable> headVars = Srd.getVarsToSet(head);
		for (SWRLVariable headVar : headVars) {
			Set<SWRLAtom> varSplit = Srd.getAtomsWithArg(head, headVar);

			boolean modified = true;
			while (modified) {
				modified = false;
				Set<SWRLArgument> splitArgs = Srd.getArgsToSet(varSplit);
				for (SWRLAtom headAtom : head)
					for (SWRLArgument headAtomArg : headAtom.getAllArguments())
						if (splitArgs.contains(headAtomArg) && Srd.getVarsToSet(headAtom).isEmpty())
							modified = varSplit.add(headAtom) || modified;
			}

			if (!varSplit.isEmpty()) {
				ruleSplits.add(factory.getSWRLRule(body, varSplit));
				head.removeAll(varSplit);
			}
		}

		Set<SWRLArgument> headArgs = Srd.getArgsToSet(head);
		for (SWRLArgument arg : headArgs) {
			Set<SWRLAtom> argSplit = Srd.getAtomsWithArg(head, arg);

			boolean modified = true;
			while (modified) {
				modified = false;
				Set<SWRLArgument> splitArgs = Srd.getArgsToSet(argSplit);
				for (SWRLAtom headAtom : head)
					for (SWRLArgument headAtomArg : headAtom.getAllArguments())
						if (splitArgs.contains(headAtomArg))
							modified = argSplit.add(headAtom) || modified;
			}

			if (!argSplit.isEmpty()) {
				ruleSplits.add(factory.getSWRLRule(body, argSplit));
				head.removeAll(argSplit);
			}
		}

		return ruleSplits;
	}

	//		for (SWRLAtom headAtom : headAtoms) {
	//			Set<SWRLVariable> headAtomVars = Srd.getVarsToSet(headAtom);
	//			switch (headAtomVars.size()) {
	//			case 1:
	//				SWRLVariable headVariable = headAtomVars.iterator().next();
	//				if (Srd.isObjVar(headVariable, headAtoms)) {
	//					singleObjVarHeadSplits.putIfAbsent(headVariable, new HashSet<SWRLAtom>());
	//					singleObjVarHeadSplits.get(headVariable).add(headAtom);
	//				} else {
	//					singleDataVarHeadSplits.putIfAbsent(headVariable, new HashSet<SWRLAtom>());
	//					singleDataVarHeadSplits.get(headVariable).add(headAtom);
	//				}
	//				break;
	//			case 2:
	//				if (headAtom instanceof SWRLObjectPropertyAtom)
	//					objPropHeadSplits.add((SWRLObjectPropertyAtom) headAtom);
	//				else if (headAtom instanceof SWRLDataPropertyAtom)
	//					dataPropHeadSplits.add((SWRLDataPropertyAtom) headAtom);
	//				else if (headAtom instanceof SWRLSameIndividualAtom)
	//					sameIndSplits.add((SWRLSameIndividualAtom) headAtom);
	//				break;
	//			}
	//		}
	//
	//		headAtoms.removeAll(objPropHeadSplits);
	//		headAtoms.removeAll(dataPropHeadSplits);
	//		for (Set<SWRLAtom> singleVarHeadSplit : singleObjVarHeadSplits.values())
	//			headAtoms.removeAll(singleVarHeadSplit);
	//
	//		boolean modified = true;
	//		while (modified) {
	//			modified = false;
	//
	//			for (SWRLAtom unnasignedHeadAtom : headAtoms) {
	//				boolean assigned = false;
	//				for (Set<SWRLAtom> singleVarHeadSplit : singleObjVarHeadSplits.values())
	//					for (SWRLArgument argument : unnasignedHeadAtom.getAllArguments())
	//						if (Srd.getArgsToSet(singleObjVarHeadSplits.get(singleVarHeadSplit)).contains(argument) && !assigned) {
	//							assigned = true;
	//							modified = singleVarHeadSplit.add(unnasignedHeadAtom) || modified;
	//						}
	//			}
	//
	//			for (Set<SWRLAtom> singleVarHeadSplit : singleObjVarHeadSplits.values())
	//				headAtoms.removeAll(singleVarHeadSplit);
	//		}
	//
	//		if (!headAtoms.isEmpty())
	//			if (singleObjVarHeadSplits.get(choosenBodyRoot) != null)
	//				singleObjVarHeadSplits.get(choosenBodyRoot).addAll(headAtoms);
	//			else
	//				singleObjVarHeadSplits.put(choosenBodyRoot, headAtoms);

}
