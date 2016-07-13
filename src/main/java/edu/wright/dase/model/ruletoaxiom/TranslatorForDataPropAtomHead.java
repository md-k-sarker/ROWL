package edu.wright.dase.model.ruletoaxiom;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLVariable;

public class TranslatorForDataPropAtomHead {

	public static Set<OWLAxiom> translate(SWRLVariable root, SWRLVariable root2, SWRLDataPropertyAtom headDataPropAtom, Set<SWRLAtom> bodyAtoms) {
		if (bodyAtoms.size() != 1)
			return null;

		SWRLAtom bodyAtom = bodyAtoms.iterator().next();
		if (!(bodyAtom instanceof SWRLDataPropertyAtom))
			return null;

		SWRLDataPropertyAtom bodyDataPropAtom = (SWRLDataPropertyAtom) bodyAtom;
		if (!bodyDataPropAtom.getFirstArgument().equals(root) || !bodyDataPropAtom.getSecondArgument().equals(root2))
			return null;

		Set<OWLAxiom> resultingAxioms = new HashSet<OWLAxiom>();
		resultingAxioms.add(Srd.factory.getOWLSubDataPropertyOfAxiom(bodyDataPropAtom.getPredicate(), headDataPropAtom.getPredicate()));
		return resultingAxioms;
	}

}
