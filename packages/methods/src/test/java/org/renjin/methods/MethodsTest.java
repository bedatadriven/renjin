package org.renjin.methods;


import org.junit.Before;
import org.junit.Test;
import org.renjin.eval.EvalException;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;
import org.renjin.sexp.ExpressionVector;
import org.renjin.sexp.SEXP;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertThat;

public class MethodsTest {

    private Session session;

    @Before
    public void setUp() throws Exception {
        session = new SessionBuilder().build();
        eval("library(methods)");
    }

    private SEXP eval(String source) throws IOException {
        ExpressionVector sexp = RParser.parseAllSource(new StringReader(source + "\n"));
        try {
            return session.getTopLevelContext().evaluate(sexp);
        } catch (EvalException e) {
            e.printRStackTrace(System.out);
            throw new RuntimeException(e);
        }
    }
    
    @Test
    public void hadley() throws IOException {
        eval("setClass('Person', representation(name = 'character', age = 'numeric'))");
        eval("setClass('Employee', representation(boss = 'Person'), contains = 'Person')");
        eval("hadley <- new(\"Person\", name = \"Hadley\", age = 31)");
        
        eval("print(hadley@age)");

    }
    
}
