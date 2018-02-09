import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Order {
    private int id;
    private int customerId;
    private Date orderDate;
    private boolean expedited;
    private List<Shoe> shoes;

    public Order() {
        shoes = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public boolean isExpedited() {
        return expedited;
    }

    public void setExpedited(boolean expedited) {
        this.expedited = expedited;
    }

    public List<Shoe> getShoes() {
        return shoes;
    }

    public void addShoe(Shoe shoe) {
        shoes.add(shoe);
    }
}
