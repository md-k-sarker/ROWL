package edu.wsu.dase.model.ruletoaxiom;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLVariable;

import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl;

public class Remover {

	public static OWLDataFactory factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();

	public static Set<OWLAxiom> removeConstantsFromHead(Set<SWRLAtom> head) {

		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		Set<SWRLVariable> variables = Srd.getVariablesToSet(head);

		for (SWRLVariable variable : variables) {
			Set<SWRLAtom> varToConstAtoms = getVarToConstantAtoms(variable, head);
			for (SWRLAtom vatToConstAtom : varToConstAtoms) {
				SWRLIArgument rootConstant = Srd.getConstantsToSet(vatToConstAtom).iterator().next();
				Set<SWRLAtom> constantAtoms = getLinkedConstantAtoms(head, rootConstant);
				head.removeAll(constantAtoms);
				head.remove(vatToConstAtom);
				constantAtoms.add(vatToConstAtom);
				OWLClassExpression freshClass = Srd.createFreshUniqueClass();
				head.add(factory.getSWRLClassAtom(freshClass, variable));
				axioms.add(new OWLSubClassOfAxiomImpl(freshClass, ClassExpBuilder.atomsToExp(constantAtoms, variable), new HashSet<OWLAnnotation>()));
			}
		}

		return axioms;
	}

	public static Set<OWLAxiom> removeConstantsFromBody(Set<SWRLAtom> body) {
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		Set<SWRLVariable> variables = Srd.getVariablesToSet(body);

		for (SWRLVariable variable : variables) {
			Set<SWRLAtom> varToConstAtoms = getVarToConstantAtoms(variable, body);

			for (SWRLAtom vatToConstAtom : varToConstAtoms) {
				SWRLIArgument rootConstant = Srd.getConstantsToSet(vatToConstAtom).iterator().next();
				Set<SWRLAtom> constantAtoms = getLinkedConstantAtoms(body, rootConstant);
				constantAtoms.add(vatToConstAtom);
				OWLClassExpression freshClass = Srd.createFreshUniqueClass();
				body.add(factory.getSWRLClassAtom(freshClass, variable));
				axioms.add(new OWLSubClassOfAxiomImpl(ClassExpBuilder.atomsToExp(constantAtoms, variable), freshClass, new HashSet<OWLAnnotation>()));
			}
		}

		HashSet<SWRLAtom> auxAtoms = new HashSet<SWRLAtom>(body);
		for (SWRLAtom auxAtom : auxAtoms)
			if (!Srd.getConstantsToSet(auxAtom).isEmpty())
				body.remove(auxAtom);

		return axioms;
	}

	private static Set<SWRLAtom> getVarToConstantAtoms(SWRLVariable variable, Set<SWRLAtom> atoms) {
		Set<SWRLAtom> varToConstAtoms = new HashSet<SWRLAtom>();
		for (SWRLAtom atom : atoms)
			if (Srd.getVariablesToSet(atom).contains(variable))
				if (Srd.getConstantsToSet(atom).size() == 1)
					varToConstAtoms.add(atom);
		return varToConstAtoms;
	}

	private static Set<SWRLAtom> getLinkedConstantAtoms(Set<SWRLAtom> atoms, SWRLIArgument rootConstant) {

		Set<SWRLAtom> linkedAtoms = new HashSet<SWRLAtom>();
		Set<SWRLIArgument> linkedConstants = new HashSet<SWRLIArgument>();
		linkedConstants.add(rootConstant);

		boolean modified = true;
		while (modified) {
			modified = false;
			for (SWRLAtom atom : atoms)
				if (Srd.getVariablesToSet(atom).isEmpty()) {
					for (SWRLIArgument constant : Srd.getArgumentsToSet(atom))
						if (linkedConstants.contains(constant)) {
							linkedAtoms.add(atom);
							linkedConstants.addAll(Srd.getArgumentsToSet(atom));
						}
				}
		}

		return linkedAtoms;
	}
}
