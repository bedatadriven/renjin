package org.renjin.stats.dist;


import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.Null;
import org.renjin.sexp.Vector;

public class Distance {

  
  public static Euclidean1dDistanceTriangle euclideanDistance(Vector x) {
    return new Euclidean1dDistanceTriangle(x, AttributeMap.EMPTY);
  }
  
  public static Vector toMatrix(Vector x) {
    if(x instanceof Euclidean1dDistanceTriangle) {
      return new Euclidean1dDistanceMatrix ( ((Euclidean1dDistanceTriangle) x).getVector() );
    } else {
      return Null.INSTANCE;
    }
  }
}
