package randoop.generation.fieldcoverage.canonizationanalysis;

import static randoop.generation.fieldcoverage.canonizationanalysis.CanonizationAnalyzer.*;

import java.util.Arrays;
import java.util.regex.Pattern;

public class Value {

  public static final Value NONE = new Value("");

  private final String rawValue;
  private String className;
  private boolean isClass;
  private boolean isNull;
  private boolean isArray;
  private int matrixSize;
  private boolean isPrimitive;
  private boolean boxedPrimitive;
  private boolean unknown;

  private static final Pattern numberPattern = Pattern.compile("(-)?\\d*(\\.\\d+)?(E(-)?\\d+)?");
  private static final Pattern booleanPattern = Pattern.compile("(true|false)");
  private static final Pattern hexadecimalPattern = Pattern.compile("(-)?[0-9a-fA-F]+");
  private static final Pattern binaryPattern = Pattern.compile("(-)?[01]+b");
  private static final Pattern characterPattern = Pattern.compile("'.'");
  private static final Pattern stringPattern = Pattern.compile("\".*\"");
  private static final Pattern arrayPattern = Pattern.compile("\\[[ZBCDFIJS]:\\d+");
  private static final Pattern arrayClassPattern = Pattern.compile("\\[L[^;]+;:\\d+");
  private static final Pattern boxedPrimitivePattern =
      Pattern.compile("(java.lang.)?(Character|Byte|Short|Integer|Long|Float|Double):\\d+");
  private static final Pattern classPattern =
      Pattern.compile("([a-z]+\\w*\\.)*[A-Z](\\w)*(\\$(([A-Z](\\w)*)|(\\d+)))?:\\d+");

  public Value(String rawValue) {
    this.matrixSize = 0;
    if (rawValue.trim().isEmpty()) {
      this.rawValue = "NONE";
      this.unknown = true;
      this.isClass = false;
      this.className = "NONE";
      this.isNull = false;
      this.isArray = false;
      this.isPrimitive = false;
      this.boxedPrimitive = false;
    } else {
      this.rawValue = rawValue;
      parse(rawValue);
    }
  }

  public String getRawValue() {
    return rawValue;
  }

  public String getClassName() {
    return className;
  }

  public boolean isArray() {
    return isArray;
  }

  public boolean isBoxedPrimitive() {
    return boxedPrimitive;
  }

  public boolean isClass() {
    return isClass;
  }

  public boolean isNull() {
    return isNull;
  }

  public boolean isPrimitive() {
    return isPrimitive;
  }

  public boolean isUnknown() {
    return unknown;
  }

  @Override
  public String toString() {
    String value;
    if (isUnknown()) {
      value = "Unknown(" + rawValue + ")";
    } else if (isArray()) {
      value = matrixRep(className, matrixSize);
    } else if (isNull()) {
      value = "null";
    } else {
      value = className;
    }
    return value;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(toString().toCharArray());
  }

  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (!(other instanceof Value)) {
      return false;
    }
    Value otherString = (Value) other;
    return toString().equals(otherString.toString());
  }

  private void parse(String rawValue) {
    unknown = false;
    if (rawValue.equalsIgnoreCase("null")) {
      isNull = true;
    } else {
      isNull = false;
      if (numberPattern.matcher(rawValue).matches()
          || hexadecimalPattern.matcher(rawValue).matches()
          || binaryPattern.matcher(rawValue).matches()) {
        className = "Number";
        isPrimitive = true;
        isArray = false;
        isClass = false;
        boxedPrimitive = false;
      } else if (booleanPattern.matcher(rawValue).matches()) {
        className = "boolean";
        isPrimitive = true;
        isArray = false;
        isClass = false;
        boxedPrimitive = false;
      } else if (characterPattern.matcher(rawValue).matches()) {
        className = "char";
        isPrimitive = true;
        isArray = false;
        isClass = false;
        boxedPrimitive = false;
      } else if (stringPattern.matcher(rawValue).matches()) {
        className = "java.lang.String";
        isPrimitive = false;
        isArray = false;
        isClass = true;
        boxedPrimitive = false;
      } else if (arrayPattern.matcher(rawValue).find()) {
        className = primitiveArrayTypeToType(rawValue.charAt(rawValue.lastIndexOf('[') + 1));
        isPrimitive = true;
        isArray = true;
        isClass = false;
        boxedPrimitive = false;
      } else if (arrayClassPattern.matcher(rawValue).find()) {
        int semiColonIndex = rawValue.lastIndexOf(';');
        assert semiColonIndex != -1;
        className = rawValue.substring(2, semiColonIndex);
        int colonIndex = className.lastIndexOf(':');
        if (colonIndex != -1) {
          className = className.substring(0, colonIndex);
        }
        isPrimitive = false;
        isArray = true;
        isClass = true;
        boxedPrimitive = false;
      } else if (boxedPrimitivePattern.matcher(rawValue).matches()) {
        className = rawValue;
        isPrimitive = false;
        isArray = false;
        isClass = true;
        boxedPrimitive = true;
      } else if (classPattern.matcher(rawValue).matches()) {
        className = rawValue;
        isPrimitive = false;
        isArray = false;
        isClass = true;
        boxedPrimitive = false;
      } else {
        unknown = true;
        className = rawValue;
      }
      if (isArray) {
        matrixSize = calculateMatrixSize(rawValue);
      }
      int colonIndex = className.lastIndexOf(':');
      if (colonIndex != -1) {
        className = className.substring(0, colonIndex);
      }
    }
  }
}
