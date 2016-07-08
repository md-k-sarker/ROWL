package edu.wsu.dase.view;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.swrlapi.ui.action.DisableAllRulesAction;
import org.swrlapi.ui.action.EnableAllRulesAction;
import org.swrlapi.ui.dialog.SWRLRuleEngineDialogManager;
import org.swrlapi.ui.model.SWRLRuleEngineModel;
import org.swrlapi.ui.model.SWRLRulesAndSQWRLQueriesTableModel;
import org.swrlapi.ui.view.SWRLAPIView;

import edu.wsu.dase.controller.Engine;
import edu.wsu.dase.model.RuleTableModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;

/**
 * Provides a model for graphical display of SWRL rules
 *
 * @see SWRLRulesAndSQWRLQueriesTableModel
 */
public class RuleTablePanel extends JPanel implements SWRLAPIView {
	private static final long serialVersionUID = 1L;

	private static final String EDIT_BUTTON_TITLE = "Edit";
	private static final String DELETE_BUTTON_TITLE = "Delete";

	private static final int ACTIVE_COLUMN_MAX_WIDTH = 50;
	private static final int RULE_NAME_COLUMN_PREFERRED_WIDTH = 150;
	private static final int RULE_NAME_COLUMN_MAX_WIDTH = 200;
	private static final int RULE_TEXT_COLUMN_PREFERRED_WIDTH = 500;
	private static final int RULE_TEXT_COLUMN_MAX_WIDTH = 1400;
	private static final int COMMENT_COLUMN_PREFERRED_WIDTH = 200;
	private static final int COMMENT_COLUMN_MAX_WIDTH = 300;

	@NonNull
	private final Engine engine;
	@NonNull
	private final RuleEditorPanel ruleEditorPanel;
	@NonNull
	private final RuleTableModel rulesTableModel;
	@NonNull
	private final JTable rulesTable;
	@NonNull
	private final JButton editButton, deleteButton;
	@NonNull
	private final Component parent;

	public RuleTablePanel(@NonNull Engine engine, @NonNull RuleTableModel ruleTableModel,
			@NonNull RuleEditorPanel ruleEditorPanel) {
		this.engine = engine;
		this.rulesTableModel = ruleTableModel;
		this.ruleEditorPanel = ruleEditorPanel;

		this.parent = (JFrame) SwingUtilities.getWindowAncestor(this);

		this.rulesTable = new JTable(this.rulesTableModel);
		
		this.rulesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.deleteButton = new JButton(DELETE_BUTTON_TITLE);
		this.editButton = new JButton(EDIT_BUTTON_TITLE);

		initialize();
	}

	@Override
	public void initialize() {
		// this.swrlRuleEngineModel.getSWRLRulesTableModel().setView(this);

		addTableListeners();

		setPreferredColumnWidths();

		createComponents();

		createPopupMenu();
	}

	@Override
	public void update() {
		getRulesTableModel().fireTableDataChanged();
		validate();
	}

	public Optional<@NonNull String> getSelectedSWRLRuleName() {
		int selectedRow = this.rulesTable.getSelectedRow();

		if (selectedRow != -1)
			return Optional.of(getRulesTableModel().getRuleNameByIndex(selectedRow));
		else
			return Optional.<@NonNull String> empty();
	}

	private Optional<@NonNull String> getSelectedSWRLRuleText() {
		int selectedRow = this.rulesTable.getSelectedRow();

		if (selectedRow != -1)
			return Optional.of(getRulesTableModel().getRuleTextByIndex(selectedRow));
		else
			return Optional.<@NonNull String> empty();
	}

	private Optional<@NonNull String> getSelectedSWRLRuleComment() {
		int selectedRow = this.rulesTable.getSelectedRow();

		if (selectedRow != -1)
			return Optional.of(getRulesTableModel().getRuleCommentByIndex(selectedRow));
		else
			return Optional.<@NonNull String> empty();
	}

	private void setPreferredColumnWidths() {
		TableColumnModel columnModel = this.rulesTable.getColumnModel();

		columnModel.getColumn(RuleTableModel.RULE_NAME_COLUMN).setPreferredWidth(RULE_NAME_COLUMN_PREFERRED_WIDTH);
		columnModel.getColumn(RuleTableModel.RULE_NAME_COLUMN).setMaxWidth(RULE_NAME_COLUMN_MAX_WIDTH);

		columnModel.getColumn(RuleTableModel.RULE_TEXT_COLUMN).setPreferredWidth(RULE_TEXT_COLUMN_PREFERRED_WIDTH);
		columnModel.getColumn(RuleTableModel.RULE_TEXT_COLUMN).setMaxWidth(RULE_TEXT_COLUMN_MAX_WIDTH);

		columnModel.getColumn(RuleTableModel.RULE_COMMENT_COLUMN).setPreferredWidth(COMMENT_COLUMN_PREFERRED_WIDTH);
		columnModel.getColumn(RuleTableModel.RULE_COMMENT_COLUMN).setMaxWidth(COMMENT_COLUMN_MAX_WIDTH);
	}

	private void addTableListeners() {
		this.rulesTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(@NonNull MouseEvent e) {
				if (e.getClickCount() == 2) {
					if (e.getSource() == RuleTablePanel.this.rulesTable)
						editSelectedSWRLRule();
				}
			}
		});

		this.rulesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (hasSelectedRule())
					enableEditAndDelete();
				else
					disableEditAndDelete();
			}
		});
	}

	private void editSelectedSWRLRule() {
		if (this.rulesTable.getSelectedRow() != -1) {
			String ruleName = getSelectedSWRLRuleName().get();
			String ruleText = getSelectedSWRLRuleText().get();
			String ruleComment = getSelectedSWRLRuleComment().get();

			this.ruleEditorPanel.loadEdittingRule(ruleName, ruleComment, ruleText);

		}
	}

	private void createComponents() {
		JScrollPane scrollPane = new JScrollPane(this.rulesTable);
		JViewport viewport = scrollPane.getViewport();

		setLayout(new BorderLayout());

		JPanel headingPanel = new JPanel(new BorderLayout());
		add(headingPanel, BorderLayout.SOUTH);

		viewport.setBackground(this.rulesTable.getBackground());

		JPanel buttonPanel = new JPanel(new BorderLayout());
		headingPanel.add(buttonPanel, BorderLayout.EAST);

		this.editButton.addActionListener(new EditRuleActionListener());
		buttonPanel.add(this.editButton, BorderLayout.CENTER);

		this.deleteButton.addActionListener(new DeleteRuleActionListener(this.parent));
		buttonPanel.add(this.deleteButton, BorderLayout.EAST);

		disableEditAndDelete(); // Will get enabled by listener on rule table if
								// a rule is selected

		add(scrollPane, BorderLayout.CENTER);

		validate();
	}

	private void enableEditAndDelete() {
		this.editButton.setEnabled(true);
		this.deleteButton.setEnabled(true);
	}

	private void disableEditAndDelete() {
		this.editButton.setEnabled(false);
		this.deleteButton.setEnabled(false);
	}

	private boolean hasSelectedRule() {
		return this.rulesTable.getSelectedRow() != -1;
	}

	private @NonNull RuleTableModel getRulesTableModel() {
		return this.rulesTableModel;
	}

	@NonNull
	private Engine getEngine() {
		return this.engine;
	}

	private class EditRuleActionListener implements ActionListener {
		public EditRuleActionListener() {

		}

		@Override
		public void actionPerformed(@NonNull ActionEvent e) {
			editSelectedSWRLRule();
		}
	}

	private class DeleteRuleActionListener implements ActionListener {
		Component parent;

		public DeleteRuleActionListener(@NonNull Component parent) {
			this.parent = parent;
		}

		@Override
		public void actionPerformed(@NonNull ActionEvent e) {
			deleteSelectedSWRLRule();
		}

		private void deleteSelectedSWRLRule() {
			Optional<@NonNull String> selectedRuleName = getSelectedSWRLRuleName();

			if (selectedRuleName.isPresent()) {
				if (RuleTablePanel.this.getRulesTableModel().hasRule(selectedRuleName.get()))

					if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this.parent,
							"Do you really want to delete the rule?", "Delete Rule", JOptionPane.YES_NO_OPTION)) {

						// delete the rule from engine
						getEngine().deleteRule(selectedRuleName.get());

						// reload tableModel
						getRulesTableModel().updateView();

						// request Table to update the view
						update();
					}
			}
		}
	}

	private void createPopupMenu() {
		JPopupMenu popup = new JPopupMenu();
		popup.add(new EnableAllRulesAction());
		popup.add(new DisableAllRulesAction());
		addMouseListener(new PopupListener(popup));
	}

	private class PopupListener extends MouseAdapter {
		@NonNull
		private final JPopupMenu popup;

		public PopupListener(@NonNull JPopupMenu popupMenu) {
			this.popup = popupMenu;
		}

		@Override
		public void mousePressed(@NonNull MouseEvent e) {
			maybeShowPopup(e);
		}

		@Override
		public void mouseReleased(@NonNull MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(@NonNull MouseEvent e) {
			if (e.isPopupTrigger())
				this.popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}

}
