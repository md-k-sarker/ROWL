/**
 * 
 */
package edu.wright.dase.controller;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.model.parameters.ChangeApplied;
import org.swrlapi.core.IRIResolver;

import edu.wright.dase.view.RuleEditorPanel;

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
	private RuleEditorPanel ruleEditorPanel;

	/**
	 * 
	 */
	public SuggestionPopup(RuleEditorPanel ruleEditorPanel, Engine engine, String errorText) {
		// TODO Auto-generated constructor stub

		this.errorText = errorText;
		this.engine = engine;
		this.ruleEditorPanel = ruleEditorPanel;
		this.owlDataFactory = this.engine.getOwlDataFactory();
		this.activeOntology = this.engine.getActiveOntology();
		this.iriResolver = this.engine.getIriResolver();
		this.owlOntologyManager = this.engine.getOwlOntologyManager();
		this.prefixManager = this.engine.getPrefixManager();

		createUserInterface();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JPopupMenu#show(java.awt.Component, int, int)
	 */
	@Override
	public void show(Component invoker, int x, int y) {
		// TODO Auto-generated method stub

		// OWLOntologyID ontoID = this.activeOntology.getOntologyID();
		//
		// if (ontoID == null) {
		// JOptionPane.showMessageDialog(invoker, "Please Specify Ontology
		// ID(Ontology IRI) first.");
		// return;
		// }

		super.show(invoker, x, y);
	}

	private void createUserInterface() {

		String entityName = getNameFromErrorText(this.errorText);
		String ruleText = this.ruleEditorPanel.getRuleText();

		if (!this.errorText.contains("cannot use name of existing OWL class")) {

			if (this.errorText.contains("Invalid SWRL atom predicate")) {

				// if (noOfArgument(ruleText, entityName) == 1) {
				// // class
				// add(bind("Add '" + entityName + "' as OWLClass", new
				// AddClassAction(entityName), "/class.add.png"));
				// } else if (noOfArgument(ruleText, entityName) == 2) {
				// // object property
				// add(bind("Add '" + entityName + "' as OWLObjectProperty", new
				// AddObjPropAction(entityName),
				// "/objprop.add.png"));
				// addSeparator();
				// // data property
				// add(bind("Add '" + entityName + "' as OWLDataProperty", new
				// AddDataPropAction(entityName),
				// "/dataprop.add.png"));
				// } else
				{
					// class
					add(bind("Add '" + entityName + "' as OWLClass", new AddClassAction(entityName), "/class.add.png"));
					addSeparator();
					// object property
					add(bind("Add '" + entityName + "' as OWLObjectProperty", new AddObjPropAction(entityName),
							"/objprop.add.png"));
					addSeparator();
					// data property
					add(bind("Add '" + entityName + "' as OWLDataProperty", new AddDataPropAction(entityName),
							"/dataprop.add.png"));
				}

			} else if (this.errorText.contains("Invalid OWL individual name")) {
				// namedindividual
				add(bind("Add '" + entityName + "' as OWLNamedIndividual", new AddNamedIndVAction(entityName),
						"/individual.add.png"));

			} else if (this.errorText.contains("invalid datatype name")) {
				// datatype
				add(bind("Add '" + entityName + "' as OWLDataType", new AddDataTypeAction(entityName),
						"/datatype.add.png"));

			}
		}
	}

	private String getNameFromErrorText(String errorText) {
		if (errorText.contains("'")) {
			int firstIndex = errorText.indexOf("'");
			int lastIndex = errorText.lastIndexOf("'");

			String atom = errorText.substring(firstIndex + 1, lastIndex);

			return atom;
		} else {
			// System.out.println("not found");
			return "";
		}
	}

	private int noOfArgument(String ruleText, String Atom) {
		try {

			String tmp = ruleText.substring(ruleText.indexOf(Atom) + Atom.length());
			// System.out.println("tmp 0: "+tmp);
			if (tmp.length() > 0) {
				tmp = tmp.substring(1, tmp.indexOf(")"));
				// System.out.println("tmp 1: "+tmp);
				if (tmp.length() > 0) {

					String[] ss = tmp.split(",");
					// System.out.println("ss length: "+ss.length);
					return ss.length;
				} else
					return -1;
			} else
				return -1;
		} catch (IndexOutOfBoundsException e) {
			return -1;
		}
	}

	private Engine getEngine() {
		// System.out.println(this.engine);
		return this.engine;
	}

	private String getentityNameWithHTMLCosmetics(String name) {
		return name;
	}

	private int createOWLObjectProperty(String Name) {

		OWLObjectProperty newOWLObjectProperty = this.owlDataFactory.getOWLObjectProperty(Name, prefixManager);

		OWLAxiom declareaxiom = this.owlDataFactory.getOWLDeclarationAxiom(newOWLObjectProperty);
		AddAxiom addAxiom = new AddAxiom(this.activeOntology, declareaxiom);
		this.owlOntologyManager.applyChange(addAxiom);

		this.ruleEditorPanel.update();
		return 0;
	}

	private int createOWLDataProperty(String Name) {

		OWLDataProperty newOWLDataProperty = owlDataFactory.getOWLDataProperty(Name, prefixManager);

		OWLAxiom declareaxiom = owlDataFactory.getOWLDeclarationAxiom(newOWLDataProperty);
		AddAxiom addAxiom = new AddAxiom(activeOntology, declareaxiom);
		owlOntologyManager.applyChange(addAxiom);

		this.ruleEditorPanel.update();
		return 0;

	}

	private int createOWLNamedIndividual(String Name) {

		OWLNamedIndividual newOWLIndividual = owlDataFactory.getOWLNamedIndividual(Name, prefixManager);

		OWLAxiom declareaxiom = owlDataFactory.getOWLDeclarationAxiom(newOWLIndividual);
		AddAxiom addAxiom = new AddAxiom(activeOntology, declareaxiom);
		owlOntologyManager.applyChange(addAxiom);

		this.ruleEditorPanel.update();
		return 0;

	}

	private int createOWLClass(String Name) {

		// System.out.println("New Class Name: " + Name + "\t" +
		// prefixManager.getDefaultPrefix());
		OWLClass newClass = owlDataFactory.getOWLClass(Name, prefixManager);

		OWLAxiom declareaxiom = owlDataFactory.getOWLDeclarationAxiom(newClass);
		AddAxiom addAxiom = new AddAxiom(activeOntology, declareaxiom);
		ChangeApplied cA = owlOntologyManager.applyChange(addAxiom);
		if (cA == ChangeApplied.SUCCESSFULLY) {
			// System.out.println("Successfull: " +
			// newClass.getIRI().toString());
		}
		if (cA == ChangeApplied.UNSUCCESSFULLY) {
			// System.out.println("Unsuccessfull");
		}
		if (cA == ChangeApplied.NO_OPERATION) {
			// System.out.println("No op");
		}
		this.ruleEditorPanel.update();
		return 0;

	}

	private int createOWLDataType(String Name) {

		OWLDatatype newDataType = owlDataFactory.getOWLDatatype(Name, prefixManager);

		OWLAxiom declareaxiom = owlDataFactory.getOWLDeclarationAxiom(newDataType);

		AddAxiom addAxiom = new AddAxiom(activeOntology, declareaxiom);
		owlOntologyManager.applyChange(addAxiom);

		this.ruleEditorPanel.update();
		return 0;
	}

	private Component getParentForView() {
		return this.ruleEditorPanel;
	}

	public class AddClassAction extends AbstractAction {
		/**
		 * 
		 */
		protected String name;

		/**
		 * 
		 * @param key
		 */
		public AddClassAction(String name) {
			this.name = name;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			Engine ee = getEngine();
			String owlCompatibleName = ee.getValueAsOWLCompatibleName(this.name);
			// System.out.println("inside actionPerformed to createOWLClass() in
			// showSuggestionPopup: " + ee);
			if (owlCompatibleName == null) {
				JOptionPane.showMessageDialog(getParentForView(), "Can not create OWLEntity with name " + this.name,
						"Syntax Error", JOptionPane.ERROR_MESSAGE);
			} else {
				createOWLClass(owlCompatibleName);
			}
		}
	}

	public class AddObjPropAction extends AbstractAction {
		/**
		 * 
		 */
		protected String name;

		/**
		 * 
		 * @param key
		 */
		public AddObjPropAction(String name) {
			this.name = name;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			String owlCompatibleName = getEngine().getValueAsOWLCompatibleName(this.name);
			if (owlCompatibleName == null) {
				JOptionPane.showMessageDialog(getParentForView(), "Can not create OWLEntity with name " + this.name,
						"Syntax Error", JOptionPane.ERROR_MESSAGE);
			} else {
				createOWLObjectProperty(owlCompatibleName);
			}
		}
	}

	public class AddDataPropAction extends AbstractAction {
		/**
		 * 
		 */
		protected String name;

		/**
		 * 
		 * @param key
		 */
		public AddDataPropAction(String name) {
			this.name = name;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			String owlCompatibleName = getEngine().getValueAsOWLCompatibleName(this.name);
			if (owlCompatibleName == null) {
				JOptionPane.showMessageDialog(getParentForView(), "Can not create OWLEntity with name " + this.name,
						"Syntax Error", JOptionPane.ERROR_MESSAGE);
			} else {
				createOWLDataProperty(owlCompatibleName);
			}
		}
	}

	public class AddDataTypeAction extends AbstractAction {
		/**
		 * 
		 */
		protected String name;

		/**
		 * 
		 * @param key
		 */
		public AddDataTypeAction(String name) {
			this.name = name;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			String owlCompatibleName = getEngine().getValueAsOWLCompatibleName(this.name);
			if (owlCompatibleName == null) {
				JOptionPane.showMessageDialog(getParentForView(), "Can not create OWLEntity with name " + this.name,
						"Syntax Error", JOptionPane.ERROR_MESSAGE);
			} else {
				createOWLDataType(owlCompatibleName);
			}
		}
	}

	public class AddNamedIndVAction extends AbstractAction {
		/**
		 * 
		 */
		protected String name;

		/**
		 * 
		 * @param key
		 */
		public AddNamedIndVAction(String name) {
			this.name = name;
		}

		/**
		 * 
		 */
		public void actionPerformed(ActionEvent e) {
			String owlCompatibleName = getEngine().getValueAsOWLCompatibleName(this.name);
			if (owlCompatibleName == null) {
				JOptionPane.showMessageDialog(getParentForView(), "Can not create OWLEntity with name " + this.name,
						"Syntax Error", JOptionPane.ERROR_MESSAGE);
			} else {
				createOWLNamedIndividual(owlCompatibleName);
			}
		}
	}

	@SuppressWarnings("serial")
	public Action bind(String name, final Action action, String iconUrl) {
		AbstractAction newAction = new AbstractAction(name,
				(iconUrl != null) ? new ImageIcon(SuggestionPopup.class.getResource(iconUrl)) : null) {
			public void actionPerformed(ActionEvent e) {
				action.actionPerformed(new ActionEvent(e.getSource(), e.getID(), e.getActionCommand()));
			}
		};

		newAction.putValue(Action.SHORT_DESCRIPTION, action.getValue(Action.SHORT_DESCRIPTION));

		return newAction;
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
