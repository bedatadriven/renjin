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
package org.renjin.primitives;

import org.junit.Ignore;
import org.junit.Test;
import org.renjin.EvalTestCase;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;


public class SystemTest extends EvalTestCase {
  
  @Ignore("platform dependent")
  @Test
  public void listFiles() {
    eval(" list.files <- function (path = '.'," +
        "pattern = NULL, all.files = FALSE, full.names = FALSE, " +
        "recursive = FALSE, ignore.case = FALSE) " +
        ".Internal(list.files(path, pattern, all.files, full.names, recursive, ignore.case))");

    assertThat( eval("list.files('classpath:/afolder')"), equalTo( c("file1.ext", "second.file")));
    assertThat( eval("list.files('classpath:/afolder', all.files=TRUE)"), equalTo( c(".", "..", ".secret", "file1.ext", "second.file")));
    assertThat( eval("list.files('classpath:/afolder', all.files=TRUE, full.names=TRUE)"),
        equalTo( c(fullPathPlus("."), fullPathPlus(".."), fullPath(".secret"), fullPath("file1.ext"), fullPath("second.file"))));

    assertThat( eval("list.files('classpath:/r/library', pattern='^survey$')"), equalTo( c("survey")) );
  }

  private String fullPath(String name) {
    return getClass().getResource("/afolder/" + name)
        .getFile().toString().replace("\\", "/");
  }

  private String fullPathPlus(String name) {
    return fullPath("") + name;
  }

  @Test
  public void dirname() {
    assertThat( eval(" .Internal(dirname(c('c:\\\\anyfolder\\\\file.txt', '/bin/bash', 'myfile'))) "),
        equalTo( c("c:\\anyfolder", "/bin", ".") ));
  }

  @Test
  public void basename() {
    assertThat( eval(" .Internal(basename(c('c:\\\\anyfolder\\\\file.txt', '/bin/bash', 'myfile'))) "),
        equalTo( c("file.txt", "bash", "myfile") ));
  }

  @Test
  @Ignore
  public void date(){
    assertThat( eval(" .Internal(date()) "), equalTo( c("Fri Sep 19 12:20:00 2011") ));
  }

  @Test
  public void SysSleep() {
    assumingBasePackagesLoad();

    long start = java.lang.System.currentTimeMillis();
    eval("Sys.sleep(1)");
    long stop = java.lang.System.currentTimeMillis();

    // We aren't guaranteed to be woken up after 1000 milliseconds,
    // so the most we can probably do is verify that *some* sleeping occurred
    assertThat((double)(stop-start), greaterThan(900d));
  }

  @Test
  public void parseCommandLineArgs() {
    List<String> parsed = System.parseArgs("cp -Lr --preserve=timestamps 'datasets' '/tmp/Rbuild'");
    assertThat(parsed.get(0), equalTo("cp"));
    assertThat(parsed.get(1), equalTo("-Lr"));
    assertThat(parsed.get(2), equalTo("--preserve=timestamps"));
    assertThat(parsed.get(3), equalTo("datasets"));
    assertThat(parsed.get(4), equalTo("/tmp/Rbuild"));
  }
}
