package edu.wright.dase.view.axiomManchesterDialog;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxPrefixNameShortFormProvider;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import edu.wright.dase.view.RuleEditorPanel;

/*
 * Create and show the dialog of generated axioms.
 */

public class AxiomsDialog extends JDialog {

	private static final long serialVersionUID = 4648172894076113183L;
	private Button integrateBtn;
	private Button cancelBtn;
	final String integrateBtnText = "Integrate";
	final String cancelBtnText = "Cancel";
	final String existingAxiomsLblText = "Existing Axioms";
	final String newAxiomsLblText = "Generated Axioms";
	final String infoText = "    Click Integrate to combine the axioms with active ontology.";

	private JPanel mainPnl;
	private JPanel bottomPnl;
	private JLabel infoLbl;
	private JPanel axiomsPnl;
	private JScrollPane axiomsScroll;
	private final Set<OWLAxiom> selectedAxioms;
	private JFrame parent;
	private boolean isClickedOK;
	private final RuleEditorPanel ruleEditorPanel;
	
	private static ManchesterOWLSyntaxOWLObjectRendererImpl rendering = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	
	static ArrayList<String> boldFaceText = new ArrayList<String>(
			Arrays.asList("disjointwith", "min", "max", "some", "only", "subclassof", "inverse", "or", "and",
					"equivalentto", "self", "value", "not", "inverseof", "subpropertyof", "exactly"));

	public boolean isClickedOK() {
		return this.isClickedOK;
	}

	public void setClickedOK(boolean isClickedOK) {
		this.isClickedOK = isClickedOK;
	}

	public AxiomsDialog(RuleEditorPanel ruleEditorPanel, JFrame parent, OWLOntology activeOntology) {

		super(parent);
		this.ruleEditorPanel = ruleEditorPanel;
		this.parent = parent;
		this.selectedAxioms = new HashSet<OWLAxiom>();
		
		this.isClickedOK = false;

		// call to set the activeOntology and parent
		new UserObjectforTreeView(parent, activeOntology);

		// for  rendering
		ManchesterOWLSyntaxPrefixNameShortFormProvider shortFormProvider = new ManchesterOWLSyntaxPrefixNameShortFormProvider(
				activeOntology);
		rendering.setShortFormProvider(shortFormProvider);

		initUI();
		showUI();
	}

	public void initUI() {

		setSize(800, 500);
		setLocationRelativeTo(parent);
		setTitle("Integrate with active ontology");
		this.getContentPane().setLayout(new BorderLayout());

		// main panel
		mainPnl = new JPanel();
		mainPnl.setLayout(new BorderLayout());

		// bottom Panel
		bottomPnl = new JPanel();
		bottomPnl.setLayout(new BorderLayout());

		integrateBtn = new Button(integrateBtnText);
		integrateBtn.setSize(new Dimension(100, 30));
		integrateBtn.setPreferredSize(new Dimension(100, 30));
		integrateBtn.setMaximumSize(new Dimension(100, 30));
		integrateBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// now integrate with protege
				// extractSelectedAxioms();
				setClickedOK(true);
				dispose();
			}
		});

		infoLbl = new JLabel();
		infoLbl.setText(infoText);

		JPanel pnl1 = new JPanel();
		pnl1.setLayout(new BorderLayout());
		pnl1.add(infoLbl, BorderLayout.WEST);
		pnl1.add(integrateBtn, BorderLayout.EAST);
		bottomPnl.add(pnl1, BorderLayout.CENTER);

		cancelBtn = new Button(cancelBtnText);
		cancelBtn.setSize(new Dimension(100, 30));
		cancelBtn.setPreferredSize(new Dimension(100, 30));
		cancelBtn.setMaximumSize(new Dimension(100, 30));

		cancelBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setClickedOK(false);
				dispose();
			}
		});


		JPanel pnl2 = new JPanel();
		pnl2.setLayout(new BorderLayout());
		pnl2.add(cancelBtn, BorderLayout.EAST);
		bottomPnl.add(pnl2, BorderLayout.EAST);

		mainPnl.add(getAxiomsPnl(), BorderLayout.CENTER);

		this.add(mainPnl, BorderLayout.CENTER);
		this.add(bottomPnl, BorderLayout.SOUTH);

	}

	private void showUI() {
		this.setModal(true);
		this.setVisible(true);
	}


	private JPanel getAxiomsPnl() {

		axiomsPnl = new JPanel();
		axiomsPnl.setLayout(new BorderLayout());

		JLabel lblNewAxioms = new JLabel(newAxiomsLblText);
		lblNewAxioms.setBorder(BorderFactory.createLineBorder(Color.orange, 2));
		lblNewAxioms.setHorizontalAlignment(SwingConstants.CENTER);
		axiomsPnl.add(lblNewAxioms, BorderLayout.NORTH);

		//create tree
		
		String value = "<html><b style=\"color:#624FDB;\">" + "Axioms" + "</b></html>";
		
		DefaultMutableTreeNode treeRoot = new DefaultMutableTreeNode(value);

		DefaultMutableTreeNode childNode;

		// create child node from each Axioms
		if (ruleEditorPanel.getGeneratedAxioms() != null && !ruleEditorPanel.getGeneratedAxioms().isEmpty()) {

			for (OWLAxiom axiom : ruleEditorPanel.getGeneratedAxioms()) {

				childNode = new DefaultMutableTreeNode(getAxiomWithCostemtics(axiom));
				treeRoot.add(childNode);
			}
		}

		JTree axiomTree = new JTree(treeRoot);
		try{
			//add icon
			//Icon closedIcon = new ImageIcon(getClass().getResource("tree.png"));
			//DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) axiomTree.getCellRenderer();
			//renderer.setClosedIcon(closedIcon);
		}
		catch (Exception e) {
			// TODO: handle exception
		}
		axiomsScroll = new JScrollPane(axiomTree);

		axiomsPnl.add(axiomsScroll, BorderLayout.CENTER);

		return axiomsPnl;
	}

	
	private String getAxiomWithCostemtics(OWLAxiom axiom) {

		String value = "";

		String tmpValue = rendering.render(axiom);
		tmpValue = tmpValue.replace("<", "&lt;");
		value = getCosmetics(tmpValue);

		return value;
	}

	private String getCosmetics(String fullAxiomAsString) {

		fullAxiomAsString = fullAxiomAsString.replace("  ", " ");
		String fullAxiomAsFormattedString = " ";

		String[] values = fullAxiomAsString.split(" ");

		for (String eachToken : values) {
			// System.out.println(eachToken);
			if (boldFaceText.contains(eachToken.toLowerCase())) {
				// fullAxiomAsString = fullAxiomAsString.replace(eachToken, "<b
				// style=\"color:#F09128;\">" + eachToken + "</b>");
				fullAxiomAsFormattedString += "<b style=\"color:#F09128;\">" + eachToken + "</b>" + " ";
			} else {
				fullAxiomAsFormattedString += eachToken + " ";
			}
		}

		return "<html>" + fullAxiomAsFormattedString + "</html>";
	}


	public Set<OWLAxiom> getSelectedAxioms() {
		return this.selectedAxioms;
	}

}
