package fieldcoverage;

public class SimpleIntList {

  private int size;
  private SimpleIntNode head;

  public SimpleIntList() {
    this.size = 0;
    this.head = null;
  }

  public void append(int value) {
    insertAt(value, this.size);
  }

  public void insertAt(int value, int at) {
    assert at >= 0;
    SimpleIntNode newNode = new SimpleIntNode(value);
    if (at == 0) {
      this.head = newNode;
    } else {
      SimpleIntNode current = this.head;
      for (int i = 0; (i < at - 1) && current.hasNext(); i++) {
        current = current.next();
      }
      newNode.next(current.next());
      current.next(newNode);
    }
    this.size++;
  }

  public int sum() {
    int res = 0;
    SimpleIntNode current = this.head;
    while (current != null) {
      res += current.value();
      current = current.next();
    }
    return res;
  }

  public boolean contains(int value) {
    boolean found = false;
    SimpleIntNode current = this.head;
    while (current != null && !found) {
      found = current.value() == value;
      current = current.next();
    }
    return found;
  }

  public int size() {
    return this.size;
  }

}
