package gitlet;

import com.sun.source.tree.Tree;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import static gitlet.Repository.STAGEFILE;

/**
 * @author Jeffreyzhou
 *
 * */

public class Commit implements Serializable {

    /** The commit message. */
    private String _message;
    /** The date. */
    private Date datevalue;
    /** The timestamp. */
    private String timestamp;
    /** The date format thingy. */
    public static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("EEE MMM d kk:mm:ss yyyy -0800 ");
    /** The SHA1 value of the current commit. */
    private String currcommitSHA1;
    /** The thing that contains all the blob values. */
    private TreeMap<String, String> _blobs;
    /** The thing that I had to add and im very annoyed about. */
    private boolean ismergecommit;
    /** The same as above. */
    private String printmergemessage;
    /** Parent of the commit. */
    private String _parent;

    /** Commit constructor.
     * @param ismerge is a string
     * @param mergemessage is a string
     * @param message ghjk
     * @param parent ghj
     * */
    public Commit(String message, String parent, Boolean ismerge,
                   String mergemessage) {
        _message = message;
        _parent = parent;
        if (_parent == null) {
            this.datevalue = new Date(0);
            _blobs = null;
        } else {
            this.datevalue = new Date();
            _blobs = new TreeMap<String, String>();
            Stage stagingarea = Utils.readObject(STAGEFILE, Stage.class);
            if (stagingarea.getTobeadded().isEmpty()
                    && stagingarea.getToberemoved().isEmpty()) {
                System.out.println("No changes added to the commit.");
                System.exit(0);
            }
            copyParent(parent);
            updateBlobs(stagingarea);
            stagingarea.clear();
        }
        this.timestamp = DATE_FORMAT.format(datevalue);
        this.ismergecommit = ismerge;
        this.printmergemessage = mergemessage;
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Gets the commit message.
     * @return returns the message
     * */
    public String getMessage() {
        return _message;
    }

    /** Gets the commit timestamp.
     * @return returns ths timestamp
     * */
    public String getTimestamp() {
        return this.timestamp;
    }

    /** Gets the commit parent.
     * @return returns ths timestamp
     * */
    public String getParent() {
        return _parent;
    }

    /** Gets the commit SHA1.
     * @return returns ths currsha
     * */
    public String getCurrentSHA1() {
        return this.currcommitSHA1;
    }

    /** Gets the commit blobs.
     * @return returns ths blob
     * */
    public TreeMap<String, String> getBlobs() {
        return _blobs;
    }

    /** Gets the commit mergemessage.
     * @return returns ths print
     * */
    public String getPrintmergemessage() {
        return this.printmergemessage;
    }

    /** Helper function.
     * @param parent is a string
     * */
    private void copyParent(String parent) {
        File parentFile = Utils.join(Repository.COMMITS, parent);
        Commit parentC = Utils.readObject(parentFile, Commit.class);
        if (parentC._blobs == null) {
            return;
        }
        _blobs.putAll(parentC._blobs);
    }

    /** Another helper func.
     * @param stagingarea is a stage
     * */
    private void updateBlobs(Stage stagingarea) {
        for (Map.Entry indivblob : stagingarea.getTobeadded().entrySet()) {
            _blobs.put((String) indivblob.getKey(),
                    (String) indivblob.getValue());
        }
        for (String filename : stagingarea.getToberemoved()) {
            _blobs.remove(filename);
        }
    }

    /** Gets the commit mergebool.
     * @return mergecommit
     * */
    public boolean ismergecommit() {
        return this.ismergecommit;
    }

    /** Save func.
     * */
    private void save() throws GitletException, IOException {
        this.currcommitSHA1 = Utils.sha1(Utils.serialize(this));
        File newcommit = Utils.join(Repository.COMMITS, currcommitSHA1);
        newcommit.createNewFile();
        Utils.writeContents(newcommit, Utils.serialize(this));
    }

    /** Compare func.
     * @param c is a commit
     * @return a boolean
     * */
    private boolean equals(Commit c) {
        if (c.getCurrentSHA1().equals(this.currcommitSHA1)) {
            return true;
        }
        return false;
    }

    /** Is the commit message.
     * @param current is a string
     * @param mergein is a string
     * */
    public void mergemessage(String current, String mergein) {
        String currenthex = current.substring(0, 7);
        String mergehex = mergein.substring(0, 7);
        printmergemessage = "Merge: " + currenthex + " " + mergehex;
    }

}
