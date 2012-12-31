package org.renjin;

import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


@Ignore("needs to be moved out of core")
public class SurveyPackageTest extends PackageTest {

  @Test
  public void surveyPackage() throws Exception {

    eval(" library(survey, lib.loc='src/test/resources') ");

    assertThat( eval(" data(hospital, verbose=TRUE) "), equalTo(c("hospital")) );

    assertThat(eval("typeof(hospital)"), equalTo(c("list")));
    assertThat(eval("sum(hospital$births)"), equalTo(c(25667)));

    eval("dstr <- svydesign(id = ~1, strata = ~oblevel, fpc = ~tothosp, weight = ~weighta, data = hospital)");
    eval("r <- svymean(~births, dstr)");
    
    assertThat( eval("r['births']"), closeTo(c(1164.4), 0.1));
    assertThat( eval("attr(r,'var')['births','births']"), closeTo(c(46345.78), 0.01));

  }
}
