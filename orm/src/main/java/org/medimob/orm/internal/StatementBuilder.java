package org.medimob.orm.internal;

import android.support.annotation.NonNull;

/**
 * Created by Poopaou on 17/01/2015.
 */
public class StatementBuilder {

  private StringBuilder builder;

  public StatementBuilder() {
    this.builder = new StringBuilder();
  }

  /**
   * Append a new sql word.
   *
   * @param word to append
   * @return builder.
   */
  @NonNull
  public StatementBuilder appendWord(@NonNull String word) {
    this.builder.append(' ')
        .append(word);
    return this;
  }

  /**
   * Append expression between brackets
   *
   * @param exp expression to append
   * @return builder.
   */
  @NonNull
  public StatementBuilder appendBetweenBracket(@NonNull String exp) {
    this.builder.append(" (")
        .append(exp)
        .append(") ");
    return this;
  }

  /**
   * Open bracket.
   *
   * @return builder.
   */
  @NonNull
  public StatementBuilder openBracket() {
    this.builder.append(" ( ");
    return this;
  }

  /**
   * Open expression with separators between each expression.
   *
   * @return builder.
   */
  @NonNull
  public StatementBuilder appendWithSeparator(@NonNull String[] expressions, char separator) {
    for (int i = 0; i < expressions.length; i++) {
      if (i > 0) {
        builder.append(' ');
        builder.append(separator);
      }
      builder.append(expressions[i]);
    }
    return this;
  }

  /**
   * Close bracket.
   *
   * @return builder.
   */
  public StatementBuilder closeBracket() {
    this.builder.append(" )");
    return this;
  }

  @Override
  public String toString() {
    return builder.toString().trim();
  }
}
