package r.scripting;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;

import r.jvmi.r2j.converters.Converters;
import r.lang.Frame;
import r.lang.SEXP;
import r.lang.Symbol;

public class RenjinBindings implements Bindings {

  private final Frame frame;
  
  public RenjinBindings(Frame frame) {
    super();
    this.frame = frame;
  }

  @Override
  public void clear() {
    frame.clear();
  }

  @Override
  public boolean containsValue(Object value) {
    for(Symbol symbol : frame.getSymbols()) {
      if(frame.getVariable(symbol).equals(value)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Set<java.util.Map.Entry<String, Object>> entrySet() {
    throw new UnsupportedOperationException("nyi");
  }

  @Override
  public boolean isEmpty() {
    return frame.getSymbols().isEmpty();
  }

  @Override
  public Set<String> keySet() {
    throw new UnsupportedOperationException("nyi");
  }

  @Override
  public int size() {
    return frame.getSymbols().size();
  }

  @Override
  public Collection<Object> values() {
    throw new UnsupportedOperationException("nyi");
  }

  @Override
  public boolean containsKey(Object key) {
    return frame.getSymbols().contains(toSymbol(key));
  }

  @Override
  public Object get(Object key) {
    return frame.getVariable(toSymbol(key));
  }
  
  private Symbol toSymbol(Object key) {
    if(key instanceof Symbol) {
      return (Symbol)key;
    } else if(key instanceof String) {
      return Symbol.get((String)key);
    } else {
      return Symbol.UNBOUND_VALUE;
    }
  }

  @Override
  public Object put(String name, Object value) {
    Symbol symbol = Symbol.get(name);
    SEXP previousValue = frame.getVariable(symbol);
    SEXP exp = Converters.get(value.getClass()).convertToR(value);
    frame.setVariable(symbol, exp);
    return previousValue;
  }

  @Override
  public void putAll(Map<? extends String, ? extends Object> toMerge) {
    for(Map.Entry<? extends String, ? extends Object> entry : toMerge.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public Object remove(Object key) {
    throw new UnsupportedOperationException("nyi");
  }
}
