package gitlet;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;

/**
 * A copy of newest snapshot.
 *  @author Tiffany Kim */

public class Staging implements Serializable {
    /** Construct index from commit COMMIT. */
    public Staging(Commit commit) {
        stage = new HashMap<String, String>(commit.getTree());
        lastCommit = new HashMap<String, String>(commit.getTree());
    }

    /** Return file tree. */
    public HashMap<String, String> getTree() {
        return stage;
    }

    /** Add a file FILENAME with hash HASH. */
    public void addFile(String filename, String hash) {
        stage.put(filename, hash);
    }

    /** Remove a file FILENAME, return true if needs remove from hardisk.
     *  If is not in last commit, remove from index tree.
     *  If is in last commit and is in this index tree,
     *  remove the file from disk and tree.
     *  If is in last commit, but not in this index,
     *  do nothing.
     */
    public boolean rmFile(String filename) {
        boolean returned = false;
        if (lastCommit.get(filename) == null) {
            stage.remove(filename);
        } else if (stage.containsKey(filename)) {
            stage.remove(filename);
            returned = true;
        }
        return returned;
    }

    /** Check whether a file FILE is tracked,
     *  return its hash if tracked or empty.
     */
    public String tracked(String file) {
        if (stage.containsKey(file)) {
            return stage.get(file);
        }
        return "";
    }

    /** Return a key set. */
    public Set<String> getKeys() {
        return stage.keySet();
    }

    /** The file tree in this index. */
    private HashMap<String, String> stage;

    /** File tree from last commit. */
    private HashMap<String, String> lastCommit;
}
