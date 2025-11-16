public class CmdInsertCoin implements Command {
    @Override
    public String execute(VendingMachine v, String[] cmdParts) {
        if (cmdParts.length < 2) {
            return "Invalid command.";
        }

        int coinValue;
        try {
            coinValue = Integer.parseInt(cmdParts[1]);
        } catch (NumberFormatException ex) {
            return "Invalid coin value.";
        }

        if (coinValue <= 0) {
            return "Invalid coin value.";
        }

        v.insertCoin(cmdParts[1], coinValue);

        int total = v.getTotalInsertedCoins();
        return "Inserted a $" + coinValue + " coin. $" + total + " in total.";
    }
}