/*
Copyright 2011 Rodion Gorkovenko

This file is a part of FREJ
(project FREJ - Fuzzy Regular Expressions for Java - http://frej.sf.net)

FREJ is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

FREJ is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with FREJ.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.renjin.primitives.text.regex;

/**
 * Finds the region for which Demerau-Levenshtein distance is minimal.
 * 
 * @author Rodion Gorkovenko
 * @see <a href="https://github.com/RodionGork/FREJ">FREJ on GitHub</a>
 */
public class FuzzyMatcher {
  
  private final String pattern;
  private final boolean ignoreCase;
  
  public FuzzyMatcher(String pattern, boolean ignoreCase) {
    this.ignoreCase = ignoreCase;
    if(ignoreCase) {
      this.pattern = pattern.toLowerCase();
    } else {
      this.pattern = pattern;
    }
  }
  
  private static enum EditType {
    TRANSIT, INSERT, DELETE, SUBST, SWAP
  }

  /**
   * @return best distance
   */
  public int contains(String source) {
      int m = pattern.length() + 1;
      int n = source.length() + 1;
      int best, start;
      char p, s, p1, s1;
      
      if(ignoreCase) {
        source = source.toLowerCase();
      }
      
      int[][] e = new int[pattern.length() + 1][source.length() + 1];
      EditType[][] w = new EditType[pattern.length() + 1][source.length() + 1];

      for (int x = 0; x < n; x++) {
          e[0][x] = 0;
      } 

      p = 0;
      
      for (int y = 1; y < m; y++) {
          e[y][0] = y;
          w[y][0] = EditType.DELETE; 

          p1 = p;
          p = pattern.charAt(y - 1);
          s = 0;
          
          for (int x = 1; x < n; x++) {
              int cost;
              int val, temp;

              s1 = s;
              s = source.charAt(x - 1);
              
              cost =  (p == s) ? 0 : 1;
              
              val = e[y - 1][x - 1] + cost;
              w[y][x] = EditType.SUBST;

              temp = e[y - 1][x] + 1;
              if (val > temp) {
                  val = temp;
                  w[y][x] = EditType.DELETE;
              } 
              
              temp = e[y][x - 1] + 1;
              if (val > temp) {
                  val = temp;
                  w[y][x] = EditType.INSERT;
              } 

              if (p1 == s && p == s1) {
                  temp = e[y - 2][x - 2] + cost;
                  if (val > temp) {
                      val = temp;
                      w[y][x] = EditType.SWAP;
                  }
              }    
              e[y][x] = val;
          }
      }

      best = n - 1;
      for (int x = 0; x < n; x++) {
          if (e[m - 1][x] < e[m - 1][best]) {
              best = x;
          }
      }

      start = best;
      for (int y = m - 1; y > 0;) {
          switch (w[y][start]) {
          case INSERT:
              start--;
              break;
          case DELETE:
              y--;
              break;
          case SWAP:
              y-=2;
              start-=2;
              break;
          default:
              start--;
              y--;
              break;
          }
      } 

      return e[m - 1][best];
  }
}
