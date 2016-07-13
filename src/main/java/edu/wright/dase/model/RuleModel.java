package edu.wright.dase.model;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.dataflow.qual.SideEffectFree;

public class RuleModel {
	@NonNull
	private final String ruleName, ruleText, comment;

	public RuleModel(@NonNull String ruleName, @NonNull String ruleText, @NonNull String comment) {

		this.ruleText = ruleText;
		this.ruleName = ruleName;
		this.comment = comment;
	}

	@NonNull
	public String getRuleText() {
		return this.ruleText;
	}

	@NonNull
	public String getRuleName() {
		return this.ruleName;
	}

	@NonNull
	public String getComment() {
		return this.comment;
	}

	@SideEffectFree
	@NonNull
	@Override
	public String toString() {
		return "(ruleName: " + this.ruleName + ", ruleText: " + this.ruleText + ", comment: " + this.comment + ")";
	}
}

