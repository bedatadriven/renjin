package org.renjin.util;

/**
 * This utility heapsorts two arrays in tandem.
 *
 * @author Adriano Caloiaro
 * @date 12/6/14
 */
public final class HeapsortTandem {
  static double dtmp = 0;
  static int itmp = 0;

  /**
   * Sorts two arrays descending in tandem using heapsort.
   * <p>
   * a1's values are the values of interest, while a2 is sorted in tandem. This is useful when a2 is an identity array.
   *
   * @param a1 The array to be sorted
   * @param a2 The array to sort in tandem
   * @param n  The size of the arrays ( should be equal )
   */
  public static void heapsort(double[] a1, int[] a2, int n) {
    int end = n - 1;
    heapify(a1, a2, n);

    while (end > 0) {
      swap(a1, end, 0);
      end--;

      // Restore the heap property in the range 0:end
      siftDown(a1, a2, 0, end);
    }
  }


  /**
   * Heapifies an array with the largest value at the root
   */
  private static void heapify(double[] a1, int[] a2, int count) {
    // Choose the last parent node in the array as the start position
    int start = (int) Math.floor((count - 2) / 2);

    while (start >= 0) {
      siftDown(a1, a2, start, count - 1);
      start--;
    }
  }

  /**
   * Orders the nodes between startNode and endNode in descending heap order
   */
  private static void siftDown(double[] a1, int[] a2, int startNode, int endNode) {
    int root = startNode;
    int swapNode, leftChild, rightChild;

    // While the root node has at least a single child
    leftChild = root * 2 + 1;
    while (leftChild <= endNode) {
      leftChild = root * 2 + 1;
      rightChild = leftChild + 1;
      swapNode = root;

      // If the left child is great, choose it
      if (a1[swapNode] < a1[leftChild]) swapNode = leftChild;

      // If the right child exists and is greater than the left child, choose it
      if (rightChild <= endNode && a1[swapNode] < a1[rightChild]) swapNode = rightChild;

      // We're done if we've chosen to swap the root
      if (swapNode == root) return;

        // Else do the swap since we're not done
      else {
        swap(a1, root, swapNode);
        swap(a2, root, swapNode);
        root = swapNode;
        leftChild = root * 2 + 1;
      }
    }
  }

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

    if (n <= 1) return;

    l = (n >> 1);
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
        if (j < ir && a1[j] > a1[j + 1] || i == j) ++j;
        if (ra > a1[j]) {
          a1[i] = a1[j];
          a2[i] = a2[j];
          j += (i = j);
        }
        else
          j = ir+1;
      }

      a1[i] = ra;
      a2[i] = ii;
    }
  }

  /**
   * Swaps the values of a double array
   *
   * @param array
   * @param i
   * @param j
   */

  private static void swap(double[] array, int i, int j) {
    dtmp = array[i];
    array[i] = array[j];
    array[j] = dtmp;
  }

  /**
   * Swaps the values of an integer array
   *
   * @param array
   * @param i
   * @param j
   */
  private static void swap(int[] array, int i, int j) {
    itmp = array[i];
    array[i] = array[j];
    array[j] = itmp;
  }

}