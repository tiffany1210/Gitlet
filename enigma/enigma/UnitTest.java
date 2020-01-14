package enigma;

import org.junit.Test;
import ucb.junit.textui;

import java.util.ArrayList;

/** The suite of all JUnit tests for the enigma package.
 *  @author Tiffany Kim
 */
public class UnitTest {

    @Test
    public void machinetest1() {
        Alphabet abc = new CharacterRange('A', 'Z');
        Permutation p1 = new Permutation("(AELTPHQXRU) "
               + "(BKNW) (CMOY) (DFG) (IV) (JZ) (S)", abc);
        Permutation p2 = new Permutation("(ABDHPEJT) "
                + "(CFLVMZOYQIRWUKXSG) (N)", abc);
        Permutation p3 = new Permutation("(AEPLIYWCOXMRFZBSTGJQNH) "
               + "(DV) (KU)", abc);
        Permutation p4 = new Permutation("(ALBEVFCYODJWUGNMQTZSKPR) "
                + "(HIX)", abc);
        Permutation p5 = new Permutation("(AE) (BN) (CK) (DQ) "
                + "(FU) (GY) (HW) (IJ) (LO) (MP) (RX) (SZ) (TV)", abc);
        Rotor I = new MovingRotor("I", p1, "Q");
        Rotor iii = new MovingRotor("III", p2, "V");
        Rotor iv = new MovingRotor("IV", p3, "J");
        Rotor beta = new FixedRotor("Beta", p4);
        Rotor B = new Reflector("B", p5);
        ArrayList<Rotor>  R = new ArrayList<>();
        R.add(B);
        R.add(beta);
        R.add(iii);
        R.add(iv);
        R.add(I);
        Machine M = new Machine(abc, 5, 3, R);
        M.setPlugboard(new Permutation("(HQ) (EX) (IP) (TR) (BY)", abc));
        String[] rotors = {"B", "Beta", "III", "IV", "I"};
        M.insertRotors(rotors);
        M.setRotors("AXLE");
        System.out.println(M.numRotors() == 5);
        System.out.println(M.numPawls() == 3);
        System.out.println(M.convert(5) == 16);
        System.out.println(M.convert(5) == 16);
        System.out.println(M.convert(abc.toInt('C')));
        System.out.println(M.convert("QVPQS OKOIL PUBKJ ZPISF XDW"));
    }

    /** Run the JUnit tests in this package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        textui.runClasses(PermutationTest.class, MovingRotorTest.class);
    }

}

