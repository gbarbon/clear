package clear_analyser.utils;

import java.util.Scanner;

/**
 * Input reader.
 */
public class InputReader {
    private Scanner scanner;

    public InputReader() {
        scanner = new Scanner(System.in);
    }

    public String next(){
        return scanner.next();
    }

    public int nextInt() {
        return scanner.nextInt();
    }

}
