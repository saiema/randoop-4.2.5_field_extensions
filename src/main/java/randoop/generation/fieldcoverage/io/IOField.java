package randoop.generation.fieldcoverage.io;

import java.util.LinkedList;
import java.util.List;

public class IOField {

  private final String name;
  private List<IOSource> sources;

  public IOField(String name, List<IOSource> sources) {
    this.name = name;
    this.sources = sources;
  }

  @SuppressWarnings({
    "JdkObsolete", // for LinkedList
  })
  public IOField(String name) {
    this(name, new LinkedList<>());
  }

  public void addSource(IOSource source) {
    this.sources.add(source);
  }

  public String getName() {
    return name;
  }

  public List<IOSource> getSources() {
    return sources;
  }
}
