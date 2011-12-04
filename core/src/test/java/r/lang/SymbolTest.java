package r.lang;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SymbolTest {

  @Test
  public void hashBits() {
    print("<-");
    print("+");
    print("*");
    print(".Internal");
    print("if");
    print("sep");
    print("...");
    print("collapse");
    print("aardvark");
    print("AAAA");
    print("list");
    print("b");
    print("c");
    print("zoo");
    print(".completeToken");
    print("?");
    print("{");
    
    System.out.println(toBinaryString(8388736));
    System.out.println(toBinaryString(128));
 
  }
  
  @Test
  public void reservedWord() {
    assertTrue(Symbol.get("*").isReservedWord());
  }

  private void print(String name) {
    System.out.println(toBinaryString(Symbol.get(name).hashBit()) + " " + name);
  }

  private String toBinaryString(int hashBits) {
    String bits = Integer.toBinaryString(hashBits);
    StringBuilder display = new StringBuilder();
    for(int i=0;i!=32-bits.length();++i) {
      display.append('0');
    }
    display.append(bits);
    String b = display.toString().replace('0', '.');
    return b;
  }
  
}
