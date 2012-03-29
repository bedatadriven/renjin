package org.renjin;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;


public class SurveyPackageTest extends PackageTest {

  @Test
  public void surveyPackage() throws Exception {

    java.lang.System.out.println(eval(".find.package('survey') "));
    eval(" library(survey) ");

    assertThat( eval(" data(hospital, verbose=TRUE) "), equalTo(c("hospital")) );

    java.lang.System.out.println( eval("ls() "));

    assertThat(eval("typeof(hospital)"), equalTo(c("list")));
    assertThat(eval("sum(hospital$births)"), equalTo(c(25667)));

    eval("dstr <- svydesign(id = ~1, strata = ~oblevel, fpc = ~tothosp, weight = ~weighta, data = hospital)");
    eval("r <- svymean(~births, dstr)");
    
    assertThat( eval("r['births']"), closeTo(c(1164.4), 0.1));
    assertThat( eval("attr(r,'var')['births','births']"), closeTo(c(46345.78), 0.01));

  }
}
