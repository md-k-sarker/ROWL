package edu.wsu.dase.model.ruletoaxiom;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLIArgument;

import uk.ac.manchester.cs.owl.owlapi.OWLObjectHasSelfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectOneOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectSomeValuesFromImpl;

public class ClassExpBuilder {

	private static OWLDataFactory factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();
	private static Set<SWRLAtom> atoms;
	private static Set<SWRLAtom> evaluatedAtoms;

	protected static OWLClassExpression atomsToExp(Set<SWRLAtom> initialAtoms, SWRLArgument root) {
		atoms = new HashSet<SWRLAtom>();
		atoms.addAll(initialAtoms);
		evaluatedAtoms = new HashSet<SWRLAtom>();
		return atomsToExpRecursive(root);
	}

	private static OWLClassExpression atomsToExpRecursive(SWRLArgument root) {
		Set<OWLClassExpression> classExpressionConjuncts = new HashSet<OWLClassExpression>();

		if (!root.toString().contains("Variable")) {
			Set<OWLIndividual> individualSet = new HashSet<OWLIndividual>();
			individualSet.add(Srd.argumentToIndividual(root));
			classExpressionConjuncts.add(new OWLObjectOneOfImpl(individualSet));
		}

		HashSet<SWRLAtom> atomsAux = new HashSet<SWRLAtom>(atoms);
		for (SWRLAtom atom : atomsAux) {
			if (!evaluatedAtoms.contains(atom)) {
				ArrayList<SWRLIArgument> arguments = Srd.getArgumentsToArrayList(atom);
				if (arguments.contains(root)) {
					if (atom.toString().contains("ClassAtom")) {
						classExpressionConjuncts.add(Srd.predicateToClass(atom.getPredicate()));
						evaluatedAtoms.add(atom);
					} else if (atom.toString().contains("ObjectPropertyAtom")) {
						evaluatedAtoms.add(atom);
						OWLObjectPropertyExpression objectRole;
						SWRLArgument arg0;
						SWRLArgument arg1;
						if (arguments.get(0).equals(root)) {
							objectRole = Srd.predicateToRole(atom.getPredicate());
							arg0 = arguments.get(0);
							arg1 = arguments.get(1);
						} else {
							objectRole = Srd.invert(Srd.predicateToRole(atom.getPredicate()));
							arg0 = arguments.get(1);
							arg1 = arguments.get(0);
						}

						if (arg0.equals(arg1))
							classExpressionConjuncts.add(new OWLObjectHasSelfImpl(objectRole));
						else
							classExpressionConjuncts.add(new OWLObjectSomeValuesFromImpl(objectRole, atomsToExpRecursive(arg1)));
					} else {
						System.out.println("WARNING!!! Unimplemented Functionality: Unrecognized Type of Atom" + "\t" + atom);
					}
				}
			}
		}

		if (classExpressionConjuncts.size() == 1)
			return classExpressionConjuncts.iterator().next();
		else if (classExpressionConjuncts.size() > 1)
			return new OWLObjectIntersectionOfImpl(classExpressionConjuncts);
		else
			return factory.getOWLThing();
	}

}
