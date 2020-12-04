package gitlet;

import java.io.File;
import java.util.ArrayList;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Ethan Brown
 */
public class Main {
    /** FILE representing location of gitlet directory in CWD. */
    static final File GITLET_DIRECTORY = new File(".gitlet");
    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        try {
            new Main(args);
        } catch (GitletException exc) {
            System.out.println(exc.getMessage());
        }
        System.exit(0);
    }
    /** Checks ARGS and opens and runs necessary methods. */
    Main(String[] args) {
        if (args.length == 0) {
            throw new GitletException("Please enter a command.");
        }
        if (args[0].equals("init")) {
            Repository.init(GITLET_DIRECTORY);
        } else {
            if (!GITLET_DIRECTORY.exists()) {
                throw new GitletException
                ("Not in an initialized Gitlet directory.");
            }
            Repository repo = Repository.getRepo();
            if (args[0].equals("add")) {
                File f = new File(repo.getWorkingDirectory() + "/" + args[1]);
                if (!f.exists()) {
                    throw new GitletException("File does not exist.");
                }
                repo.add(f);
            } else if (args[0].equals("commit")) {
                if (args.length == 1 || args[1].equals("")) {
                    throw new GitletException("Please enter a commit message.");
                }
                Repository.commitComm(args);
            } else if (args[0].equals("checkout")) {
                if (args.length == 2) {
                    repo.branchCheckout(args[1]);
                } else if (args.length == 3) {
                    repo.checkout(repo.getHEAD(), args[2]);
                } else if (args.length == 4) {
                    if (!args[2].equals("--")) {
                        throw new GitletException("Incorrect operands.");
                    }
                    repo.checkout(args[1], args[3]);
                }
            } else if (args[0].equals("log")) {
                repo.log(repo.getHEAD(), new ArrayList<>());
            } else if (args[0].equals("rm")) {
                File f = new File(repo.getWorkingDirectory() + "/" + args[1]);
                repo.remove(f);
            } else if (args[0].equals("global-log")) {
                repo.global();
            } else if (args[0].equals("find")) {
                repo.find(args[1]);
            } else if (args[0].equals("status")) {
                repo.status();
            } else if (args[0].equals("branch")) {
                repo.branch(args[1]);
            } else if (args[0].equals("rm-branch")) {
                repo.remBranch(args[1]);
            } else if (args[0].equals("reset")) {
                repo.reset(args[1]);
            } else if (args[0].equals("merge")) {
                repo.merge(args[1]);
            } else {
                throw new GitletException("No command with that name exists.");
            }
        }
    }
}



