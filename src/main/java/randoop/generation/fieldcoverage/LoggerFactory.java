package randoop.generation.fieldcoverage;

import java.util.Objects;
import java.util.logging.*;

public final class LoggerFactory {

  public static Logger getLogger(Class<?> clazz) {
    Logger logger = Logger.getLogger(clazz.getName());
    try {
      StreamHandler sh =
          new StreamHandler(
              FieldOptionsManager.getInstance().outputStream(),
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
    logger.setLevel(FieldOptionsManager.getInstance().debug() ? Level.ALL : Level.OFF);
    return logger;
  }
}
