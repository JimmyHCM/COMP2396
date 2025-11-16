import java.io.*;
import java.util.*;

public class Question1 {
    static int balance = 0;
    static String route1;
    static String comCode1;
    static int fall1 = 0;
    static String route2;
    static String comCode2;
    static int fall2 = 0;

    public static void inputLine(BufferedReader reader) throws IOException {
        balance = Integer.parseInt(reader.readLine());

        String input2 = reader.readLine();
        String[] temp2 = input2.trim().split("\\s+");
        if (temp2.length >= 3) {
            route1 = temp2[0];
            comCode1 = temp2[1];
            fall1 = Integer.parseInt(temp2[2]);
        }

        String input3 = reader.readLine();
        String[] temp3 = input3.trim().split("\\s+");
        if (temp3.length >= 3) {
            route2 = temp3[0];
            comCode2 = temp3[1];
            fall2 = Integer.parseInt(temp3[2]);
        }
    }

    public static int remainingBalance(BufferedReader reader) throws IOException {
        inputLine(reader);
        if (balance < 0) {
            return balance;
        }
        // Exclusions
        if (!"BNB".equals(comCode1)) {
            return balance - fall2;
        }
        if (route2 != null && !route2.isEmpty() && route2.charAt(0) == 'A') {
            return balance - fall2;
        }

        // Discounts
        if (route1 != null && !route1.isEmpty() && route1.charAt(0) == 'P') {
            return balance;
        }
        if ("BNB".equals(comCode1) && "BNB".equals(comCode2)) {
            int toPay = Math.max(0, fall2 - fall1);
            return balance - toPay;
        }

        // Default
        return balance;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        int result = remainingBalance(reader);
        System.out.println("The remaining balance is " + result + ".");
    }
}