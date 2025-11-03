package randoop.generation.fieldcoverage;

import canonicalizer.CanonicalizerConfig;
import canonicalizer.ObjectCanonicalizer;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import randoop.generation.fieldcoverage.canonizationanalysis.CanonizationAnalyzer;
import representations.CanonicalObjectRepresentation;
import representations.FieldExtension;
import representations.FieldExtensions;

/**
 * Class {@code FieldMetricsAnalyzer} is responsible for collecting and calculating all metrics
 * associated with Field Coverage.
 *
 * @see <a href="https://dl.acm.org/doi/10.1145/2950290.2950336">Field-exhaustive testing</a>
 */
public class FieldMetricsAnalyzer {

  private final FieldOptionsManager optionsManager;
  private final ObjectCanonicalizer objectCanonicalizer;
  private final CanonizationAnalyzer canonizationAnalyzer;
  private final PrintStream output;

  public FieldMetricsAnalyzer(FieldOptionsManager optionsManager) {
    this.optionsManager = optionsManager;
    output = optionsManager.outputFile();
    CanonicalizerConfig cfg = new CanonicalizerConfig();
    configure(cfg);
    this.canonizationAnalyzer = new CanonizationAnalyzer(this.optionsManager);
    this.objectCanonicalizer = new ObjectCanonicalizer(cfg);
  }

  // Metrics values
  private long objectSeen = 0;
  private long distinctObjectsSeen = 0;
  private long fieldsSeen = 0;
  private long distinctFieldsSeen = 0;

  private boolean newFieldValuePairsSeen = false;

  public boolean newFieldValuePairsSeen() {
    return newFieldValuePairsSeen;
  }

  /**
   * Given an object, it will canonize it as field/value tuples, use those tuples to calculate field
   * coverage metrics.
   *
   * @param object the object to canonicalize
   * @return the field/value tuples from {@code object}
   */
  public FieldExtensions canonicalize(Object object) {
    newFieldValuePairsSeen = false;
    long fieldsSavedBefore = this.objectCanonicalizer.storageSize();
    this.objectCanonicalizer.canonicalize(object);
    long fieldsSavedAfter = this.objectCanonicalizer.storageSize();
    long fieldsInLastCanonization = this.objectCanonicalizer.getObjectRepresentation().size();
    long newDistinctFieldsSeen = fieldsSavedAfter - fieldsSavedBefore;
    if (newDistinctFieldsSeen > 0) {
      newFieldValuePairsSeen = true;
    }
    fieldsSeen += fieldsInLastCanonization;
    distinctFieldsSeen += newDistinctFieldsSeen;
    objectSeen++;
    if (this.objectCanonicalizer.lastObjectIsNew()) {
      distinctObjectsSeen++;
    }
    FieldExtensions lastObjectRep =
        (FieldExtensions) this.objectCanonicalizer.getObjectRepresentation();
    if (optionsManager.canonizationDetailedAnalysis()) {
      detailedCanonizationAnalysis(lastObjectRep);
    }
    if (optionsManager.debug()) {
      LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
      output.println("DEBUG " + now.format(formatter));
      output.println("Is new object: " + this.objectCanonicalizer.lastObjectIsNew());
      output.println("Distinct objects:\n" + distinctObjectsSeen);
      output.println("Objects seen:\n" + objectSeen);
      output.println("Distinct fields:\n" + fieldsSeen);
      output.println("Fields seen:\n" + distinctFieldsSeen);
      output.println("Variability (simple): " + simpleObjectVariability() + "%");
      output.println("Variability (fields): " + fieldsVariability() + "%");
      output.println("Last object rep:\n---Start---\n" + lastObjectRep + "\n---End---");
      output.flush();
    }
    return lastObjectRep;
  }

  public CanonicalObjectRepresentation getLastObjectRepresentation() {
    return this.objectCanonicalizer.getObjectRepresentation();
  }

  private void detailedCanonizationAnalysis(FieldExtensions lastObjectRep) {
    for (String field : lastObjectRep.getFields()) {
      FieldExtension fieldExtension = lastObjectRep.getExtensionForField(field);
      for (String obj : fieldExtension.getDomain()) {
        for (String value : fieldExtension.getValues(obj)) {
          this.canonizationAnalyzer.parseRawFieldExtension(field, obj, value);
        }
      }
    }
  }

  /** Prints all Field metrics into the output set by {@link FieldOptionsManager#outputFile()} */
  public void printMetrics() {
    output.println("Field Coverage Metrics:");
    output.println("Objects Seen: " + objectSeen);
    output.println("Distinct Objects Seen: " + distinctObjectsSeen);
    output.println("Fields Seen: " + fieldsSeen);
    output.println("Distinct Fields Seen: " + distinctFieldsSeen);
    output.println("Variability (simple): " + simpleObjectVariability() + "%");
    output.println("Variability (fields): " + fieldsVariability() + "%");
    output.println("Field Coverage Metric: " + objectCanonicalizer.storageSize());
    if (optionsManager.canonizationDetailedAnalysis()) {
      output.println("Canonization Analysis:\n" + canonizationAnalyzer);
    }
  }

  private float fieldsVariability() {
    return ((float) distinctFieldsSeen / fieldsSeen) * 100.0f;
  }

  private float simpleObjectVariability() {
    return ((float) distinctObjectsSeen / objectSeen) * 100.0f;
  }

  private void configure(CanonicalizerConfig cfg) {
    String dontSaveFieldRegex = optionsManager.dontSaveFieldsRegex();
    if (dontSaveFieldRegex != null) {
      cfg.setDontSaveFieldsRegex(dontSaveFieldRegex);
    }
    String dontTraverseFieldRegex = optionsManager.dontTraverseFieldsRegex();
    if (dontTraverseFieldRegex != null) {
      cfg.setDontTraverseFieldsRegex(dontTraverseFieldRegex);
    }
    String ignoreClassesRegex = optionsManager.ignoreClassesRegex();
    if (ignoreClassesRegex != null) {
      cfg.setIgnoreClassesRegex(ignoreClassesRegex);
    }
    cfg.setFailOnExceedingBounds(optionsManager.failOnExceedingBounds());
    Integer maxFieldDistance = optionsManager.maxFieldDistance();
    if (maxFieldDistance != null) {
      cfg.setMaxFieldDistance(maxFieldDistance);
    }
    cfg.setMaxObjects(optionsManager.maxObjects());
    cfg.setMaxArrayValues(optionsManager.maxArrayValues());
    cfg.setMaxBFDepth(optionsManager.maxBFDepth());
    cfg.setSaveArrayNullValues(optionsManager.saveArrayNullValues());
    cfg.setStorageType(ObjectCanonicalizer.StorageType.FIELDEXT);
  }
}
