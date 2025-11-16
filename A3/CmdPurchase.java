public class CmdPurchase implements Command {
    @Override
    public String execute(VendingMachine v, String[] cmdParts) {
        if (cmdParts.length < 2) {
            return "Invalid command.";
        }

        String productName = String.join(" ", java.util.Arrays.copyOfRange(cmdParts, 1, cmdParts.length)).trim();
        if (productName.isEmpty()) {
            return "Invalid product name.";
        }

        Product product = v.getProduct(productName);
        if (product == null) {
            return productName + " does not exist.";
        }

        int totalInserted = v.getTotalInsertedCoins();
        int price = product.getPrice();

        if (product.getQuantity() <= 0) {
            return productName + " is out of stock!";
        }

        if (totalInserted < price) {
            return "Not enough credit to buy " + productName + "! Inserted $" + totalInserted + " but needs $" + price + ".";
        }

        int changeAmount = totalInserted - price;
        v.purchase(productName);

        if (changeAmount == 0) {
            return "Dropped " + productName + ". Paid $" + totalInserted + ". No change.";
        }

        return "Dropped " + productName + ". Paid $" + totalInserted + ". " + formatChange(changeAmount);
    }

    private String formatChange(int changeAmount) {
        int[] denominations = {10, 5, 2, 1};
        java.util.ArrayList<String> parts = new java.util.ArrayList<>();
        int remaining = changeAmount;

        for (int denom : denominations) {
            while (remaining >= denom) {
                parts.add("$" + denom);
                remaining -= denom;
            }
        }

        return parts.isEmpty() ? "No change." : "Your change: " + String.join(", ", parts) + ".";
    }
}
