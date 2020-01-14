package enigma;
import static enigma.EnigmaException.*;

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author TiffanyKim
 */
class Alphabet {

    /**
     * A new alphabet containing CHARS.  Character number #k has index
     * K (numbering from 0). No character may be duplicated.
     */
    Alphabet(String chars) {
        _chars = chars;
        chararray = new char[size()];
        for (int i = 0; i < size(); i++) {
            chararray[i] = _chars.charAt(i);
        }
        noduplicate(chararray);
    }
    /**
     * Checks if there are duplicates.
     * @param charArray is chararray.
     */
    private void noduplicate(char[] charArray) {
        int j = 0;
        for (int i = j + 1; i < charArray.length; i++) {
            if (charArray[j] == charArray[i]) {
                throw error("There should be no duplicate characters.");
            }
            j += 1;
        }
    }

    /**
     * A default alphabet of all upper-case characters.
     */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /**
     * Returns the size of the alphabet.
     */
    int size() {
        return _chars.length();
    }

    /**
     * Returns true if preprocess(CH) is in this alphabet.
     */
    boolean contains(char ch) {
        return _chars.contains(String.valueOf(ch));
    }

    /**
     * Returns character number INDEX in the alphabet, where
     * 0 <= INDEX < size().
     */
    char toChar(int index) {
        if (index < 0 || index >= size()) {
            throw error("index out of bounds");
        }
        return _chars.charAt(index);
    }

    /**
     * Returns the index of character preprocess(CH), which must be in
     * the alphabet. This is the inverse of toChar().
     */
    int toInt(char ch) {
        int integer = -1;
        if (!this.contains(ch)) {
            throw error("character is not in the alphabet");
        } else {
            for (int i = 0; i < size(); i++) {
                if (chararray[i] == ch) {
                    integer = i;
                }
            }
        }
        return integer;

    }
    /**
     * Returns a string of characters.
     */
    private String _chars;
    /**
     * Returns a chararray.
     */
    private char[] chararray;
}
