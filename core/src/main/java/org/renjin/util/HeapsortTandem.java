package org.renjin.util;

/**
 * This utility heapsorts two arrays in tandem.
 *
 * @author Adriano Caloiaro
 * @date 12/6/14
 */
public final class HeapsortTandem {


  /**
   * Sorts a1 and a2 in tandem using using GNU R's 'revsort' heapsort algorithm ( sort.c )
   *
   * @param a1 The array to sort
   * @param a2 The array to sort in tandem ( Typically an identity array )
   * @param n The length of a and ib
   */
  public static void heapsortDescending(double[] a1, int[] a2, int n) {
    int l, j, ir, i;
    double ra;
    int ii;

    if (n <= 1) {
      return;
    }

    l = (n >> 1) + 1;
    ir = n-1;

    while(true) {
      // ==================================================
      // Heapify a and ib and then sort them
      // ==================================================

      // If the child node index is greater than 0, there must be a right node, so choose the right for swapping
      if (l > 0) {
        l = l - 1;
        ra = a1[l];
        ii = a2[l];
      }

      else {
        ra = a1[ir];
        ii = a2[ir];
        a1[ir] = a1[0];
        a2[ir] = a2[0];

        if (--ir == 0) {
          a1[0] = ra;
          a2[0] = ii;
          return; // We're done
        }
      }

      i = l;
      j = (l << 1);
      while (j <= ir) {
        if (j < ir && a1[j] > a1[j + 1] || i == j) {
          ++j;
        }
        if (ra > a1[j]) {
          a1[i] = a1[j];
          a2[i] = a2[j];
          j += (i = j);
        }
        else {
          j = ir + 1;
        }
      }

      a1[i] = ra;
      a2[i] = ii;
    }
  }

}