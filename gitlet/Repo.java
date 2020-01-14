package gitlet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.io.File;
import java.io.Serializable;

/** A repository.
 *  @author Tiffany Kim */

public class Repo extends Utils implements Serializable {

    /** Initialize an empty repo in current directory. */
    public Repo() {
        myDirectory = new File(".");
        folder = join(myDirectory, ".gitlet");
        _remoteDir = new TreeMap<String, File>();
        _commitToMessage = new TreeMap<String, String>();
        commitList = new ArrayList<String>();
        headCache = new Commit();
        _branches = new TreeMap<String, String>();
        head = headCache.getHash();
        _curBranch = "master";
        newCommit();
        _stagingArea = new Staging(headCache);
    }

    /** Turn current stage area to commit with message MESSAGE.
     *  And clear the Area;
     *  Store the newest commit hash;
     */
    public void newCommit(String message) {
        if (_stagingArea.getTree().equals(headCache.getTree())) {
            throw new GitletException("No changes added to the commit.");
        }
        head = headCache.getHash();
        headCache = new Commit(_stagingArea, message, head);
        byte[] blob = serialize(headCache);
        head = headCache.getHash();
        commitList.add(head);
        _commitToMessage.put(head, message);
        File outputDir = join(folder, head);
        writeContents(outputDir, blob);
        _stagingArea = new Staging(headCache);
        _branches.put(_curBranch, head);
    }

    /** Add a new merge commit with message MESSAGE, and another parent
     *  hash COPARENT.
     *  */
    private void mergeCommit(String message, String coparent) {
        if (_stagingArea.getTree().equals(headCache.getTree())) {
            throw new GitletException("No changes added to the commit.");
        }
        head = headCache.getHash();
        headCache = new Commit(_stagingArea, message, head);
        headCache.setCoParent(_branches.get(coparent));
        byte[] blob = serialize(headCache);
        head = headCache.getHash();
        commitList.add(head);
        _commitToMessage.put(head, message);
        writeContents(join(folder, head), blob);
        _stagingArea = new Staging(headCache);
        _branches.put(_curBranch, head);
    }

    /** Store the initial hash. */
    public void newCommit() {
        String hash = headCache.getHash();
        writeContents(join(folder, hash), serialize(headCache));
        commitList.add(hash);
        _commitToMessage.put(hash, "initial commit");
        _branches.put(_curBranch, head);
    }

    /** Add a new file FILE to index and local disk. */
    public void addFile(String file) {
        byte[] blob = readContents(join(myDirectory, file));
        String hash = readFileHash(file);
        writeContents(join(folder, hash), blob);
        _stagingArea.addFile(file, hash);
    }

    /** Extract a file FILE from blobs HASH, store it with name. */
    private void extractFile(String file, String hash) {
        File blobDir = join(folder, hash);
        File writeDir = join(myDirectory, file);
        writeContents(writeDir, readContents(blobDir));
    }

    /** Remove FILE from index. */
    public void removeFile(String file) {
        if (_stagingArea.tracked(file).length() == 0
                && headCache.tracked(file).length() == 0) {
            throw new GitletException("No reason to remove the file.");
        }
        if (_stagingArea.rmFile(file)) {
            rmfromFolder(file);
        }
    }

    /** Remove FILE from folder. */
    private void rmfromFolder(String file) {
        restrictedDelete(join(myDirectory, file));
    }

    /** Show logs. */
    public void printLog() {
        String print = head;
        while (!(print.length() == 0)) {
            Commit c = getCommit(print);
            System.out.println(c.toString());
            print = c.getParent();
        }
    }

    /** Show global log. */
    public void printGlobalLog() {
        for (String c : commitList) {
            Commit commit = getCommit(c.toString());
            System.out.println(commit);
        }
    }

    /** Get modification but not staged files, return a set. */
    private TreeSet<String> getModifiedNotStaged() {
        TreeSet<String> result = new TreeSet<String>();
        for (HashMap.Entry<String, String> e
                : _stagingArea.getTree().entrySet()) {
            String file = e.getKey();
            String hash = e.getValue();
            if (!hash.equals(readFileHash(file))) {
                result.add(file);
            }
        }
        return result;
    }

    /** Print the status. */
    public void printStatus() {
        System.out.println("=== Branches ===");
        for (String s : _branches.keySet()) {
            if (s.equals(_curBranch)) {
                System.out.println("*" + s);
            } else {
                System.out.println(s);
            }
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        for (String s : getStaged()) {
            System.out.println(s);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        for (String s : getRemovedFiles()) {
            System.out.println(s);
        }
        System.out.println();

        TreeSet<String> allFilesNameSet
                = new TreeSet<>(plainFilenamesIn(myDirectory));
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String s : getModifiedNotStaged()) {
            if (allFilesNameSet.contains(s)) {
                System.out.println(s + " (modified)");
            } else {
                System.out.println(s + " (deleted)");
            }
        }
        System.out.println();

        System.out.println("=== Untracked Files ===");
        allFilesNameSet.removeAll(_stagingArea.getKeys());
        for (String s : allFilesNameSet) {
            System.out.println(s);
        }
    }

    /** Read and return a commit with hash HASH from the history. */
    public Commit getCommit(String hash) {
        if (!commitList.contains(hash)) {
            throw new GitletException("No commit with that id exists.");
        }
        return readObject(join(folder, hash), Commit.class);
    }

    /** Checkout to branch NAME. */
    public void checkout2branch(String name) {
        if (name.equals(_curBranch)) {
            throw new GitletException(
                    "No need to checkout the current branch.");
        } else if (!_branches.keySet().contains(name)) {
            throw new GitletException("No such branch exists.");
        } else {
            _curBranch = name;
            revertWorkingFolder(_branches.get(name));
        }
    }

    /** Reset a FILE from head. */
    public void revertFile(String file) {
        String hashBlob = headCache.tracked(file);
        if (hashBlob.equals("")) {
            throw new GitletException("File does not exist in that commit.");
        } else {
            extractFile(file, hashBlob);
        }
    }

    /** Reset a file from past commit COMMIT, MODIFIED FILE NOT STAGED. */
    public void revertFile(String commit, String file) {
        String hashBlob = getCommit(commit).tracked(file);
        if (hashBlob.length() == 0) {
            throw new GitletException("File does not exist in that commit.");
        } else {
            extractFile(file, hashBlob);
        }
    }

    /** Simply reset to a commit(dangerous) COMMIT. */
    public void dangerousReset(String commit) {
        for (String f : _stagingArea.getTree().keySet()) {
            rmfromFolder(f);
        }
        HashMap<String, String> oldTree = getCommit(commit).getTree();
        for (String f : oldTree.keySet()) {
            extractFile(f, oldTree.get(f));
        }
        headCache = getCommit(commit);
        head = commit;
        _branches.put(_curBranch, head);
        _stagingArea = new Staging(headCache);
    }

    /** Reset whole working folder to past commit COMMIT. */
    public void revertWorkingFolder(String commit) {
        if (!commitList.contains(commit)) {
            throw new GitletException("No commit with that id exists.");
        }
        HashMap<String, String> oldTree = getCommit(commit).getTree();
        for (String file : oldTree.keySet()) {
            if (_stagingArea.tracked(file).equals("")) {
                File fileDir = join(myDirectory, file);
                if (fileDir.exists()) {
                    throw new GitletException(
                            "There is an untracked file in the way; "
                                    + "delete it or add it first.");
                }
            }
        }
        for (String f : _stagingArea.getTree().keySet()) {
            rmfromFolder(f);
        }
        for (String f : oldTree.keySet()) {
            extractFile(f, oldTree.get(f));
        }
        headCache = getCommit(commit);
        head = headCache.getHash();
        _stagingArea = new Staging(headCache);
        _branches.put(_curBranch, head);
    }

    /** Create a new branch NAME. */
    public void createBranch(String name) {
        if (_branches.containsKey(name)) {
            throw new GitletException(
                    "A branch with that name already exists.");
        } else {
            _branches.put(name, head);
        }
    }

    /** Remove a branch NAME. */
    public void removeBranch(String name) {
        if (name.equals(_curBranch)) {
            throw new GitletException("Cannot remove the current branch.");
        } else if (!_branches.containsKey(name)) {
            throw new GitletException(
                    "A branch with that name does not exist.");
        } else {
            _branches.remove(name);
        }
    }

    /** Read FILE's content, return its hash. */
    private String readFileHash(String file) {
        File fileDir = join(myDirectory, file);
        if (!fileDir.exists()) {
            return "";
        } else {
            return sha1(readContents(fileDir));
        }
    }

    /** Get staged files, return a set. */
    public TreeSet<String> getStaged() {
        TreeSet<String> result = new TreeSet<String>();
        for (HashMap.Entry<String, String> e
                : _stagingArea.getTree().entrySet()) {
            String file = e.getKey();
            String hash = e.getValue();
            if (!hash.equals(headCache.tracked(file))) {
                result.add(file);
            }
        }
        return result;
    }

    /** Get removed files, return a set. */
    public TreeSet<String> getRemovedFiles() {
        TreeSet<String> returnTree = new TreeSet<String>();
        for (HashMap.Entry<String, String> e
                : headCache.getTree().entrySet()) {
            String file = e.getKey();
            if (_stagingArea.tracked(file).length() == 0) {
                returnTree.add(file);
            }
        }
        return returnTree;
    }

    /** Iterate through givenBranch.
     *  @param givenBranch is a given Branch.
     *  @param curBranch is a current Branch.
     *  @param splitCommit is a split point Commit.
     *  @param toBeCheckedOut is a set of files to be Checked out.
     *  @param conflictFile is a set of files that conflict.
     */
    private void firstMergeIterate(Commit givenBranch,
                                    Commit curBranch,
                                    Commit splitCommit,
                                    Set<String> toBeCheckedOut,
                                    Set<String> conflictFile) {
        for (String file : givenBranch.getKeys()) {
            String givenFileHash = givenBranch.tracked(file);
            String currentFileHash = curBranch.tracked(file);
            if (!givenFileHash.equals(currentFileHash)) {
                String splitPointHash = splitCommit.tracked(file);
                if ((!currentFileHash.equals(splitPointHash))
                        && (!givenFileHash.equals(splitPointHash))) {
                    conflictFile.add(file);
                    continue;
                }
                if (currentFileHash.equals(splitCommit.tracked(file))) {
                    if ((readFileHash(file).length() != 0)
                            && (currentFileHash.equals(""))) {
                        throw new GitletException(
                                        "There is an untracked"
                                                + "file in the way; "
                                                + "delete it or add it first.");
                    }
                    toBeCheckedOut.add(file);
                }
            }
        }
    }

    /** Iterate through givenBranch.
     *  @param givenBranch is a given Branch.
     *  @param curBranch is a current Branch.
     *  @param splitCommit is a split point Commit.
     *  @param toBeDeleted is a set of files to be deleted.
     *  @param conflictFile is a set of files that conflict.
     */
    private void secondMergeIterate(Commit givenBranch,
                                     Commit curBranch,
                                     Commit splitCommit,
                                     Set<String> toBeDeleted,
                                     Set<String> conflictFile) {
        for (String file : curBranch.getKeys()) {
            String givenFileHash = givenBranch.tracked(file);
            String currentFileHash = curBranch.tracked(file);
            if (givenFileHash.equals("")) {
                if ((!splitCommit.tracked(file).equals("")
                        &&
                        (!currentFileHash.equals(
                                splitCommit.tracked(file))))) {
                    conflictFile.add(file);
                    continue;
                }
                if (currentFileHash.equals(splitCommit.tracked(file))) {
                    if (!_stagingArea.tracked(file).equals("")) {
                        toBeDeleted.add(file);
                    } else {
                        if (!readFileHash(file).equals("")) {
                            throw new GitletException(
                                    "There is an untracked file in the way;"
                                            + " delete it or add it first.");
                        }
                    }
                }
            }
        }
    }

    /** Check whether BRANCH satisfies basic merge conditions. */
    private void checkMergeCondition(String branch) {
        if (!_branches.containsKey(branch)) {
            throw new GitletException(
                    "A branch with that name does not exist.");
        }
        if (branch.equals(_curBranch)) {
            throw new GitletException("Cannot merge a branch with itself.");
        }
        if ((!getRemovedFiles().isEmpty()) || (!getStaged().isEmpty())) {
            throw new GitletException("You have uncommitted changes.");
        }
    }

    /** Merge another branch with current branch BRANCH. */
    public void merge(String branch) {
        checkMergeCondition(branch);
        String splitPoint = getSplitPoint(branch);
        Commit splitCommit = getCommit(splitPoint);
        Commit givenBranch = getCommit(_branches.get(branch));
        Commit curBranch = getCommit(_branches.get(_curBranch));
        TreeSet<String> toBeDeleted = new TreeSet<String>();
        TreeSet<String> toBeCheckedOut = new TreeSet<String>();
        TreeSet<String> conflictFile = new TreeSet<String>();
        if (splitPoint.equals(_branches.get(_curBranch))) {
            throw new GitletException("Current branch fast-forwarded.");
        }
        if (splitPoint.equals(_branches.get(branch))) {
            head = _branches.get(branch);
            _branches.put(_curBranch, head);
            headCache = getCommit(head);
            throw new GitletException(
                    "Given branch is an ancestor of the current branch.");
        }
        firstMergeIterate(givenBranch, curBranch, splitCommit,
                toBeCheckedOut, conflictFile);
        secondMergeIterate(givenBranch, curBranch, splitCommit,
                toBeDeleted, conflictFile);
        for (String file : toBeCheckedOut) {
            revertFile(givenBranch.getHash(), file); addFile(file);
        }
        for (String file : toBeDeleted) {
            removeFile(file);
        }
        if (!conflictFile.isEmpty()) {
            System.out.println("Encountered a merge conflict.");
        }
        for (String file : conflictFile) {
            String first, second;
            String givenFileHash = givenBranch.tracked(file);
            String currentFileHash = curBranch.tracked(file);
            if (!currentFileHash.equals("")) {
                first = readContentsAsString(
                        join(folder, currentFileHash));
            } else {
                first = "";
            }
            if (!givenFileHash.equals("")) {
                second = readContentsAsString(
                        join(folder, givenFileHash));
            } else {
                second = "";
            }
            String third =
                    "<<<<<<< HEAD\n" + first
                            + "=======\n" + second + ">>>>>>>\n";
            writeContents(join(myDirectory, file), third);
            addFile(file);
        }
        mergeCommit(String.format("Merged %s into %s.",
                branch, _curBranch), branch);
    }

    /** Return split point of BRANCH with current branch. */
    private String getSplitPoint(String branch) {
        TreeSet<String> pathGivenBranch = new TreeSet<String>();
        String first = _branches.get(branch);
        while (first.length() != 0) {
            Commit c = getCommit(first);
            pathGivenBranch.add(first);
            first = c.getParent();
        }
        first = _branches.get(_curBranch);
        while (first.length() != 0) {
            Commit c = getCommit(first);
            if (pathGivenBranch.contains(first)) {
                return first;
            }
            first = c.getParent();
        }
        return "NOT FOUND";
    }

    /** Find commmit with specific message MESSAGE. */
    public void doFind(String message) {
        Boolean find = false;
        for (HashMap.Entry<String, String> e : _commitToMessage.entrySet()) {
            String c = e.getKey();
            String m = e.getValue();
            if (m.equals(message)) {
                System.out.println(c);
                find = true;
            }
        }
        if (!find) {
            throw new GitletException("Found no commit with that message.");
        }
    }

    /** Convert short UID ID back to full length, return the result. */
    public String convertID(String id) {
        for (String c : commitList) {
            if (c.startsWith(id)) {
                return c;
            }
        }
        return "";
    }

    /** Return hash for a branch BRANCH. */
    private String branch2hash(String branch) {
        return _branches.get(branch);
    }


    /**
     * ******************
     * Start remote part.
     * ******************
     */
    /** Fetch the remote branch BRANCH from remote name NAME. */
    public void fetchBranch(String name, String branch) {
        File fileName = _remoteDir.get(name);
        if (!fileName.exists()) {
            throw new GitletException("Remote directory not found.");
        }
        File remoteObjectDir = fileName;
        Repo remoteRepo =
                readObject(join(remoteObjectDir, "GITLET"), Repo.class);
        String remoteBranchHead = remoteRepo.branch2hash(branch);
        if (remoteBranchHead == null) {
            throw new GitletException("That remote does not have that branch.");
        }
        String first = remoteRepo.branch2hash(branch);
        while (first.length() != 0) {
            byte[] blob =
                    readContents(join(remoteObjectDir, first));
            File outputDir = join(folder, first);
            writeContents(outputDir, blob);
            commitList.add(first);
            remoteRepo.moveAllBlobs(first, folder);
            first = remoteRepo.getCommit(first).getParent();
        }
        String newBranchName = String.format("%s/%s", name, branch);
        _branches.put(newBranchName, remoteBranchHead);
    }

    /** Pull the remote branch BRANCH from remote name NAME. */
    public void pull(String name, String branch) {
        fetchBranch(name, branch);
        String newBranchName = String.format("%s/%s", name, branch);
        merge(newBranchName);
    }

    /** Push the current branch into remote BRANCH in NAME. */
    public void push(String name, String branch) {
        File fileName = _remoteDir.get(name);
        if (!fileName.exists()) {
            throw new GitletException("Remote directory not found.");
        }
        File remoteObjectDir = join(fileName);
        Repo remoteRepo
                = readObject(join(remoteObjectDir, "GITLET"), Repo.class);
        String remoteBranchHead = remoteRepo.branch2hash(branch);
        TreeSet<String> diffCommits = new TreeSet<String>();
        Boolean b = false;
        for (String first = _branches.get(_curBranch); !first.equals("");
             first = getCommit(first).getParent()) {
            diffCommits.add(first);
            if (first.equals(remoteBranchHead)) {
                b = true;
                break;
            }
        }
        if (!b) {
            throw new GitletException(
                            " Please pull down remote changes before pushing.");
        }
        for (String commit : diffCommits) {
            byte[] blob =
                    readContents(join(folder, commit));
            File outputDir = join(remoteObjectDir, commit);
            writeContents(outputDir, blob);
            moveAllBlobs(commit, remoteObjectDir);
            remoteRepo.commitList.add(commit);
        }
        remoteRepo.dangerousReset(_branches.get(_curBranch));
        writeObject(join(remoteObjectDir, "GITLET"), remoteRepo);
    }

    /** Move all blobs involved in current repo's COMMIT
     *  to another folder OUTPUTDIR. */
    private void moveAllBlobs(String commit, File outputDir) {
        File absObjectFolder = join(absPath, ".gitlet");
        for (String file : getCommit(commit).getKeys()) {
            String hash = getCommit(commit).tracked(file);
            byte[] blob
                    = readContents(join(absObjectFolder, hash));
            writeContents(join(outputDir, hash), blob);
        }
    }

    /** Add a remote with path FILE, name NAME. */
    public void addRemote(String name, File file) {
        if (_remoteDir.containsKey(name)) {
            throw new GitletException(
                    "A remote with that name already exists.");
        }
        _remoteDir.put(name, file);
    }

    /** Remove remote NAME. */
    public void removeRemote(String name) {
        if (!_remoteDir.containsKey(name)) {
            throw new GitletException(
                    "A remote with that name does not exist.");
        } else {
            _remoteDir.remove(name);
        }
    }

    /** Store the current working folder. */
    private File myDirectory;
    /** Store the folder for objects. */
    private File folder;
    /** Store all the commitList in this folder, store the hash. */
    private List<String> commitList;
    /** Current Staging Area. */
    private Staging _stagingArea;
    /** Newest commit. */
    private String head;
    /** Cache for the head commit. */
    private Commit headCache;
    /** Branches. name to hash. */
    private TreeMap<String, String> _branches;
    /** Current branch name. */
    private String _curBranch;
    /** Store commit-message pair. */
    private TreeMap<String, String> _commitToMessage;
    /** Store remote dir. */
    private TreeMap<String, File> _remoteDir;
    /** Store absoloute path. */
    private File absPath = new File(System.getProperty("user.dir"));
}
