package org.swrltab.util;

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;

public class SWRLRuleEditorAutoCompleteState
{
  @NonNull private final String prefix;
  @NonNull private final List<@NonNull String> expansions;
  private final int textPosition;
  private int expansionIndex;

  public SWRLRuleEditorAutoCompleteState(int textPosition, @NonNull String prefix, @NonNull List<@NonNull String> expansions)
  {
    this.textPosition = textPosition;
    this.prefix = prefix;
    this.expansions = new ArrayList<>(expansions);
    this.expansionIndex = 0;
  }

  public int getTextPosition()
  {
    return this.textPosition;
  }

  @NonNull public String getPrefix()
  {
    return this.prefix;
  }

  @NonNull public String getCurrentExpansion()
  {
    if (!this.expansions.isEmpty()) {
      return this.expansions.get(this.expansionIndex);
    } else
      return "";
  }

  @NonNull public String getNextExpansion()
  {
    if (!this.expansions.isEmpty()) {
      this.expansionIndex++;

      if (this.expansionIndex == this.expansions.size())
        this.expansionIndex = 0;

      return this.expansions.get(this.expansionIndex);
    } else
      return "";
  }
}
