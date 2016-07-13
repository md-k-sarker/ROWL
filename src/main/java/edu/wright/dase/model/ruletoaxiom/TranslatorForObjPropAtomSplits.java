package edu.wright.dase.model.ruletoaxiom;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLVariable;

import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.SWRLClassAtomImpl;

public class TranslatorForObjPropAtomSplits {

	public static Set<OWLAxiom> translate(SWRLVariable root1, SWRLVariable root2, SWRLObjectPropertyAtom objPropAtomHead, Set<SWRLAtom> bodyAtoms) {
		Set<OWLAxiom> resultingAxioms = new HashSet<OWLAxiom>();

		// Initializing Body Split
		Set<SWRLAtom> bodySplitAtoms = new HashSet<SWRLAtom>();
		bodySplitAtoms.addAll(bodyAtoms);
		Set<SWRLVariable> bodySplitVars = Srd.getVarsToSet(bodySplitAtoms);
		if (!bodySplitVars.contains(root1))
			bodySplitAtoms.add(new SWRLClassAtomImpl(Srd.factory.getOWLThing(), root1));
		if (!bodySplitVars.contains(root2))
			bodySplitAtoms.add(new SWRLClassAtomImpl(Srd.factory.getOWLThing(), root2));

		// Preprocess Body
		Connector.connect(root1, bodySplitAtoms);
		RollUp.rollUpIndsAndVars(root1, root2, bodySplitAtoms);
		Connector.connect(root1, bodySplitAtoms);

		SWRLVariable currentVar = root1;
		SWRLVariable nextVar = null;
		ArrayList<OWLObjectPropertyExpression> roleChainExpr = new ArrayList<OWLObjectPropertyExpression>();

		while (currentVar != null) {

			Set<SWRLAtom> removeAtoms = new HashSet<SWRLAtom>();
			Set<OWLClassExpression> classExpressions = new HashSet<OWLClassExpression>();
			OWLObjectPropertyExpression currentObjProp = null;

			for (SWRLAtom bodySplitAtom : bodySplitAtoms) {
				if (bodySplitAtom instanceof SWRLClassAtom) {
					SWRLClassAtom classBodyAtom = (SWRLClassAtom) bodySplitAtom;
					if (classBodyAtom.getArgument().equals(currentVar)) {
						classExpressions.add(classBodyAtom.getPredicate());
						removeAtoms.add(bodySplitAtom);
					}
				}

				if (bodySplitAtom instanceof SWRLObjectPropertyAtom) {
					SWRLObjectPropertyAtom objPropBodyAtom = (SWRLObjectPropertyAtom) bodySplitAtom;
					if (objPropBodyAtom.getFirstArgument().equals(currentVar)) {
						if (currentObjProp == null) {
							currentObjProp = objPropBodyAtom.getPredicate();
							nextVar = (SWRLVariable) objPropBodyAtom.getSecondArgument();
						} else
							return new HashSet<OWLAxiom>();
						removeAtoms.add(objPropBodyAtom);
					}

					if (objPropBodyAtom.getSecondArgument().equals(currentVar)) {
						if (currentObjProp == null) {
							currentObjProp = Srd.invert(objPropBodyAtom.getPredicate());
							nextVar = (SWRLVariable) objPropBodyAtom.getFirstArgument();
						} else
							return new HashSet<OWLAxiom>();
						removeAtoms.add(objPropBodyAtom);
					}
				}
			}

			if (!classExpressions.isEmpty()) {
				OWLObjectProperty freshRole = Srd.factory.getOWLObjectProperty(IRI.create("freshProp" + ++Srd.freshCounter));
				if (classExpressions.size() == 1)
					resultingAxioms.add(
							new OWLSubClassOfAxiomImpl(classExpressions.iterator().next(), Srd.factory.getOWLObjectHasSelf(freshRole), new HashSet<OWLAnnotation>()));
				else
					resultingAxioms.add(new OWLSubClassOfAxiomImpl(Srd.factory.getOWLObjectIntersectionOf(classExpressions), Srd.factory.getOWLObjectHasSelf(freshRole),
							new HashSet<OWLAnnotation>()));
				roleChainExpr.add(freshRole);
			}

			if (currentObjProp != null)
				roleChainExpr.add(currentObjProp);

			bodySplitAtoms.removeAll(removeAtoms);

			currentVar = nextVar;
			nextVar = null;
		}

		if (!bodySplitAtoms.isEmpty())
			return new HashSet<OWLAxiom>();

		if (roleChainExpr.size() == 1)
			resultingAxioms.add(Srd.factory.getOWLSubObjectPropertyOfAxiom(roleChainExpr.get(0), objPropAtomHead.getPredicate()));
		else
			resultingAxioms.add(Srd.factory.getOWLSubPropertyChainOfAxiom(roleChainExpr, objPropAtomHead.getPredicate()));

		return resultingAxioms;
	}

}
