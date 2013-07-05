package org.fife.ui.rsyntaxtextarea;

import java.util.Collections;
import java.util.Set;

import org.fife.ui.rsyntaxtextarea.TokenMaker;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;

public class RenjinTokenMakerFactory extends TokenMakerFactory {

  public static final String SYNTAX_STYLE_R = "R";
  
  @Override
  protected TokenMaker getTokenMakerImpl(String key) {
    return new RenjinTokenMaker();
  }

  @Override
  public Set<String> keySet() {
    return Collections.singleton(SYNTAX_STYLE_R);
  }

}
