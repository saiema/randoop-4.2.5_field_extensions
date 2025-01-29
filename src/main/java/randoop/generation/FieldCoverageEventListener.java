package randoop.generation;

import java.util.List;
import randoop.generation.fieldcoverage.FieldMetricsAnalyzer;
import randoop.sequence.ExecutableSequence;
import randoop.sequence.ReferenceValue;
import randoop.util.ProgressDisplay;

public class FieldCoverageEventListener implements IEventListener {

  private final FieldMetricsAnalyzer fieldMetricsAnalyzer;

  public FieldCoverageEventListener(FieldMetricsAnalyzer fieldMetricsAnalyzer) {
    super();
    this.fieldMetricsAnalyzer = fieldMetricsAnalyzer;
  }

  /**
   * Called immediately at the start of test generation, before any generation steps have occurred.
   */
  @Override
  public void explorationStart() {}

  /** Called immediately after the end of test generation. */
  @Override
  public void explorationEnd() {}

  /**
   * Called by the AbstractGenerator during each generation iteration, immediately before a
   * generation {@code step()} is performed.
   *
   * @see AbstractGenerator
   */
  @Override
  public void generationStepPre() {}

  /**
   * Called by the AbstractGenerator during each generation iteration, immediately after a
   * generation {@code step()} has completed.
   *
   * @param eseq sequence that was generated and executed in the last generation step. Can b null,
   *     which means the last step was unable to generate a sequence (e.g. due to a bad random
   *     choice).
   * @see AbstractGenerator
   */
  @Override
  public void generationStepPost(ExecutableSequence eseq) {
    if (eseq == null || !eseq.isNormalExecution()) return;
    List<ReferenceValue> lastStatementValues = eseq.getLastStatementValues();
    for (ReferenceValue value : lastStatementValues) {
      this.fieldMetricsAnalyzer.canonicalize(value.getObjectValue());
    }
  }

  /**
   * Called by ProgressDisplay at regular intervals to monitor progress. Implementing classes can
   * use this opportunity to update state.
   *
   * @see ProgressDisplay
   */
  @Override
  public void progressThreadUpdate() {}

  /**
   * Called by AbstractGenerator to determine if generation should stop. True signals to the
   * generator that generation should stop.
   *
   * @return true if generation should stop, false otherwise
   * @see AbstractGenerator
   */
  @Override
  public boolean shouldStopGeneration() {
    return false;
  }
}
