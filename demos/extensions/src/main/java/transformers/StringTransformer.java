package transformers;

public class StringTransformer {

  public static String toNumber(String txt) {
    StringBuilder result = new StringBuilder();
    txt.chars().mapToObj(i -> (char) i)
        .filter(c -> Character.isDigit(c))
        .forEach(c -> result.append(c));
    return result.toString();
  }
}
