package randoop.generation.fieldcoverage.canonizationanalysis;

import java.util.*;
import java.util.stream.Collectors;
import randoop.generation.fieldcoverage.FieldOptionsManager;

public class CanonizationAnalyzer {

  private static class Pair<A, B> {
    private final A fst;
    private final B snd;

    public Pair(A fst, B snd) {
      this.fst = fst;
      this.snd = snd;
    }

    public A fst() {
      return fst;
    }

    public B snd() {
      return snd;
    }
  }

  public enum ORDERING {
    ASCENDING,
    DESCENDING,
    NONE
  }

  private final Map<String, List<Pair<Integer, Set<String>>>> fieldExtensions = new HashMap<>();
  private final Map<String, List<Pair<Integer, List<String>>>> fieldExtensionsAllValues =
      new HashMap<>();
  private final Map<String, Integer> fieldExtensionsCount = new HashMap<>();
  private final Map<String, Integer> fieldExtensionsDifferentValuesCount = new HashMap<>();
  private final ORDERING ordering;
  private final boolean csvFriendly;
  private final List<Field> allSeenFields_debug = new ArrayList<>();
  private final boolean debug;

  public CanonizationAnalyzer(FieldOptionsManager fieldOptionsManager) {
    this.ordering = fieldOptionsManager.canonizationAnalyzerOrder();
    this.csvFriendly = fieldOptionsManager.csvFriendlyReport();
    this.debug = fieldOptionsManager.debug();
  }

  public void parseRawFieldExtension(String fieldRaw, String objectRaw, String valueRaw) {
    Field field = new Field(objectRaw, fieldRaw);
    if (debug) {
      allSeenFields_debug.add(field);
    }
    int objectID = field.getObjectID();
    Value value = new Value(valueRaw);
    List<Pair<Integer, Set<String>>> extensions;
    if (fieldExtensions.containsKey(field.toString())) {
      extensions = fieldExtensions.get(field.toString());
    } else {
      extensions = new ArrayList<>();
      fieldExtensions.put(field.toString(), extensions);
    }
    if (!fieldExtensionsCount.containsKey(field.toString())) {
      fieldExtensionsCount.put(field.toString(), 0);
    }
    List<Pair<Integer, List<String>>> extensionsRawValues;
    if (fieldExtensionsAllValues.containsKey(field.toString())) {
      extensionsRawValues = fieldExtensionsAllValues.get(field.toString());
    } else {
      extensionsRawValues = new ArrayList<>();
      fieldExtensionsAllValues.put(field.toString(), extensionsRawValues);
    }
    if (!fieldExtensionsDifferentValuesCount.containsKey(field.toString())) {
      fieldExtensionsDifferentValuesCount.put(field.toString(), 0);
    }
    Pair<Integer, Set<String>> valuesForObject = getValuesForObject(objectID, extensions);
    if (valuesForObject.snd().isEmpty()) {
      extensions.add(valuesForObject);
    }
    if (valuesForObject.snd().add(value.getRawValue())) {
      fieldExtensionsDifferentValuesCount.put(
          field.toString(), fieldExtensionsDifferentValuesCount.get(field.toString()) + 1);
    }
    Pair<Integer, List<String>> rawValuesForObject =
        getRawValuesForObject(objectID, extensionsRawValues);
    if (rawValuesForObject.snd().isEmpty()) {
      extensionsRawValues.add(rawValuesForObject);
    }
    rawValuesForObject.snd().add(value.getRawValue());
    fieldExtensionsCount.put(field.toString(), fieldExtensionsCount.get(field.toString()) + 1);
  }

  private Pair<Integer, Set<String>> getValuesForObject(
      int objectID, List<Pair<Integer, Set<String>>> allExtensions) {
    for (Pair<Integer, Set<String>> extensions : allExtensions) {
      if (extensions.fst() == objectID) {
        return extensions;
      }
    }
    Set<String> values = new HashSet<>();
    return new Pair<>(objectID, values);
  }

  private Pair<Integer, List<String>> getRawValuesForObject(
      int objectID, List<Pair<Integer, List<String>>> allExtensions) {
    for (Pair<Integer, List<String>> extensions : allExtensions) {
      if (extensions.fst() == objectID) {
        return extensions;
      }
    }
    List<String> rawValues = new ArrayList<>();
    return new Pair<>(objectID, rawValues);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    Map<String, Set<String>> fieldExtensionsCondensed = new HashMap<>();
    for (Map.Entry<String, List<Pair<Integer, Set<String>>>> fieldExtension :
        fieldExtensions.entrySet()) {
      Set<String> condensedValues =
          fieldExtension.getValue().stream()
              .flatMap(s -> s.snd().stream())
              .collect(Collectors.toSet());
      fieldExtensionsCondensed.put(fieldExtension.getKey(), condensedValues);
    }
    for (Map.Entry<String, Set<String>> entry :
        ordering.equals(ORDERING.NONE)
            ? fieldExtensionsCondensed.entrySet()
            : fieldExtensionsCondensed.entrySet().stream()
                .sorted(new CanonizationOrdering(ordering, fieldExtensionsCount))
                .collect(Collectors.toList())) {
      String rep;
      if (csvFriendly) {
        rep =
            entry.getKey()
                + ","
                + fieldExtensionsCount.get(entry.getKey())
                + ","
                + entry.getValue().size()
                + ","
                + fieldExtensionsDifferentValuesCount.get(entry.getKey());
      } else {
        rep =
            entry.getKey()
                + " -> "
                + entry.getValue().toString()
                + "[seen: "
                + fieldExtensionsCount.get(entry.getKey())
                + "][value types: "
                + entry.getValue().size()
                + "][different values: "
                + fieldExtensionsDifferentValuesCount.get(entry.getKey())
                + "]";
      }
      sb.append(rep).append("\n");
    }
    if (csvFriendly) {
      sb.insert(0, "ClassAndField, seen, value types, different values\n");
    }
    if (debug) {
      sb.append("Sanity check: all values counted [")
          .append(sumAllValuesSeen())
          .append("], different values counted [")
          .append(sumAllDifferentValuesSeen())
          .append("]\n");
      for (Field seenField : allSeenFields_debug) {
        sb.append("Raw field: ").append(seenField.getRawValue()).append("\n");
      }
    }
    return sb.toString();
  }

  private int sumAllValuesSeen() {
    int sum = 0;
    for (Map.Entry<String, Integer> entry : fieldExtensionsCount.entrySet()) {
      sum += entry.getValue();
    }
    return sum;
  }

  private int sumAllDifferentValuesSeen() {
    int sum = 0;
    for (Map.Entry<String, Integer> entry : fieldExtensionsDifferentValuesCount.entrySet()) {
      sum += entry.getValue();
    }
    return sum;
  }

  private static class CanonizationOrdering implements Comparator<Map.Entry<String, Set<String>>> {
    private final ORDERING ordering;
    private final Map<String, Integer> classFieldCounts;

    public CanonizationOrdering(ORDERING ordering, Map<String, Integer> classFieldCounts) {
      if (ordering == ORDERING.NONE) {
        throw new IllegalArgumentException("Can't create with " + ORDERING.NONE + " ordering");
      }
      this.ordering = ordering;
      this.classFieldCounts = classFieldCounts;
    }

    @Override
    public int compare(Map.Entry<String, Set<String>> o1, Map.Entry<String, Set<String>> o2) {
      if (ordering == ORDERING.ASCENDING) {
        return classFieldCounts.get(o1.getKey()).compareTo(classFieldCounts.get(o2.getKey()));
      } else {
        return classFieldCounts.get(o2.getKey()).compareTo(classFieldCounts.get(o1.getKey()));
      }
    }
  }

  public static String primitiveArrayTypeToType(char primitiveArrayType) {
    switch (primitiveArrayType) {
      case 'Z':
        {
          return "boolean";
        }
      case 'B':
        {
          return "byte";
        }
      case 'C':
        {
          return "char";
        }
      case 'D':
        {
          return "double";
        }
      case 'F':
        {
          return "float";
        }
      case 'I':
        {
          return "int";
        }
      case 'J':
        {
          return "long";
        }
      case 'S':
        {
          return "short";
        }
      default:
        throw new IllegalArgumentException(
            "Unsupported primitive array type: " + primitiveArrayType);
    }
  }

  public static int calculateMatrixSize(String stringRep) {
    boolean noMoreBrackets = false;
    int matrixSize = 0;
    while (!noMoreBrackets) {
      int indexBracket = stringRep.indexOf('[');
      if (indexBracket != -1) {
        matrixSize++;
        stringRep = stringRep.substring(indexBracket + 1);
      } else {
        noMoreBrackets = true;
      }
    }
    return matrixSize;
  }

  public static String matrixRep(String internalValue, int matrixSize) {
    StringBuilder rep = new StringBuilder();
    rep.append(internalValue);
    for (int i = 0; i < matrixSize; i++) {
      rep.insert(0, '[');
      rep.append(']');
    }
    return rep.toString();
  }
}
