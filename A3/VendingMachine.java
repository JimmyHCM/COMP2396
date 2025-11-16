import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class VendingMachine {
    private static final int[] ACCEPTED_DENOMINATIONS = {10, 5, 2, 1};

    private final LinkedHashMap<Integer, Integer> insertedCoins;
    private final HashMap<String, Product> products;

    public VendingMachine() {
        insertedCoins = new LinkedHashMap<>();
        for (int denom : ACCEPTED_DENOMINATIONS) {
            insertedCoins.put(denom, 0);
        }

        products = new HashMap<>();
    }

    public void addProduct(String name, int quantity) {
        if (quantity <= 0) {
            return;
        }

        Product product = products.get(name);
        if (product == null) {
            product = new Product(name, 0, 0);
            products.put(name, product);
        }

        product.increaseQuantity(quantity);
    }

    public void insertCoin(String coin, Integer value) {
        int denomination = value != null ? value : parseDenomination(coin);
        if (!insertedCoins.containsKey(denomination)) {
            return;
        }

        insertedCoins.put(denomination, insertedCoins.get(denomination) + 1);
    }

    public Map<Integer, Integer> getInsertedCoins() {
        LinkedHashMap<Integer, Integer> snapshot = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> entry : insertedCoins.entrySet()) {
            if (entry.getValue() > 0) {
                snapshot.put(entry.getKey(), entry.getValue());
            }
        }
        return snapshot;
    }

    public int getTotalInsertedCoins() {
        int total = 0;
        for (Map.Entry<Integer, Integer> entry : insertedCoins.entrySet()) {
            total += entry.getKey() * entry.getValue();
        }
        return total;
    }

    public void initializeProducts() {
        products.put("Cocacola", new Product("Cocacola", 4, 0));
        products.put("Pepsi", new Product("Pepsi", 5, 0));
        products.put("Sprite", new Product("Sprite", 6, 0));
        products.put("Mirinda", new Product("Mirinda", 7, 0));
        products.put("Gatorade", new Product("Gatorade", 8, 0));
        products.put("Bonaqua", new Product("Bonaqua", 11, 0));
        products.put("RedBull", new Product("RedBull", 12, 0));
        products.put("Tropicana", new Product("Tropicana", 15, 0));
        products.put("MinuteMaid", new Product("MinuteMaid", 10, 0));
    }

    public void rejectCoins() {
        resetInsertedCoins();
    }

    public void purchase(String name) {
        Product product = products.get(name);
        if (product == null) {
            return;
        }

        product.decrementQuantity();
        resetInsertedCoins();
    }

    public String getProductInfo(String name) {
        Product product = products.get(name);
        if (product == null) {
            return name + " does not exist.";
        }
        return product.getProductInfo();
    }

    public Product getProduct(String name) {
        return products.get(name);
    }

    public ArrayList<Integer> calculateChange(int amount) {
        ArrayList<Integer> change = new ArrayList<>();
        int remaining = amount;
        for (int denom : ACCEPTED_DENOMINATIONS) {
            while (remaining >= denom) {
                change.add(denom);
                remaining -= denom;
            }
        }
        Collections.sort(change, Collections.reverseOrder());
        return change;
    }

    private void resetInsertedCoins() {
        for (int denom : insertedCoins.keySet()) {
            insertedCoins.put(denom, 0);
        }
    }

    private int parseDenomination(String coinLabel) {
        try {
            return Integer.parseInt(coinLabel);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }
}