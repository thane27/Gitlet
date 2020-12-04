package gitlet;

import java.io.File;
import java.io.Serializable;

/** Class used to serialize and keep track of the contents of FILE.
 * @author Ethan Brown
 */
public class Blob implements Serializable {

    /** Instantiates an instance of blob class based on file F. */
    Blob(File f) {
        String fContentsString = Utils.readContentsAsString(f);
        contents = fContentsString.getBytes();
        name = f.getName();
    }
    /** Method which returns STRING of this instances name value. */
    String getName() {
        return this.name;
    }
    /** Method which return BYTE[] representation of this instances contents. */
    byte[] getContents() {
        return this.contents;
    }
    /** Private STRING representing this instances name. */
    private String name;
    /** Private BYTE[] representing this instances contents. */
    private byte[] contents;
}
