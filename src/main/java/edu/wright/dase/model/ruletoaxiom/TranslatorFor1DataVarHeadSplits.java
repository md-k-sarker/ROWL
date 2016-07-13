package edu.wright.dase.model.ruletoaxiom;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLDataRangeAtom;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLVariable;

import uk.ac.manchester.cs.owl.owlapi.SWRLDataPropertyAtomImpl;

public class TranslatorFor1DataVarHeadSplits {

	public static Set<OWLAxiom> translate(SWRLVariable root2, Set<SWRLAtom> head, Set<SWRLAtom> body) {
		Set<OWLAxiom> resultingAxioms = new HashSet<OWLAxiom>();
		// A sqs forall R.B

		// B
		Set<OWLDataRange> fillerBConjuncts = new HashSet<OWLDataRange>();
		for (SWRLAtom headAtom : head)
			if (headAtom instanceof SWRLDataRangeAtom)
				fillerBConjuncts.add(((SWRLDataRangeAtom) headAtom).getPredicate());
			else
				return new HashSet<OWLAxiom>();
		OWLDataRange fillerB = Srd.buildDataRangeIntersection(fillerBConjuncts);

		// A
		SWRLIArgument root = null;

		for (SWRLDataPropertyAtom dataPropBodyAtom : Srd.getDataPropAtoms(body))
			if (dataPropBodyAtom.getSecondArgument().equals(root2))
				root = (SWRLIArgument) dataPropBodyAtom.getFirstArgument();

		if (root == null) {
			root = Srd.chooseRoot(body);
			body.add(new SWRLDataPropertyAtomImpl(Srd.factory.getOWLTopDataProperty(), root, root2));
		}

		Connector.connect(root, body);
		RollUp.rollUpLiterals(root, body);
		Connector.connect(root, body);
		RollUp.rollUpIndsAndVars(root, root2, body);

		Set<OWLClassExpression> subClassConjuncts = new HashSet<OWLClassExpression>();
		OWLDataPropertyExpression rangeProp = null;

		for (SWRLAtom bodySplitAtom : body) {
			if (bodySplitAtom instanceof SWRLClassAtom) {
				SWRLClassAtom classAtom = (SWRLClassAtom) bodySplitAtom;
				if (classAtom.getArgument().equals(root))
					subClassConjuncts.add(classAtom.getPredicate());
				else
					return resultingAxioms;
			} else if (bodySplitAtom instanceof SWRLDataPropertyAtom) {
				SWRLDataPropertyAtom dataPropAtom = (SWRLDataPropertyAtom) bodySplitAtom;
				if (dataPropAtom.getFirstArgument().equals(root) && dataPropAtom.getSecondArgument().equals(root2) && rangeProp == null)
					rangeProp = dataPropAtom.getPredicate();
				else
					return resultingAxioms;
			} else
				return resultingAxioms;
		}

		if (subClassConjuncts.isEmpty())
			subClassConjuncts.add(Srd.factory.getOWLThing());

		if (subClassConjuncts.size() == 1)
			resultingAxioms.add(Srd.factory.getOWLSubClassOfAxiom(subClassConjuncts.iterator().next(), Srd.factory.getOWLDataAllValuesFrom(rangeProp, fillerB)));
		else
			resultingAxioms.add(Srd.factory.getOWLSubClassOfAxiom(Srd.factory.getOWLObjectIntersectionOf(subClassConjuncts),
					Srd.factory.getOWLDataAllValuesFrom(rangeProp, fillerB)));
		return resultingAxioms;
	}

}
