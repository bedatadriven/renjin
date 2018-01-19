package org.renjin.primitives.text.regex;

import org.renjin.eval.EvalException;

/**
 * An empty '' pattern
 */
public class EmptyFixedRE implements RE {
    @Override
    public boolean match(String search) {
        return true;
    }

    @Override
    public String subst(String substituteIn, String substitution) {
        throw new EvalException("zero-length pattern");
    }

    @Override
    public String subst(String substituteIn, String substitution, int flags) {
        throw new EvalException("zero-length pattern");
    }

    @Override
    public String[] split(String s) {
        String[] chars = new String[s.length()];
        for(int i=0;i<s.length();++i) {
            chars[i] = s.substring(i, i+1);
        }
        return chars;
    }

    @Override
    public int getGroupStart(int groupIndex) {
        throw new EvalException("zero-length pattern");
    }

    @Override
    public int getGroupEnd(int groupIndex) {
        throw new EvalException("zero-length pattern");
    }

  @Override
  public int getGroupCount() {
    return 0;
  }
}
