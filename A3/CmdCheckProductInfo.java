import java.util.Arrays;

public class CmdCheckProductInfo implements Command {
    @Override
    public String execute(VendingMachine v, String[] cmdParts) {
        if (cmdParts.length < 2) {
            return "Invalid command.";
        }

        String productName = String.join(" ", Arrays.copyOfRange(cmdParts, 1, cmdParts.length)).trim();
        if (productName.isEmpty()) {
            return "Invalid product name.";
        }

        Product product = v.getProduct(productName);
        if (product == null) {
            return productName + " does not exist.";
        }

        return product.getProductInfo();
    }
}