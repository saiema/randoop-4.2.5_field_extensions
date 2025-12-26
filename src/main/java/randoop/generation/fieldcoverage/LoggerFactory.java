package randoop.generation.fieldcoverage;

import java.io.PrintStream;
import java.util.Objects;
import java.util.logging.*;

public final class LoggerFactory {

  public static Logger getLogger(Class<?> clazz) {
    return getLogger(
        clazz,
        FieldOptionsManager.getInstance().outputStream(),
        FieldOptionsManager.getInstance().debug() ? Level.ALL : Level.OFF);
  }

  public static Logger getLogger(Class<?> clazz, PrintStream outstream, Level level) {
    Logger logger = Logger.getLogger(clazz.getName());
    try {
      StreamHandler sh =
          new StreamHandler(
              outstream,
              new SimpleFormatter() {
                @Override
                public synchronized String format(LogRecord record) {
                  if (Objects.equals(record.getLevel(), Level.INFO)) {
                    return String.format("%s: %s%n", record.getLevel(), record.getMessage());
                  }
                  return String.format("%s%n", record.getMessage());
                }
              });
      logger.addHandler(sh);
      logger.setUseParentHandlers(false);
    } catch (SecurityException e) {
      logger.throwing(clazz.getName(), "static", e);
    }
    logger.setLevel(level);
    return logger;
  }
}
