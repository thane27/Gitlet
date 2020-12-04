package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

/** Class used to make Commits and keep track of their contents.
 * @author Ethan Brown
 */
public class Commit implements Serializable {

    /** Instantiates commit by taking in current
     * Repository R, String message S,
     * parent commit C, and date D. */
    public Commit(Repository r, String s, Commit c, Date d) {
        if (c != null) {
            boolean changesAdded = false;
            byte[] cSerial = Utils.serialize(c);
            firstParent = Utils.sha1((Object) cSerial);
            this.date = d;
            message = s;
            this.contents = c.contents;
            File stagingAddDir = new File(Repository.ADD_STAGE);
            File stagingRemDir = new File(Repository.REM_STAGE);
            File[] addDirectoryListing = stagingAddDir.listFiles();
            File[] remDirectoryListing = stagingRemDir.listFiles();
            if (addDirectoryListing != null) {
                for (File i: addDirectoryListing) {
                    changesAdded = true;
                    Blob b = Utils.readObject(i, Blob.class);
                    byte[] bSerial = Utils.serialize(b);
                    String bSHA = Utils.sha1((Object) bSerial);
                    contents.put(b.getName(), bSHA);
                    i.delete();
                }
            }
            if (remDirectoryListing != null) {
                for (File j: remDirectoryListing) {
                    changesAdded = true;
                    contents.remove(j.getName());
                    j.delete();
                }
            }
            if (!changesAdded) {
                throw new GitletException("No changes added to the commit.");
            }
        } else {
            this.date = d;
            message = s;
            contents = new HashMap<>();
        }
        this.commitFile(r);
    }
    /** Instantiates commit with two parents PAR1 and PAR2. Takes in
     * String message S, current Repository R and Date D. */
    public Commit(Repository r, String s, Commit par1, Commit par2, Date d) {
        boolean changesAdded = false;
        byte[] par1Serial = Utils.serialize(par1);
        byte[] par2Serial = Utils.serialize(par2);
        this.firstParent = Utils.sha1((Object) par1Serial);
        this.secondParent = Utils.sha1((Object) par2Serial);
        this.date = d;
        message = s;
        this.contents = par1.contents;
        File stagingAddDir = new File(Repository.ADD_STAGE);
        File stagingRemDir = new File(Repository.REM_STAGE);
        File[] addDirectoryListing = stagingAddDir.listFiles();
        File[] remDirectoryListing = stagingRemDir.listFiles();
        if (addDirectoryListing != null) {
            for (File i: addDirectoryListing) {
                changesAdded = true;
                Blob b = Utils.readObject(i, Blob.class);
                byte[] bSerial = Utils.serialize(b);
                String bSHA = Utils.sha1((Object) bSerial);
                contents.put(b.getName(), bSHA);
                i.delete();
            }
        }
        if (remDirectoryListing != null) {
            for (File j: remDirectoryListing) {
                changesAdded = true;
                contents.remove(j.getName());
                j.delete();
            }
        }
        if (!changesAdded) {
            throw new GitletException("No changes added to the commit.");
        }
        this.commitFile(r);
    }
    /** Takes in Repository R and serializes and saves commit in file
     * in OBJ_DIR named its shaID. */
    void commitFile(Repository r) {
        byte[] commitSerial = Utils.serialize(this);
        String commitSHA = Utils.sha1((Object) commitSerial);
        File commitFile = new File(Repository.OBJ_DIR + commitSHA);
        Utils.writeObject(commitFile, this);
    }
    /** Returns HashMap of this commit's contents. */
    HashMap<String, String> getContents() {
        return this.contents;
    }
    /** Returns this commit's first parent shaID in the form of a string. */
    String getParent() {
        return this.firstParent;
    }
    /** Returns this commit's second parent shaID in the form of a string. */
    String getSecondParent() {
        return this.secondParent;
    }
    /** Return this commit's String message. */
    String getMessage() {
        return this.message;
    }
    /** Return this commit's Date. */
    Date getDate() {
        return this.date;
    }
    /** HashMap of this commit's contents. */
    private HashMap<String, String> contents;
    /** This commit's string message. */
    private String message;
    /** This commit's date. */
    private Date date;
    /** This commit's first parent's shaID. */
    private String firstParent;
    /** This commit's second parent's shaID. */
    private String secondParent = null;
}
