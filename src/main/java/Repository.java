import javax.xml.transform.Result;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@SuppressWarnings("Duplicates")
public class Repository {
    private String username;
    private String password;
    private String mysqlAddress;

    public Repository() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Properties p = new Properties();
        try (FileInputStream file = new FileInputStream("settings.properties")) {
            p.load(file);
            username = p.getProperty("username");
            password = p.getProperty("password");
            mysqlAddress = p.getProperty("mysqlAddress");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Shoe> getAllProducts() {
        String sqlQuery = "select shoe.id as ShoeID, category.name as Kategori, shoe.name as Namn from category\n" +
                "inner join shoecategory on category.id = shoecategory.categoryid\n" +
                "inner join shoe on shoecategory.shoeId = shoe.id\n" +
                "order by category.name";
        List<Shoe> shoes = new ArrayList<>();

        try (Connection con = DriverManager.getConnection(mysqlAddress, username, password);
             PreparedStatement stmt = con.prepareStatement(sqlQuery);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Shoe s = new Shoe();
                s.setId(rs.getInt("ShoeID"));
                s.setName(rs.getString("Namn"));
                s.addCategory(rs.getString("Kategori").toUpperCase());
                shoes.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return shoes;
    }

    public List<Shoe> getAllProductsInStock() {
        String sqlQuery = "select * from shoe where stock > 0";
        List<Shoe> shoes = new ArrayList<>();

        try (Connection con = DriverManager.getConnection(mysqlAddress, username, password);
        PreparedStatement stmt = con.prepareStatement(sqlQuery);
        ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Shoe s = new Shoe();
                s.setId(rs.getInt("id"));
                s.setName(rs.getString("name"));
                s.setSize(rs.getInt("shoeSize"));
                s.setPrice(rs.getBigDecimal("price"));
                s.setStock(rs.getInt("stock"));
                s.setColour(Colour.valueOf(getColourById(rs.getInt("colourId")).toUpperCase()));
                s.setBrand(Brand.valueOf(getBrandById(rs.getInt("brandId")).toUpperCase()));
                shoes.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return shoes;
    }

    public List<Shoe> getProductByName(String shoeName) {
        String sqlQuery = "select * from shoe where name like ?";
        List<Shoe> shoes = new ArrayList<>();

        try (Connection con = DriverManager.getConnection(mysqlAddress, username, password);
             PreparedStatement stmt = con.prepareStatement(sqlQuery)) {
            stmt.setString(1, shoeName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Shoe s = new Shoe();
                    s.setId(rs.getInt("id"));
                    s.setName(rs.getString("name"));
                    s.setSize(rs.getInt("shoeSize"));
                    s.setPrice(rs.getBigDecimal("price"));
                    s.setStock(rs.getInt("stock"));
                    s.setColour(Colour.valueOf(getColourById(rs.getInt("colourId")).toUpperCase()));
                    s.setBrand(Brand.valueOf(getBrandById(rs.getInt("brandId")).toUpperCase()));
                    shoes.add(s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return shoes;
    }

    public String getCustomerTotalOrderValue(int id) {
        String result = "-- Totala beställningsvärde --";
        String sqlQuery = "select customer.name as Namn, sum(shoe.price) as Total from customer\n" +
                "inner join orders on customer.id = orders.customerid\n" +
                "inner join orderitem on orders.id = orderitem.ordersid\n" +
                "inner join shoe on orderitem.shoeid = shoe.id\n";
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = DriverManager.getConnection(mysqlAddress, username, password);
            if (id == 0) {
                sqlQuery += "group by customer.name";
                stmt = con.prepareStatement(sqlQuery);
                rs = stmt.executeQuery();
                while (rs.next()) {
                    result += "\nNamn: " + rs.getString("Namn");
                    result += " - Totala värde: " + rs.getInt("Total") + " kr";
                }
            } else if (id > 0) {
                sqlQuery += "where customer.id = ?";
                stmt = con.prepareStatement(sqlQuery);
                stmt.setInt(1, id);
                rs = stmt.executeQuery();
                while (rs.next()) {
                    if (rs.getString("Namn") == null) {
                        result = "Kund id kunde inte hittas.";
                    } else {
                        result += "\nNamn: " + rs.getString("Namn");
                        result += " - Totala värde: " + rs.getInt("Total") + " kr";
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                rs.close();
                stmt.close();
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public Map<Integer, Customer> getAllCustomers() {
        String sqlQuery = "select * from customer order by name";
        Map<Integer, Customer> customerMap = Collections.synchronizedMap(new LinkedHashMap<>());

        try (Connection con = DriverManager.getConnection(mysqlAddress, username, password);
        PreparedStatement stmt = con.prepareStatement(sqlQuery);
        ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int customerId = rs.getInt("id");
                String customerName = rs.getString("name");
                Customer customer = new Customer();
                customer.setId(customerId);
                customer.setName(customerName);
                customerMap.put(customerId, customer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customerMap;
    }

    public String addToCart(int customerId, int orderId, int shoeId) {
        String sqlQuery = "call addToCart(?, ?, ?)";
        String message = "";

        try (Connection con = DriverManager.getConnection(mysqlAddress, username, password);
        CallableStatement stmt = con.prepareCall(sqlQuery)) {
            stmt.setInt(1, customerId);
            stmt.setInt(2, orderId);
            stmt.setInt(3, shoeId);
            stmt.execute();
            try (ResultSet rs = stmt.getResultSet()) {
                while (rs.next()) {
                    message = rs.getString("Message");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return message;
    }

    public Order getNonExpeditedOrder(Customer customer) {
        Order order = getOrderByCustomer(customer);
        return order;
    }

    private Order getOrderByCustomer(Customer customer) {
        String sqlQuery = "select * from orders where customerId = ? and expedited = 0";
        Order order = new Order();

        try (Connection con = DriverManager.getConnection(mysqlAddress, username, password);
             PreparedStatement stmt = con.prepareStatement(sqlQuery)) {
            stmt.setInt(1, customer.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    order.setId(rs.getInt("id"));
                    order.setCustomerId(rs.getInt("customerId"));
                    order.setOrderDate(rs.getDate("orderDate"));
                    order.setExpedited(rs.getBoolean("expedited"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        List<Integer> shoeIds = getOrderItemIds(order);
        for (int id : shoeIds) {
            order.addShoe(getShoeById(id));
        }
        return order;
    }

    private List<Integer> getOrderItemIds(Order order) {
        String sqlQuery = "select * from orderitem where ordersId = ?";
        List<Integer> shoeIds = new ArrayList<>();

        try (Connection con = DriverManager.getConnection(mysqlAddress, username, password);
             PreparedStatement stmt = con.prepareStatement(sqlQuery)) {
            stmt.setInt(1, order.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    shoeIds.add(rs.getInt("shoeId"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return shoeIds;
    }

    private Shoe getShoeById(int shoeId) {
        String sqlQuery = "select * from Shoe where id = ?";
        Shoe s = null;

        try (Connection con = DriverManager.getConnection(mysqlAddress, username, password);
             PreparedStatement stmt = con.prepareStatement(sqlQuery)) {
            stmt.setInt(1, shoeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    s = new Shoe();
                    s.setId(rs.getInt("id"));
                    s.setName(rs.getString("name"));
                    s.setSize(rs.getInt("shoeSize"));
                    s.setPrice(rs.getBigDecimal("price"));
                    s.setColour(Colour.valueOf(getColourById(rs.getInt("colourId")).toUpperCase()));
                    s.setBrand(Brand.valueOf(getBrandById(rs.getInt("brandId")).toUpperCase()));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return s;
    }

    public String getColourById(int colourId) {
        String sqlQuery = "select name from colour where colour.id = ?";
        String colour = "";

        try (Connection con = DriverManager.getConnection(mysqlAddress, username, password);
        PreparedStatement stmt = con.prepareStatement(sqlQuery)) {
            stmt.setInt(1, colourId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    colour = rs.getString("name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return colour;
    }

    public String getBrandById(int brandId) {
        String sqlQuery = "select name from brand where brand.id = ?";
        String brand = "";

        try (Connection con = DriverManager.getConnection(mysqlAddress, username, password);
             PreparedStatement stmt = con.prepareStatement(sqlQuery)) {
            stmt.setInt(1, brandId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    brand = rs.getString("name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return brand;
    }
}
