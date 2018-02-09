import java.math.BigDecimal;
import java.util.EnumSet;

public class Shoe {
    private int id;
    private String name;
    private int size;
    private BigDecimal price;
    private int stock;
    private Colour colour;
    private Brand brand;
    private EnumSet<Category> categories;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public Colour getColour() {
        return colour;
    }

    public void setColour(Colour colour) {
        this.colour = colour;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public EnumSet<Category> getCategories() {
        return categories;
    }

    public void addCategory(String category) {
        categories.add(Category.valueOf(category));
    }

    public Shoe() {
        categories = EnumSet.noneOf(Category.class);
    }

    @Override
    public String toString() {
        String result = "";
        result += "Namn: " + name;
        result += ", Storlek: " + size;
        result += ", Pris: " + price;
        result += ", Färg: " + colour;
        result += ", Märke: " + brand;
        return result;
    }
}
