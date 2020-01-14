package enigma;

/**
 * Represents a permutation of a range of integers starting at 0 corresponding
 * to the characters of an alphabet.
 *
 * @author TiffanyKim
 */
class Permutation {

    /**
     * Set this Permutation to that specified by CYCLES, a string in the
     * form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     * is interpreted as a permutation in cycle notation.  Characters in the
     * alphabet that are not included in any cycle map to themselves.
     * Whitespace is ignored.
     */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;

        _cycle = cycles.replaceAll(" ", "");
        _cycle = _cycle.replaceAll("\\(", "");
        _cycles = _cycle.split("\\)");
    }
    /**
     * Check cycle.
     * @return checkcycle.
     * @param cycles is cycles.
     */
    private boolean checkcycle(String cycles) {
        boolean valid = true;
        while (!cycles.equals("")) {
            if (!(cycles.startsWith("(") && cycles.endsWith(")"))) {
                valid = false;
            }
            cycles = cycles.replace("(", "");
            cycles = cycles.replace(")", "");
            char[] characterarray = new char[cycles.length()];
            for (int i = 0; i < cycles.length(); i++) {
                characterarray[i] = cycles.charAt(i);
                if (!alphabet().contains(cycles.charAt(i))) {
                    valid = false;
                }
            }
        }
        return valid;
    }

    /**
     * Return the value of P modulo the size of this permutation.
     */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /**
     * Returns the size of the alphabet I permute.
     */
    int size() {
        return _alphabet.size();
    }

    /**
     * Return the result of applying this permutation to P modulo the
     * alphabet size.
     */
    int permute(int p) {
        char enterC = _alphabet.toChar(wrap(p));
        char exitC = permute(enterC);
        int exitN = _alphabet.toInt(exitC);
        return exitN;
    }

    /**
     * Return the result of applying the inverse of this permutation
     * to  C modulo the alphabet size.
     */
    int invert(int c) {
        char enterC = _alphabet.toChar(wrap(c));
        char exitC = invert(enterC);
        int exitN = _alphabet.toInt(exitC);
        return exitN;
    }

    /**
     * Return the result of applying this permutation to the index of P
     * in ALPHABET, and converting the result to a character of ALPHABET.
     */
    char permute(char p) {
        char result = p;
        for (int i = 0; i < _cycles.length; i++) {
            String cycle = _cycles[i];
            for (int j = 0; j < cycle.length(); j++) {
                if (p == cycle.charAt(j)) {
                    if (j == cycle.length() - 1) {
                        result = cycle.charAt(0);
                    } else {
                        result = cycle.charAt(j + 1);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Return the result of applying the inverse of this permutation to C.
     */
    char invert(char c) {
        char result = c;
        for (int i = 0; i < _cycles.length; i++) {
            String cycle = _cycles[i];
            for (int j = 0; j < cycle.length(); j++) {
                if (c == cycle.charAt(j)) {
                    if (j == 0) {
                        result = cycle.charAt(cycle.length() - 1);
                    } else {
                        result = cycle.charAt(j - 1);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Return the alphabet used to initialize this Permutation.
     */
    Alphabet alphabet() {
        return _alphabet;
    }

    /**
     * Return true iff this permutation is a derangement (i.e., a
     * permutation for which no value maps to itself).
     */
    boolean derangement() {
        int val = 0;
        for (int i = 0; i < _cycles.length; i++) {
            if (_cycles[i].length() == 1) {
                return false;
            }
            val += _cycles[i].length();
        }
        return val == alphabet().size();
    }

    /**
     * Alphabet of this permutation.
     */
    private Alphabet _alphabet;
    /**
     * Cycles.
     */
    private String[] _cycles;
    /**
     * Cycle.
     */
    private String _cycle;
}
