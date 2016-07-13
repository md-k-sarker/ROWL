/**
 * 
 */
package edu.wright.dase.controller;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
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
	private String defaultPrefix;
	private JButton addClassButton;
	private JButton addIndVButton;
	private JButton addObjPropButton;
	private JButton addDataPropButton;
	private JButton addDataTypeButton;

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
		this.defaultPrefix = this.engine.getDefaultPrefix();

		createUserInterface();

	}

	private void createUserInterface() {

		String entityName = getNameFromErrorText(this.errorText);
		String ruleText = this.ruleEditorPanel.getRuleText();

		if (!this.errorText.contains("cannot use name of existing OWL class")) {

			if (this.errorText.contains("Invalid SWRL atom predicate")) {

				if (noOfArgument(ruleText, entityName) == 1) {
					// class
					add(bind("Add '" + entityName + "' as OWLClass", new AddClassAction(entityName), "/class.add.png"));
				} else if (noOfArgument(ruleText, entityName) == 2) {
					// object property
					add(bind("Add '" + entityName + "' as OWLObjectProperty", new AddObjPropAction(entityName),
							"/objprop.add.png"));
					addSeparator();
					// data property
					add(bind("Add '" + entityName + "' as OWLDataProperty", new AddDataPropAction(entityName),
							"/dataprop.add.png"));
				} else {
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

	private int noOfArgument(String ruleText, String Atom) {
		try {

			String tmp = ruleText.substring(ruleText.lastIndexOf(Atom) + 1);
			if (tmp.length() > 0) {
				tmp = tmp.substring(1, tmp.indexOf(")"));
				if (tmp.length() > 0) {
					String[] ss = tmp.split(",");
					return ss.length;
				} else
					return -1;
			} else
				return -1;
		} catch (IndexOutOfBoundsException e) {
			return -1;
		}
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

		OWLClass newClass = owlDataFactory.getOWLClass(Name, prefixManager);

		OWLAxiom declareaxiom = owlDataFactory.getOWLDeclarationAxiom(newClass);
		AddAxiom addAxiom = new AddAxiom(activeOntology, declareaxiom);
		owlOntologyManager.applyChange(addAxiom);

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
			String owlCompatibleName = getValueAsOWLCompatibleName(this.name);
			createOWLClass(owlCompatibleName);
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
			String owlCompatibleName = getValueAsOWLCompatibleName(this.name);

			createOWLObjectProperty(owlCompatibleName);
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
			String owlCompatibleName = getValueAsOWLCompatibleName(this.name);
			createOWLDataProperty(owlCompatibleName);
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
			String owlCompatibleName = getValueAsOWLCompatibleName(this.name);
			createOWLDataType(owlCompatibleName);
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
			String owlCompatibleName = getValueAsOWLCompatibleName(this.name);
			createOWLNamedIndividual(owlCompatibleName);
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
