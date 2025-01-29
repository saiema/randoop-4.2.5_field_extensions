package randoop.generation.fieldcoverage.canonizationanalysis;

import static randoop.generation.fieldcoverage.canonizationanalysis.CanonizationAnalyzer.*;

import java.util.Arrays;
import java.util.regex.Pattern;

public class Field {
  private final String rawValue;
  private final String className;
  private final String fieldName;
  private final int objectID;
  private boolean isArrayClass;
  private boolean isArrayIndex;
  private int matrixSize;
  private static final Pattern arrayPatternForObject = Pattern.compile("\\[[ZBCDFIJS]:\\d+");
  private static final Pattern arrayClassPatternForObject = Pattern.compile("\\[L[^;]+;:\\d+");
  private static final Pattern arrayIndexPatternForField = Pattern.compile("\\[[ZBCDFIJS]\\.\\d+");
  private static final Pattern arrayIndexClassPatternForField =
      Pattern.compile("\\[L[^;]+;\\.\\d+");

  public Field(String objectString, String fieldName) {
    String parsedObject = parseObjectString(objectString);
    this.className = removeIndex(parsedObject);
    this.fieldName = parseField(fieldName);
    this.objectID = getObjectIndex(parsedObject);
    this.rawValue = "obj:" + objectString + " | field: " + fieldName + " | id: " + objectID;
  }

  private String parseObjectString(String objectString) {
    if (objectString == null) {
      throw new IllegalArgumentException("objectString is null");
    }
    String className;
    this.matrixSize = 0;
    if (arrayPatternForObject.matcher(objectString).find()) {
      isArrayClass = true;
      try {
        className =
            primitiveArrayTypeToType(objectString.charAt(objectString.lastIndexOf('[') + 1));
      } catch (IllegalArgumentException e) {
        throw new IllegalStateException(
            "Issue with primitive array processing (" + objectString + ")", e);
      }
    } else if (arrayClassPatternForObject.matcher(objectString).find()) {
      int semiColonIndex = objectString.lastIndexOf(';');
      int openingBracketsIndex = objectString.lastIndexOf('[');
      if (semiColonIndex == -1 || openingBracketsIndex == -1) {
        throw new IllegalStateException("Issue with array processing (" + objectString + ")");
      }
      className = objectString.substring(openingBracketsIndex + 2, semiColonIndex);
      isArrayClass = true;
    } else {
      int colonIndex = objectString.lastIndexOf(':');
      if (colonIndex != -1) {
        className = objectString.substring(0, colonIndex);
      } else {
        className = objectString;
      }
    }
    if (isArrayClass) {
      matrixSize = calculateMatrixSize(objectString);
    }
    className += ":" + getObjectIndex(objectString);
    return className;
  }

  private int getObjectIndex(String objectString) {
    int objectIDIndex = objectString.lastIndexOf(':');
    int indexID = -1;
    if (objectIDIndex > 0) {
      indexID = Integer.parseInt(objectString.substring(objectIDIndex + 1));
    }
    return indexID;
  }

  private String removeIndex(String string) {
    int objectIDIndex = string.lastIndexOf(':');
    if (objectIDIndex > 0) {
      return string.substring(0, objectIDIndex);
    }
    return string;
  }

  private String parseField(String rawFieldName) {
    assert rawFieldName != null;
    isArrayIndex = false;
    String fieldName;
    if (arrayIndexPatternForField.matcher(rawFieldName).find()
        || arrayIndexClassPatternForField.matcher(rawFieldName).find()) {
      isArrayIndex = true;
      int indexDotIndex = rawFieldName.lastIndexOf('.');
      int indexValue = -1;
      if (indexDotIndex > 0) {
        indexValue = Integer.parseInt(rawFieldName.substring(indexDotIndex + 1));
      }
      fieldName = "INDEX(" + indexValue + ")";
    } else {
      int lastDotIndex = rawFieldName.lastIndexOf('.');
      if (lastDotIndex != -1) {
        fieldName = rawFieldName.substring(lastDotIndex + 1);
      } else {
        fieldName = rawFieldName;
      }
    }
    return fieldName;
  }

  public String getClassName() {
    return className;
  }

  public String getFieldName() {
    return fieldName;
  }

  public boolean isArrayClass() {
    return isArrayClass;
  }

  public boolean isArrayIndex() {
    return isArrayIndex;
  }

  public int getObjectID() {
    return objectID;
  }

  public String getRawValue() {
    return rawValue;
  }

  private String stringRep = null;

  @Override
  public String toString() {
    if (stringRep != null) {
      return stringRep;
    }
    String classNameRep = className;
    if (isArrayClass) {
      classNameRep = matrixRep(className, matrixSize);
    }
    stringRep = classNameRep + "." + fieldName;
    return stringRep;
  }

  private int hashCode = -1;

  @Override
  public int hashCode() {
    if (hashCode != -1) {
      return hashCode;
    }
    hashCode = Arrays.hashCode(toString().toCharArray());
    return hashCode;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (!(other instanceof Field)) {
      return false;
    }
    Field otherString = (Field) other;
    return toString().equals(otherString.toString());
  }
}
