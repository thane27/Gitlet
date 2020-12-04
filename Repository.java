package gitlet;



import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Date;
import java.util.ArrayList;
import java.util.Set;
import java.util.Arrays;

/** Class representing instance of gitlet program in CWD.
 * @author Ethan Brown
 */
public class Repository implements Serializable {
    /** String representing object directory within .gitlet directory. */
    static final String OBJ_DIR = ".gitlet/obj/";
    /** String representing branches directory within .gitlet directory. */
    static final String BRANCHES = ".gitlet/branches/";
    /** String representing index directory within .gitlet directory. */
    static final String INDEX = ".gitlet/index/";
    /** String representing stage for addition within index. */
    static final String ADD_STAGE = ".gitlet/index/add/";
    /** String representing stage for removal within index. */
    static final String REM_STAGE = ".gitlet/index/rem/";
    /** String representing location of repository
     * file within .gitlet directory. */
    static final String REPO_LOC = ".gitlet/REPOLOC";
    /** FILE which is current working directory of .gitlet directory. */
    private File workingDirectory;

    /** Instantiates instance of Repository class where working directory
     * is locating at file F. */
    Repository(File f) {
        workingDirectory = f;
        long dateLong = 0;
        Date d = new Date(dateLong);
        Commit c = new Commit(this, "initial commit", null, d);
        byte[] cSerial = Utils.serialize(c);
        head = Utils.sha1((Object) cSerial);
        currBranchName = "master";
        currBranch = BRANCHES + "master";
        File g = new File(currBranch);
        Utils.writeObject(g, c);
    }
    /** String representing path to current head branch of repository. */
    private String currBranch;
    /** String representing name of current head branch of repository. */
    private String currBranchName;
    /** String shaID of current head commit of repository. */
    private String head;
    /** Initializes repository class and creates .gitlet
     * directory where file F is located. */
    public static void init(File f) {
        if (f.exists()) {
            throw new GitletException("A Gitlet version-control system "
                    + "already exists in the current directory.");
        } else {
            f.mkdir();
            File a = new File(OBJ_DIR);
            File b = new File(BRANCHES);
            File c = new File(INDEX);
            File d = new File(ADD_STAGE);
            File e = new File(REM_STAGE);

            a.mkdir();
            b.mkdir();
            c.mkdir();
            d.mkdir();
            e.mkdir();
            File cwd = new File(System.getProperty("user.dir"));
            Repository repo = new Repository(cwd);
            File repoFile = new File(REPO_LOC);
            Utils.writeObject(repoFile, repo);
        }
    }
    /** Returns current REPOSITORY instance. */
    public static Repository getRepo() {
        File repoFile = new File(REPO_LOC);
        Repository rV = Utils.readObject(repoFile, Repository.class);
        return rV;
    }
    /** Used to update repository REPO. */
    public static void updateRepo(Repository repo) {
        File repoFile = new File(REPO_LOC);
        repoFile.delete();
        Utils.writeObject(repoFile, repo);
    }
    /** Adds copy of file G to staging area as it currently exists. */
    void add(File g) {
        File f = g;
        Blob b = new Blob(f);
        byte[] bSerial = Utils.serialize(b);
        String bSHA = Utils.sha1((Object) bSerial);
        Commit c = getCommit(this.head);
        File staging = new File(ADD_STAGE + b.getName());
        File stagingRem = new File(REM_STAGE + b.getName());
        File blobFile = new File(OBJ_DIR + bSHA);
        if (!c.getContents().isEmpty()
                && c.getContents().containsKey(f.getName())) {
            String commitBlobSHA = c.getContents().get(f.getName());
            if (commitBlobSHA.equals(bSHA)) {
                if (staging.exists()) {
                    staging.delete();
                }
                if (stagingRem.exists()) {
                    stagingRem.delete();
                }
                return;
            }
        }
        if (staging.exists()) {
            staging.delete();
        }
        if (stagingRem.exists()) {
            stagingRem.delete();
        }
        Utils.writeObject(blobFile, b);
        Utils.writeObject(staging, b);
        Repository.updateRepo(this);
    }
    /** Method used to commit given file within String[] ARGS. */
    public static void commitComm(String[] args) {
        if (args.length == 1) {
            throw new GitletException("Please enter a commit message.");
        }
        Repository repo = Repository.getRepo();
        Commit head = repo.getCommit(repo.head);
        Date d = new Date();
        Commit c = new Commit(repo, args[1], head, d);
        byte[] cSerial = Utils.serialize(c);
        String cSHA = Utils.sha1((Object) cSerial);
        File branchFile = new File(repo.getCurrBranch());
        if (branchFile.exists()) {
            branchFile.delete();
        }
        repo.head = cSHA;
        Utils.writeObject(branchFile, c);
        Repository.updateRepo(repo);
    }
    /** Returns location of current head branch of repository. */
    private String getCurrBranch() {
        return this.currBranch;
    }
    /** Takes file S as it exists in commit with shaID COM and places
     * it in working directory overwriting the version of the file
     * that's already there if there is one.*/
    void checkout(String com, String s) {
        File f = new File(s);
        if (com.length() < Utils.UID_LENGTH) {
            File objects = new File(OBJ_DIR);
            String[] shaIDs = objects.list();
            if (shaIDs != null) {
                for (String string : shaIDs) {
                    if (string.indexOf(com) == 0) {
                        com = string;
                    }
                }
            }
        }
        Commit headComm = getCommit(com);
        if (headComm.getContents().containsKey(f.getName())) {
            String checkoutFileSHA = headComm.getContents().get(f.getName());
            File checkoutFileBlob = new File(OBJ_DIR + checkoutFileSHA);
            Blob b = Utils.readObject(checkoutFileBlob, Blob.class);
            File cwdVersion = new File(workingDirectory
                    + "/" + f.getName());
            if (cwdVersion.exists()) {
                cwdVersion.delete();
            }
            String checkoutFileStr = new String(b.getContents());
            Utils.writeContents(cwdVersion, checkoutFileStr);
        } else {
            throw new GitletException("File does not exist in that commit.");
        }
        Repository.updateRepo(this);
    }
    /** Returns commit with shaID S. */
    Commit getCommit(String s) {
        File headFile = new File(Repository.OBJ_DIR + s);
        if (!headFile.exists()) {
            throw new GitletException("No commit with that id exists.");
        }
        return Utils.readObject(headFile, Commit.class);
    }
    /** Starting at the commit with shaID S, display information about each
     * commit backwards along the commit tree if its shaID is not
     * contained in A. Returns ArrayList containing shaID's of
     * commits that have been printed in current log session. **/
    ArrayList<String> log(String s, ArrayList<String> a) {
        SimpleDateFormat formatter = new
                SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        Commit headCom = getCommit(s);
        String currID = s;
        while (headCom != null) {
            if (!a.contains(currID)) {
                a.add(currID);
                String currDate = formatter.format(headCom.getDate());
                System.out.println("===");
                System.out.println("commit " + currID);
                System.out.println("Date: " + currDate);
                System.out.println(headCom.getMessage());
                System.out.print(System.lineSeparator());
            }
            if (headCom.getParent() == null) {
                break;
            }
            currID = headCom.getParent();
            headCom = getCommit(headCom.getParent());
        }
        return a;
    }
    /** Unstage file F if it is currently staged for addition.
     * If the file is tracked in the current commit, stage it
     * for removal and remove the file from the working directory
     * if the user has not already done so */
    void remove(File f) {
        boolean errorBool = true;
        Commit c = getCommit(this.head);
        File staging = new File(REM_STAGE + f.getName());
        String fileName = f.getName();
        File stagingAddDir = new File(Repository.ADD_STAGE);
        File[] addDirectoryListing = stagingAddDir.listFiles();
        if (addDirectoryListing != null) {
            for (File file: addDirectoryListing) {
                if (f.getName().equals(file.getName())) {
                    file.delete();
                    errorBool = false;
                }
            }
        }
        Set<String> fileNames = c.getContents().keySet();
        for (String s: fileNames) {
            if (f.getName().equals(s)) {
                Utils.writeObject(staging, fileName);
                errorBool = false;
                if (f.exists()) {
                    f.delete();
                }
            }
        }
        if (errorBool) {
            throw new GitletException("No reason to remove the file.");
        }
        Repository.updateRepo(this);
    }
    /** Like log, except displays information about all commits ever made. */
    void global() {
        File branchDir = new File(BRANCHES);
        File[] branchFiles = branchDir.listFiles();
        ArrayList<String> passedIn = new ArrayList<>();
        for (File f: branchFiles) {
            Commit c = Utils.readObject(f, Commit.class);
            byte[] content = Utils.serialize(c);
            String cID = Utils.sha1((Object) content);
            passedIn = this.log(cID, passedIn);
        }
        File objDir = new File(OBJ_DIR);
        File[] objFiles = objDir.listFiles();
        for (File f: objFiles) {
            try {
                Commit orphan = Utils.readObject(f, Commit.class);
                byte[] content = Utils.serialize(orphan);
                String orphanID = Utils.sha1((Object) content);
                passedIn = this.log(orphanID, passedIn);
            } catch (IllegalArgumentException ignored) {
                ignored.getMessage();
            }
        }
    }
    /** Prints shaID of all commits with message STRMES. */
    void find(String strMes) {
        boolean commitFound = false;
        File branchDir = new File(BRANCHES);
        File[] branchFiles = branchDir.listFiles();
        ArrayList<String> seenSoFar = new ArrayList<>();
        if (branchFiles != null) {
            for (File f: branchFiles) {
                Commit c = Utils.readObject(f, Commit.class);
                byte[] content = Utils.serialize(c);
                String id = Utils.sha1((Object) content);
                while (true) {
                    if (c.getMessage().equals(strMes)
                            && !seenSoFar.contains(id)) {
                        commitFound = true;
                        System.out.println(id);
                        seenSoFar.add(id);
                    }
                    if (c.getParent() == null) {
                        break;
                    }
                    id = c.getParent();
                    c = getCommit(c.getParent());
                }
            }
        }
        File objDir = new File(OBJ_DIR);
        File[] objFiles = objDir.listFiles();
        for (File f: objFiles) {
            try {
                Commit orphan = Utils.readObject(f, Commit.class);
                byte[] content = Utils.serialize(orphan);
                String orphanID = Utils.sha1((Object) content);
                if (orphan.getMessage().equals(strMes)
                        && !seenSoFar.contains(orphanID)) {
                    commitFound = true;
                    System.out.println(orphanID);
                    seenSoFar.add(orphanID);
                }
            } catch (IllegalArgumentException ignored) {
                ignored.getMessage();
            }
        }
        if (!commitFound) {
            throw new GitletException("Found no commit with that message.");
        }
    }
    /** Displays what branches currently exist, and marks the
     * current branch with a *. Also displays what files have
     * been staged for addition or removal. */
    void status() {
        System.out.println("=== Branches ===");
        File branchDir = new File(BRANCHES);
        String[] branchFiles = branchDir.list();
        Arrays.sort(branchFiles);
        for (String s: branchFiles) {
            if (s.equals(this.currBranchName)) {
                System.out.println("*" + s);
            } else {
                System.out.println(s);
            }
        }
        System.out.print(System.lineSeparator());
        System.out.println("=== Staged Files ===");
        File addDir = new File(ADD_STAGE);
        String[] addFiles = addDir.list();
        Arrays.sort(addFiles);
        for (String s: addFiles) {
            System.out.println(s);
        }
        System.out.print(System.lineSeparator());
        System.out.println("=== Removed Files ===");
        File remDir = new File(REM_STAGE);
        String[] remFiles = remDir.list();
        Arrays.sort(remFiles);
        for (String s: remFiles) {
            System.out.println(s);
        }
        System.out.print(System.lineSeparator());
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.print(System.lineSeparator());
        System.out.println("=== Untracked Files ===");
        System.out.print(System.lineSeparator());
    }
    /** Creates new branch named S which points to current head commit. */
    void branch(String s) {
        File f = new File(BRANCHES + s);
        if (f.exists()) {
            throw new GitletException("A branch with "
                    + "that name already exists.");
        }
        Commit c = getCommit(this.head);
        Utils.writeObject(f, c);
        Repository.updateRepo(this);
    }
    /** Takes all files in the commit at the head of the
     * branch S, and puts them in the working directory,
     * overwriting the versions of the files that are already
     * there if they exist. Also, at the end of this command,
     * the given branch will now be considered the current branch.
     * Any files that are tracked in the current branch but are
     * not present in the checked-out branch are deleted.*/
    void branchCheckout(String s) {
        File currentWorking = workingDirectory;
        String[] cwdFiles = currentWorking.list();
        File f = new File(BRANCHES + s);
        if (!f.exists()) {
            throw new GitletException("No such branch exists.");
        }
        if (getCurrBranchName().equals(s)) {
            throw new GitletException("No need to checkout"
                    + " the current branch.");
        }
        Commit curr = getCommit(getHEAD());
        HashMap<String, String> currContents = curr.getContents();
        Set<String> currFileNames = currContents.keySet();
        Commit c = Utils.readObject(f, Commit.class);
        HashMap<String, String> cContents = c.getContents();
        Set<String> fileNames = cContents.keySet();
        if (cwdFiles != null) {
            for (String cwdStr : cwdFiles) {
                if (!currFileNames.contains(cwdStr)
                        && fileNames.contains(cwdStr)) {
                    throw new GitletException("There is an untracked file "
                            + "in the way; delete it, or add "
                            + "and commit it first.");
                }
            }
        }
        for (String string: currFileNames) {
            if (!fileNames.contains(string)) {
                File deleted = new File(workingDirectory
                        + "/" + string);
                deleted.delete();
            }
        }
        byte[] cByte = Utils.serialize(c);
        String cSHAID = Utils.sha1(cByte);
        for (String str: fileNames) {
            this.checkout(cSHAID, str);
        }
        this.head = cSHAID;
        this.currBranchName = s;
        this.currBranch = BRANCHES + s;
        this.clearStage();
        Repository.updateRepo(this);
    }
    /** Returns name of current head branch in current repository. */
    String getCurrBranchName() {
        return this.currBranchName;
    }
    /** Clears all files in ADD_STAGE and REM_STAGE. */
    void clearStage() {
        File addDir = new File(ADD_STAGE);
        File remDir = new File(REM_STAGE);
        File[] addFiles = addDir.listFiles();
        File[] remFiles = remDir.listFiles();
        if (addFiles != null) {
            for (File f: addFiles) {
                f.delete();
            }
        }
        if (remFiles != null) {
            for (File f: remFiles) {
                f.delete();
            }
        }
    }
    /** Removes branch named S. */
    void remBranch(String s) {
        File f = new File(BRANCHES + s);
        if (!f.exists()) {
            throw new GitletException("A branch with "
                    + "that name does not exist.");
        }
        if (s.equals(getCurrBranchName())) {
            throw new GitletException("Cannot remove the current branch.");
        }
        f.delete();
    }
    /** Checks out all files in commit with shaID S. S can be abbreviated. */
    void reset(String s) {
        Commit c = null;
        if (s.length() == 6) {
            File objects = new File(OBJ_DIR);
            String[] shaIDs = objects.list();
            for (String string: shaIDs) {
                if (string.indexOf(s) == 0) {
                    s = string;
                }
            }
        }
        File f = new File(OBJ_DIR);
        String[] objFileNames = f.list();
        if (objFileNames != null) {
            for (String string: objFileNames) {
                if (string.equals(s)) {
                    c = getCommit(s);
                }
            }
        }
        if (c == null) {
            throw new GitletException("No commit with that id exists.");
        }
        HashMap<String, String> newFileContent = c.getContents();
        Set<String> newFiles = newFileContent.keySet();
        File currentWorking = workingDirectory;
        String[] cwdFiles = currentWorking.list();
        Commit curr = getCommit(this.head);
        HashMap<String, String> currFileContent = curr.getContents();
        Set<String> currFiles = currFileContent.keySet();
        if (cwdFiles != null) {
            for (String string: cwdFiles) {
                if (!currFiles.contains(string)
                        && newFiles.contains(string)) {
                    throw new GitletException("There is an untracked "
                            + "file in the "
                            + "way; delete it, or add and commit it first.");
                }
            }
        }
        for (String string: currFiles) {
            if (!newFiles.contains(string)) {
                File deleted = new File(workingDirectory
                        + "/" + string);
                deleted.delete();
            }
        }
        byte[] cByte = Utils.serialize(c);
        String cSHAID = Utils.sha1(cByte);
        for (String str: newFiles) {
            this.checkout(cSHAID, str);
        }
        File currBranchFile = new File(getCurrBranch());
        currBranchFile.delete();
        Utils.writeObject(currBranchFile, c);
        clearStage();
        this.head = cSHAID;
        Repository.updateRepo(this);
    }
    /** Returns STRING common ancestor of current branch
     * and MERGEBRANCH. */
    String findCommonAncestor(File mergeBranch) {
        String ancestor = null;
        int ancestorCount = 1000;
        int distanceHead = 1000;
        int counterCurr = 0;
        int counterBranch = 0;
        Commit curr = getCommit(getHEAD());
        byte[] currByte = Utils.serialize(curr);
        String currSHA = Utils.sha1(currByte);
        Commit branchCommit = Utils.readObject(mergeBranch, Commit.class);
        byte[] branchByte = Utils.serialize(branchCommit);
        String branchSHA = Utils.sha1((Object) branchByte);
        while (curr != null) {
            while (branchCommit != null) {
                if (branchSHA.equals(currSHA)) {
                    int countThis = counterCurr + counterBranch;
                    if (countThis < ancestorCount) {
                        ancestorCount = countThis;
                        ancestor = branchSHA;
                        distanceHead = counterCurr;
                    } else if (countThis == ancestorCount) {
                        if (counterCurr < distanceHead) {
                            distanceHead = counterCurr;
                            ancestor = branchSHA;
                        }
                    }
                }
                counterBranch += 1;
                branchSHA = branchCommit.getParent();
                if (branchSHA != null) {
                    branchCommit = getCommit(branchSHA);
                } else {
                    break;
                }
            }
            counterCurr += 1;
            counterBranch = 0;
            currSHA = curr.getParent();
            branchCommit = Utils.readObject(mergeBranch, Commit.class);
            branchByte = Utils.serialize(branchCommit);
            branchSHA = Utils.sha1((Object) branchByte);
            if (currSHA != null) {
                curr = getCommit(currSHA);
            } else {
                break;
            }
        }
        return ancestor;
    }
    /** Checks Branch Files for merge conflicts and through
     * BRANCHCONTENT, SPLITCONTENT, CURRCONTENT, BRANCHFILES, and S. */
    void checkBranchFiles(HashMap<String, String> branchContent,
                          HashMap<String, String> splitContent,
                          HashMap<String, String> currContent,
                          Set<String> branchFiles, String s) {
        for (String file : branchFiles) {
            File f = new File(workingDirectory + "/" + file);
            File objF = new File(OBJ_DIR + branchContent.get(file));
            if (!splitContent.containsKey(file)
                    && !currContent.containsKey(file)) {
                Blob b = Utils.readObject(objF, Blob.class);
                if (f.exists()) {
                    f.delete();
                }
                Utils.writeContents(f, new String(b.getContents()));
                File addBlob = new File(ADD_STAGE + file);
                Utils.writeObject(addBlob, b);
            } else if (splitContent.containsKey(file)
                    && currContent.containsKey(file)) {
                if (splitContent.get(file).equals(currContent.get(file))
                        && !splitContent.get(file).equals(
                                branchContent.get(file))) {
                    Blob b = Utils.readObject(objF, Blob.class);
                    if (f.exists()) {
                        f.delete();
                    }
                    Utils.writeContents(f, new String(b.getContents()));
                    File addBlob = new File(ADD_STAGE + file);
                    Utils.writeObject(addBlob, b);
                }
                if (!splitContent.get(file).equals
                        (currContent.get(file))
                        && !splitContent.get(file).equals
                        (branchContent.get(file))
                        && !branchContent.get(file).equals
                        (currContent.get(file))) {
                    System.out.println("Encountered a merge conflict.");
                    File currFile = new File(OBJ_DIR
                            + currContent.get(file));
                    Blob currBlobFile = Utils.readObject(
                            currFile, Blob.class);
                    String currBlobStr = new String(
                            currBlobFile.getContents());
                    Blob branchBlob = Utils.readObject(objF, Blob.class);
                    String branchBlobStr = new String(
                            branchBlob.getContents());
                    String contentString = "<<<<<<< HEAD"
                            + System.lineSeparator()
                            + currBlobStr + " in " + getCurrBranchName()
                            + System.lineSeparator()
                            + "=======" + System.lineSeparator()
                            + branchBlobStr
                            + " in " + s;
                    if (f.exists()) {
                        f.delete();
                    }
                    File staging = new File(ADD_STAGE + file);
                    byte[] newbytes = contentString.getBytes();
                    Utils.writeObject(staging, newbytes);
                    Utils.writeContents(f, contentString);
                }
            }
        }
    }
    /** Merges files from branch S into current head branch. */
    void merge(String s) {
        if (s.equals(getCurrBranchName())) {
            throw new GitletException("Cannot merge a branch with itself.");
        }
        File addDir = new File(ADD_STAGE);
        File remDir = new File(REM_STAGE);
        if (addDir.list().length != 0 || remDir.list().length != 0) {
            throw new GitletException("You have uncommitted changes.");
        }
        File mergeBranch = new File(BRANCHES + s);
        if (!mergeBranch.exists()) {
            throw new GitletException("A branch with "
                    + "that name does not exist.");
        }
        String ancestor = findCommonAncestor(mergeBranch);
        if (ancestor != null) {
            Commit branchCommit = Utils.readObject(mergeBranch, Commit.class);
            byte[] branchByte = Utils.serialize(branchCommit);
            String branchSHA = Utils.sha1(branchByte);
            if (ancestor.equals(branchSHA)) {
                System.out.println("Given branch is an "
                        + "ancestor of the current branch.");
            } else if (ancestor.equals(getHEAD())) {
                System.out.println("Current branch fast-forwarded.");
                this.branchCheckout("s");
            } else {
                HashMap<String, String> splitContent =
                        getCommit(ancestor).getContents();
                HashMap<String, String> branchContent =
                        branchCommit.getContents();
                HashMap<String, String> currContent =
                        getCommit(getHEAD()).getContents();
                Set<String> branchFiles = branchContent.keySet();
                checkBranchFiles(branchContent,
                        splitContent, currContent, branchFiles, s);
                for (String file: splitContent.keySet()) {
                    if (currContent.containsKey(file)) {
                        if (splitContent.get(file).equals(currContent.get(file))
                                && !branchContent.containsKey(file)) {
                            File remove = new File(REM_STAGE + file);
                            try {
                                remove.createNewFile();
                            } catch (IOException exc) {
                                throw new GitletException
                                ("error while creating file");
                            }
                        }
                    }
                }
            }
            mergeCommit(s, mergeBranch);
        }
        Repository.updateRepo(this);
    }
    /** Commit what was just merged from
     * string S and file MERGEBRANCH. */
    void mergeCommit(String s, File mergeBranch) {
        String commitMessage = "Merged " + s
                + " into " + getCurrBranchName();
        Commit par1 = getCommit(getHEAD());
        Commit par2 = Utils.readObject(mergeBranch, Commit.class);
        Date d = new Date();
        Commit c = new Commit(this, commitMessage, par1, par2, d);
    }
    /** Returns shaID of current head commit of repository. */
    String getHEAD() {
        return this.head;
    }
    /** Returns current working directory of repository. */
    File getWorkingDirectory() {
        return this.workingDirectory;
    }
}
