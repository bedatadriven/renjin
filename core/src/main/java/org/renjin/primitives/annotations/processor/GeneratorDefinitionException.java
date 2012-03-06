package org.renjin.primitives.annotations.processor;

/**
 * Exception thrown when there is a problem in the way that
 * the (renjin) developer has defined or annotated a primitive method.
 * 
 * @author alex
 *
 */
public class GeneratorDefinitionException extends RuntimeException {

  public GeneratorDefinitionException() {
    super();
  }

  public GeneratorDefinitionException(String message, Throwable cause) {
    super(message, cause);
  }

  public GeneratorDefinitionException(String message) {
    super(message);
  }

  public GeneratorDefinitionException(Throwable cause) {
    super(cause);
  }

}
