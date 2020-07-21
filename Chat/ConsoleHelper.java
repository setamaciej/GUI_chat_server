package Chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleHelper {
    private static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    public static void writeMessage(String message){
        System.out.println(message);
    }

    public static int readInt(){
        try {
            return Integer.parseInt(readString());
        } catch (NumberFormatException e) {
            writeMessage("An error occurred while trying to enter a number. Try again.");
            return readInt();
        }
    }

    public static String readString(){
        try {
            return br.readLine();
        } catch (IOException e) {
            writeMessage("An error occurred while trying to enter text. Try again.");
            return readString();
        }
    }
}
