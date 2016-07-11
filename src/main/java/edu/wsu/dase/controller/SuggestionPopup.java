/**
 * 
 */
package edu.wsu.dase.controller;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.swrlapi.core.IRIResolver;
import org.swrlapi.parser.SWRLParser;

/**
 * @author sarker
 *
 */
public class SuggestionPopup extends JPopupMenu {

	JPanel suggestionPanel;
	private String errorText;
	private IRIResolver iriResolver;
	private OWLOntology activeOntology;
	private OWLDataFactory owlDataFactory;
	private OWLOntologyManager owlOntologyManager;
	private PrefixManager prefixManager;
	private Engine engine;
	private String defaultPrefix;

	/**
	 * 
	 */
	public SuggestionPopup(Engine engine, String errorText) {
		// TODO Auto-generated constructor stub

		this.errorText = errorText;
		this.owlDataFactory = engine.getOwlDataFactory();
		this.activeOntology = engine.getActiveOntology();
		this.iriResolver = engine.getIriResolver();
		this.owlOntologyManager = engine.getOwlOntologyManager();
		this.prefixManager = engine.getPrefixManager();
		this.defaultPrefix = engine.getDefaultPrefix();

		createUserInterface();

	}

	private void createUserInterface() {
		suggestionPanel = new JPanel();
		suggestionPanel.setLayout(new BorderLayout());
		suggestionPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

		JLabel lbl = new JLabel("Text");
		suggestionPanel.add(lbl, BorderLayout.CENTER);

		add(suggestionPanel);

		createOWLObjectProperty(getValueAsOWLCompatibleName("Text"));
	}

	private String getValueAsOWLCompatibleName(String name) {

		if (name.contains(":")) {
			String[] subParts = name.split(":");
			if (subParts.length == 2) {
				if (prefixManager.containsPrefixMapping(subParts[0] + ":")) {
					return name;
				} else {
					return null;
				}
			} else
				return null;
		} else {
			if (defaultPrefix.length() > 0) {
				return defaultPrefix + name;
			} else {
				return null;
			}
		}

	}

	private int createOWLObjectProperty(String Name) {

		OWLObjectProperty newOWLObjectProperty = this.owlDataFactory.getOWLObjectProperty(Name, prefixManager);

		OWLAxiom declareaxiom = this.owlDataFactory.getOWLDeclarationAxiom(newOWLObjectProperty);
		AddAxiom addAxiom = new AddAxiom(this.activeOntology, declareaxiom);
		this.owlOntologyManager.applyChange(addAxiom);
		return 0;
	}

	private int createOWLDataProperty(String Name) {

		OWLDataProperty newOWLDataProperty = owlDataFactory.getOWLDataProperty(Name, prefixManager);

		OWLAxiom declareaxiom = owlDataFactory.getOWLDeclarationAxiom(newOWLDataProperty);
		AddAxiom addAxiom = new AddAxiom(activeOntology, declareaxiom);
		owlOntologyManager.applyChange(addAxiom);
		return 0;

	}

	private int createOWLIndividual(String Name) {

		OWLNamedIndividual newOWLIndividual = owlDataFactory.getOWLNamedIndividual(Name, prefixManager);

		OWLAxiom declareaxiom = owlDataFactory.getOWLDeclarationAxiom(newOWLIndividual);
		AddAxiom addAxiom = new AddAxiom(activeOntology, declareaxiom);
		owlOntologyManager.applyChange(addAxiom);
		return 0;

	}

	private int createOWLClass(String Name) {

		OWLClass newClass = owlDataFactory.getOWLClass(Name, prefixManager);

		OWLAxiom declareaxiom = owlDataFactory.getOWLDeclarationAxiom(newClass);
		AddAxiom addAxiom = new AddAxiom(activeOntology, declareaxiom);
		owlOntologyManager.applyChange(addAxiom);
		return 0;

	}

	// for testing purpose only
	// public void create() {
	// JFrame frame = new JFrame();
	//
	// frame.setSize(400, 500);
	//
	// JTextPane textPane = new JTextPane();
	// StyledDocument doc = textPane.getStyledDocument();
	//
	// JButton btn = new JButton("btn");
	// btn.addActionListener(new ActionListener() {
	//
	// @Override
	// public void actionPerformed(ActionEvent e) {
	// // TODO Auto-generated method stub
	// SuggestionPopup sg = new SuggestionPopup();
	// sg.show(textPane, 40, 30);
	// }
	// });
	//
	// frame.add(btn, BorderLayout.NORTH);
	//
	// frame.add(textPane, BorderLayout.CENTER);
	//
	// frame.setVisible(true);
	//
	//
	// }
	//
	// //for testing purpose only
	// public static void main(String[] args) {
	// SuggestionPopup sg = new SuggestionPopup();
	// sg.create();
	// }

	// class NewEditorKit extends StyledEditorKit {
	// public ViewFactory getViewFactory() {
	// return new NewViewFactory();
	// }
	// }
	//
	// class NewViewFactory implements ViewFactory {
	// public View create(Element elem) {
	// String kind = elem.getName();
	// if (kind != null) {
	// if (kind.equals(AbstractDocument.ContentElementName)) {
	// return new JaggedLabelView(elem);
	// } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
	// return new ParagraphView(elem);
	// } else if (kind.equals(AbstractDocument.SectionElementName)) {
	// return new BoxView(elem, View.Y_AXIS);
	// } else if (kind.equals(StyleConstants.ComponentElementName)) {
	// return new ComponentView(elem);
	// } else if (kind.equals(StyleConstants.IconElementName)) {
	// return new IconView(elem);
	// }
	// }
	//
	// // default to text display
	// return new LabelView(elem);
	// }
	// }
	//
	// class JaggedLabelView extends LabelView {
	//
	// public JaggedLabelView(Element elem) {
	// super(elem);
	// }
	//
	// public void paint(Graphics g, Shape allocation) {
	// super.paint(g, allocation);
	// paintJaggedLine(g, allocation);
	// }
	//
	// public void paintJaggedLine(Graphics g, Shape a) {
	// int y = (int) (a.getBounds().getY() + a.getBounds().getHeight());
	// int x1 = (int) a.getBounds().getX();
	// int x2 = (int) (a.getBounds().getX() + a.getBounds().getWidth());
	//
	// Color old = g.getColor();
	// if (isJagged) {
	// g.setColor(Color.red);
	// for (int i = x1; i <= x2; i += 6) {
	// g.drawArc(i + 3, y - 3, 3, 3, 0, 180);
	// g.drawArc(i + 6, y - 3, 3, 3, 180, 181);
	// }
	// g.setColor(old);
	// }
	// }
	// }

}
