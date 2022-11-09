package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author jeffreyzhou
 * */

public class Stage implements Serializable {

    /** The add stage. */
    private Map<String, String> tobeadded;
    /** The remove stage. */
    private ArrayList<String> toberemoved;

    /** The Stage constructor. */
    public Stage() {
        tobeadded = new TreeMap<String, String>();
        toberemoved = new ArrayList<>();
        save();
    }

    /** Gets tobeadded.
     * @return returns a Map
     * */
    public Map<String, String> getTobeadded() {
        return this.tobeadded;
    }

    /** Gets toberemoved.
     * @return returns a list
     * */
    public ArrayList<String> getToberemoved() {
        return this.toberemoved;
    }

    /** The add function.
     * @param filename is a String
     * */
    public void add(String filename) {
        File newfile = Utils.join(Repository.CWD, filename);
        if (!newfile.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        String filecontent = Utils.readContentsAsString(newfile);
        String fileSHA1 = Utils.sha1(Utils.serialize(filecontent));

        Branches branches = Utils.readObject(Repository.BRANCHFILE,
                Branches.class);
        Commit current = Utils.readObject(Utils.join(Repository.COMMITS,
                branches.getHead()), Commit.class);

        if (toberemoved.contains(filename)) {
            toberemoved.remove(filename);
            File fileblob = Utils.join(Repository.BLOB_DIR, fileSHA1);
            try {
                if (!fileblob.exists()) {
                    fileblob.createNewFile();
                }
                Utils.writeContents(fileblob, filecontent);
                save();
            } catch (IOException e) {
                System.exit(0);
            }
            System.exit(0);
        }

        if (current.getBlobs() != null
                && current.getBlobs().containsKey(filename)
                && current.getBlobs().get(filename).equals(fileSHA1)) {
            System.exit(0);
        }

        if (tobeadded.containsKey(filename)) {
            if (tobeadded.get(filename) != fileSHA1) {
                tobeadded.put(filename, fileSHA1);
                save();
            }
            System.exit(0);
        }

        tobeadded.put(filename, fileSHA1);
        File fileblob = Utils.join(Repository.BLOB_DIR, fileSHA1);
        try {
            if (!fileblob.exists()) {
                fileblob.createNewFile();
            }
            Utils.writeContents(fileblob, filecontent);
            save();
        } catch (IOException e) {
            System.exit(0);
        }
    }

    /** The remove func.
     * @param filename is a string
     * @param currentCommit is a commit
     * */
    public void remove(Commit currentCommit, String filename) {
        if (currentCommit.getBlobs() == null
                || !currentCommit.getBlobs().containsKey(filename)) {
            if (!tobeadded.containsKey(filename)) {
                System.out.println("No reason to remove the file.");
                System.exit(0);
            }
        }

        if (tobeadded.containsKey(filename)) {
            tobeadded.remove(filename);
        }

        if (currentCommit.getBlobs() != null
                && currentCommit.getBlobs().containsKey(filename)) {
            toberemoved.add(filename);
            Utils.restrictedDelete(Utils.join(Repository.CWD, filename));
        }
        save();
    }

    /** The clear func. */
    public void clear() {
        tobeadded.clear();
        toberemoved.clear();
        save();
    }

    /** The save func. */
    public void save() {
        Utils.writeContents(Repository.STAGEFILE, Utils.serialize(this));
    }
}
