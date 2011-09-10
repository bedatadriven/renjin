package r.base.matrix;

import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import r.EvalTestCase;
import r.lang.DoubleVector;
import r.lang.SEXP;
import r.lang.Vector;

public class MatrixTest extends EvalTestCase {

  @Test
  public void svd() throws IOException {
    topLevelContext.init();
    
    eval("hilbert <- function(n) { i <- 1:n; 1 / outer(i - 1, i, '+') }");
    eval("X <- hilbert(9)[,1:6]");
    eval("s <- svd(X)");
    
    assertThat( eval("s$d"), closeTo(c(
        1.668433e+00, 
        2.773727e-01, 
        2.223722e-02, 
        1.084693e-03, 
        3.243788e-05, 
        5.234864e-07), 0.000001) );
    
    assertThat( eval("s$u"), closeTo(matrix(
        row(-0.7244999,  0.6265620,  0.27350003, -0.08526902,  0.02074121, -0.004024550),
        row(-0.4281556, -0.1298781, -0.64293597,  0.55047428, -0.27253421,  0.092815916),
        row(-0.3121985, -0.2803679, -0.33633240, -0.31418014,  0.61632113, -0.440903754),
        row(-0.2478932, -0.3141885, -0.06931246, -0.44667149,  0.02945426,  0.530119859),
        row(-0.2063780, -0.3140734,  0.10786005, -0.30241655, -0.35566839,  0.237038375),
        row(-0.1771408, -0.3026808,  0.22105904, -0.09041508, -0.38878613, -0.260449267),
        row(-0.1553452, -0.2877310,  0.29280775,  0.11551327, -0.19285565, -0.420944825),
        row(-0.1384280, -0.2721599,  0.33783778,  0.29312535,  0.11633231, -0.160790254),
        row(-0.1248940, -0.2571250,  0.36542543,  0.43884649,  0.46496714,  0.434599540)), 0.0000001));    

    assertThat( eval("s$v"), closeTo(matrix(
        row(-0.7364928,  0.6225002,  0.2550021, -0.06976287,  0.01328234, -0.001588146),
        row(-0.4432826, -0.1818705, -0.6866860,  0.50860089, -0.19626669,  0.041116974),
        row(-0.3274789, -0.3508553, -0.2611139, -0.50473697,  0.61605641, -0.259215626),
        row(-0.2626469, -0.3921783,  0.1043599, -0.43747940, -0.40833605,  0.638901622),
        row(-0.2204199, -0.3945644,  0.3509658,  0.01612426, -0.46427916, -0.675826789),
        row(-0.1904420, -0.3831871,  0.5110654,  0.53856351,  0.44663632,  0.257248908)), 0.0000001));

    //    
//D <- diag(s$d)
//s$u %*% D %*% t(s$v) #  X = U D V'
//t(s$u) %*% X %*% s$v #  D = U' X V")
//   
   
  }
  
  
  private Matcher<SEXP> closeTo(final SEXP expectedSexp, final double epsilon) {
    final Vector expected = (Vector)expectedSexp;
    return new TypeSafeMatcher<SEXP>() {

      @Override
      public void describeTo(Description d) {
          d.appendText(expectedSexp.toString());
      }

      @Override
      public boolean matchesSafely(SEXP item) {
        if(!(item instanceof Vector)) {
          return false;
        }
        Vector vector = (Vector)item;
        if(vector.length() != expected.length()) {
          return false;
        }
        for(int i=0;i!=expected.length();++i) {
          if(expected.isElementNA(i) != vector.isElementNA(i)) {
            return false;
          }
          if(!expected.isElementNA(i)) {
            if(Math.abs(expected.getElementAsDouble(i)-vector.getElementAsDouble(i)) > epsilon) {
              return false;
            }
          }
        }
        return true;
      }
    };
  }


  private double[] row(double... d) {
    return d;
  }
  
  private SEXP matrix(double[]... rows) {
    DoubleVector.Builder matrix = new DoubleVector.Builder();
    int nrows = rows.length;
    int ncols = rows[0].length;
    
    for(int j=0;j!=ncols;++j) {
      for(int i=0;i!=nrows;++i) {
        matrix.add(rows[i][j]);
      }
    }
    return matrix.build();
  }
}
