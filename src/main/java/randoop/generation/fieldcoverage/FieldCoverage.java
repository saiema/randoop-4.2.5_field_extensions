package randoop.generation.fieldcoverage;

import canonicalizer.CanonicalizerConfig;
import canonicalizer.ObjectCanonicalizer;
import java.io.PrintStream;
import java.util.logging.Logger;
import randoop.generation.fieldcoverage.canonizationanalysis.CanonizationAnalyzer;
import randoop.sequence.ExecutableSequence;
import representations.FieldExtension;
import representations.FieldExtensions;

/**
 * Class {@code FieldMetricsAnalyzer} is responsible for collecting and calculating all metrics
 * associated with Field Coverage.
 *
 * @see <a href="https://dl.acm.org/doi/10.1145/2950290.2950336">Field-exhaustive testing</a>
 */
public class FieldCoverage {

  private final Logger logger = LoggerFactory.getLogger(FieldCoverage.class);
  private static FieldCoverage instance;

  public static FieldCoverage getInstance() {
    if (instance == null) {
      instance = new FieldCoverage();
    }
    return instance;
  }

  private final CanonicalizerConfig canonicalizerConfig;
  private final ObjectCanonicalizer objectCanonicalizer;
  private final CanonizationAnalyzer canonizationAnalyzer;
  private final PrintStream output;
  private final FieldExtensions fieldExtensions;

  public FieldCoverage() {
    output = FieldOptionsManager.getInstance().outputStream();
    canonicalizerConfig = new CanonicalizerConfig();
    configure(canonicalizerConfig);
    this.canonizationAnalyzer = new CanonizationAnalyzer();
    this.objectCanonicalizer = new ObjectCanonicalizer(canonicalizerConfig);
    this.fieldExtensions = new FieldExtensions();
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
   * Given an object, it will canonize it as field/value tuples, and return the field extensions.
   *
   * @param object the object to canonicalize
   * @return the field/value tuples from {@code object}
   */
  public FieldExtensions canonicalize(Object object) {
    ObjectCanonicalizer canonicalizer = new ObjectCanonicalizer(canonicalizerConfig);
    canonicalizer.canonicalize(object);
    return (FieldExtensions) canonicalizer.getObjectRepresentation();
  }

  /**
   * Adds field extensions from an ExecutableSequence to the object variability analysis
   *
   * @param eseq the ExecutableSequence from where to get the field extensions to add
   */
  public void addFieldExtensionsToAnalysis(ExecutableSequence eseq) {
    if (!eseq.hasFieldExtensions()) {
      throw new IllegalArgumentException("The sequence has no field extensions");
    }
    FieldExtensions fieldExtensions = eseq.getFieldExtensions();
    newFieldValuePairsSeen = false;
    long fieldsSavedBefore = this.fieldExtensions.size();
    this.fieldExtensions.addAll(fieldExtensions);
    long fieldsSavedAfter = this.fieldExtensions.size();
    long fieldsInLastCanonization = fieldExtensions.size();
    long newDistinctFieldsSeen = fieldsSavedAfter - fieldsSavedBefore;
    if (newDistinctFieldsSeen > 0) {
      newFieldValuePairsSeen = true;
      distinctObjectsSeen++;
    }
    fieldsSeen += fieldsInLastCanonization;
    distinctFieldsSeen += newDistinctFieldsSeen;
    objectSeen++;
    if (FieldOptionsManager.getInstance().canonizationDetailedAnalysis()) {
      detailedCanonizationAnalysis(fieldExtensions);
    }
    logger.info("Is new object: " + this.objectCanonicalizer.lastObjectIsNew());
    logger.info("Distinct objects:\n" + distinctObjectsSeen);
    logger.info("Objects seen:\n" + objectSeen);
    logger.info("Distinct fields:\n" + fieldsSeen);
    logger.info("Fields seen:\n" + distinctFieldsSeen);
    logger.info("Variability (simple): " + simpleObjectVariability() + "%");
    logger.info("Variability (fields): " + fieldsVariability() + "%");
    logger.info("Last object rep:\n---Start---\n" + fieldExtensions + "\n---End---");
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

  /** Prints all Field metrics into the output set by {@link FieldOptionsManager#outputStream()} */
  public void printMetrics() {
    output.println("Field Coverage Metrics:");
    output.println("Objects Seen: " + objectSeen);
    output.println("Distinct Objects Seen: " + distinctObjectsSeen);
    output.println("Fields Seen: " + fieldsSeen);
    output.println("Distinct Fields Seen: " + distinctFieldsSeen);
    output.println("Variability (simple): " + simpleObjectVariability() + "%");
    output.println("Variability (fields): " + fieldsVariability() + "%");
    output.println("Field Coverage Metric: " + objectCanonicalizer.storageSize());
    if (FieldOptionsManager.getInstance().canonizationDetailedAnalysis()) {
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
    String dontSaveFieldRegex = FieldOptionsManager.getInstance().dontSaveFieldsRegex();
    if (dontSaveFieldRegex != null) {
      cfg.setDontSaveFieldsRegex(dontSaveFieldRegex);
    }
    String dontTraverseFieldRegex = FieldOptionsManager.getInstance().dontTraverseFieldsRegex();
    if (dontTraverseFieldRegex != null) {
      cfg.setDontTraverseFieldsRegex(dontTraverseFieldRegex);
    }
    String ignoreClassesRegex = FieldOptionsManager.getInstance().ignoreClassesRegex();
    if (ignoreClassesRegex != null) {
      cfg.setIgnoreClassesRegex(ignoreClassesRegex);
    }
    cfg.setFailOnExceedingBounds(FieldOptionsManager.getInstance().failOnExceedingBounds());
    Integer maxFieldDistance = FieldOptionsManager.getInstance().maxFieldDistance();
    if (maxFieldDistance != null) {
      cfg.setMaxFieldDistance(maxFieldDistance);
    }
    cfg.setMaxObjects(FieldOptionsManager.getInstance().maxObjects());
    cfg.setMaxArrayValues(FieldOptionsManager.getInstance().maxArrayValues());
    cfg.setMaxBFDepth(FieldOptionsManager.getInstance().maxBFDepth());
    cfg.setSaveArrayNullValues(FieldOptionsManager.getInstance().saveArrayNullValues());
    cfg.setStorageType(ObjectCanonicalizer.StorageType.FIELDEXT);
  }
}
