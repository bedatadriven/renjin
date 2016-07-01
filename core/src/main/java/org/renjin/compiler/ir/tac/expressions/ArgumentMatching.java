package org.renjin.compiler.ir.tac.expressions;

import java.util.Arrays;
import java.util.List;

/**
 * Static argument mapping
 */
public class ArgumentMatching {

  private int[] matching;

  public ArgumentMatching(List<ReadParam> params, String[] argumentNames) {
    matching = new int[params.size()];
    Arrays.fill(matching, -1);

    boolean matched[] = new boolean[argumentNames.length];

    // First match arguments exactly
    for (ReadParam param : params) {

    }

  }

}
