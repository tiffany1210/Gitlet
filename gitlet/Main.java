package gitlet;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author Tiffany Kim
 */
public class Main extends Utils {

    /** Store repo. */
    private static Repo repo;
    /** Store object dir. */
    private static File objectDir;
    /** Store working dir. */
    private static File workingDir;

    /** Main function input ARGS. */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String command = args[0];
        workingDir = new File(".");
        objectDir = join(workingDir, ".gitlet");
        if (command.equals("init")) {
            commandInit(args);
        }
        if (!join(objectDir, "GITLET").exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        repo = readObject(join(objectDir, "GITLET"), Repo.class);
        runMain(command, args);
    }

    /** Runs Main.
     *  It takes in one or more Strings ARGS and args[1] COMMAND*/
    private static void runMain(String command, String... args) {
        try {
            if (command.equals("add-remote")) {
                commandAddRemote(args); System.exit(0);
            } else if (command.equals("push")) {
                commandPush(args); System.exit(0);
            } else if (command.equals("pull")) {
                commandPull(args); System.exit(0);
            } else if (command.equals("fetch")) {
                commandFetch(args); System.exit(0);
            } else if (command.equals("rm-remote")) {
                commandReRemote(args); System.exit(0);
            }
        } catch (GitletException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
        if (command.equals("add")) {
            commandAdd(args);
        } else if (command.equals("reset")) {
            commandReset(args);
        } else if (command.equals("commit")) {
            commandCommit(args);
        } else if (command.equals("rm")) {
            commandRm(args);
        } else if (command.equals("log")) {
            commandLog(args);
        } else if (command.equals("status")) {
            commandStatus(args);
        } else if (command.equals("branch")) {
            commandBranch(args);
        } else if (command.equals("rm-branch")) {
            commandRmBranch(args);
        } else if (command.equals("global-log")) {
            commandGlobalLog(args);
        } else if (command.equals("find")) {
            commandFind(args);
        } else if (command.equals("merge")) {
            commandMerge(args);
        } else if (command.equals("checkout")) {
            commandCheckout(args);
        } else {
            System.out.println("No command with that name exists.");
            System.exit(0);
        }
    }

    /** Performs init command.
     *  It takes in one or more Strings ARGS*/
    private static void commandInit(String... args) {
        if (join(objectDir, "GITLET").exists()) {
            System.out.println("A Gitlet version-control system"
                    + " already exists in the current directory.");
            System.exit(0);
        } else {
            objectDir.mkdir();
            repo = new Repo();
            writeObject(join(objectDir, "GITLET"), repo);
            System.exit(0);
        }
    }

    /** Performs add command.
     *  It takes in one or more Strings ARGS*/
    /** INPUT ARGS. */
    private static void commandAdd(String... args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        if (!join(workingDir, args[1]).exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        } else {
            try {
                repo.addFile(args[1]);
                writeObject(join(objectDir, "GITLET"), repo);
            } catch (GitletException e) {
                System.out.println(e.getMessage());
            }
            System.exit(0);
        }
    }

    /** Performs commit command.
     *  It takes in one or more Strings ARGS*/
    private static void commandCommit(String... args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        try {
            if (args[1].length() == 0) {
                System.out.println("Please enter a commit message.");
                System.exit(0);
            }
            repo.newCommit(args[1]);
            writeObject(join(objectDir, "GITLET"), repo);
        } catch (GitletException e) {
            System.out.println(e.getMessage());
        }
        System.exit(0);
    }

    /** Performs rm command.
     *  It takes in one or more Strings ARGS*/
    private static void commandRm(String... args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        try {
            repo.removeFile(args[1]);
            writeObject(join(objectDir, "GITLET"), repo);
        } catch (GitletException e) {
            System.out.println(e.getMessage());
        }
        System.exit(0);
    }

    /** Performs log command.
     *  It takes in one or more Strings ARGS*/
    private static void commandLog(String... args) {
        if (args.length != 1) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        } else {
            repo.printLog();
            System.exit(0);
        }
    }

    /** Performs global-log command.
     *  It takes in one or more Strings ARGS*/
    private static void commandGlobalLog(String... args) {
        if (args.length != 1) {
            System.out.println("Incorrect operands:");
            System.exit(0);
        } else {
            repo.printGlobalLog();
            System.exit(0);
        }
    }

    /** Performs find command.
     *  It takes in one or more Strings ARGS*/
    private static void commandFind(String... args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands:");
            System.exit(0);
        }
        try {
            repo.doFind(args[1]);
        } catch (GitletException e) {
            System.out.println(e.getMessage());
        }
        System.exit(0);
    }

    /** Performs status command.
     *  It takes in one or more Strings ARGS*/
    private static void commandStatus(String... args) {
        if (args.length != 1) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        } else {
            repo.printStatus();
            System.exit(0);
        }
    }

    /** Performs checkout command.
     *  It takes in one or more Strings ARGS*/
    private static void commandCheckout(String... args) {
        if (args[1].equals("--")) {
            if (args.length != 3) {
                System.out.println("Incorrect operands.");
                System.exit(0);
            } else {
                try {
                    repo.revertFile(args[2]);
                    writeObject(join(objectDir, "GITLET"), repo);
                } catch (GitletException e) {
                    System.out.println(e.getMessage());
                }
                System.exit(0);
            }
        } else {
            Pattern p = Pattern.compile("[a-f0-9]+");
            File file = join(objectDir, "GITLET");
            if (args.length == 2) {
                try {
                    repo.checkout2branch(args[1]);
                    writeObject(file, repo);
                } catch (GitletException e) {
                    System.out.println(e.getMessage());
                }
                System.exit(0);
            } else {
                if (args.length != 4) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (!args[2].equals("--")) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (!Pattern.matches("[a-f0-9]+", args[1])) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                try {
                    String id = repo.convertID(args[1]);
                    repo.revertFile(id, args[3]);
                    writeObject(file, repo);
                } catch (GitletException e) {
                    System.out.println(e.getMessage());
                }
                System.exit(0);
            }
        }
    }

    /** Performs branch command.
     *  It takes in one or more Strings ARGS*/
    private static void commandBranch(String... args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        try {
            repo.createBranch(args[1]);
            writeObject(join(objectDir, "GITLET"), repo);
        } catch (GitletException e) {
            System.out.println(e.getMessage());
        }
        System.exit(0);
    }

    /** Performs rm-branch command.
     *  It takes in one or more Strings ARGS*/
    private static void commandRmBranch(String... args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        try {
            repo.removeBranch(args[1]);
            writeObject(join(objectDir, "GITLET"), repo);
        } catch (GitletException e) {
            System.out.println(e.getMessage());
        }
        System.exit(0);
    }

    /** Performs reset command.
     *  It takes in one or more Strings ARGS*/
    private static void commandReset(String... args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        try {
            String id = repo.convertID(args[1]);
            repo.revertWorkingFolder(id);
            writeObject(join(objectDir, "GITLET"), repo);
        } catch (GitletException e) {
            System.out.println(e.getMessage());
        }
        System.exit(0);
    }

    /** Performs merge command.
     *  It takes in one or more Strings ARGS*/
    private static void commandMerge(String... args) {
        if (args.length != 2) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
        try {
            repo.merge(args[1]);
            writeObject(join(objectDir, "GITLET"), repo);
        } catch (GitletException e) {
            System.out.println(e.getMessage());
        }
        System.exit(0);
    }

    /** Performs add-remote command.
     *  It takes in one or more Strings ARGS*/
    private static void commandAddRemote(String... args) {
        repo.addRemote(args[1], new File(args[2]));
        writeObject(join(objectDir, "GITLET"), repo);
    }

    /** Performs fetch command.
     *  It takes in one or more Strings ARGS*/
    private static void commandFetch(String... args) {
        repo.fetchBranch(args[1], args[2]);
        writeObject(join(objectDir, "GITLET"), repo);
    }

    /** Performs pull command.
     *  It takes in one or more Strings ARGS*/
    private static void commandPull(String... args) {
        repo.pull(args[1], args[2]);
        writeObject(join(objectDir, "GITLET"), repo);
    }

    /** Performs push command.
     *  It takes in one or more Strings ARGS*/
    private static void commandPush(String... args) {
        repo.push(args[1], args[2]);
        writeObject(join(objectDir, "GITLET"), repo);
    }

    /** Performs rm-remote command.
     *  It takes in one or more Strings ARGS*/
    private static void commandReRemote(String... args) {
        repo.removeRemote(args[1]);
        writeObject(join(objectDir, "GITLET"), repo);
    }
}
