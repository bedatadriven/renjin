package org.renjin.primitives;

import org.apache.commons.math.linear.RealMatrix;
import org.junit.Before;
import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.Vector;
import org.renjin.util.CommonsMath;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class MatrixTest extends EvalTestCase {

  private static final double ERROR = 0.00001;

  @Before
  public void setup() throws IOException{
    super.setUp();
    assumingBasePackagesLoad();
  }

  @Before
  public void defineMatrix() {
    eval("matrix <- function (data = NA, nrow = 1, ncol = 1, byrow = FALSE, dimnames = NULL) " +
            " .Internal(matrix(data, nrow, ncol, byrow, dimnames, missing(nrow), missing(ncol)))");
  }


  @Test
  public void aperm() {
    // from docs
    eval("x <- 1:24");
    eval("dim(x) <- 2:4 ");
    eval("xt <- .Internal(aperm(x, c(2,1,3), TRUE)) ");

    assertThat(eval("xt"), equalTo(c_i(1, 3, 5, 2, 4, 6, 7, 9, 11, 8, 10, 12, 13, 15, 17, 14, 16, 18, 19, 21, 23, 20, 22, 24)));
  }

  @Test
  public void matrix() {
    assertThat(eval("matrix(c(1,2,3,4),2,2)"), equalTo(c(1, 2, 3, 4)));
    assertThat(eval("matrix(c(1,2,3,4),2,4)"), equalTo(c(1, 2, 3, 4, 1, 2, 3, 4)));
    assertThat(eval("as.double(matrix(1:10,5,2))"), equalTo(c(1,2,3,4,5,6,7,8,9,10)));
  }

  @Test
  public void matrixByRow() {
    assertThat(eval("matrix(c(1,2,3,4),2,2,TRUE)"), equalTo(c(1, 3, 2, 4)));
    assertThat(eval("matrix(c(1,2,3,4),2,4,TRUE)"), equalTo(c(1, 1, 2, 2, 3, 3, 4, 4)));
    assertThat(eval("as.double(matrix(1:10,5,2,TRUE))"), equalTo(c(1,3,5,7,9,2,4,6,8,10)));
  }

  @Test
  public void rowTest() {
    assertThat(eval(".Internal(row(dim(matrix(1:12,3,4))))"), equalTo(c_i(1,2,3,1,2,3,1,2,3,1,2,3)));
  }

  @Test
  public void colTest() {
    assertThat(eval(".Internal(col(dim(matrix(1:12,3,4))))"), equalTo(c_i(1,1,1,2,2,2,3,3,3,4,4,4)));
  }

  @Test
  public void svd() throws IOException {

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
  }

  @Test
  public void eigenWithLa_rs() throws IOException {

    eval("res <- eigen(cbind(c(1,-1),c(-1,1)))");
    assertThat( eval("res$values"), equalTo(c(2,0)));
    assertThat( eval("res$vectors"), closeTo(matrix(
            row(-0.7071068, -0.7071068),
            row( 0.7071068, -0.7071068)), 0.00001));

  }

  @Test
  public void eigenValuesWithImaginaryResults() throws IOException {
    assertThat(eval("eigen(matrix(c(3, 5, -2, -1),2))$values"),closeTo(c(complex(1,2.44949),complex( 1,2.44949)),ERROR));
    assertThat( eval("eigen(matrix(c(3, 4, -2, -1),2))$values"), closeTo(c(complex(1,2), complex(1,-2)),ERROR));
  }

  @Test
  public void eigenValuesWithImaginaryResultsThreeRowMatrix() throws IOException{
    assertThat(eval("eigen(matrix(c(3,4,1,-2,-1,1,1,1,5),3))$values"),
            closeTo(c(complex(5.457299,0.000000),complex(0.771350,1.996507),complex(0.771350,1.996507)),ERROR));
  }

  @Test
//  @Ignore("not yet working")
  public void eigenVectorsWithImaginaryResults() throws IOException {

//    eval("eigen(matrix(c(3, 5, -2, -1),2))");
    eval("res <- eigen(matrix(c(3, 4, -2, -1),2))");
//    eval("eigen(matrix(c(3,4,1,-2,-1,1,1,1,5),3))");
//    assertThat(eval("res$vectors[1]")(complex(0.4082483,0.4082483)));
//    assertThat( eval("res$vectors"), closeTo(matrix(
//        row(complex(0.4082483,0.4082483), complex(0.4082483,-0.4082483)),
//        row(complex(0.8164966),complex(0.8164966))), 0.00001));
    assertThat(eval("Re(res$vectors[1])"),closeTo(c(0.4082483),0.00001));
    assertThat(eval("Re(res$vectors[2])"),closeTo(c(0.8164966),0.00001));
    assertThat(eval("Re(res$vectors[3])"),closeTo(c(0.4082483),0.00001));
    assertThat(eval("Re(res$vectors[4])"),closeTo(c(0.8164966),0.00001));
    assertThat(eval("Im(res$vectors[1])"),closeTo(c(0.4082483),0.00001));
    assertThat(eval("Im(res$vectors[2])"),closeTo(c(0.0),0.00001));
    assertThat(eval("Im(res$vectors[3])"),closeTo(c(-0.4082483),0.00001));
    assertThat(eval("Im(res$vectors[4])"),closeTo(c(0.0),0.00001));
  }

//  public static Complex complex(double x, double y){
//    return new Complex(x,y);
//  }


  @Test
  public void eigenWithLa_rg() throws IOException {

    eval("res <- eigen(cbind(c(1,-1),c(-1,1)), symmetric=FALSE)");
    assertThat( eval("res$values"), equalTo(c(2,0)));
    assertThat( eval("res$vectors"), closeTo(matrix(
            row( 0.7071068,  0.7071068),
            row(-0.7071068,  0.7071068)), 0.00001));

  }

  @Test
  public void testSolve() throws IOException {
    eval("x <- solve(matrix(c(1,3,7,6),2,2))");

    assertThat(eval("x"), closeTo(matrix(
            row(-0.4,  0.46666667),
            row( 0.2, -0.06666667)), 0.0000001));

    assertThat(eval("dim(x)"), equalTo(c_i(2,2)));
  }

  @Test(expected = org.renjin.eval.EvalException.class)
  public void testSolveSingularity() throws IOException {

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
    eval("q <- matrix(c(NA, 4, 3, 5, 9, 20), 3)");

    assertThat(eval("rowSums(q)"), equalTo(c(DoubleVector.NA, 13, 23)));
    assertThat(eval("rowSums(q, na.rm=TRUE)"), equalTo(c(5, 13, 23)));
  }

  @Test
  public void rowMeans() throws IOException {
    eval("q <- matrix(1:32, 4)");
    assertThat(eval("rowMeans(q)"), equalTo(c(15,16,17,18)));
  }

  @Test
  public void colSums() throws IOException {
    eval("q <- matrix(1:32, 4)");
    eval("print(q)");
    assertThat(eval("colSums(q)"), equalTo(c(10,26,42,58,74,90,106,122)));
  }

  @Test
  public void transpose() throws IOException {
    assertThat(eval("t(c(1,2,3,4))"), equalTo(c(1,2,3,4)));

    eval("m <- 1:12");
    eval("dim(m) <- c(3,4)");
    eval("mt <- t(m) ");
    assertThat(eval("dim(mt)"), equalTo(c_i(4,3)));

    // try with big matrix
    eval("m <- 1:(3000*5000)");
    eval("dim(m) <- c(3000,5000)");
    eval("mt <- t(m) ");
    assertThat(eval("dim(mt)"), equalTo(c_i(5000,3000)));

    // try with a big vector
    eval("m <- 1:1e6");
    eval("mt <- t(m)");
    assertThat(eval("dim(mt)"), equalTo(c_i(1, 1000000)));
  }

  @Test
  public void colMeans() throws IOException {
    eval("q <- matrix(1:32, 4)");
    assertThat(eval("colMeans(q)"), equalTo(c(2.5, 6.5, 10.5, 14.5, 18.5, 22.5, 26.5, 30.5)));
  }

  @Test
  public void matrixDimNames() {
    eval(" m <- matrix(nrow=2,ncol=2,dimnames=list(c('a','b'), c('x', 'y'))) ");
    assertThat( eval(" dimnames(m)[[1]]"), equalTo(c("a", "b")));
    assertThat( eval(" dimnames(m)[[2]]"), equalTo(c("x", "y")));
  }

  @Test
  public void matrixEmptyData() throws IOException {
    eval("m <- matrix(double(), 2, 3)");
    assertThat(eval("m"), equalTo(c(DoubleVector.NA, DoubleVector.NA, DoubleVector.NA,
            DoubleVector.NA, DoubleVector.NA, DoubleVector.NA)));
  }

  @Test
  public void crossprod() {
    assertThat(eval(".Internal(crossprod(1:4, NULL))"), equalTo(c(30)));
  }

  @Test
  public void tcrossprod() throws IOException {
    assertThat(eval("tcrossprod(matrix(1:4,2,2))"), equalTo(c(10,14,14,20)));
  }

}
