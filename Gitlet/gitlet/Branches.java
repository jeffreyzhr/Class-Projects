package gitlet;

import java.io.Serializable;
import java.util.TreeMap;
/**
 * @author jeffreyzhou
 * */

public class Branches implements Serializable {

    /** All the branches in the file. */
    private TreeMap<String, String> branchtree;
    /** Head SHA1. */
    private String head;
    /** Current branch name. */
    private String currBranch;
    /** Current branch name. */
    private TreeMap<String, String> splitbranches;


    /** Constructor.
     * @param name is a name
     * */
    public Branches(String name) {
        branchtree = new TreeMap<String, String>();
        splitbranches = new TreeMap<String, String>();
        currBranch = name;
        save();
    }

    /** Gets branchtree.
     * @return returns a string
     * */
    public TreeMap<String, String> getBranchtree() {
        return this.branchtree;
    }

    /** Get currbranch.
     * @return returns a string
     * */
    public String getCurrBranch() {
        return this.currBranch;
    }

    /** Add function.
     * @param branchname is a string
     * @param S is also a string
     * @param splitcommit is a string
     * */
    public void add(String branchname, String S, String splitcommit) {
        branchtree.put(branchname, S);
        if (splitcommit != null) {
            splitbranches.put(branchname, splitcommit);
        }
        save();
    }

    /** Update function.
     * @param branchname is a string
     * */
    public void update(String branchname) {
        this.currBranch = branchname;
        save();
    }

    /** Gets head SHA1.
     * @return returns a string
     * */
    public String getHead() {
        head = branchtree.get(currBranch);
        return this.head;
    }

    /** Gets head SHA1.
     * @return returns a string
     * */
    public TreeMap<String, String> getSplitbranches() {
        return this.splitbranches;
    }

    /** Save function. */
    public void save() {
        Utils.writeContents(Repository.BRANCHFILE,
                Utils.serialize(this));
    }


}
