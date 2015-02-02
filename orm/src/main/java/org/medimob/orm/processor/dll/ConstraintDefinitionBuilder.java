package org.medimob.orm.processor.dll;

import org.medimob.orm.annotation.Action;
import org.medimob.orm.annotation.Conflict;
import org.medimob.orm.annotation.Sort;
import org.medimob.orm.internal.StatementBuilder;
import org.medimob.orm.processor.MappingException;

import static org.medimob.orm.processor.dll.DefinitionUtils.notEmpty;
import static org.medimob.orm.processor.dll.DefinitionUtils.notNull;

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
  private Action onUpdateAction;
  private Action onDeleteAction;
  private String referenceTable;
  private String referenceColumn;

  private ConstraintDefinitionBuilder(boolean columnConstraint) {
    this.columnConstraint = columnConstraint;
  }

  public static ConstraintDefinitionBuilder newTableConstraint() {
    return new ConstraintDefinitionBuilder(false);
  }

  public static ConstraintDefinitionBuilder newColumnConstraint() {
    return new ConstraintDefinitionBuilder(true);
  }

  public ConstraintDefinitionBuilder setOnDeleteAction(Action onDeleteAction) {
    this.onDeleteAction = onDeleteAction;
    return this;
  }

  public ConstraintDefinitionBuilder setOnUpdateAction(Action onUpdateAction) {
    this.onUpdateAction = onUpdateAction;
    return this;
  }

  public ConstraintDefinitionBuilder setReferenceColumn(String referenceColumn) {
    this.referenceColumn = referenceColumn;
    return this;
  }

  public ConstraintDefinitionBuilder setReferenceTable(String referenceTable) {
    this.referenceTable = referenceTable;
    return this;
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
    if (!columnConstraint) {
      notEmpty(name, "Constraint's  name is empty");
    }
    notNull(type, "Constraint's type cannot be null");
    switch (type) {
      case CHECK:
        notEmpty(exp, "Check expression cannot be empty");
        break;
      case COLLATE:
        notEmpty(exp, "Collation name cannot be empty");
        break;
      case DEFAULT:
        notEmpty(exp, "Default value cannot be empty");
        break;
      case PRIMARY_KEY:
        if (!columnConstraint) {
          notNull(columnNames, "Primary key columns cannot be null");
        }
        notNull(conflictClause, "Primary key on conflict clause cannot be null");
        break;
      case UNIQUE:
        if (!columnConstraint) {
          notNull(columnNames, "Unique columns cannot be null");
        }
        notNull(conflictClause, "Unique on conflict clause cannot be null");
        break;
      case NOT_NULL:
        notNull(conflictClause, "Not null on conflict clause cannot be null");
        break;
      case REFERENCES:
        notEmpty(referenceTable, "references table's name cannot be empty");
        notEmpty(referenceColumn, "references column's name cannot be empty");
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
      case REFERENCES:
        if (columnConstraint) {
          builder.appendWord(type.getSql());
          builder.appendWord(referenceTable);
          builder.appendBetweenBracket(referenceColumn);
          if (onDeleteAction != null) {
            builder.appendWord("ON DELETE");
            builder.appendWord(onDeleteAction.getSql());
          }
          if (onUpdateAction != null) {
            builder.appendWord("ON UPDATE");
            builder.appendWord(onUpdateAction.getSql());
          }
        }
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
