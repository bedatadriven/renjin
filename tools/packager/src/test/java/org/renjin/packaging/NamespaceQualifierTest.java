package org.renjin.packaging;

import org.junit.Test;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.primitives.Deparse;
import org.renjin.primitives.packaging.NamespaceFile;
import org.renjin.repackaged.guava.io.CharSource;
import org.renjin.sexp.ExpressionVector;
import org.renjin.sexp.SEXP;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class NamespaceQualifierTest {

  @Test
  public void test() throws IOException {
    CharSource namespaceFile = CharSource.wrap("import(BBmisc, except = isFALSE)\n");
    Map<String, String> packageGroupMap = new HashMap<>();

    NamespaceQualifier qualifier = new NamespaceQualifier(packageGroupMap);
    ExpressionVector vector = qualifier.qualify(NamespaceFile.parseSexp(namespaceFile));

    Session session = new SessionBuilder().build();

    SEXP statement = vector.getElementAsSEXP(0);
    String string = Deparse.deparseExp(session.getTopLevelContext(), statement);

    assertThat(string, equalTo("import(\"BBmisc\", except = isFALSE)"));
  }

}