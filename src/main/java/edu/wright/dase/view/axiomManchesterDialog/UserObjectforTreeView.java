package edu.wright.dase.view.axiomManchesterDialog;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;

import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxPrefixNameShortFormProvider;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserObjectforTreeView {
	
	private static final long serialVersionUID = -4194122328392241790L;
	private static final Logger log = LoggerFactory.getLogger(UserObjectforTreeView.class);

	private boolean isAxiom;
	private OWLAxiom axiom;
	private String lblVal;
	static OWLOntology activeOntology;
	Component parent;

	public String getLblVal() {
		return lblVal;
	}

	public void setLblVal(String lblVal) {
		this.lblVal = lblVal;
	}

	static ManchesterOWLSyntaxOWLObjectRendererImpl rendering = new ManchesterOWLSyntaxOWLObjectRendererImpl();

	//setting the activeOntology and parent
	public UserObjectforTreeView(Component parent, OWLOntology activeO) {
		activeOntology = activeO;
		this.parent = parent;
	}

	public UserObjectforTreeView(boolean isAxiom, String lblVal) {

		ManchesterOWLSyntaxPrefixNameShortFormProvider shortFormProvider = new ManchesterOWLSyntaxPrefixNameShortFormProvider(
				activeOntology);

		rendering.setShortFormProvider(shortFormProvider);

		if (!isAxiom) {
			this.isAxiom = false;
			this.lblVal = lblVal;
		}
	}

	static ArrayList<String> boldFaceText = new ArrayList<String>(
			Arrays.asList("disjointwith", "min", "max", "some", "only", "subclassof", "inverse", "or", "and",
					"equivalentto", "self", "value", "not", "inverseof", "subpropertyof", "exactly"));

	public UserObjectforTreeView(boolean isAxiom, OWLAxiom axiom) {
		if (isAxiom) {
			this.isAxiom = true;
			this.axiom = axiom;
		}
	}

	
	/**
	 * Note that the first occurrence is found by n = 0. Second occurrence by n = 1 and so on.
	 * @param str
	 * @param s
	 * @param n
	 * @return
	 */
	
	public  int ordinalIndexOf(String fullText, String matchingText, int n) {
	    int pos = fullText.indexOf(matchingText, 0);
	    while (n-- > 0 && pos != -1)
	        pos = fullText.indexOf(matchingText, pos+1);
	    return pos;
	}
	
	private String getCosmetics(String fullAxiomAsString) {

		fullAxiomAsString = fullAxiomAsString.replace("  ", " ");
		String fullAxiomAsFormattedString = " ";
		
		String[] values = fullAxiomAsString.split(" ");
		//System.out.println("-------------");

		for (String eachToken : values) {
			//System.out.println(eachToken);
			if (boldFaceText.contains(eachToken.toLowerCase())) {
				//fullAxiomAsString = fullAxiomAsString.replace(eachToken, "<b style=\"color:#F09128;\">" + eachToken + "</b>");
				fullAxiomAsFormattedString  += "<b style=\"color:#F09128;\">" + eachToken + "</b>"+" ";
			}else{
				fullAxiomAsFormattedString += eachToken + " ";
			}
		}
		//System.out.println("-------------");

		return "<html>" + fullAxiomAsFormattedString + "</html>";
	}
	
	 
	@Override
	public String toString() {

		String value = "";

		if (this.isAxiom) {

			String tmpValue = rendering.render(this.axiom);
			tmpValue = tmpValue.replace("<", "&lt;");
			value = getCosmetics(tmpValue);
		} else {
			value = lblVal.toString();
			value = "<html><b style=\"color:#624FDB;\">" + value + "</b></html>";
		}

		return value;
	}

	public boolean isAxiom() {
		return isAxiom;
	}

	public void setIsAxiom(boolean isAxiom) {
		this.isAxiom = isAxiom;
	}

	public OWLAxiom getAxiom() {
		return axiom;
	}

	public void setAxiom(OWLAxiom axiom) {
		this.axiom = axiom;
	}


}
