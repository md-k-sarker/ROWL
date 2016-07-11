package edu.wsu.dase.model.ruletoaxiom;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLVariable;

import uk.ac.manchester.cs.owl.owlapi.SWRLClassAtomImpl;

public class TranslatorFor1ObjVarHeadSplits {

	public static Set<OWLAxiom> translate(SWRLVariable root, Set<SWRLAtom> head, Set<SWRLAtom> body) {
		Set<OWLAxiom> resultingAxioms = new HashSet<OWLAxiom>();

		// Initializing Body and Head Splits
		Set<SWRLAtom> bodySplit = new HashSet<SWRLAtom>();
		bodySplit.addAll(body);

		// Updating root variable
		if (!Srd.getVarsToSet(bodySplit).contains(root))
			bodySplit.add(new SWRLClassAtomImpl(Srd.factory.getOWLThing(), root));

		// Roll up Head
		RollUp.rollUpLiterals(root, head);
		Connector.connect(root, head);
		RollUp.rollUpIndsAndVars(root, head);

		// Connecting body
		Connector.connect(root, bodySplit);
		RollUp.rollUpLiterals(root, bodySplit);
		Connector.connect(root, bodySplit);
		RollUp.rollUpIndsAndVars(root, bodySplit);

		// Construct Axiom
		OWLClassExpression subClass = attemptToBuildClassExpressionFromConjunction(bodySplit, root);
		OWLClassExpression superClass = attemptToBuildClassExpressionFromConjunction(head, root);

		if (subClass != null && superClass != null)
			resultingAxioms.add(Srd.factory.getOWLSubClassOfAxiom(subClass, superClass));
		return resultingAxioms;
	}

	private static OWLClassExpression attemptToBuildClassExpressionFromConjunction(Set<SWRLAtom> atoms, SWRLVariable root) {

		Set<SWRLClassAtom> classAtoms = new HashSet<SWRLClassAtom>();
		for (SWRLAtom atom : atoms)
			if (atom instanceof SWRLClassAtom)
				classAtoms.add((SWRLClassAtom) atom);
			else
				return null;

		Set<OWLClassExpression> conjuncts = new HashSet<OWLClassExpression>();
		for (SWRLClassAtom classAtom : classAtoms)
			if (classAtom.getArgument().equals(root))
				conjuncts.add(classAtom.getPredicate());
			else
				return null;

		if (conjuncts.size() == 1)
			return conjuncts.iterator().next();
		else
			return Srd.factory.getOWLObjectIntersectionOf(conjuncts);
	}

}
