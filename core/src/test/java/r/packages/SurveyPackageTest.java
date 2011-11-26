package r.packages;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class SurveyPackageTest extends PackageTest {

  @Test
  public void surveyPackage() throws Exception {
    topLevelContext.init();

    java.lang.System.out.println(eval(".find.package('survey') "));
    eval(" library(survey) ");

    assertThat( eval(" data(hospital, verbose=TRUE) "), equalTo(c("hospital")) );

    java.lang.System.out.println( eval("ls() "));

    assertThat(eval("typeof(hospital)"), equalTo(c("list")));
    assertThat(eval("sum(hospital$births)"), equalTo(c(25667)));

    eval("dstr <- svydesign(id = ~1, strata = ~oblevel, fpc = ~tothosp, weight = ~weighta, data = hospital)");
   // eval("print(svymean(~births, dstr))");
  }
}
