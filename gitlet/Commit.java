package gitlet;

import java.io.Serializable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.Formatter;

/**
 * Each object is a commit.
 * Every commit contains a reference.
 *  @author Tiffany Kim */

public class Commit extends Utils implements Serializable {
    /** Create init commit. */
    public Commit() {
        _commitTime = ZonedDateTime.ofInstant(
                INITIALDATE, ZoneId.systemDefault());
        _message = "initial commit";
        _parentHash = "";
        _tree = new HashMap<String, String>();
        calcLocalHash();
    }

    /** Create new commit from INDEX.
     *  Add MESSAGE and HEAD hash.
     *  STAGE is a Staging area.
     *  */
    public Commit(Staging stage, String message, String head) {
        _message = message;
        _parentHash = head;
        _tree = new HashMap<String, String>(stage.getTree());
        _commitTime = ZonedDateTime.now();
        calcLocalHash();
    }

    /** Update hash value. */
    private void calcLocalHash() {
        _commitHash = sha1(serialize(_tree), _message,
                serialize(_commitTime));
    }

    /** Return hash. */
    public String getHash() {
        return _commitHash;
    }

    /** Return tree. */
    public HashMap<String, String> getTree() {
        return _tree;
    }

    /** Date format in log. */
    static final DateTimeFormatter DTF
            = DateTimeFormatter.ofPattern(
            "EEE MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);

    /** Return commit time string. */
    public String timeToString() {
        return _commitTime.format(DTF);
    }

    /** Return hash code of FILE if file is in _tree, empty elsewise. */
    public String tracked(String file) {
        if (_tree.containsKey(file)) {
            return _tree.get(file);
        }
        return "";
    }

    /** Return message. */
    public String getMessage() {
        return _message;
    }

    /** Return a key set. */
    public Set<String> getKeys() {
        return _tree.keySet();
    }

    /** Return parent. */
    public String getParent() {
        return _parentHash;
    }

    /** Add co-parent S. */
    public void setCoParent(String s) {
        secondparent = s;
    }

    /** Override toString for log. */
    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("===\n");
        out.format("commit %s\n", _commitHash);
        if (secondparent != null) {
            out.format("Merge: %s %s\n",
                    _parentHash.substring(0, 7),
                    secondparent.substring(0, 7));
        }
        out.format("Date: %s\n", timeToString());
        out.format("%s\n", _message);
        return out.toString();
    }

    /** Commit time. */
    private ZonedDateTime _commitTime;

    /** Commit message. */
    private String _message;

    /** Parent commit hash. */
    private String _parentHash;

    /** File tree. */
    private HashMap<String, String> _tree;

    /** Hash for the commit itself. */
    private String _commitHash;

    /** Merged branch. */
    private String secondparent;

    /**
     * Initial Date.
     */
    public static final Instant INITIALDATE = Instant.EPOCH;
}
