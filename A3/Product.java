public class Product {
    private String name;
    private int price;
    private int quantity;

    public Product(String name, int price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getProductInfo() {
        return name + ": Price = " + price + ", Quantity = " + quantity + ".";
    }

    public void decrementQuantity() {
        if (quantity > 0) {
            quantity--;
        }
    }

    public void increaseQuantity(int amount) {
        if (amount > 0) {
            quantity += amount;
        }
    }
}