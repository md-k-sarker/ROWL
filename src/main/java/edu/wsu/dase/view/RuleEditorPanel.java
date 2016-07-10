package edu.wsu.dase.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.protege.editor.owl.ui.framelist.SwitchToDefiningOntologyAction;
import org.protege.editor.owl.ui.prefix.PrefixUtilities;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.SWRLRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.ui.dialog.SWRLRuleEditorDialog;
import org.swrlapi.ui.dialog.SWRLRuleEngineDialogManager;
import org.swrlapi.ui.model.SWRLAutoCompleter;
import org.swrlapi.ui.model.SWRLRuleEngineModel;
import org.swrlapi.ui.model.SWRLRulesAndSQWRLQueriesTableModel;
import org.swrlapi.ui.view.SWRLAPIView;
import org.swrltab.util.SWRLRuleEditorAutoCompleteState;
import org.swrltab.util.SWRLRuleEditorInitialDialogState;
import org.swrlapi.parser.SWRLIncompleteRuleException;
import org.swrlapi.parser.SWRLParseException;
import org.swrlapi.parser.SWRLParser;

import edu.wsu.dase.controller.Engine;
import edu.wsu.dase.model.Constants;
import edu.wsu.dase.model.RuleModel;
import edu.wsu.dase.model.RuleTableModel;
import edu.wsu.dase.model.ruletoaxiom.Transformer;
import edu.wsu.dase.view.axiomManchesterDialog.AxiomsDialog;

/**
 * developed by sarker.3 JPanel providing a SWRL rule
 *
 */
public class RuleEditorPanel extends JPanel implements SWRLAPIView {
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(RuleEditorPanel.class);

	// private static final String TITLE = "Edit";
	private static final String RULE_NAME_TITLE = "Name";
	private static final String COMMENT_LABEL_TITLE = "Comment";
	private static final String STATUS_LABEL_TITLE = "Status";
	private static final String CONVERT_TO_OWL_BUTTON_TITLE = "Convert to OWL Axiom";
	private static final String CANCEL_BUTTON_TITLE = "Clear";
	private static final String STATUS_OK = "Ok";
	private static final String STATUS_NO_RULE_TEXT = "Use Tab key to cycle through auto-completions;"
			+ " use Escape key to remove auto-complete expansion";
	private static final String INVALID_RULE_TITLE = "Invalid";
	private static final String MISSING_RULE = "Nothing to save!";
	private static final String MISSING_RULE_NAME_TITLE = "Empty Name";
	private static final String MISSING_RULE_NAME = "A name must be supplied!";
	private static final String QUIT_CONFIRM_TITLE = "Unsaved Changes";
	private static final String QUIT_CONFIRM_MESSAGE = "Are you sure you want discard your changes?";
	private static final String DUPLICATE_RULE_TEXT = "Name already in use - please pick another name.";
	private static final String DUPLICATE_RULE_TITLE = "Duplicate Name";
	private static final String INTERNAL_ERROR_TITLE = "Internal Error";

	private static final int BUTTON_PREFERRED_WIDTH = 200;
	private static final int BUTTON_PREFERRED_HEIGHT = 30;
	private static final int RULE_EDIT_AREA_COLUMNS = 20;
	private static final int RULE_EDIT_AREA_ROWS = 60;

	@NonNull
	private SWRLRuleEngineModel swrlRuleEngineModel;
	@NonNull
	private SWRLRuleEngineDialogManager dialogManager;

	@NonNull
	private Engine engine;

	@NonNull
	private final SWRLRuleEditorInitialDialogState initialDialogState = new SWRLRuleEditorInitialDialogState();
	@NonNull
	private final JTextField ruleNameTextField, commentTextField, statusTextField;
	@NonNull
	private final JTextPane ruleTextTextPane;
	@NonNull
	private final JButton convertToOWLButton, cancelButton;
	@NonNull
	private final Border loweredBevelBorder;
	@NonNull
	private final Border yellowBorder;

	private JScrollPane scrollPane;
	private JTabbedPane tabbedPane;
	private JLabel lbl1;
	private JLabel lbl2;

	@NonNull
	private Optional<@NonNull SWRLRuleEditorAutoCompleteState> autoCompleteState = Optional
			.<@NonNull SWRLRuleEditorAutoCompleteState> empty(); // Present if
																	// auto-complete
	private boolean editMode = false;
	private OWLOntology activeOntology;
	private OWLDataFactory owlDataFactory;
	private OWLOntologyManager owlOntologyManager;
	JPanel pnlForCreateNewEntity;

	// for test purpose only
	private RuleEditorPanel() {

		// this.dialogManager = dialogManager;
		this.loweredBevelBorder = BorderFactory.createLoweredBevelBorder();
		this.yellowBorder = BorderFactory.createLineBorder(Color.YELLOW);

		this.ruleTextTextPane = new JTextPane();
		this.convertToOWLButton = new JButton(CONVERT_TO_OWL_BUTTON_TITLE);
		this.cancelButton = new JButton(CANCEL_BUTTON_TITLE);
		this.ruleNameTextField = new JTextField("");
		this.commentTextField = new JTextField("");
		this.statusTextField = new JTextField(STATUS_NO_RULE_TEXT);
		initialize();
	}

	public RuleEditorPanel(@NonNull SWRLRuleEngineModel swrlRuleEngineModel, @NonNull Engine engine,
			@NonNull OWLOntology activeOntology, @NonNull SWRLRuleEngineDialogManager dialogManager,
			@NonNull JTabbedPane tabbedPane) {

		this.swrlRuleEngineModel = swrlRuleEngineModel;
		this.dialogManager = dialogManager;
		this.engine = engine;
		this.loweredBevelBorder = BorderFactory.createLoweredBevelBorder();
		this.yellowBorder = BorderFactory.createLineBorder(Color.YELLOW);
		this.ruleTextTextPane = new JTextPane();
		this.tabbedPane = tabbedPane;
		this.convertToOWLButton = new JButton(CONVERT_TO_OWL_BUTTON_TITLE);
		this.cancelButton = new JButton(CANCEL_BUTTON_TITLE);
		this.activeOntology = activeOntology;
		this.owlDataFactory = activeOntology.getOWLOntologyManager().getOWLDataFactory();
		this.owlOntologyManager = activeOntology.getOWLOntologyManager();
		lbl1 = new JLabel();
		lbl2 = new JLabel();
		this.ruleNameTextField = new JTextField("");
		this.commentTextField = new JTextField("");
		this.statusTextField = new JTextField(STATUS_NO_RULE_TEXT);
		initialize();
	}

	@Override
	public void initialize() {
		// Container contentPane = getContentPane();

		initializeComponents();

		this.ruleTextTextPane.addKeyListener(new SWRLRuleEditorKeyAdapter());
		this.cancelButton.addActionListener(new CancelSWRLRuleEditActionListener());
		this.convertToOWLButton.addActionListener(new ConvertSWRLRuleActionListener(this));
	}

	public void loadEdittingRule(String ruleName, String ruleComment, String ruleText) {

		cancelEditMode();
		this.ruleNameTextField.setText(ruleName); //
		this.ruleNameTextField.setCaretPosition(this.ruleNameTextField.getText().length());
		this.ruleTextTextPane.setText(ruleText);
		this.commentTextField.setText(ruleComment);
		this.statusTextField.setText("");
		updateStatus();
		this.editMode = true;
	}

	@Override
	public void update() {
		updateStatus();
	}

	/**
	 * 
	 */
	private void updateSuggestion(){
		
	}
	
	private void updateStatus() {
		String ruleText = getRuleText();

		if (ruleText.isEmpty()) {
			setInformationalStatusText(STATUS_NO_RULE_TEXT);
			disableSave();
		} else {
			try {
				createSWRLParser().parseSWRLRule(ruleText, true, getRuleName(), getComment());
				this.ruleTextTextPane.requestFocus();
				setInformationalStatusText(STATUS_OK);
				enableSave();
			} catch (SWRLIncompleteRuleException e) {
				setIncompleteStatusText(e.getMessage() == null ? "" : e.getMessage());
				disableSave();
			} catch (SWRLParseException e) {
				setErrorStatusText(e.getMessage() == null ? "" : e.getMessage());
				disableSave();
			} catch (RuntimeException e) {
				setInformationalStatusText(e.getMessage() == null ? "" : e.getMessage());
				disableSave();
			}
		}
	}

	private void cancelEditMode() {
		this.ruleNameTextField.setText("");
		this.ruleNameTextField.setEnabled(true);
		this.ruleTextTextPane.setText("");
		this.ruleTextTextPane.setEnabled(true);
		this.ruleTextTextPane.setText("");
		this.commentTextField.setText("");
		this.statusTextField.setText("");

		this.editMode = false;
	}

	private void initializeComponents() {
		JLabel ruleNameLabel = new JLabel(RULE_NAME_TITLE);
		JLabel commentLabel = new JLabel(COMMENT_LABEL_TITLE);
		JLabel statusLabel = new JLabel(STATUS_LABEL_TITLE);
		JPanel upperPanel = new JPanel(new GridLayout(6, 2));
		JPanel rulePanel = new JPanel(new GridLayout(1, 1));
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JPanel surroundPanel = new JPanel(new BorderLayout());
		// Container contentPane = getRootPane();

		this.ruleNameTextField.setBorder(this.loweredBevelBorder);

		// this.ruleTextTextArea.setLineWrap(true);
		// this.ruleTextTextArea.setWrapStyleWord(true);
		this.ruleTextTextPane.setBorder(this.loweredBevelBorder);
		this.ruleTextTextPane.setPreferredSize(new Dimension(300, 300));

		this.commentTextField.setDisabledTextColor(Color.BLACK);
		this.commentTextField.setBorder(this.loweredBevelBorder);

		this.statusTextField.setDisabledTextColor(Color.BLACK);
		this.statusTextField.setEnabled(false);
		this.statusTextField.setBorder(this.loweredBevelBorder);

		this.cancelButton.setPreferredSize(new Dimension(BUTTON_PREFERRED_WIDTH, BUTTON_PREFERRED_HEIGHT));
		this.convertToOWLButton.setPreferredSize(new Dimension(BUTTON_PREFERRED_WIDTH, BUTTON_PREFERRED_HEIGHT));

		this.setLayout(new BorderLayout());

		upperPanel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
		upperPanel.add(ruleNameLabel);
		upperPanel.add(this.ruleNameTextField);
		upperPanel.add(commentLabel);
		upperPanel.add(this.commentTextField);
		upperPanel.add(statusLabel);
		upperPanel.add(this.statusTextField);

		rulePanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		this.scrollPane = new JScrollPane(this.ruleTextTextPane);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setPreferredSize(new Dimension(300, 200));
		rulePanel.add(this.scrollPane);

		buttonPanel.add(cancelButton);
		buttonPanel.add(this.convertToOWLButton);

		surroundPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

		surroundPanel.add(upperPanel, BorderLayout.NORTH);
		surroundPanel.add(rulePanel, BorderLayout.CENTER);
		surroundPanel.add(buttonPanel, BorderLayout.SOUTH);
		this.add(surroundPanel, BorderLayout.CENTER);
		// pack();
	}

	private void autoComplete() {
		if (!isInAutoCompleteMode()) {
			String ruleText = getRuleText();
			int textPosition = this.ruleTextTextPane.getCaretPosition();
			int i = SWRLParser.findSplittingPoint(ruleText.substring(0, textPosition));
			String prefix = ruleText.substring(i, textPosition);
			if (!prefix.equals("")) {
				List<@NonNull String> expansions = getExpansions(prefix); // All
																			// expansions
																			// will
																			// start
																			// with
																			// the
																			// empty
																			// string.

				if (expansions.size() > 1) { // More than the empty string
												// expansion; if not, do not
												// enter autoComplete mode
					SWRLRuleEditorAutoCompleteState state = new SWRLRuleEditorAutoCompleteState(textPosition, prefix,
							expansions);
					insertExpansion(textPosition, prefix, state.getNextExpansion()); // Skip
																						// the
																						// empty
																						// string
					enableAutoCompleteMode(state);
				}
			}
		} else { // Already in auto-complete mode
			int textPosition = this.autoCompleteState.get().getTextPosition();
			String prefix = this.autoCompleteState.get().getPrefix();
			String currentExpansion = this.autoCompleteState.get().getCurrentExpansion();
			String nextExpansion = this.autoCompleteState.get().getNextExpansion();

			replaceExpansion(textPosition, prefix, currentExpansion, nextExpansion);
		}
	}

	private boolean isInAutoCompleteMode() {
		return this.autoCompleteState.isPresent();
	}

	private void enableAutoCompleteMode(@NonNull SWRLRuleEditorAutoCompleteState autoCompleteState) {
		this.autoCompleteState = Optional.of(autoCompleteState);
	}

	private void disableAutoCompleteModeIfNecessary() {
		if (this.autoCompleteState.isPresent())
			disableAutoCompleteMode();
	}

	private void disableAutoCompleteMode() {
		this.autoCompleteState = Optional.<@NonNull SWRLRuleEditorAutoCompleteState> empty();
	}

	private void cancelAutoCompleteIfNecessary() {
		if (isInAutoCompleteMode()) {
			int textPosition = this.autoCompleteState.get().getTextPosition();
			String prefix = this.autoCompleteState.get().getPrefix();
			String currentExpansion = this.autoCompleteState.get().getCurrentExpansion();

			replaceExpansion(textPosition, prefix, currentExpansion, "");
			disableAutoCompleteMode();
		}
	}

	private void insertExpansion(int textPosition, @NonNull String prefix, @NonNull String expansion) {
		String expansionTail = expansion.substring(prefix.length());

		try {
			Document document = this.ruleTextTextPane.getDocument();
			if (document != null)
				document.insertString(textPosition, expansionTail, SimpleAttributeSet.EMPTY);
			else
				disableAutoCompleteMode();
		} catch (BadLocationException e) {
			disableAutoCompleteMode();
		}
	}

	private void replaceExpansion(int textPosition, @NonNull String prefix, @NonNull String currentExpansion,
			@NonNull String nextExpansion) {
		String currentExpansionTail = currentExpansion.isEmpty() ? "" : currentExpansion.substring(prefix.length());
		String nextExpansionTail = nextExpansion.isEmpty() ? "" : nextExpansion.substring(prefix.length());

		try {
			if (!currentExpansionTail.isEmpty())
				this.ruleTextTextPane.getDocument().remove(textPosition, currentExpansionTail.length());

			if (!nextExpansionTail.isEmpty())
				this.ruleTextTextPane.getDocument().insertString(textPosition, nextExpansionTail,
						SimpleAttributeSet.EMPTY);
		} catch (BadLocationException e) {
			disableAutoCompleteMode();
		}
	}

	@NonNull
	private List<@NonNull String> getExpansions(@NonNull String prefix) {
		List<@NonNull String> expansions = new ArrayList<>();

		expansions.add(""); // Add empty expansion that we can cycle back to
		expansions.addAll(createSWRLAutoCompleter().getCompletions(prefix));

		return expansions;
	}

	@NonNull
	private SWRLAutoCompleter createSWRLAutoCompleter() {
		return this.swrlRuleEngineModel.createSWRLAutoCompleter();
	}

	private void disableSave() {
		this.convertToOWLButton.setEnabled(false);
	}

	private void enableSave() {
		this.convertToOWLButton.setEnabled(true);
	}

	private void setInformationalStatusText(@NonNull String status) {

		this.statusTextField.setBorder(loweredBevelBorder);
		this.statusTextField.setDisabledTextColor(Color.BLACK);
		this.statusTextField.setText(status);

	}

	private void setIncompleteStatusText(@NonNull String status) {

		this.statusTextField.setBorder(yellowBorder);
		this.statusTextField.setDisabledTextColor(Color.BLACK);
		this.statusTextField.setText(status);

	}

	private void setErrorStatusText(@NonNull String status) {

		this.statusTextField.setDisabledTextColor(Color.RED);
		this.statusTextField.setText(status);

	}

	@NonNull
	private String getRuleName() {
		return this.ruleNameTextField.getText().trim();

	}

	@NonNull
	private String getComment() {
		return this.commentTextField.getText().trim();

	}

	@NonNull
	private String getRuleText() { // We replace the Unicode characters when
									// parsing
		return this.ruleTextTextPane.getText().replaceAll(Character.toString(SWRLParser.RING_CHAR), ".");
	}

	@NonNull
	private SWRLRuleEngine getSWRLRuleEngine() {
		return this.swrlRuleEngineModel.getSWRLRuleEngine();
	}

	@NonNull
	private SWRLRuleEngineModel getSWRLRuleEngineModel() {
		return this.swrlRuleEngineModel;
	}

	@NonNull
	private SWRLRuleEditorInitialDialogState getInitialDialogState() {
		return this.initialDialogState;
	}

	private SWRLParser createSWRLParser() {
		return this.swrlRuleEngineModel.createSWRLParser();
	}

	private @NonNull RuleTableModel getRuleTableModel() {

		return this.engine.getRuleTableModel();
	}

	@NonNull
	private SWRLRuleEngineDialogManager getDialogManager() {
		return this.dialogManager;
	}

	private void setInitialDialogState() {
		this.initialDialogState.setState(getRuleName(), getComment(), getRuleText());
	}

	private boolean hasDialogStateChanged() {
		return this.initialDialogState.hasStateChanged(getRuleName(), getComment(), getRuleText());
	}

	private void clearIfOk() {

		this.ruleNameTextField.setText(""); //
		this.ruleNameTextField.setCaretPosition(this.ruleNameTextField.getText().length());
		this.ruleTextTextPane.setText("");
		this.commentTextField.setText("");
		this.statusTextField.setText("");
		updateStatus();

	}

	private class SWRLRuleEditorKeyAdapter extends KeyAdapter {
		@Override
		public void keyPressed(@NonNull KeyEvent event) {
			int code = event.getKeyCode();
			if ((code == KeyEvent.VK_TAB) || (code == KeyEvent.VK_SPACE && event.isControlDown())) {

				autoComplete();
				event.consume();
			} else if (code == KeyEvent.VK_ESCAPE) {
				cancelAutoCompleteIfNecessary();
				event.consume();
			} else if (code == KeyEvent.VK_DELETE) {
				cancelAutoCompleteIfNecessary();
			} else { // Any other key will disable auto-complete mode if it is
						// active
				disableAutoCompleteModeIfNecessary();
				super.keyPressed(event);
			}
		}

		@Override
		public void keyReleased(@NonNull KeyEvent event) {
			updateStatus();
			
			updateSuggestion();
		}
	}

	private class CancelSWRLRuleEditActionListener implements ActionListener {

		public CancelSWRLRuleEditActionListener() {

		}

		@Override
		public void actionPerformed(@NonNull ActionEvent e) {
			clearIfOk();
		}
	}

	/*
	 * public static SWRLAtom createObjectPropertyAtom(String roleStr, String
	 * arg0Str, String arg1Str) { SWRLIArgument arg0; if
	 * (arg0Str.startsWith("v")) /// variable arg0 =
	 * factory.getSWRLVariable(IRI.create(arg0Str)); else /// namedinstance arg0
	 * = factory.getSWRLIndividualArgument(factory.getOWLNamedIndividual(IRI.
	 * create(arg0Str)));
	 * 
	 * SWRLIArgument arg1; if (arg1Str.startsWith("v")) arg1 =
	 * factory.getSWRLVariable(IRI.create(arg1Str)); else arg1 =
	 * factory.getSWRLIndividualArgument(factory.getOWLNamedIndividual(IRI.
	 * create(arg1Str))); return
	 * factory.getSWRLObjectPropertyAtom(factory.getOWLObjectProperty(IRI.create
	 * (roleStr)), arg0, arg1); }
	 * 
	 * public static SWRLAtom createAtom(String conceptStr, String argStr) {
	 * SWRLArgument arg; if (argStr.startsWith("v")) arg =
	 * factory.getSWRLVariable(IRI.create(argStr)); else arg =
	 * factory.getSWRLIndividualArgument(factory.getOWLNamedIndividual(IRI.
	 * create(argStr))); return
	 * factory.getSWRLClassAtom(factory.getOWLClass(IRI.create(conceptStr)),
	 * (SWRLIArgument) arg); }
	 * 
	 * public Set<OWLAxiom> getEquivalentAxiom() {
	 * 
	 * this.equivalentAxioms = Transformer.ruleToAxioms(testRules); return
	 * this.equivalentAxioms; }
	 */

	private int noOfArgument(String ruleText, String Atom) throws SWRLParseException {
		try {
			String tmp = ruleText.substring(ruleText.lastIndexOf(Atom) + 1);
			if (tmp.length() > 0) {
				tmp = tmp.substring(1, tmp.indexOf(")"));
				if (tmp.length() > 0) {
					String[] ss = tmp.split(",");
					return ss.length;
				}
			}
		} catch (Exception e) {
			throw new SWRLParseException("Parse Exception");
		}
		throw new SWRLParseException("Parse Exception");
	}

	private int createNewClass(String className) {
		SWRLParser parser = createSWRLParser();
		Optional<@NonNull IRI> iri = swrlRuleEngineModel.getSWRLRuleEngine().getSWRLAPIOWLOntology().getIRIResolver()
				.prefixedName2IRI(className);
		if (iri.isPresent()) {

			OWLClass newClass = owlDataFactory.getOWLClass(iri.get());

			OWLAxiom declareaxiom = owlDataFactory.getOWLDeclarationAxiom(newClass);
			AddAxiom addAxiom = new AddAxiom(activeOntology, declareaxiom);
			owlOntologyManager.applyChange(addAxiom);
			return 0;
		}
		return -1;
	}

	private int createNewRole(String RoleName) {
		SWRLParser parser = createSWRLParser();
		Optional<@NonNull IRI> iri = swrlRuleEngineModel.getSWRLRuleEngine().getSWRLAPIOWLOntology().getIRIResolver()
				.prefixedName2IRI(RoleName);
		if (iri.isPresent()) {

			OWLObjectProperty objprop = owlDataFactory.getOWLObjectProperty(iri.get());
			OWLAxiom declareaxiom = owlDataFactory.getOWLDeclarationAxiom(objprop);

			AddAxiom addaxiom = new AddAxiom(activeOntology, declareaxiom);

			owlOntologyManager.applyChange(addaxiom);
			return 0;
		}
		return -1;
	}

	private JPanel getPnlForCreateNewEntity(String message, String type) {
		JPanel pnl = new JPanel();
		if (pnlForCreateNewEntity == null) {
			pnlForCreateNewEntity = new JPanel();
		}
		pnlForCreateNewEntity.setLayout(new BorderLayout());

		lbl1.setText(message + " not found in active ontology.");
		lbl2.setText("Do you want to create new " + type + " \nnamed: " + message + " ?");
		pnlForCreateNewEntity.add(lbl1, BorderLayout.PAGE_START);
		pnlForCreateNewEntity.add(lbl2, BorderLayout.PAGE_END);
		return pnlForCreateNewEntity;
	}

	private JPanel getPnlForSwitchToSWRLTab(String message) {
		JPanel pnl = new JPanel();
		if (pnlForCreateNewEntity == null) {
			pnlForCreateNewEntity = new JPanel();
		}

		pnlForCreateNewEntity.setLayout(new BorderLayout());
		lbl1.setText(message + " can not be transformed to OWL Axiom.");
		lbl2.setText("Do you want to switch to SWRLTab ?");
		pnlForCreateNewEntity.add(lbl1, BorderLayout.PAGE_START);
		pnlForCreateNewEntity.add(lbl2, BorderLayout.PAGE_END);
		return pnlForCreateNewEntity;
	}

	private SWRLRule getSWRLRule(String ruleText) throws SWRLParseException {
		SWRLParser parser = createSWRLParser();

		Optional<@NonNull SWRLRule> rule = null;

		// parser.setforRuletoOWL(true);
		rule = parser.parseSWRLRule(ruleText, false, getRuleName(), "comment");

		if (rule.isPresent()) {
			System.out.println("rule, body: " + rule.get().getBody() + " head:" + rule.get().getHead());
			return rule.get();
		}

		// catch (SWRLAtomNotFoundException e) {
		// System.out.println(e.getMessage() + " not found in vocabulary");
		// try {
		// int argno = noOfArgument(ruleText, e.getMessage());
		// if (argno == 1) {
		// if (JOptionPane.OK_OPTION == JOptionPane.showOptionDialog(this,
		// getPnlForCreateNewEntity(e.getMessage(), "Class"), "Creating Class",
		// JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null,
		// null)) {
		// createNewClass(e.getMessage());
		// }
		//
		// } else if (argno == 2) {
		// // createRole
		// if (JOptionPane.OK_OPTION == JOptionPane.showOptionDialog(this,
		// getPnlForCreateNewEntity(e.getMessage(), "ObjectProperty"), "Creating
		// Object Property",
		// JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null,
		// null)) {
		// createNewRole(e.getMessage());
		// }
		// } else if (argno > 2) {
		// // createRole
		// JOptionPane.showMessageDialog(this,
		// e.getMessage() + " has more than 2 argument. Cannot convert to Class
		// or Object Property.",
		// "More than two Argument", JOptionPane.ERROR_MESSAGE);
		// }else if (argno == 0) {
		// // createRole
		// JOptionPane.showMessageDialog(this,
		// e.getMessage() + " has no argument. Cannot convert to Class or Object
		// Property.",
		// "More than two Argument", JOptionPane.ERROR_MESSAGE);
		// }
		// } catch (SWRLParseException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }
		//
		// }

		return null;
	}

	Set<OWLAxiom> generatedAxioms = new HashSet<OWLAxiom>();

	/**
	 * @return the generatedAxioms
	 */
	public Set<OWLAxiom> getGeneratedAxioms() {
		return generatedAxioms;
	}

	/**
	 * @param generatedAxioms
	 *            the generatedAxioms to set
	 */
	public void setGeneratedAxioms(Set<OWLAxiom> generatedAxioms) {
		this.generatedAxioms = generatedAxioms;
	}

	private void applyChangetoOntology(Set<OWLAxiom> owlAxioms) {
		for (OWLAxiom axiom : owlAxioms) {
			AddAxiom addaxiom = new AddAxiom(activeOntology, axiom);
			owlOntologyManager.applyChange(addaxiom);
		}
	}

	private OWLAnnotation getOWLAnnotation() {

		OWLAnnotationProperty fixedAnnotationProperty;
		fixedAnnotationProperty = owlDataFactory.getOWLAnnotationProperty(Constants.FIXED_ANNOTATION_NAME,
				PrefixUtilities.getPrefixOWLOntologyFormat(activeOntology));

		String value = getRuleName() + "___" + getRuleText() + "___" + getComment();

		OWLAnnotationValue owlLiteral = owlDataFactory.getOWLLiteral(value);
		OWLAnnotation annotation = owlDataFactory.getOWLAnnotation(fixedAnnotationProperty, owlLiteral);

		return annotation;
	}

	public OWLAxiom addAxiomAnnotation(OWLAxiom axiom, OWLAnnotation annotation) {

		Set<OWLAnnotation> newAnnotations = new HashSet<OWLAnnotation>();

		newAnnotations.add(annotation);

		OWLAxiom annotatedAxiom = axiom.getAnnotatedAxiom(newAnnotations);

		return annotatedAxiom;
	}

	public void showAxiomsDialog() {

		JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
		// show the dialog
		AxiomsDialog dialog = new AxiomsDialog(this, topFrame, activeOntology);

		/**
		 * create axioms with annotation from the generated axiom annotation:-
		 * ruleName___ruleText___ruleComment
		 */
		Set<OWLAxiom> axiomWithAnnotations = new HashSet<OWLAxiom>();

		if (dialog.isClickedOK()) {
			Set<OWLAxiom> selectedAxioms = dialog.getSelectedAxioms();
			if (!selectedAxioms.isEmpty()) {
				for (OWLAxiom axiom : selectedAxioms) {
					axiomWithAnnotations.add(addAxiomAnnotation(axiom, getOWLAnnotation()));
				}

				//if the ruleID exist then remove the corresponding axioms first
				this.engine.deleteRule(getRuleName());
				
				// save changes in the ontology.
				applyChangetoOntology(axiomWithAnnotations);

				// show the rule in the table
				RuleModel rule = new RuleModel(getRuleName(), getRuleText(), getComment());
				this.engine.addARulesWithID(getRuleName(), rule);
				getRuleTableModel().updateView();
				
				//clear the rule textarea
				clearIfOk();
			}
		}
	}

	private void switchToSWRLTab(String ruleName, String ruleText, String ruleComment) {

		if (JOptionPane.OK_OPTION == JOptionPane.showOptionDialog(this, getPnlForSwitchToSWRLTab(ruleText),
				"Not transferable to OWL Axiom.", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null,
				null, null)) {
			// switch to swrltab;

			tabbedPane.setSelectedIndex(1);
			SWRLRuleEditorDialog dialog = (SWRLRuleEditorDialog) this.dialogManager.getSWRLRuleEditorDialog(this,
					ruleName, ruleText, ruleComment);
			dialog.setVisible(true);
			System.out.println("Clicked to switch to swrltab");

		}
	}

	private class ConvertSWRLRuleActionListener implements ActionListener {

		@NonNull
		Component parent;

		public ConvertSWRLRuleActionListener(Component parent) {
			this.parent = parent;
		}

		@Override
		public void actionPerformed(@NonNull ActionEvent e) {

			String ruleName = getRuleName();
			String ruleText = getRuleText();
			String comment = getComment();
			boolean errorOccurred;

			if (ruleName.trim().length() == 0) {
				getDialogManager().showErrorMessageDialog(this.parent, MISSING_RULE_NAME, MISSING_RULE_NAME_TITLE);
				errorOccurred = true;
			} else if (ruleText.trim().length() == 0) {
				getDialogManager().showErrorMessageDialog(this.parent, MISSING_RULE, MISSING_RULE);
				errorOccurred = true;
			} else if (getRuleTableModel().hasRule(ruleName) && !RuleEditorPanel.this.editMode) {
				getDialogManager().showErrorMessageDialog(this.parent, DUPLICATE_RULE_TEXT, DUPLICATE_RULE_TITLE);
				errorOccurred = true;
			} else {
				try {

					SWRLRule swrlRules = getSWRLRule(ruleText);

					if (swrlRules != null) {
						Set<OWLAxiom> owlAxioms = Transformer.ruleToAxioms(swrlRules);

						if (Transformer.isTransferred) {

							generatedAxioms.clear();
							generatedAxioms.addAll(owlAxioms);
							showAxiomsDialog();

						} else {
							// can not transfer to axioms need to switch
							// to swrltab

							switchToSWRLTab(ruleName, ruleText, comment);
							System.out.println("can not transfer to axioms");
						}
						return;
					}

					errorOccurred = false;

				} catch (SWRLParseException pe) {
					getDialogManager().showErrorMessageDialog(this.parent,
							(pe.getMessage() != null ? pe.getMessage() : ""), INVALID_RULE_TITLE);
					errorOccurred = true;
				} catch (RuntimeException pe) {
					getDialogManager().showErrorMessageDialog(this.parent,
							(pe.getMessage() != null ? pe.getMessage() : ""), INTERNAL_ERROR_TITLE);
					errorOccurred = true;
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					pe.printStackTrace(pw);
					log.warn(sw.toString());
				}

				if (!errorOccurred) {
					cancelEditMode();
				} else
					updateStatus();
			}
		}
	}

	public static void main(String[] arg) {
		JFrame frame = new JFrame();
		frame.add(new RuleEditorPanel());
		frame.setSize(500, 500);
		frame.setVisible(true);
	}

	private class EditorPopup extends JPopupMenu {
		/**
		 * 
		 */
		public EditorPopup(JTextPane textPane) {
			// TODO Auto-generated constructor stub
			add(new JMenuItem("Create Class"));
			add(new JMenuItem("Create ObjectProperty"));
		}
	}
}
