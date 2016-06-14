package edu.wsu.dase.model.ruletoaxiom;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLVariable;

public class XGraph {

	Set<SWRLIArgument> vertices = new HashSet<SWRLIArgument>();
	Set<SWRLIArgument[]> edges = new HashSet<SWRLIArgument[]>();

	public XGraph(Set<SWRLAtom> atoms) {
		for (SWRLAtom atom : atoms) {
			ArrayList<SWRLIArgument> arguments = new ArrayList<SWRLIArgument>();
			Iterator<SWRLArgument> argsIterator = atom.getAllArguments().iterator();
			while (argsIterator.hasNext()) {
				SWRLIArgument argument = (SWRLIArgument) argsIterator.next();
				arguments.add(argument);
			}

			vertices.addAll(arguments);

			if (arguments.size() == 2) {
				SWRLIArgument arg0 = arguments.get(0);
				SWRLIArgument arg1 = arguments.get(1);
				if (!arg0.equals(arg1)) {
					SWRLIArgument[] edge1 = { arg0, arg1 };
					edges.add(edge1);
					SWRLIArgument[] edge2 = { arg1, arg0 };
					edges.add(edge2);
				}
			}
		}
	}

	public boolean containsCycleOverVariables() {

		Set<SWRLIArgument> variableVertices = new HashSet<SWRLIArgument>();
		for (SWRLIArgument vertex : vertices)
			if (vertex.toString().contains("Variable"))
				variableVertices.add(vertex);

		Set<SWRLIArgument[]> variableEdges = new HashSet<SWRLIArgument[]>();
		for (SWRLIArgument[] edge : edges)
			if (edge[0].toString().contains("Variable") && edge[1].toString().contains("Variable"))
				variableEdges.add(edge);

		return (variableEdges.size() / 2) >= variableVertices.size();
	}

	public Set<SWRLIArgument> returnUnreachableArguments(SWRLVariable rootVariable) {
		Set<SWRLIArgument> visitedVertices = new HashSet<SWRLIArgument>();
		visitedVertices.add(rootVariable);

		int visitedVariablesCount = 0;
		do {
			visitedVariablesCount = visitedVertices.size();
			for (SWRLIArgument[] edge : edges)
				if (visitedVertices.contains(edge[0]))
					visitedVertices.add(edge[1]);
		} while (visitedVertices.size() != visitedVariablesCount);

		Set<SWRLIArgument> unreachableVariables = new HashSet<SWRLIArgument>();
		for (SWRLIArgument vertex : vertices)
			if (!visitedVertices.contains(vertex))
				unreachableVariables.add(vertex);

		return unreachableVariables;
	}
}

//	public boolean isConnected() {
//		Set<SWRLVariable> visitedVertices = new HashSet<SWRLVariable>();
//		visitedVertices.add(vertices.iterator().next());
//
//		int visitedVariablesCount = 0;
//		do {
//			visitedVariablesCount = visitedVertices.size();
//
//			for (SWRLVariable[] edge : edges) {
//				if (visitedVertices.contains(edge[0]))
//					visitedVertices.add(edge[1]);
//			}
//
//		} while (visitedVertices.size() != visitedVariablesCount);
//
//		return visitedVertices.size() == vertices.size();
//	}