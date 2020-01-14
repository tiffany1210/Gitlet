package enigma;
import static enigma.EnigmaException.*;

/**
 * CharacterRange Class.
 * @author Tiffany Kim
 */
public class CharacterRange extends Alphabet {
    /**
     * CharacterRange.
     * @Param A.
     * @Param Z.
     */
    CharacterRange(char A, char Z) {
        _A = Character.toUpperCase(A);
        _Z = Character.toUpperCase(Z);
        if (_A > _Z) {
            throw error("Characters not in the range");
        }
    }

    @Override
    int size() {
        return _Z - _A + 1;
    }

    @Override
    boolean contains(char ch) {
        return ch >= _A && ch <= _Z;
    }

    @Override
    char toChar(int index) {
        if (!contains((char) (_A))) {
            throw error("Character index out of range");
        }
        return (char) (_A + index);
    }

    @Override
    int toInt(char ch) {
        if (!contains(ch)) {
            throw error("Character out of range");
        }
        return ch - _A;
    }
    /**
     * character.
     */
    private char _A, _Z;
}
