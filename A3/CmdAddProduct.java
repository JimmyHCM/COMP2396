public class CmdAddProduct implements Command {
    public String execute(VendingMachine v, String[] cmdParts) {
        if (cmdParts.length < 3) {
            return "Invalid command.";
        }

        String productName = cmdParts[1];
        int quantity = Integer.parseInt(cmdParts[2]);
        v.addProduct(productName, quantity);
        return "Added " + productName + " for " + quantity + " can(s).";
    }
}
