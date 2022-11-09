package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

import static gitlet.Utils.join;
import static gitlet.Utils.readContentsAsString;
import static gitlet.Utils.plainFilenamesIn;

/**
 * A gitlet directory, running most of the methods in main. Probably.
 * That's the intention anyway
 *
 * @author jeffrey zhou
 */

public class Repository implements Serializable {

    /**
     * Current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * .gitlet directory.
     */
    public static final File GITLET_DIR = Utils.join(CWD, ".gitlet");
    /**
     * Commit directory.
     */
    public static final File COMMITS = Utils.join(GITLET_DIR,
            "/commits");
    /**
     * Blob directory.
     */
    public static final File BLOB_DIR = Utils.join(GITLET_DIR,
            "/blobs");
    /**
     * Hidden directory.
     */
    public static final File HIDDEN_DIR = Utils.join(GITLET_DIR,
            "/hidden");
    /**
     * Branches file.
     */
    public static final File BRANCHFILE = Utils.join(HIDDEN_DIR,
            "/branches");
    /**
     * Staging file.
     */
    public static final File STAGEFILE = Utils.join(HIDDEN_DIR,
            "/staging");

    /** Constructor.*/
    public Repository() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already"
                    + " exists in the current directory.");
            System.exit(0);
        }
        try {
            GITLET_DIR.mkdir();
            COMMITS.mkdir();
            BLOB_DIR.mkdir();
            HIDDEN_DIR.mkdir();
            BRANCHFILE.createNewFile();
            STAGEFILE.createNewFile();
            Commit initial = new Commit("initial commit",
                    null, false, null);
            Branches branches = new Branches("master");
            branches.add(branches.getCurrBranch(), initial.getCurrentSHA1(),
                    null);
            Stage stagingarea = new Stage();

        } catch (GitletException | IOException idk) {
            return;
        }

    }

    /** Add function.
     * @param filename is a string
     * */
    public static void add(String filename) {
        Stage stagingarea = Utils.readObject(STAGEFILE, Stage.class);
        stagingarea.add(filename);
    }

    /** Commit function.
     * @param commitmessage is a string
     * @param ismerge is a bool
     * */
    public static void commit(String commitmessage, Boolean ismerge) {
        Stage stagingarea = Utils.readObject(STAGEFILE, Stage.class);
        Branches branches = Utils.readObject(BRANCHFILE, Branches.class);
        Commit newcommit = new Commit(commitmessage, branches.getHead(),
                ismerge, null);
        stagingarea.clear();
        branches.add(branches.getCurrBranch(), newcommit.getCurrentSHA1(),
                null);

    }

    /** Checkout1 function.
     * @param filename is a string
     * */
    public static void checkout1(String filename) {
        Branches branches = Utils.readObject(BRANCHFILE, Branches.class);
        checkout2(branches.getHead(), filename);
    }

    /** Checkout2 function.
     * @param filename is a string
     * @param commitid1 is a string
     * */
    public static void checkout2(String commitid1, String filename) {
        String commitid = "";
        if (commitid1.length() < Utils.UID_LENGTH) {
            Integer commitlength = commitid1.length();
            List<String> commitList = plainFilenamesIn(COMMITS);
            for (String S : commitList) {
                if (S.substring(0, commitlength).equals(commitid1)) {
                    commitid = S;
                }
            }
        } else {
            commitid = commitid1;
        }
        File checkoutfile = Utils.join(COMMITS, commitid);
        if (!checkoutfile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit checkoutcommit = Utils.readObject(checkoutfile, Commit.class);
        if (!checkoutcommit.getBlobs().containsKey(filename)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        String filenameSHA1 = (String) checkoutcommit.getBlobs().get(filename);
        File wantedblob = Utils.join(BLOB_DIR, filenameSHA1);
        String blobcontent = Utils.readContentsAsString(wantedblob);
        if (!Utils.plainFilenamesIn(CWD).contains(filename)) {
            File newfile = Utils.join(CWD, filename);
            try {
                newfile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Utils.writeContents(Utils.join(CWD, filename), blobcontent);
    }

    /** Checkout3 function.
     * @param branchname is a string
     * */
    public static void checkout3(String branchname) throws IOException {
        Branches branches = Utils.readObject(BRANCHFILE, Branches.class);
        if (branches.getCurrBranch().equals(branchname)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        } else if (!branches.getBranchtree().containsKey(branchname)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        String checkoutcommitSHA1 = branches.getBranchtree().get(branchname);
        Commit bheadcommit = Utils.readObject(Utils.join(Repository.COMMITS,
                checkoutcommitSHA1), Commit.class);
        Commit currheadcommit = Utils.readObject(Utils.join(COMMITS,
                branches.getHead()), Commit.class);
        List<String> cwdfiles = plainFilenamesIn(CWD);
        if (currheadcommit.getBlobs() != null) {
            for (String file : cwdfiles) {
                if (!currheadcommit.getBlobs().containsKey(file)) {
                    if (bheadcommit.getBlobs() != null && bheadcommit
                            .getBlobs().containsKey(file)) {
                        System.out.print("There is an untracked file in the wa"
                                + "y; delete it, or add and commit it first.");
                        System.exit(0);
                    }
                }
            }
        } else {
            if (cwdfiles.size() >= 1) {
                System.out.print("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        branches.update(branchname);
        if (bheadcommit.getBlobs() != null) {
            for (String filename : bheadcommit.getBlobs().keySet()) {
                if (cwdfiles.contains(filename)) {
                    File changefile = Utils.join(CWD, filename);
                    String newcontent = readContentsAsString(join(BLOB_DIR,
                            bheadcommit.getBlobs().get(filename)));
                    Utils.writeContents(changefile, newcontent);
                } else {
                    File addfile = Utils.join(CWD, filename);
                    addfile.createNewFile();
                    String newcontent = readContentsAsString(Utils.join
                            (BLOB_DIR, bheadcommit.getBlobs().get(filename)));
                    Utils.writeContents(addfile, newcontent);
                }
            }
        }
        for (String file : cwdfiles) {
            if (bheadcommit.getBlobs() == null
                    || !bheadcommit.getBlobs().containsKey(file)) {
                Utils.restrictedDelete(file);
            }
        }
        Stage stagingarea = Utils.readObject(STAGEFILE, Stage.class);
        stagingarea.clear();
    }

    /** Log function.
     * */
    public static void log() {
        Branches branches = Utils.readObject(BRANCHFILE, Branches.class);
        Commit toprint = Utils.readObject(Utils.join(Repository.COMMITS,
                branches.getHead()), Commit.class);
        System.out.println("===");
        System.out.println("commit " + toprint.getCurrentSHA1());
        if (toprint.ismergecommit()) {
            System.out.println(toprint.getPrintmergemessage());
        }
        System.out.println("Date: " + toprint.getTimestamp());
        System.out.println(toprint.getMessage());
        System.out.println("");
        while (toprint.getParent() != null) {
            toprint = Utils.readObject(Utils.join(COMMITS, toprint.getParent()),
                    Commit.class);
            System.out.println("===");
            System.out.println("commit " + toprint.getCurrentSHA1());
            if (toprint.ismergecommit()) {
                System.out.println(toprint.getPrintmergemessage());
            }
            System.out.println("Date: " + toprint.getTimestamp());
            System.out.println(toprint.getMessage());
            System.out.println("");
        }
    }

    /** Branch function.
     * @param branchname is a string
     * */
    public static void branch(String branchname) {
        Branches branches = Utils.readObject(Repository.
                BRANCHFILE, Branches.class);
        if (branches.getBranchtree().containsKey(branchname)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        branches.add(branchname, branches.getHead(), branches.getHead());
        branches.add(branches.getCurrBranch(), branches.getHead(),
                branches.getHead());
        branches.add("splitbranch", branches.getHead(),
                branches.getHead());
    }

    /** Remove function.
     * @param filename is a string
     * */
    public static void remove(String filename) {
        Branches branches = Utils.readObject(Repository.BRANCHFILE,
                Branches.class);
        Stage stagingarea = Utils.readObject(STAGEFILE, Stage.class);
        Commit current = Utils.readObject(Utils.join(COMMITS,
                branches.getHead()), Commit.class);
        stagingarea.remove(current, filename);
    }

    /** Global log function.
     * */
    public static void glog() {
        List<String> allcommits = plainFilenamesIn(COMMITS);
        for (String commit : allcommits) {
            Commit toprint = Utils.readObject(Utils.join(Repository.COMMITS,
                    commit), Commit.class);
            System.out.println("===");
            System.out.println("commit " + toprint.getCurrentSHA1());
            System.out.println("Date: " + toprint.getTimestamp());
            System.out.println(toprint.getMessage());
            System.out.println("");
        }
    }

    /** Find function.
     * @param commitmessage is a string
     * */
    public static void find(String commitmessage) {
        List<String> allcommits = plainFilenamesIn(COMMITS);
        int count = 0;
        for (String commit : allcommits) {
            Commit tofind = Utils.readObject(Utils.join(Repository.COMMITS,
                    commit), Commit.class);
            if (tofind.getMessage().equals(commitmessage)) {
                System.out.println(tofind.getCurrentSHA1());
            } else {
                count += 1;
            }
        }
        if (count == allcommits.size()) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    /** Status function. */
    public static void status() {
        Branches branches = Utils.readObject(Repository.BRANCHFILE,
                Branches.class);
        Stage stagingarea = Utils.readObject(STAGEFILE, Stage.class);
        Commit curr = Utils.readObject(Utils.join(COMMITS, branches.getHead()),
                Commit.class);
        ArrayList<String> untracked = new ArrayList<>();
        System.out.println("=== Branches ===");
        for (Map.Entry branch : branches.getBranchtree().entrySet()) {
            String branchname = (String) branch.getKey();
            if (branchname.equals("splitbranch")) {
                break;
            }
            if (branchname.equals(branches.getCurrBranch())) {
                System.out.println("*" + branchname);
            } else {
                System.out.println(branchname);
            }
        }
        System.out.println("");
        System.out.println("=== Staged Files ===");
        for (Map.Entry addfile : stagingarea.getTobeadded().entrySet()) {
            System.out.println(addfile.getKey());
        }
        System.out.println("");
        System.out.println("=== Removed Files ===");
        for (String removefile : stagingarea.getToberemoved()) {
            System.out.println(removefile);
        }
        System.out.println("");
        System.out.println("=== Modifications Not Staged For Commit ===");
        statushelper(curr, stagingarea);
        System.out.println("");
        System.out.println("=== Untracked Files ===");
        List<String> cwdfiles = plainFilenamesIn(CWD);
        for (String filename : cwdfiles) {
            if (!stagingarea.getTobeadded().containsKey(filename)
                    && !stagingarea.getToberemoved().contains(filename)) {
                if (curr.getBlobs() == null
                        || !curr.getBlobs().containsKey(filename)) {
                    untracked.add(filename);
                }
            }
        }
        if (!curr.ismergecommit()) {
            for (String untrackedfile : untracked) {
                System.out.println(untrackedfile);
            }
        }
        System.out.println("");
    }

    /** Status function.
     * @param stagingarea is a stage
     * @param curr is a commit
     * */
    public static void statushelper(Commit curr, Stage stagingarea) {
        ArrayList<String> modified = new ArrayList<>();
        List<String> cwdfiles = plainFilenamesIn(CWD);
        Set<String> currentfiles = Collections.emptySet();
        if (curr.getBlobs() != null) {
            currentfiles = curr.getBlobs().keySet();
        }
        for (String filename : currentfiles) {
            if (cwdfiles.contains(filename)) {
                File cwdfile = Utils.join(CWD, filename);
                File currfile = Utils.join(BLOB_DIR,
                        curr.getBlobs().get(filename));
                if (!Utils.readContentsAsString(cwdfile)
                        .equals(Utils.readContentsAsString(currfile))) {
                    if (!stagingarea.getTobeadded().containsKey(filename)
                            && !stagingarea.getToberemoved()
                            .contains(filename)) {
                        modified.add(filename + " (modified)");
                    }
                }
            }
        }
        for (String filename : stagingarea.getTobeadded().keySet()) {
            if (cwdfiles.contains(filename)) {
                File cwdfile = Utils.join(CWD, filename);
                File currfile = Utils.join(BLOB_DIR,
                        stagingarea.getTobeadded().get(filename));
                if (!Utils.readContentsAsString(cwdfile)
                        .equals(Utils.readContentsAsString(currfile))) {
                    modified.add(filename + " (modified)");
                }
            } else {
                modified.add(filename + " (deleted)");
            }
        }
        if (!currentfiles.isEmpty()) {
            for (String filename : currentfiles) {
                if (!stagingarea.getToberemoved().contains(filename)
                        && !cwdfiles.contains(filename)) {
                    modified.add(filename + " (deleted)");
                }
            }
        }
        if (!curr.ismergecommit()) {
            for (String modifiedfile : modified) {
                System.out.println(modifiedfile);
            }
        }
    }

    /** Remove branch function.
     * @param branchname is a string
     * */
    public static void rmbranch(String branchname) {
        Branches branches = Utils.readObject(Repository.BRANCHFILE,
                Branches.class);
        if (branchname.equals(branches.getCurrBranch())) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        if (!branches.getBranchtree().containsKey(branchname)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        branches.getBranchtree().remove(branchname);
        branches.save();
    }

    /** Reset function.
     * @param commitID is a string
     * */
    public static void reset(String commitID) throws IOException {
        Branches branches = Utils.readObject(Repository.BRANCHFILE,
                Branches.class);
        Stage stagingarea = Utils.readObject(STAGEFILE, Stage.class);
        File resetfile = Utils.join(COMMITS, commitID);
        if (!resetfile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        branches.add("resetbranch", commitID, null);
        checkout3("resetbranch");
        branches.getBranchtree().remove("resetbranch");
        branches.add(branches.getCurrBranch(), commitID, null);
        stagingarea.clear();
    }

    /** Merge function.
     * @param branchname is a string
     * */
    public static void merge(String branchname) throws IOException {
        Branches branches = Utils.readObject(Repository.BRANCHFILE,
                Branches.class);
        Stage stagingarea = Utils.readObject(STAGEFILE, Stage.class);
        if (branchname.equals(branches.getCurrBranch())) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        } else if (!branches.getBranchtree().containsKey(branchname)) {
            System.out.print("A branch with that name does not exist.");
            System.exit(0);
        }
        if (!stagingarea.getToberemoved().isEmpty()
                || !stagingarea.getTobeadded().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        String splittemp = branches.getSplitbranches().get(branches
                .getCurrBranch());
        Commit splitbranch = Utils.readObject(Utils.join(COMMITS, splittemp),
                Commit.class);
        Set<String> splitfiles = Collections.emptySet();
        if (splitbranch.getBlobs() != null) {
            splitfiles = splitbranch.getBlobs().keySet();
        }
        Commit currentbranch = Utils.readObject(Utils.join(COMMITS,
                branches.getHead()), Commit.class);
        Set<String> currentfiles = Collections.emptySet();
        if (currentbranch.getBlobs() != null) {
            currentfiles = currentbranch.getBlobs().keySet();
        }
        String mergetemp = branches.getBranchtree().get(branchname);
        Commit mergebranch = Utils.readObject(Utils.join(COMMITS,
                mergetemp), Commit.class);
        Set<String> mergefiles = Collections.emptySet();
        if (mergebranch.getBlobs() != null) {
            mergefiles = mergebranch.getBlobs().keySet();
        }
        error(splittemp, currentbranch, branchname, mergetemp, mergebranch);
        Boolean mergeconflict = merge1(splitfiles, splitbranch, currentfiles,
                currentbranch, mergefiles, mergebranch, stagingarea);
        for (String filename : mergefiles) {
            if (!splitfiles.contains(filename)
                    && !currentfiles.contains(filename)) {
                String mergefileSHA1 = mergebranch.getBlobs().get(filename);
                File update = Utils.join(CWD, filename);
                update.createNewFile();
                String updatecontent = Utils.readContentsAsString(Utils.
                        join(BLOB_DIR, mergefileSHA1));
                Utils.writeContents(update, updatecontent);
                stagingarea.add(filename);
                break;
            }
        }
        merge2(branchname, currentbranch, mergetemp);
        if (mergeconflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /** Merge helper function.
     * @param currentbranch is something
     * @param stagingarea is something
     * @param currentfiles is something
     * @param mergebranch is something
     * @param splitfiles is something
     * @param mergefiles is something
     * @param splitbranch is a string
     * @return returns a boolean
     * */
    public static Boolean merge1(Set<String> splitfiles, Commit splitbranch,
                              Set<String> currentfiles, Commit currentbranch,
                              Set<String> mergefiles, Commit mergebranch,
                              Stage stagingarea) {
        boolean mergeconflict1 = false;
        for (String filename : splitfiles) {
            String splitfileSHA1 = splitbranch.getBlobs().get(filename);
            String currentfileSHA1;
            String mergefileSHA1;
            if (currentfiles.contains(filename)) {
                currentfileSHA1 = currentbranch.getBlobs().get(filename);
            } else {
                currentfileSHA1 = null;
            }
            if (mergefiles.contains(filename)) {
                mergefileSHA1 = mergebranch.getBlobs().get(filename);
            } else {
                mergefileSHA1 = null;
            }
            if (splitfileSHA1.equals(currentfileSHA1)) {
                if (mergefileSHA1 == null) {
                    remove(filename);
                    break;
                }
                if (!splitfileSHA1.equals(mergefileSHA1)) {
                    File update = Utils.join(CWD, filename);
                    String updatecontent = Utils.readContentsAsString(Utils.
                            join(BLOB_DIR, mergefileSHA1));
                    Utils.writeContents(update, updatecontent);
                    stagingarea.add(filename);
                    break;
                } else {
                    break;
                }
            }
            if (!splitfileSHA1.equals(currentfileSHA1)
                    && !splitfileSHA1.equals(mergefileSHA1)) {
                if (!currentfileSHA1.equals(mergefileSHA1)) {
                    String contentcurr = "";
                    String contentmerge = "";
                    if (currentfileSHA1 != null) {
                        contentcurr = Utils.readContentsAsString(join
                                (BLOB_DIR, currentfileSHA1));
                    }
                    if (mergefileSHA1 != null) {
                        contentmerge = Utils.readContentsAsString(join
                                (BLOB_DIR, mergefileSHA1));
                    }
                    String newContent = "<<<<<<< HEAD" + "\n" + contentcurr
                            + "=======" + "\n" + contentmerge + ">>>>>>>\n";
                    Utils.writeContents(join(CWD, filename), newContent);
                    add(filename);
                    mergeconflict1 = true;
                    break;
                } else {
                    break;
                }
            }
        }
        return mergeconflict1;
    }
    /** Merge helper function.
     * @param currentbranch is something
     * @param splittemp is something
     * @param branchname is something
     * @param mergebranch is something
     * @param mergetemp is something
     * */
    public static void error(String splittemp, Commit currentbranch,
                             String branchname, String mergetemp,
                             Commit mergebranch) throws IOException {
        if (splittemp.equals(currentbranch.getCurrentSHA1())) {
            checkout3(branchname);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        if (splittemp.equals(mergetemp)) {
            System.out.println("Given branch is an"
                    + " ancestor of the current branch.");
            System.exit(0);
        }
        List<String> cwdfiles = plainFilenamesIn(CWD);
        if (currentbranch.getBlobs() != null) {
            for (String file : cwdfiles) {
                if (!currentbranch.getBlobs().containsKey(file)
                        && mergebranch.getBlobs().containsKey(file)) {
                    System.out.print("There is an untracked file in the"
                            + " way; delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        } else {
            if (cwdfiles.size() >= 1) {
                System.out.print("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
                System.exit(0);
            }
        }
    }

    /** Merge helper function.
     * @param currentbranch is something
     * @param mergetemp is something
     * @param branchname is something
     * */
    public static void merge2(String branchname, Commit currentbranch,
                              String mergetemp) {
        Branches branches = Utils.readObject(Repository.BRANCHFILE,
                Branches.class);
        Stage stagingarea = Utils.readObject(STAGEFILE, Stage.class);
        String commitmessage = "Merged " + branchname + " into "
                + branches.getCurrBranch() + ".";
        String currenthex = currentbranch.getCurrentSHA1().substring(0, 7);
        String mergehex = mergetemp.substring(0, 7);
        String printmergemessage = "Merge: " + currenthex + " " + mergehex;
        Commit mergecommit = new Commit(commitmessage, branches.getHead(),
                true, printmergemessage);
        stagingarea.clear();
        branches.add(branches.getCurrBranch(), mergecommit.getCurrentSHA1(),
                mergetemp);
        branches.add(branchname, mergecommit.getCurrentSHA1(),
                mergecommit.getCurrentSHA1());
    }
}
