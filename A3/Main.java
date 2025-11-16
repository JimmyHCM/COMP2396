import java.io.*;
import commands.Command;

public class Main {

	public static void main(String[] args) throws IOException {
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		String inputLine = "";

		VendingMachine v = new VendingMachine();
		
		/*implement some code in initializeProducts()in VendingMachine class so that all the products
		are initialized with quantity 0 with their corresponding price when v.initializeProducts() is
		called in Main. Please refer to Notes 6 in page 5 for the products and their corresponding
		price.*/
		
		v.initializeProducts();

		System.out.println("Welcome to COMP2396 Assignment 3 - Vending Machine");

		// Reads user inputs continuously
		while (true) {
			inputLine = input.readLine();

			// Split the input line
			String[] cmdParts = inputLine.split(" ");

			Command cmdObj = null;

			if (cmdParts[0].equalsIgnoreCase("Exit")) {
				break;
			} else if (cmdParts[0].equalsIgnoreCase("Check")) {
				cmdObj = new CmdCheckProductInfo();
			} else if (cmdParts[0].equalsIgnoreCase("Insert")) {
				cmdObj = new CmdInsertCoin();
			} else if (cmdParts[0].equalsIgnoreCase("Reject")) {
				cmdObj = new CmdRejectCoins();
			} else if (cmdParts[0].equalsIgnoreCase("Buy")) {
				cmdObj = new CmdPurchase();
			} else if (cmdParts[0].equalsIgnoreCase("Add")) {
				cmdObj = new CmdAddProduct();		
			} else {
			
				System.out.println("Unknown user command.");
			}

			if (cmdObj != null) {
				System.out.println(cmdObj.execute(v, cmdParts));
			}

			inputLine = "";
		}

		System.out.println("Bye");
	}
}
