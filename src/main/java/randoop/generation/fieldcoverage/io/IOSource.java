package randoop.generation.fieldcoverage.io;

import java.util.LinkedList;
import java.util.List;

public class IOSource {

  private final String name;
  private List<String> values;

  public IOSource(String name, List<String> values) {
    this.name = name;
    this.values = values;
  }

  @SuppressWarnings({
    "JdkObsolete", // for LinkedList
  })
  public IOSource(String name) {
    this(name, new LinkedList<>());
  }

  public void addValue(String value) {
    this.values.add(value);
  }

  public String getName() {
    return name;
  }

  public List<String> getValues() {
    return values;
  }
}
