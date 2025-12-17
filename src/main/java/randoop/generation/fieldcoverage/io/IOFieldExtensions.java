package randoop.generation.fieldcoverage.io;

import java.util.LinkedList;
import java.util.List;
import randoop.sequence.ExecutableSequence;

public class IOFieldExtensions {

  private List<IOField> fields;
  private String from;

  public IOFieldExtensions(List<IOField> fields) {
    this.fields = fields;
    this.from = null;
  }

  @SuppressWarnings({
    "JdkObsolete", // for LinkedList
  })
  public IOFieldExtensions() {
    this(new LinkedList<>());
  }

  public void addField(IOField field) {
    this.fields.add(field);
  }

  public void addFrom(ExecutableSequence from) {
    this.from = from.toCodeString();
  }

  public List<IOField> fields() {
    return fields;
  }

  public String sequence() {
    return from;
  }
}
