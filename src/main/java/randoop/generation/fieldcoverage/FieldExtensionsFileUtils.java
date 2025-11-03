package randoop.generation.fieldcoverage;

import java.io.*;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import representations.FieldExtension;
import representations.FieldExtensions;

/**
 * A class to save/load FileExtensions {@link representations.FieldExtensions} to/from files. The
 * format of an extension file is as follows:
 *
 * <ul>
 *   <li>fields_count (how many fields are in the field extensions)
 *       <ul>
 *         repeats a {@code fields_count} amount of times <hr>
 *         <li>field (as an UTF8 string)
 *         <li>sources_count (how many sources a field has)
 *             <ul>
 *               repeats a {@code sources_count} amount of times <hr>
 *               <li>source (as an UTF8 string)
 *               <li>values_count (how many values a source has)
 *                   <ul>
 *                     repeats a {@code values_count} amount of times <hr>
 *                     <li>value (as an UTF8 string) <hr>
 *                   </ul>
 *                   <hr> a {@code sources_count} amount of times
 *             </ul>
 *             <hr>
 *       </ul>
 * </ul>
 */
public class FieldExtensionsFileUtils {

  public static String FILE_EXTENSION = ".fexts";

  /**
   * Save field extensions {@link representations.FieldExtensions} to a file
   *
   * @param fieldExtensions the field extensions to save
   * @param folder the folder where to save the field extensions
   * @param name the name of the new file (without the file extension)
   */
  public static void save(FieldExtensions fieldExtensions, File folder, String name) {
    IOError ioError = checkFile(folder, Mode.SAVE_TO_FOLDER);
    if (ioError != IOError.NONE) {
      throwException(ioError, folder);
    }
    if (fieldExtensions == null) {
      throw new IllegalArgumentException("Field extensions are null");
    }
    if (name == null || name.replaceAll("\\s+", "").isEmpty()) {
      throw new IllegalArgumentException("Name is null or empty");
    }
    String filename = name + FILE_EXTENSION;
    File to = folder.toPath().resolve(filename).toFile();
    save(fieldExtensions, to);
  }

  @SuppressWarnings({
    "JdkObsolete", // for LinkedList
  })
  public static void save(FieldExtensions fieldExtensions, File to) {
    IOError ioError = checkFile(to, Mode.SAVE);
    if (ioError != IOError.NONE) {
      throwException(ioError, to);
    }
    try {
      if (!to.createNewFile()) {
        throw new RuntimeException("Failed to create file: " + to.getAbsolutePath());
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to create file: " + to.getAbsolutePath(), e);
    }
    try (DataOutputStream dos = new DataOutputStream(Files.newOutputStream(to.toPath()))) {
      List<String> fields = new LinkedList<>(fieldExtensions.getFields());
      dos.writeInt(fields.size());
      for (String field : fields) {
        dos.writeUTF(field);
        FieldExtension fieldExtension = fieldExtensions.getExtensionForField(field);
        List<String> sources = new LinkedList<>(fieldExtension.getDomain());
        dos.writeInt(sources.size());
        for (String src : sources) {
          dos.writeUTF(src);
          List<String> values = new LinkedList<>(fieldExtension.getValues(src));
          dos.writeInt(values.size());
          for (String value : values) {
            dos.writeUTF(value);
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to save file: " + to.getAbsolutePath(), e);
    }
  }

  public static FieldExtensions load(File from) {
    IOError ioError = checkFile(from, Mode.LOAD);
    if (ioError != IOError.NONE) {
      throwException(ioError, from);
    }
    FieldExtensions fieldExtensions = new FieldExtensions();
    try (DataInputStream dis = new DataInputStream(Files.newInputStream(from.toPath()))) {
      int fieldCount = dis.readInt();
      for (int i = 0; i < fieldCount; i++) {
        String field = dis.readUTF();
        int soucesCount = dis.readInt();
        for (int j = 0; j < soucesCount; j++) {
          String src = dis.readUTF();
          int valuesCount = dis.readInt();
          for (int k = 0; k < valuesCount; k++) {
            String value = dis.readUTF();
            fieldExtensions.addPairToField(field, src, value);
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to load from file: " + from.getAbsolutePath(), e);
    }
    return fieldExtensions;
  }

  public enum Mode {
    /** Used to load {@link representations.FieldExtensions} from a file */
    LOAD,
    /** Used to save {@link representations.FieldExtensions} to a file */
    SAVE,
    /** Used to save a {@link representations.FieldExtensions} to a folder */
    SAVE_TO_FOLDER
  }

  public enum IOError {
    /** The file extension is not correct */
    WRONG_EXTENSION,
    /** The mode is null */
    NULL_MODE,
    /** The file is null */
    NULL_FILE,
    /** The file does not denote a file (e.g.: is a directory) */
    IS_NOT_FILE,
    /** The file does not denote a directory */
    IS_NOT_DIRECTORY,
    /** File exists */
    FILE_EXISTS,
    /** File does not exist */
    FILE_DOESNT_EXIST,
    /** Insufficient permissions */
    NO_PERMISSIONS,
    /** No error */
    NONE
  }

  public static IOError checkFile(File file, Mode mode) {
    if (file == null) {
      return IOError.NULL_FILE;
    }
    if (mode == null) {
      return IOError.NULL_MODE;
    }
    if (!file.getName().endsWith(FILE_EXTENSION)) {
      return IOError.WRONG_EXTENSION;
    }
    if (mode == Mode.SAVE_TO_FOLDER && !file.isDirectory()) {
      return IOError.IS_NOT_DIRECTORY;
    }
    if (mode == Mode.SAVE_TO_FOLDER && !file.exists()) {
      return IOError.FILE_DOESNT_EXIST;
    }
    if ((mode == Mode.LOAD) && !file.exists()) {
      return IOError.FILE_DOESNT_EXIST;
    }
    if ((mode == Mode.SAVE) && file.exists()) {
      return IOError.FILE_EXISTS;
    }
    if ((mode == Mode.LOAD) && !file.isFile()) {
      return IOError.IS_NOT_FILE;
    }
    if ((mode == Mode.LOAD) && file.canRead()) {
      return IOError.NO_PERMISSIONS;
    }
    return IOError.NONE;
  }

  private static void throwException(IOError error, File file) {
    switch (error) {
      case NULL_FILE:
        throw new IllegalArgumentException("Null file");
      case NULL_MODE:
        throw new IllegalArgumentException("Null mode");
      case NO_PERMISSIONS:
        throw new IllegalArgumentException(
            "No read permissions for file " + file.getAbsoluteFile());
      case WRONG_EXTENSION:
        throw new IllegalArgumentException("Wrong extension for file  " + file.getAbsoluteFile());
      case IS_NOT_FILE:
        throw new IllegalArgumentException("File " + file.getAbsoluteFile() + " is not a file");
      case FILE_EXISTS:
        throw new IllegalArgumentException("File " + file.getAbsoluteFile() + " already exists");
      case FILE_DOESNT_EXIST:
        throw new IllegalArgumentException("File " + file.getAbsoluteFile() + " does not exist");
      case IS_NOT_DIRECTORY:
        throw new IllegalArgumentException(
            "File " + file.getAbsoluteFile() + " is not a directory");
      case NONE:
    }
  }
}
