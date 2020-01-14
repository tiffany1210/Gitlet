package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author TiffanyKim
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        _notches = notches;
        _permutation = perm;
    }

    @Override
    boolean atNotch() {
        String[] splitnotch = _notches.split("");
        char charsetting = _permutation.alphabet().toChar(this.setting());
        String setting = String.valueOf(charsetting);
        for (int i = 0; i < splitnotch.length; i++) {
            if (splitnotch[i].contains(setting)) {
                return true;
            }
        }
        return false;
    }

    @Override
    void advance() {
        super.set(_permutation.wrap(super.setting() + 1));
    }

    @Override
    boolean rotates() {
        return true;
    }

    /**
     * Notches.
     */
    private String _notches;
    /**
     * Permutation.
     */
    private Permutation _permutation;

}
