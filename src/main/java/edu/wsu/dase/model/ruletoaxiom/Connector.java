package edu.wsu.dase.model.ruletoaxiom;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;
import org.semanticweb.owlapi.model.SWRLVariable;

import uk.ac.manchester.cs.owl.owlapi.SWRLObjectPropertyAtomImpl;

public class Connector {

	public static boolean isConnected(Set<SWRLAtom> atoms) {
		if (atoms.isEmpty() || Srd.getArgsToSet(atoms).isEmpty())
			return true;
		return computeUnreachableArgsFrom(null, atoms).isEmpty();
	}

	public static void connect(SWRLIArgument root1, Set<SWRLAtom> atoms) {
		Set<SWRLArgument> unreachableArgs;
		do {
			unreachableArgs = computeUnreachableArgsFrom(root1, atoms);
			if (!unreachableArgs.isEmpty()) {
				SWRLArgument connectToArg = chooseAppropriateArgToConnectTo(atoms, unreachableArgs);
				atoms.add(new SWRLObjectPropertyAtomImpl(Srd.factory.getOWLTopObjectProperty(), root1, (SWRLIArgument) connectToArg));
			}

		} while (!unreachableArgs.isEmpty());
	}

	private static Set<SWRLArgument> computeUnreachableArgsFrom(SWRLIArgument root, Set<SWRLAtom> atoms) {
		Set<SWRLArgument> reachableArgs = new HashSet<SWRLArgument>();
		if (root != null)
			reachableArgs.add(root);
		else
			reachableArgs.add(Srd.getArgsToSet(atoms).iterator().next());
		boolean modified = true;
		while (modified) {
			modified = false;
			for (SWRLAtom atom : atoms)
				for (SWRLArgument arg : atom.getAllArguments())
					if (reachableArgs.contains(arg))
						modified = reachableArgs.addAll(atom.getAllArguments()) || modified;

		}

		Set<SWRLArgument> unreachableArgs = new HashSet<SWRLArgument>();
		for (SWRLArgument arg : Srd.getArgsToSet(atoms))
			if (!reachableArgs.contains(arg))
				unreachableArgs.add(arg);

		return unreachableArgs;

	}

	private static SWRLArgument chooseAppropriateArgToConnectTo(Set<SWRLAtom> atoms, Set<SWRLArgument> unconnectedArgs) {

		Set<SWRLVariable> objVars = Srd.getObjVarsToSet(atoms);
		for (SWRLArgument unnconnectedArg : unconnectedArgs)
			if (objVars.contains(unnconnectedArg))
				return unnconnectedArg;

		Set<SWRLIndividualArgument> inds = Srd.getIndsToSet(atoms);
		for (SWRLArgument unnconnectedArg : unconnectedArgs)
			if (inds.contains(unnconnectedArg))
				return unnconnectedArg;

		//		Set<SWRLLiteralArgument> lits = S.getAllLitsToSet(atoms);
		//		if (!lits.isEmpty())
		//			return lits.iterator().next();

		return null;
	}

}
