package org.renjin.simple;

import org.junit.Assert;
import org.junit.Before;
import org.junit.internal.AssumptionViolatedException;
import org.renjin.eval.EvalException;
import org.renjin.primitives.Deparse;
import org.renjin.primitives.Identical;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.script.RenjinScriptEngineFactory;
import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.SEXP;

import javax.script.ScriptException;

/**
 * simple tests run string snippets of R code (usually one line) using debugging output format
 */
public class SimpleTestBase {

  private RenjinScriptEngine engine;

  @Before
  public void setupEngine() {
    engine = new RenjinScriptEngineFactory().getScriptEngine();
  }

  /** Asserts that given source evaluates to the expected result and that no errors were reported and no exceptions
   * raised.
   */
  protected void assertEval(String input, String expectedResult)  {

    try {
      SEXP result = (SEXP) engine.eval(input);
      SEXP expectedResultExp = (SEXP) engine.eval(expectedResult);

      if(!Identical.identical(result, expectedResultExp, true, true, true, false)) {
        throw new AssertionError(String.format("Expected '%s', got '%s'", expectedResultExp.toString(),
          Deparse.deparse(engine.getTopLevelContext(), result, 80, true, 0, 0)));
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected void assertEval(String input, String expectedOutput, String expectedResult) {

    //TODO: check expected output
    assertEval(input, expectedResult);
//    EvalResult result = testEval(input);
//    Assert.assertEquals("Evaluation result mismatch", expectedResult, result.result);
//    Assert.assertEquals("Evaliation output mismatch", expectedOutput, result.stdout);
//    Assert.assertFalse("Error marker was found", result.stderr.contains("ManageError.ERROR"));
//    Assert.assertTrue("Exception was thrown", result.exception == null);
  }

  /** Asserts that given source evaluates to given result and no errors, warnings or exceptions are reported or
   * thrown.
   */
  protected void assertEvalNoWarnings(String input, String expectedResult)  {

    try {
      engine.eval(input);
    } catch (ScriptException e) {
      throw new RuntimeException("eval exception", e);
    }

//    EvalResult result = testEval(input);
//    Assert.assertEquals("Evaluation result mismatch", expectedResult, result.result);
//    Assert.assertFalse("Error marker was found", result.stderr.contains("ManageError.ERROR"));
//    Assert.assertFalse("Warning marker was found", result.stderr.contains("ManageError.WARNING"));
//    Assert.assertTrue("Exception was thrown", result.exception == null);
  }

  // FIXME: this can be done without text capture, using Junit's rules and expectedException
  // FIXME: note, ExpectedException cannot be overridden, but one can implement a MethodRule

//  @Rule
//  public ExpectedException thrown = ExpectedException.none();
//
//  @Test(expected = RError.class)
//  public void testUnused1()  {
//      evalString("{ x<-function(){1} ; x(y=1) }");
//      Assert.fail("Should not be reached");
//  }
//
//  @Test(expected = RError.class)
//  public void testUnused2()  {
//      evalString("{ x<-function(){1} ; x(1) }");
//      Assert.fail("Should not be reached");
//  }


  /** Asserts that given source evaluation results in an error being reported and the exception thrown.
   */
  protected void assertEvalError(String input, String expectedError)  {
    try {
      engine.eval(input);
      // whoops, we've reached here without an error
      throw new AssertionError("Expected '" + input + "' to throw error");
    } catch(EvalException e) {
      // good boy!
    } catch(Exception e) {
      throw new AssertionError("Expected EvalException, got " + e.getClass().getName(), e);
    }
    // TODO: check error message?
  }

  /** Asserts that given source evaluates to an expected result and that a warning is produced in the stderr that
   * contains the specified text.
   */
  static void assertEvalWarning(String input, String expectedResult, String expectedWarning)  {
    // TODO:
//    EvalResult result = testEval(input);
//    Assert.assertEquals("Evaluation result mismatch", expectedResult, result.result);
//    Assert.assertTrue("Output expected to contain warning: " + expectedWarning, result.stderr.contains(expectedWarning));
//    Assert.assertTrue("Warning marker not found.", result.stderr.contains("ManageError.WARNING"));
//    Assert.assertTrue("Exception was thrown", result.exception == null);
  }

  protected void assertTrue(String input) {

    try {
      SEXP result = (SEXP) engine.eval(input);
      if(!(result instanceof LogicalVector)) {
        throw new AssertionError("Expected TRUE, got " + result);
      }
    } catch (ScriptException e) {
      throw new RuntimeException(e);
    }

  }

}
