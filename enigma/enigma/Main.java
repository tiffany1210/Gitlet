package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static enigma.EnigmaException.*;

/**
 * Enigma simulator.
 *
 * @author Tiffany Kim
 */
public final class Main {

    /**
     * Process a sequence of encryptions and decryptions, as
     * specified by ARGS, where 1 <= ARGS.length <= 3.
     * ARGS[0] is the name of a configuration file.
     * ARGS[1] is optional; when present, it names an input file
     * containing messages.  Otherwise, input comes from the standard
     * input.  ARGS[2] is optional; when present, it names an output
     * file for processed messages.  Otherwise, output goes to the
     * standard output. Exits normally if there are no errors in the input;
     * otherwise with code 1.
     */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /**
     * Check ARGS and open the necessary files (see comment on main).
     */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }
        _config = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /**
     * Return a Scanner reading from the file named NAME.
     */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /**
     * Return a PrintStream writing to the file named NAME.
     */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /**
     * Configure an Enigma machine from the contents of configuration
     * file _config and apply it to the messages in _input, sending the
     * results to _output.
     */
    private void process() {
        Machine enigma = readConfig();
        String input = "";
        int first = 0;
        while (_input.hasNextLine()) {
            input = _input.nextLine();
            first += 1;
            if (input.startsWith("*")) {
                setUp(enigma, input);
            } else if (first == 1 && !input.startsWith("*")) {
                throw new EnigmaException("Wrong config");
            } else {
                input.replace(" ", "");
                printMessageLine(enigma.convert(input));
            }
        }
    }

    /**
     * Return an Enigma machine configured from the contents of configuration
     * file _config.
     */
    private Machine readConfig() {
        try {
            if (!_config.hasNext()) {
                throw new EnigmaException("Wrong format");
            }
            _alphabet = new Alphabet(_config.next());
            if (!_config.hasNextInt()) {
                throw new EnigmaException("Wrong format");
            }
            int numrotors = _config.nextInt();
            if (!_config.hasNextInt()) {
                throw new EnigmaException("Wrong format");
            }
            int numpawls = _config.nextInt();
            if (numpawls > numrotors) {
                throw new EnigmaException("Number of pawls "
                        + "cannot be greater than amount of rotors");
            }
            if (!_config.hasNextLine()) {
                throw new EnigmaException("Wrong format");
            }
            ArrayList<Rotor> rotors = new ArrayList<>();
            while (_config.hasNextLine()) {
                Rotor addrotor = readRotor();
                rotors.add(addrotor);
                if (temp.equals("DONE")) {
                    break;
                }
            }
            return new Machine(_alphabet, numrotors, numpawls, rotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /**
     * Return a rotor, reading its description from _config.
     */
    private Rotor readRotor() {
        try {
            String rotorname;
            if (temp.equals("")) {
                rotorname = _config.next();
            } else {
                rotorname = temp;
            }
            String typeNotch = _config.next();
            String cycle = "";
            while (_config.hasNext()) {
                temp = _config.next();
                if (temp.contains("(")) {
                    cycle += temp;
                } else {
                    break;
                }
            }
            if (!_config.hasNext()) {
                temp = "DONE";
            }

            Permutation perm = new Permutation(cycle, _alphabet);
            if (typeNotch.charAt(0) == 'M') {
                return new MovingRotor(rotorname, perm, typeNotch.substring(1));
            } else if (typeNotch.charAt(0) == 'R') {
                return new Reflector(rotorname, perm);
            } else {
                return new FixedRotor(rotorname, perm);
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /**
     * Set M according to the specification given on SETTINGS,
     * which must have the format specified in the assignment.
     */
    private void setUp(Machine M, String settings) {
        Scanner scanner = new Scanner(settings);
        String[] rotors = new String[M.numRotors()];
        if (scanner.next().equals("*")) {
            for (int i = 0; i < M.numRotors(); i++) {
                rotors[i] = scanner.next();
            }
        }
        for (int i = 0; i < rotors.length - 1; i++) {
            for (int j = i + 1; j < rotors.length; j++) {
                if (rotors[i].equals(rotors[j])) {
                    throw error("Repeated Rotor");
                }
            }
        }
        M.insertRotors(rotors);
        int mrnum = 0;
        for (int j = 0; j < M.rotorlist().length; j++) {
            if (M.rotorlist()[j] instanceof MovingRotor) {
                mrnum += 1;
            }
        }
        if (M.numPawls() != mrnum) {
            throw error("Wrong number of Moving Rotors");
        }
        String set = scanner.next();
        M.setRotors(set);
        if (M.numRotors() - 1 != set.length()) {
            throw error("Wrong length of setting");
        }
        String perm = "";
        while ((scanner.hasNext())) {
            perm += scanner.next();
        }
        Permutation plugboard = new Permutation(perm, _alphabet);
        M.setPlugboard(plugboard);
    }

    /**
     * Print MSG in groups of five (except that the last group may
     * have fewer letters).
     */
    private void printMessageLine(String msg) {
        if (msg.length() < 5) {
            _output.println(msg);
        } else {
            _output.print(msg.substring(0, 5) + " ");
            printMessageLine(msg.substring(5));
        }
    }

    /**
     * Alphabet used in this machine.
     */
    private Alphabet _alphabet;

    /**
     * Source of input messages.
     */
    private Scanner _input;

    /**
     * Source of machine configuration.
     */
    private Scanner _config;

    /**
     * File for encoded/decoded messages.
     */
    private PrintStream _output;
    /**
     * temporary string.
     */
    private String temp = "";


}
