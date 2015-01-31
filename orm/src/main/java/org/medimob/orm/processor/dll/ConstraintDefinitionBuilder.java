package org.medimob.orm.processor.dll;

import org.medimob.orm.annotation.Conflict;
import org.medimob.orm.annotation.Sort;
import org.medimob.orm.internal.StatementBuilder;
import org.medimob.orm.processor.MappingException;

/**
 * Column or table constraint definition. Created by Poopaou on 21/01/2015.
 */
public class ConstraintDefinitionBuilder {

  private Constraints type;
  private String name;
  private String[] columnNames;
  private String exp;
  private Conflict conflictClause;
  private Sort sort;
  private boolean autoincrement;
  private boolean columnConstraint;

  private ConstraintDefinitionBuilder(boolean columnConstraint) {
    this.columnConstraint = columnConstraint;
  }

  public static ConstraintDefinitionBuilder newTableConstraint() {
    return new ConstraintDefinitionBuilder(false);
  }

  public static ConstraintDefinitionBuilder newColumnConstraint() {
    return new ConstraintDefinitionBuilder(true);
  }

  public ConstraintDefinitionBuilder setType(Constraints type) {
    this.type = type;
    return this;
  }

  public ConstraintDefinitionBuilder setName(String name) {
    this.name = name;
    return this;
  }

  public ConstraintDefinitionBuilder setColumnNames(String[] columnNames) {
    this.columnNames = columnNames;
    return this;
  }

  public ConstraintDefinitionBuilder setExp(String exp) {
    this.exp = exp;
    return this;
  }

  public ConstraintDefinitionBuilder setConflictClause(Conflict conflictClause) {
    this.conflictClause = conflictClause;
    return this;
  }

  public ConstraintDefinitionBuilder setSort(Sort sort) {
    this.sort = sort;
    return this;
  }

  public ConstraintDefinitionBuilder setAutoincrement(boolean autoincrement) {
    this.autoincrement = autoincrement;
    return this;
  }

  public ConstraintDefinition build() throws MappingException {
    validate();
    return new ConstraintDefinition(type, name, getStatement());
  }

  private void validate() throws MappingException {
    DefinitionUtils.notEmpty(name, "Constraint's  name is empty");
    DefinitionUtils.notNull(type, "Constraint's type cannot be null");
    switch (type) {
      case CHECK:
        DefinitionUtils.notEmpty(exp, "Check expression cannot be empty");
        break;
      case COLLATE:
        DefinitionUtils.notEmpty(exp, "Collation name cannot be empty");
        break;
      case DEFAULT:
        DefinitionUtils.notEmpty(exp, "Default value cannot be empty");
        break;
      case PRIMARY_KEY:
        if (!columnConstraint) {
          DefinitionUtils.notNull(columnNames, "Primary key columns cannot be null");
        }
        DefinitionUtils.notNull(conflictClause, "Primary key on conflict clause cannot be null");
        break;
      case UNIQUE:
        if (!columnConstraint) {
          DefinitionUtils.notNull(columnNames, "Unique columns cannot be null");
        }
        DefinitionUtils.notNull(conflictClause, "Unique on conflict clause cannot be null");
        break;
      case NOT_NULL:
        DefinitionUtils.notNull(conflictClause, "Not null on conflict clause cannot be null");
        break;
      case FOREIGN_KEY:
        // FIXME
        break;
      default:
        throw new IllegalArgumentException("unhandled type " + type);
    }
  }

  private String getStatement() {
    StatementBuilder builder = new StatementBuilder();
    if (!columnConstraint) {
      builder.appendWord("CONSTRAINT");
    }
    switch (type) {
      case CHECK:
        if (!columnConstraint) {
          builder.appendWord("CHK_" + name.toUpperCase());
        }
        builder.appendWord(type.getSql());
        builder.appendBetweenBracket(exp);
        break;
      case UNIQUE:
        if (!columnConstraint) {
          builder.appendWord("UNI_" + name.toUpperCase());
          builder.appendWord(type.getSql());

          builder.openBracket();
          builder.appendWithSeparator(columnNames, ',');
          builder.closeBracket();
          builder.appendWord(conflictClause.getSql());
        }
        break;
      case PRIMARY_KEY:
        if (!columnConstraint) {
          builder.appendWord("PK_" + name.toUpperCase());
        }
        builder.appendWord(type.getSql());
        if (columnConstraint) {
          if (sort != null) {
            builder.appendWord(sort.getSql());
          }
          if (autoincrement) {
            builder.appendWord("AUTOINCREMENT");
          }
        } else {
          builder.openBracket();
          builder.appendWithSeparator(columnNames, ',');
          builder.closeBracket();
          builder.appendWord(conflictClause.getSql());
        }
        break;
      case FOREIGN_KEY:
        if (!columnConstraint) {
          builder.appendWord("FK_" + name.toUpperCase());
        }
        builder.appendWord(type.getSql());
        builder.openBracket();
        builder.appendWithSeparator(columnNames, ',');
        builder.closeBracket();
        // FIXME...
        break;
      case DEFAULT:
        if (!columnConstraint) {
          builder.appendWord("DEF_" + name.toUpperCase());
        }
        builder.appendWord(type.getSql());
        builder.appendWord(exp);
        break;
      case COLLATE:
        if (!columnConstraint) {
          builder.appendWord("COL_" + name.toUpperCase());
        }
        builder.appendWord(type.getSql());
        builder.appendWord(exp);
        break;
      case NOT_NULL:
        if (!columnConstraint) {
          builder.appendWord("NNL_" + name.toUpperCase());
        }
        builder.appendWord(type.getSql());
        break;
      default:
        throw new IllegalArgumentException("unhandled type " + type);
    }
    return builder.toString();
  }
}
