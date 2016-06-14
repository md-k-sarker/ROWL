package edu.wsu.dase.model.ruletoaxiom;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLVariable;

public class HeadSplitter {

	protected static Set<Set<SWRLAtom>> splitHead(Set<SWRLAtom> head) {
		Set<Set<SWRLAtom>> headSplits = new HashSet<Set<SWRLAtom>>();

		Set<SWRLAtom> atomsWith1Var = new HashSet<SWRLAtom>();
		Set<SWRLAtom> atomsWith2Var = new HashSet<SWRLAtom>();
		Set<SWRLAtom> atomsWOVar = new HashSet<SWRLAtom>();

		for (SWRLAtom headAtom : head) {
			switch (Srd.getVariableCount(headAtom)) {
			case 0:
				atomsWOVar.add(headAtom);
				break;
			case 1:
				atomsWith1Var.add(headAtom);
				break;
			case 2:
				atomsWith2Var.add(headAtom);
				break;
			default:
				System.out.println("WARNING!!! Illegal headAtom: " + headAtom + "\n");
				break;
			}
		}

		// Splitting atoms containing 2 variables
		for (SWRLAtom atomWith2Var : atomsWith2Var) {
			Set<SWRLAtom> headSplit = new HashSet<SWRLAtom>();
			headSplit.add(atomWith2Var);
			headSplits.add(headSplit);
		}

		// Splitting atoms with 1 variables
		Set<SWRLVariable> variables = Srd.getVariablesToSet(atomsWith1Var);
		for (SWRLVariable variable : variables) {
			Set<SWRLAtom> headSplit = new HashSet<SWRLAtom>();
			for (SWRLAtom atomWith1Var : atomsWith1Var)
				if (Srd.getVariablesToSet(atomWith1Var).contains(variable))
					headSplit.add(atomWith1Var);

			headSplits.add(headSplit);
		}

		// Splitting atoms without variables 
		Set<SWRLAtom> distributedAtomsWOVariables = new HashSet<SWRLAtom>();
		boolean modified = true;
		while (modified) {
			modified = false;
			for (SWRLAtom atomWOVar : atomsWOVar)
				for (Set<SWRLAtom> headSplit : headSplits)
					for (SWRLArgument argOfAtomWOVar : atomWOVar.getAllArguments())
						if (Srd.getArgumentsToSet(headSplit).contains(argOfAtomWOVar))
							if (!distributedAtomsWOVariables.contains(atomWOVar)) {
								modified = true;
								headSplit.add(atomWOVar);
								distributedAtomsWOVariables.add(atomWOVar);
							}
		}
		atomsWOVar.removeAll(distributedAtomsWOVariables);

		while (!atomsWOVar.isEmpty()) {
			Set<SWRLAtom> headSplit = new HashSet<SWRLAtom>();
			SWRLAtom randomAtom = atomsWOVar.iterator().next();
			headSplit.add(randomAtom);
			boolean modified2 = true;
			while (modified2) {
				modified2 = false;
				for (SWRLAtom atomWOVar : atomsWOVar) {
					Set<SWRLIArgument> headSplitArgs = Srd.getArgumentsToSet(headSplit);
					for (SWRLArgument undistributedAtomArg : atomWOVar.getAllArguments())
						if (headSplitArgs.contains(undistributedAtomArg))
							modified2 = headSplit.add(atomWOVar);
				}
			}
			atomsWOVar.removeAll(headSplit);
			headSplits.add(headSplit);
		}

		return headSplits;
	}

}
