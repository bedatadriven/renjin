package r.parser;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.junit.Test;

import r.EvalTestCase;
import r.io.DatafileReader;
import r.lang.ListVector;
import r.lang.Null;
import r.lang.PairList;
import r.lang.PairList.Node;
import r.lang.SEXP;
import r.lang.StringVector;
import r.lang.Symbol;

public class RdParserTest extends EvalTestCase {

  @Test
  public void simple() throws IOException {
    
    RdParser parser = new RdParser();
    ListVector result = (ListVector) parser.R_ParseRd(new StringReader("\\name{plotf}"), Null.INSTANCE, false);
    assertThat(result.length(), equalTo(1));
 
    ListVector header = (ListVector) result.getElementAsSEXP(0);
    assertThat(header.getAttribute(Symbol.get("Rd_tag")), equalTo(c("\\name")));

    StringVector name = (StringVector) header.getElementAsSEXP(0);
    assertThat(name, equalTo(c("plotf")));
    //assertThat(name.getAttribute(Symbol.get("Rd_tag")), equalTo(c("VERB")));
    
  }
  
  @Test
  public void comparison() throws IOException {
    
    // output from C-R 2.12.1
    DatafileReader reader = new DatafileReader(topLevelContext, topLevelContext.getEnvironment(), 
        getClass().getResourceAsStream("expected.Rdata"));
    PairList.Node contents = (Node) reader.readFile();
    ListVector expected = contents.getElementAsSEXP(0);
    
    assertThat(expected.length(), equalTo(3));
    
    compareTree("test1.Rd", expected.get(0));  
    compareTree("test2.Rd", expected.get(1));
    compareTree("test3.Rd", expected.get(2));
  }

  private void compareTree(String resourceName, SEXP expected) throws IOException {
    InputStream in = getClass().getResourceAsStream(resourceName);
    InputStreamReader reader = new InputStreamReader(in);
    
    RdParser parser = new RdParser();
    ListVector result = (ListVector) parser.R_ParseRd(reader, Null.INSTANCE, false);

    compareNode(resourceName, result, expected);
  }

  private void compareNode(String node, SEXP result, SEXP expected) {
    for(PairList.Node attribute : expected.getAttributes().nodes()) {
      // TODO: srcref at root node is missing
      if(!attribute.getName().equals("srcref")) {
        assertThat(node + "/attribute[" + attribute.getTag() + "]", result.getAttribute(attribute.getTag()), equalTo(attribute.getValue()));
      }
    }
    if(expected instanceof ListVector) {
      if(!(result instanceof ListVector) || result.length() != expected.length()) {
        assertThat(node, result, equalTo(expected));
      } else {
        for(int i=0;i!=expected.length();++i) {
          compareNode(describeChildNode(node, (ListVector)expected, i), 
              result.getElementAsSEXP(i), ((ListVector) expected).getElementAsSEXP(i));
        }
      }
    } else {
      assertThat(node, result, equalTo(expected));
    }
  }
  
  private String describeChildNode(String parent, ListVector vector, int index) {
    StringBuilder sb = new StringBuilder();
    if(!parent.isEmpty()) {
      sb.append(parent).append("/");
    }
    sb.append("list{");
    boolean needsComma = false;
    for(PairList.Node attrib : vector.getAttributes().nodes()) {
      if(!attrib.getName().equals("srcref")) {
        if(needsComma) {
          sb.append(", ");
        } else {
          needsComma = true;
        }
        sb.append(attrib.getName() + "=" + attrib.getValue());
      }
    }
    sb.append("}/element[");
    sb.append(index);
    sb.append("]");
    return sb.toString();
  }
}
