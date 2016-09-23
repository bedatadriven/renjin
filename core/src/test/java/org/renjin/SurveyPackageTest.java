/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
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
