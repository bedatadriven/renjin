/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.io;

import org.junit.Test;
import r.lang.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class DatafileWriterTest {

  @Test
  public void test() throws IOException {

    ListVector.Builder list = new ListVector.Builder();
    list.add("foo", new StringVector("zefer", "fluuu"));
    list.setAttribute("categories", new IntVector(3));


    PairList.Builder file = new PairList.Builder();
    file.add("a", new StringVector("who", "am", "i", StringVector.NA));
    file.add("b", new IntVector(1, 2, 3, IntVector.NA, 4));
    file.add("c", new LogicalVector(Logical.NA, Logical.FALSE, Logical.TRUE));
    file.add("d", new DoubleVector(3.14, 6.02, DoubleVector.NA));
    file.add("l", list.build());


    assertReRead(file.build());
    write("test.rdata", file.build());
  }

  @Test
  public void testVerySimple() throws IOException {
    PairList.Builder pl = new PairList.Builder();
    pl.add("serialized", new IntVector(1,2,3,4));

    PairList list = pl.build();

    assertReRead(list);
    write("testsimple.rdata", list);
  }

  private void write(String fileName, SEXP exp) throws IOException {
    FileOutputStream fos = new FileOutputStream(fileName);
    GZIPOutputStream zos = new GZIPOutputStream(fos);
    DatafileWriter writer = new DatafileWriter(zos);
    writer.writeExp(exp);
    zos.close();
  }

  private void assertReRead(SEXP exp) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DatafileWriter writer = new DatafileWriter(baos);
    writer.writeExp(exp);

    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    Context context = Context.newTopLevelContext();
    DatafileReader reader = new DatafileReader(context, context.getEnvironment(), bais);
    SEXP resexp = reader.readFile();

    assertThat(resexp, equalTo(exp));
  }
}
