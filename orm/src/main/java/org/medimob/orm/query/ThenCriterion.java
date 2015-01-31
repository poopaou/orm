package org.medimob.orm.query;

/**
 * Created by Poopaou on 30/01/2015.
 */
public interface ThenCriterion extends Criterion {

  ThenCriterion when(Criterion whenCriterion, Criterion elseCriterion);

  Criterion elseEnd(Criterion elseCriterion);
}
