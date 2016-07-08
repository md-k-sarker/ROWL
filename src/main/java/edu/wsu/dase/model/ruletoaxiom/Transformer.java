package edu.wsu.dase.model.ruletoaxiom;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLVariable;

import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLSubObjectPropertyOfAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.SWRLRuleImpl;

public class Transformer {

	public static OWLDataFactory factory = OWLManager.createOWLOntologyManager().getOWLDataFactory();

	public static boolean isTransferred = false;

	public static Set<OWLAxiom> ruleToAxioms(SWRLRule rule) {

		Set<SWRLAtom> body = rule.getBody();
		Set<SWRLAtom> head = rule.getHead();
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();

		if (head.isEmpty()) {
			head = new HashSet<SWRLAtom>();
			SWRLVariable headVariable;
			if (Srd.getVariablesToSet(body).isEmpty())
				headVariable = factory.getSWRLVariable(IRI.create("vF"));
			else
				headVariable = chooseARootVariable(body);
			head.add(factory.getSWRLClassAtom(factory.getOWLNothing(), headVariable));
		}

		Set<Set<SWRLAtom>> headSplits = HeadSplitter.splitHead(head);
		for (Set<SWRLAtom> headSplit : headSplits) {
			// System.out.println(" > " + Srd.toString(headSplit));
			Set<SWRLVariable> headSplitVariables = Srd.getVariablesToSet(headSplit);
			if (headSplitVariables.size() == 0) {
				axioms.addAll(zeroVarsHeadRuleToAxiom(body, headSplit));
				
			} else if (headSplitVariables.size() == 1) {
				axioms.addAll(oneVarHeadRuleToAxiom(body, headSplit));
			
			} else if (headSplitVariables.size() == 2) {
				axioms.addAll(twoVarsHeadRuleToAxioms(body, headSplit));
				
			} else {
				isTransferred = false;
				System.out.println("WARNING!!! headSplit with more than 2 variables at ruleToAxioms.");
			}
		}

		return axioms;
	}

	private static Set<OWLAxiom> zeroVarsHeadRuleToAxiom(Set<SWRLAtom> body, Set<SWRLAtom> head) {

		Set<SWRLVariable> bodyVariables = Srd.getVariablesToSet(body);
		SWRLVariable headVariable;
		if (bodyVariables.isEmpty())
			headVariable = factory.getSWRLVariable(IRI.create("vF"));
		else
			headVariable = chooseARootVariable(body);

		head.add(factory.getSWRLClassAtom(factory.getOWLThing(), headVariable));
		return oneVarHeadRuleToAxiom(body, head);
	}

	private static Set<OWLAxiom> oneVarHeadRuleToAxiom(Set<SWRLAtom> body, Set<SWRLAtom> head) {

		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		SWRLVariable rootVariable = Srd.getHeadVariable(head);

		if (!Srd.getVariablesToSet(body).contains(rootVariable)) {
			if (body.isEmpty())
				body = new HashSet<SWRLAtom>();
			body.add(factory.getSWRLClassAtom(factory.getOWLThing(), rootVariable));
		}

		XGraph bodyGraph = new XGraph(body);
		while (!bodyGraph.returnUnreachableArguments(rootVariable).isEmpty())
			addConnection(body, bodyGraph, rootVariable);

		XGraph headGraph = new XGraph(head);
		while (!headGraph.returnUnreachableArguments(rootVariable).isEmpty())
			addConnection(head, headGraph, rootVariable);

		if (bodyGraph.containsCycleOverVariables()) {
			System.out.println("  * Cannot transform part of input rule: The shape of the body is not acyclic.");
			// cycle detected. rule can not be transformed to OWL. Can be
			// converted to SWRL
			isTransferred = false;
			axioms.add(new SWRLRuleImpl(body, head, new HashSet<OWLAnnotation>()));
			return axioms;
		}
		
		axioms.addAll(Remover.removeConstantsFromHead(head));
		axioms.addAll(Remover.removeConstantsFromBody(body));
		
		//System.out.println("in oneVarHeadRuleToAxiom before OWLSubClassOfAxiomImpl");
		for (SWRLAtom b : body) {
			//System.out.println("SWRL atom " + b);
		}
		
		axioms.add(new OWLSubClassOfAxiomImpl(ClassExpBuilder.atomsToExp(body, rootVariable), ClassExpBuilder.atomsToExp(head, rootVariable), new HashSet<OWLAnnotation>()));

		//System.out.println("in oneVarHeadRuleToAxiom after OWLSubClassOfAxiomImpl");
		for (OWLAxiom ax : axioms) {
			//System.out.println("OWL Axiom " + ax);
		}
		isTransferred = true;
		return axioms;
	}

	private static Set<OWLAxiom> twoVarsHeadRuleToAxioms(Set<SWRLAtom> body, Set<SWRLAtom> head) {
		// Head contains exactly a binary head atom with two different variables

		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		SWRLAtom binaryHeadAtom = head.iterator().next();
		ArrayList<SWRLVariable> headVariables = Srd.getVariablesToArrayList(binaryHeadAtom);
		SWRLVariable rootVariable0 = headVariables.get(0);
		SWRLVariable rootVariable1 = headVariables.get(1);

		if (body.isEmpty()) {
			axioms.add(new OWLSubObjectPropertyOfAxiomImpl(factory.getOWLTopObjectProperty(),
					factory.getOWLObjectProperty(IRI.create(binaryHeadAtom.getPredicate().toString().replace("<", "").replace(">", ""))), new HashSet<OWLAnnotation>()));
			
			isTransferred = true;
			
			return axioms;
		}

		Set<SWRLVariable> bodyVariables = Srd.getVariablesToSet(body);
		if (!bodyVariables.contains(rootVariable0))
			body.add(factory.getSWRLClassAtom(factory.getOWLThing(), rootVariable0));
		bodyVariables.add(rootVariable0);
		if (!bodyVariables.contains(rootVariable1))
			body.add(factory.getSWRLClassAtom(factory.getOWLThing(), rootVariable1));
		bodyVariables.add(rootVariable1);

		XGraph bodyGraph = new XGraph(body);
		while (!bodyGraph.returnUnreachableArguments(rootVariable0).isEmpty())
			addConnection(body, bodyGraph, rootVariable0);

		if (bodyGraph.containsCycleOverVariables()) {
			System.out.println("  * Cannot transform part of input rule: The shape of the body is not acyclic.");
			isTransferred = false;
			axioms.add(new SWRLRuleImpl(body, head, new HashSet<OWLAnnotation>()));
			return axioms;
		}

		System.out.println("in oneVarHeadRuleToAxiom");
		for (OWLAxiom ax : axioms) {
			System.out.println("SWRL atom " + ax);
		}
		
		isTransferred = true;
		return axioms;
	}

	private static SWRLVariable chooseARootVariable(Set<SWRLAtom> body) {
		Set<SWRLVariable> nonRootVariables = new HashSet<SWRLVariable>();
		for (SWRLAtom bodyAtom : body) {
			ArrayList<SWRLVariable> atomVariables = Srd.getVariablesToArrayList(bodyAtom);
			if (atomVariables.size() == 2)
				if (atomVariables.get(0).toString().equals(atomVariables.get(1).toString()))
					nonRootVariables.add(atomVariables.get(1));
		}

		Set<SWRLVariable> variables = Srd.getVariablesToSet(body);
		for (SWRLVariable variable : variables)
			if (!nonRootVariables.contains(variable))
				return variable;

		return variables.iterator().next();
	}

	private static void addConnection(Set<SWRLAtom> atoms, XGraph graph, SWRLVariable rootVariable) {
		Set<SWRLArgument> inconvenientVertices = new HashSet<SWRLArgument>();
		for (SWRLAtom atom : atoms) {
			ArrayList<SWRLIArgument> arguments = Srd.getArgumentsToArrayList(atom);
			if (arguments.size() == 2)
				inconvenientVertices.add(arguments.get(1));
		}

		Set<SWRLIArgument> unreachableVertices = graph.returnUnreachableArguments(rootVariable);
		SWRLIArgument vertexToConnect = null;
		for (SWRLIArgument unreachableVertex : unreachableVertices)
			if (!inconvenientVertices.contains(unreachableVertex))
				vertexToConnect = unreachableVertex;
		if (vertexToConnect == null)
			vertexToConnect = unreachableVertices.iterator().next();

		atoms.add(factory.getSWRLObjectPropertyAtom(factory.getOWLTopObjectProperty(), rootVariable, (SWRLIArgument) vertexToConnect));
		SWRLIArgument[] edge1 = { rootVariable, (SWRLIArgument) vertexToConnect };
		graph.edges.add(edge1);
		SWRLIArgument[] edge2 = { (SWRLIArgument) vertexToConnect, rootVariable };
		graph.edges.add(edge2);
	}

}
