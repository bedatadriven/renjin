package r.base;

import java.io.IOException;
import java.text.DecimalFormat;

import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.Before;
import org.junit.runner.RunWith;

import r.EvalTestCase;

@RunWith(ConcordionRunner.class)
public class ComplexNumberSpecTest extends EvalTestCase {

  @Before
  public void setup() throws IOException {
    topLevelContext.init();
  }

  public double r(String str) {
    return round(eval(str).asReal());
  }

  double round(double d) {
    DecimalFormat twoDForm = new DecimalFormat("#.#######");
    return Double.valueOf(twoDForm.format(d));
  }
}
