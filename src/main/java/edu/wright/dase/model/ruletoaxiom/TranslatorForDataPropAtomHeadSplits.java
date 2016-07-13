package edu.wright.dase.model.ruletoaxiom;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;

public class TranslatorForDataPropAtomHeadSplits {

	public static Set<OWLAxiom> translate(SWRLDataPropertyAtom dataPropSplit, Set<SWRLAtom> bodyAtomsWOLiterals) {
		Set<OWLAxiom> newAxioms = new HashSet<OWLAxiom>();

		return newAxioms;
	}

}
