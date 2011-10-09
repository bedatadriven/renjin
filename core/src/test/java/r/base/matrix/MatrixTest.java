package r.base.matrix;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.apache.commons.math.linear.RealMatrix;
import org.junit.Ignore;
import org.junit.Test;

import r.EvalTestCase;
import r.lang.DoubleVector;
import r.lang.Vector;
import r.util.CommonsMath;

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
  
  @Test
  public void eigenWithLa_rs() throws IOException {
    
    topLevelContext.init();
    eval("res <- eigen(cbind(c(1,-1),c(-1,1)))");
    assertThat( eval("res$values"), equalTo(c(2,0)));
    assertThat( eval("res$vectors"), closeTo(matrix(
        row(-0.7071068, -0.7071068),
        row( 0.7071068, -0.7071068)), 0.00001));
        
  }
  
  @Test
  @Ignore("not yet working")
  public void eigenWithImaginaryResults() throws IOException {
    
    topLevelContext.init();
    eval("res <- matrix(c(3, 4, -2, -1),2)");
    assertThat( eval("res$values"), equalTo(c(complex(1,2), complex(1,-2))));
    assertThat( eval("res$vectors"), closeTo(matrix(
        row(-0.7071068, -0.7071068),
        row( 0.7071068, -0.7071068)), 0.00001));
  }
  
  
  
  @Test
  public void eigenWithLa_rg() throws IOException {
    topLevelContext.init();
    
    eval("res <- eigen(cbind(c(1,-1),c(-1,1)), symmetric=FALSE)");
    assertThat( eval("res$values"), equalTo(c(2,0)));
    assertThat( eval("res$vectors"), closeTo(matrix(
        row( 0.7071068,  0.7071068),
        row(-0.7071068,  0.7071068)), 0.00001));
    
  }
  
  @Test
  public void testSolve() throws IOException {

    topLevelContext.init();

    assertThat(eval("solve(matrix(c(1,3,7,6),2,2))"), closeTo(matrix(
        row(-0.4,  0.46666667),
        row( 0.2, -0.06666667)), 0.0000001));
  }
  
  @Test(expected = r.lang.exception.EvalException.class)
  public void testSolveSingularity() throws IOException {
    topLevelContext.init();
    assertThat(eval("solve(matrix(c(1,2,2,4),2,2))"), closeTo(matrix(
            row(0, 0),
            row(0, 0)), 0.0000001));
  }
  
    
  @Test
  public void matrixMultiplication() {
    
    eval("y <- c(1,0,0,0,0,2,0,0,0,0,3,0,0,0,0,4)");
    eval("dim(y) <- c(4,4) ");
    
    eval("z <- 1:12 ");
    eval("dim(z) <- c(4,3)");
   
    RealMatrix m = CommonsMath.asRealMatrix((Vector)eval("z"));
    assertThat(m.getEntry(0, 0), equalTo(1d));
    assertThat(m.getEntry(1, 0), equalTo(2d));
    assertThat(m.getEntry(0, 2), equalTo(9d));
    assertThat(m.getEntry(3-1, 2-1), equalTo(7d));
    assertThat(m.getEntry(2-1, 3-1), equalTo(10d));
    
    eval("q <- y %*% z");

    assertThat( eval("q"), equalTo(c(1,4,9,16,5,12,21,32,9,20,33,48)));
    assertThat( eval("dim(q)"), equalTo(c_i(4,3)));
    
  }
  
  @Test
  public void matrixProduct() throws IOException{ 
   assertThat(eval("1:3 %*% c(3,2,1)"), equalTo(c(10)));
  }

  @Test
  public void rowSums() throws IOException {
    topLevelContext.init();
    eval("q <- matrix(c(NA, 4, 3, 5, 9, 20), 3)");
   
    assertThat(eval("rowSums(q)"), equalTo(c(DoubleVector.NA, 13, 23)));
    assertThat(eval("rowSums(q, na.rm=TRUE)"), equalTo(c(5, 13, 23)));
  }
  
  @Test
  public void rowMeans() throws IOException {
    topLevelContext.init();
    eval("q <- matrix(1:32, 4)");
    assertThat(eval("rowMeans(q)"), equalTo(c(15,16,17,18)));
    
  }

  @Test
  public void colSums() throws IOException {
    topLevelContext.init();
    eval("q <- matrix(1:32, 4)");
    assertThat(eval("colSums(q)"), equalTo(c(10,26,42,58,74,90,106,122)));
  }

  @Test
  public void colMeans() throws IOException {
    topLevelContext.init();
    eval("q <- matrix(1:32, 4)");
    assertThat(eval("colMeans(q)"), equalTo(c(2.5, 6.5, 10.5, 14.5, 18.5, 22.5, 26.5, 30.5)));
  }
  
}
