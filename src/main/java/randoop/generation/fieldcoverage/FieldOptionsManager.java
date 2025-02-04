package randoop.generation.fieldcoverage;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import io.github.cdimascio.dotenv.DotenvException;
import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;
import randoop.generation.fieldcoverage.canonizationanalysis.CanonizationAnalyzer;

public class FieldOptionsManager {

  private final Dotenv dotenv;

  public FieldOptionsManager(Path env) {
    if (env == null) {
      throw new IllegalArgumentException(".env path is null");
    }
    if (!env.toFile().exists()) {
      throw new IllegalArgumentException(".env path does not exist: " + env);
    }
    if (!env.toFile().canRead()) {
      throw new IllegalArgumentException(".env path is not readable: " + env);
    }
    try {
      this.dotenv =
          Dotenv.configure()
              .directory(!env.isAbsolute() ? "./" : "")
              .filename(env.toString())
              .load();
    } catch (DotenvException e) {
      throw new IllegalArgumentException(".env format is not valid: " + env, e);
    }
  }

  public boolean canonizationDetailedAnalysis() {
    String canonizationDetailedAnalysisRaw = dotenv.get("CANONIZATION_DETAILED_ANALYSIS", "false");
    return Boolean.parseBoolean(canonizationDetailedAnalysisRaw);
  }

  public String dontSaveFieldsRegex() {
    String dontSaveFieldsRegexValue = dotenv.get("DONT_SAVE_FIELDS_REGEX", null);
    if (dontSaveFieldsRegexValue == null || dontSaveFieldsRegexValue.equalsIgnoreCase("null")) {
      return null;
    } else {
      return dontSaveFieldsRegexValue;
    }
  }

  public boolean failOnExceedingBounds() {
    String failOnExceedingBoundsRawValue = dotenv.get("FAIL_ON_EXCEEDING_BOUNDS", "false");
    return Boolean.parseBoolean(failOnExceedingBoundsRawValue);
  }

  public String dontTraverseFieldsRegex() {
    String dontTraverseFieldsRegexValue = dotenv.get("DONT_TRAVERSE_FIELDS_REGEX", null);
    if (dontTraverseFieldsRegexValue == null
        || dontTraverseFieldsRegexValue.equalsIgnoreCase("null")) {
      return null;
    } else {
      return dontTraverseFieldsRegexValue;
    }
  }

  public Integer maxFieldDistance() {
    String maxFieldDistanceRawValue = dotenv.get("MAX_FIELD_DISTANCE", null);
    if (maxFieldDistanceRawValue == null || maxFieldDistanceRawValue.equalsIgnoreCase("null")) {
      return null;
    } else {
      try {
        return toInt(maxFieldDistanceRawValue);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException(
            "Invalid value for key MAX_FIELD_DISTANCE, expecting an int (or null), got "
                + maxFieldDistanceRawValue
                + ", instead",
            e);
      }
    }
  }

  public int maxObjects() {
    String maxObjectsRawValue = dotenv.get("MAX_OBJECTS", Integer.toString(Integer.MAX_VALUE));
    try {
      return toInt(maxObjectsRawValue);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
          "Invalid value for key MAX_OBJECTS, expecting an int, got "
              + maxObjectsRawValue
              + ", instead",
          e);
    }
  }

  public String ignoreClassesRegex() {
    String ignoreClassesRegexValue = dotenv.get("IGNORE_CLASSES_REGEX", null);
    if (ignoreClassesRegexValue == null || ignoreClassesRegexValue.equalsIgnoreCase("null")) {
      return null;
    } else {
      return ignoreClassesRegexValue;
    }
  }

  public int maxArrayValues() {
    String maxArrayValuesRawValue =
        dotenv.get("MAX_ARRAY_VALUES", Integer.toString(Integer.MAX_VALUE));
    try {
      return toInt(maxArrayValuesRawValue);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
          "Invalid value for key MAX_ARRAY_VALUES, expecting an int, got "
              + maxArrayValuesRawValue
              + ", instead",
          e);
    }
  }

  public int maxBFDepth() {
    String maxBFDepthRawValue = dotenv.get("MAX_BF_DEPTH", Integer.toString(Integer.MAX_VALUE));
    try {
      return toInt(maxBFDepthRawValue);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
          "Invalid value for key MAX_BF_DEPTH, expecting an int, got "
              + maxBFDepthRawValue
              + ", instead",
          e);
    }
  }

  public boolean saveArrayNullValues() {
    return Boolean.parseBoolean(dotenv.get("SAVE_ARRAY_NULL_VALUES", "false"));
  }

  private PrintStream outputStream = null;

  public PrintStream outputFile() {
    if (outputStream != null) {
      return outputStream;
    }
    String outputFileRawValue =
        dotenv.get("OUTPUT", "{\"value\":\"stdout\",\"as_path\":\"false\"}");
    Gson gsonParser = new Gson();
    JsonObject outputFileAsJson;
    try {
      outputFileAsJson = gsonParser.fromJson(outputFileRawValue, JsonObject.class);
    } catch (JsonSyntaxException e) {
      throw new IllegalArgumentException(
          "Invalid value format for key OUTPUT, expecting a JSON formatted value"
              + " got "
              + outputFileRawValue
              + " instead",
          e);
    }
    if (!outputFileAsJson.has("value")) {
      throw new IllegalArgumentException(
          "Invalid value for OUTPUT, expecting a JSON with a \"value\" member");
    }
    if (!outputFileAsJson.has("as_path")) {
      throw new IllegalArgumentException(
          "Invalid value for OUTPUT, expecting a JSON with a \"as_path\" member");
    }
    boolean as_path = Boolean.parseBoolean(outputFileAsJson.get("as_path").getAsString());
    String value = outputFileAsJson.get("value").getAsString();
    if (!as_path) {
      switch (value.toLowerCase()) {
        case "stdout":
          {
            outputStream = System.out;
            break;
          }
        case "stderr":
          {
            outputStream = System.err;
            break;
          }
        default:
          {
            throw new IllegalArgumentException(
                "value "
                    + value
                    + " is not a valid value for key OUTPUT"
                    + " when \"as_path\" is set to 'false'"
                    + " valid values are \"stdout\" and \"stderr\"");
          }
      }
    } else {
      File outputFile = getOutputFile(value);
      try {
        outputStream = new PrintStream(new FileOutputStream(outputFile));
      } catch (FileNotFoundException e) {
        throw new RuntimeException(
            "This should not happen since we already did the necessary checks", e);
      }
    }
    return outputStream;
  }

  public CanonizationAnalyzer.ORDERING canonizationAnalyzerOrder() {
    String canonizationAnalyzerOrderRaw =
        dotenv.get("CANONIZATION_ORDERING", CanonizationAnalyzer.ORDERING.NONE.name());
    if (Arrays.stream(CanonizationAnalyzer.ORDERING.values())
        .noneMatch(o -> o.toString().equals(canonizationAnalyzerOrderRaw))) {
      throw new IllegalArgumentException(
          "Invalid value for CANONIZATION_ORDERING (" + canonizationAnalyzerOrderRaw + ")");
    }
    return CanonizationAnalyzer.ORDERING.valueOf(canonizationAnalyzerOrderRaw);
  }

  public boolean csvFriendlyReport() {
    return Boolean.parseBoolean(dotenv.get("CANONIZATION_CSV", "false"));
  }

  private static File getOutputFile(String value) {
    File outputFile = new File(value);
    if (!outputFile.exists()) {
      try {
        if (!outputFile.createNewFile()) {
          throw new IllegalArgumentException(
              "File " + value + " does not exist, and couldn't be created");
        }
      } catch (IOException e) {
        throw new IllegalArgumentException(
            "File " + value + " does not exist, and couldn't be created", e);
      }
    }
    if (!outputFile.canWrite()) {
      throw new IllegalArgumentException("File " + value + " is not writable");
    }
    return outputFile;
  }

  private int toInt(String value) throws NumberFormatException {
    if (value == null) {
      throw new IllegalArgumentException("null value, this should have been checked before!");
    }
    if (value.equalsIgnoreCase("Integer.MAX_VALUE")) {
      return Integer.MAX_VALUE;
    } else if (value.equalsIgnoreCase("Integer.MIN_VALUE")) {
      return Integer.MIN_VALUE;
    } else {
      return Integer.parseInt(value);
    }
  }

  public boolean debug() {
    return Boolean.parseBoolean(dotenv.get("FIELD_COVERAGE_DEBUG", "false"));
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Options:\n");
    for (DotenvEntry dotenvEntry : dotenv.entries()) {
      sb.append(dotenvEntry.getKey()).append("=").append(dotenvEntry.getValue()).append("\n");
    }
    return sb.toString();
  }

}
