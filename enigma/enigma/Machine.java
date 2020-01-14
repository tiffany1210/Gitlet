package enigma;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Collection;

import static enigma.EnigmaException.*;

/**
 * Class that represents a complete enigma machine.
 *
 * @author Tiffany Kim
 */
class Machine {

    /**
     * A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     * and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     * available rotors.
     */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        _allRotors = allRotors;
    }

    /**
     * Return the number of rotor slots I have.
     */
    int numRotors() {
        return _numRotors;
    }

    /**
     * Return the number pawls (and thus rotating rotors) I have.
     */
    int numPawls() {
        return _pawls;
    }

    /**
     * Set my rotor slots to the rotors named ROTORS from my set of
     * available rotors (ROTORS[0] names the reflector).
     * Initially, all rotors are set at their 0 setting.
     */
    void insertRotors(String[] rotors) {
        rotorlist = new Rotor[numRotors()];
        HashMap<String, Rotor> rotorMap = new HashMap<>();
        for (Rotor r : _allRotors) {
            rotorMap.put(r.name(), r);
        }
        for (int i = 0; i < rotors.length; i++) {
            if (rotorMap.containsKey(rotors[i])) {
                rotorlist[i] = rotorMap.get(rotors[i]);
            } else {
                throw error("The rotor name does not exist");
            }
        }
    }

    /**
     * Set my rotors according to SETTING, which must be a string of
     * numRotors()-1 characters in my alphabet. The first letter refers
     * to the leftmost rotor setting (not counting the reflector).
     */
    void setRotors(String setting) {
        for (int i = 1; i < rotorlist.length; i++) {
            rotorlist[i].set(setting.charAt(i - 1));
        }
    }

    /**
     * Set the plugboard to PLUGBOARD.
     */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /**
     * Returns the result of converting the input character C (as an
     * index in the range 0..alphabet size - 1), after first advancing
     * the machine.
     */
    int convert(int c) {
        HashSet<Rotor> advanceRotor = new HashSet<>();
        advanceRotor.add(rotorlist[rotorlist.length - 1]);
        for (int i = rotorlist.length - 1; i > 0; i--) {
            if (rotorlist[i].atNotch()) {
                if (rotorlist[i].rotates() && rotorlist[i - 1].rotates()) {
                    advanceRotor.add(rotorlist[i]);
                    advanceRotor.add(rotorlist[i - 1]);
                }
            }
        }
        for (Rotor r : advanceRotor) {
            r.advance();
        }
        int exitC = c;
        if (_plugboard != null) {
            exitC = _plugboard.permute(exitC);
        }
        for (int j = rotorlist.length - 1; j >= 0; j--) {
            exitC = rotorlist[j].convertForward(exitC);
        }
        for (int k = 1; k < rotorlist.length; k++) {
            exitC = rotorlist[k].convertBackward(exitC);
        }
        if (_plugboard != null) {
            exitC = _plugboard.permute(exitC);
        }
        return exitC;
    }

    /**
     * Returns the encoding/decoding of MSG, updating the state of
     * the rotors accordingly.
     */
    String convert(String msg) {
        msg = msg.replace(" ", "");
        int[] convertint = new int[msg.length()];
        for (int i = 0; i < msg.length(); i++) {
            int j = _alphabet.toInt(msg.charAt(i));
            convertint[i] = this.convert(j);
        }
        char[] convertchar = new char[convertint.length];
        String[] result = new String[convertint.length];
        for (int k = 0; k < msg.length(); k++) {
            convertchar[k] = _alphabet.toChar(convertint[k]);
            result[k] = Character.toString(convertchar[k]);
        }
        String resultstring = "";
        for (int z = 0; z < result.length; z++) {
            resultstring += result[z];
        }
        return resultstring;
    }
    /**
     * Returns a rotorlist.
     */
    Rotor[] rotorlist() {
        return rotorlist;
    }

    /**
     * Common alphabet of my rotors.
     */
    private final Alphabet _alphabet;
    /**
     * NumRotors.
     */
    private int _numRotors;
    /**
     * Pawls.
     */
    private int _pawls;
    /**
     * allrotors.
     */
    private Collection<Rotor> _allRotors;
    /**
     * rotorlist.
     */
    private Rotor[] rotorlist;
    /**
     * plugboard.
     */
    private Permutation _plugboard;
}
