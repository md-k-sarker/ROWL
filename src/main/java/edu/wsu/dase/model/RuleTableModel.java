package edu.wsu.dase.model;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.table.AbstractTableModel;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.swrlapi.core.SWRLAPIRule;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.ui.view.SWRLAPIView;

import edu.wsu.dase.controller.Engine;

/**
 * This class models a list of SWRL rules or SQWRL queries in an ontology for
 * tabular display.
 *
 * @see org.swrlapi.ui.model.SWRLRuleEngineModel
 * @see org.swrlapi.core.SWRLAPIRule
 * @see org.swrlapi.sqwrl.SQWRLQuery
 */
public class RuleTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	public static final int RULE_NAME_COLUMN = 0;
	public static final int RULE_TEXT_COLUMN = 1;
	public static final int RULE_COMMENT_COLUMN = 2;

	private static final String RULE_NAME_COLUMN_TITLE = "Name";
	private static final String RULE_TEXT_COLUMN_TITLE = "Rule";
	private static final String RULE_COMMENT_COLUMN_TITLE = "Comment";

	@NonNull
	private Optional<@NonNull SWRLAPIView> view = Optional.<@NonNull SWRLAPIView> empty();

	public static final int NUMBER_OF_COLUMNS = 3;

	@NonNull
	private Engine engine;

	/**
	 * rule name -> SWRLRuleModel
	 */
	@NonNull
	private final SortedMap<@NonNull String, @NonNull RuleModel> ruleModels;
	// @NonNull
	// private Optional<@NonNull SWRLAPIView> view = Optional.<@NonNull
	// SWRLAPIView> empty();

	private boolean isModified;

	public RuleTableModel(@NonNull Engine engine) {
		this.engine = engine;
		this.ruleModels = new TreeMap<>();
		this.isModified = false;

		updateRuleModels();
	}

	public void setView(@NonNull SWRLAPIView view) {
		this.view = Optional.of(view);
		updateRuleModels();
	}

	public void updateModel(@NonNull Engine engine) {
		this.engine = engine;
		this.ruleModels.clear();
		this.isModified = false;

		// updateView();
	}

	@NonNull
	public Set<@NonNull RuleModel> getRuleModels() {
		return new HashSet<>(this.ruleModels.values());
	}

	public boolean hasRules() {
		return !this.ruleModels.isEmpty();
	}

	@NonNull
	public String getRuleNameByIndex(int ruleIndex) {
		Optional<@NonNull RuleModel> ruleModel = getRuleModelByIndex(ruleIndex);

		if (ruleModel.isPresent())
			return ruleModel.get().getRuleName();
		else
			return "<INVALID_INDEX>";
	}

	@NonNull
	public String getRuleTextByIndex(int ruleIndex) {
		Optional<@NonNull RuleModel> ruleModel = getRuleModelByIndex(ruleIndex);

		if (ruleModel.isPresent())
			return ruleModel.get().getRuleText();
		else
			return "<INVALID_INDEX>";
	}

	@NonNull
	public String getRuleCommentByIndex(int ruleIndex) {
		Optional<@NonNull RuleModel> ruleModel = getRuleModelByIndex(ruleIndex);

		if (ruleModel.isPresent())
			return ruleModel.get().getComment();
		else
			return "<INVALID_INDEX>";
	}

	public boolean hasRule(@NonNull String ruleName) {
		return this.ruleModels.containsKey(ruleName);
	}

	public boolean hasBeenModified() {
		return this.isModified;
	}

	public void clearModifiedStatus() {
		this.isModified = false;
	}

	@Override
	public int getRowCount() {
		return this.ruleModels.size();
	}

	@Override
	public int getColumnCount() {
		return NUMBER_OF_COLUMNS;
	}

	@NonNull
	@Override
	public String getColumnName(int column) {
		if (column == RULE_NAME_COLUMN)
			return RULE_NAME_COLUMN_TITLE;
		else if (column == RULE_TEXT_COLUMN)
			return RULE_TEXT_COLUMN_TITLE;
		else if (column == RULE_COMMENT_COLUMN)
			return RULE_COMMENT_COLUMN_TITLE;
		else
			return "INVALID COLUMN";
	}

	@NonNull
	@Override
	public Object getValueAt(int row, int column) {
		if ((row < 0 || row >= getRowCount()) || ((column < 0 || column >= getColumnCount())))
			return "OUT OF BOUNDS";
		else {
			if (column == RULE_TEXT_COLUMN)
				return ((RuleModel) this.ruleModels.values().toArray()[row]).getRuleText();
			else if (column == RULE_NAME_COLUMN)
				return ((RuleModel) this.ruleModels.values().toArray()[row]).getRuleName();
			else if (column == RULE_COMMENT_COLUMN)
				return ((RuleModel) this.ruleModels.values().toArray()[row]).getComment();

			return "INVALID COLUMN";
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {

		return super.getColumnClass(columnIndex);
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

		super.setValueAt(aValue, rowIndex, columnIndex);
	}

	public void updateView() {

		updateRuleModels();
		if (this.view != null && this.view.isPresent()) {
			this.view.get().update();
		}
	}

	@NonNull
	private Optional<@NonNull RuleModel> getRuleModelByIndex(int ruleIndex) {
		if (ruleIndex >= 0 && ruleIndex < this.ruleModels.values().size())
			return Optional.of(((RuleModel) this.ruleModels.values().toArray()[ruleIndex]));
		else
			return Optional.<@NonNull RuleModel> empty();
	}

	private void updateRuleModels() {
		this.ruleModels.clear();

		TreeMap<String, RuleModel> rulesWithID = engine.getRules();

		for (String key : rulesWithID.keySet()) {
			this.ruleModels.put(key, rulesWithID.get(key));
		}
	}

	@Override
	public String toString() {
		return "RulesTableModel{" + "engine=" + engine + ", ruleModels=" + ruleModels + ",  isModified=" + isModified
				+ '}';
	}

}
