package fieldcoverage;

public class SimpleIntNode {
  private int value;
  private SimpleIntNode next;

  public SimpleIntNode(int value) {
    this.value = value;
    this.next = null;
  }

  public SimpleIntNode next() {
    return next;
  }

  public boolean hasNext() {
    return next != null;
  }

  public void next(SimpleIntNode next) {
    this.next = next;
  }

  public int value() {
    return value;
  }

  public void value(int value) {
    this.value = value;
  }

}
