package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Jeffrey Zhou
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        if (args[0].equals("init")) {
            new Repository();
        } else if (!Repository.GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        } else if (args[0].equals("add")) {
            Repository.add(args[1]);
        } else if (args[0].equals("commit")) {
            if (args[1].equals("") || args.length != 2) {
                System.out.println("Please enter a commit message.");
                System.exit(0);
            }
            Repository.commit(args[1], false);
        } else if (args[0].equals("status")) {
            Repository.status();
        } else if (args[0].equals("log")) {
            Repository.log();
        } else if (args[0].equals("global-log")) {
            Repository.glog();
        } else if (args[0].equals("find")) {
            Repository.find(args[1]);
        } else if (args[0].equals("checkout")) {
            if (args.length == 3 && args[1].equals("--")) {
                Repository.checkout1(args[2]);
            } else if (args.length == 4 && args[2].equals("--")) {
                Repository.checkout2(args[1], args[3]);
            } else if (args.length == 2) {
                Repository.checkout3(args[1]);
            } else {
                System.out.println("Incorrect Operands");
                System.exit(0);
            }
        } else if (args[0].equals("branch")) {
            Repository.branch(args[1]);
        } else if (args[0].equals("rm")) {
            if (args.length != 2) {
                System.out.println("invalid input");
                System.exit(0);
            }
            Repository.remove(args[1]);
        } else if (args[0].equals("rm-branch")) {
            Repository.rmbranch(args[1]);
        } else if (args[0].equals("reset")) {
            Repository.reset(args[1]);
        } else if (args[0].equals("merge")) {
            Repository.merge(args[1]);
        } else {
            System.out.println("No command with that name exists.");
            System.exit(0);
        }
    }
}
