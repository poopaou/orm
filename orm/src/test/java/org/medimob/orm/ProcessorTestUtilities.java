package org.medimob.orm;

import org.medimob.orm.processor.EntityProcessor;

import java.util.Arrays;

import javax.annotation.processing.Processor;

class ProcessorTestUtilities {

  static Iterable<? extends Processor> OrmProcessors() {
    return Arrays.asList(
        new EntityProcessor()
    );
  }
}
