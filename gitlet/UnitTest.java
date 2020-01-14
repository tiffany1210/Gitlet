package gitlet;

import ucb.junit.textui;
import org.junit.Test;
import java.io.File;
import static org.junit.Assert.*;

/** The suite of all JUnit tests for the gitlet package.
 *  @author Tiffany Kim
 */
public class UnitTest {

    /** Run the JUnit tests in the loa package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        System.exit(textui.runClasses(UnitTest.class));
    }

    /** Makes sure untracked files aren't being committed unless
     added, modified, added again, and committed. */
    @Test
    public void breakCommitTest() {
        File staging = new File(".gitlet");
        assertEquals(staging.listFiles().length, 2);
    }
}


