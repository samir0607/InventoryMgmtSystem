import java.sql.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

class Category {
    private int id;
    private String name;

    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

class Supplier {
    private int id;
    private String name;
    private String contact;

    public Supplier(int id, String name, String contact) {
        this.id = id;
        this.name = name;
        this.contact = contact;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContact() {
        return contact;
    }
}

class Product {
    private int id;
    private String name;
    private int categoryId;
    private int supplierId;
    private double price;

    public Product(int id, String name, int categoryId, int supplierId, double price) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.supplierId = supplierId;
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public double getPrice() {
        return price;
    }
}

public class Server {
    private static Connection connection;
    private static final List<Category> categories = new CopyOnWriteArrayList<>();
    private static final List<Supplier> suppliers = new CopyOnWriteArrayList<>();
    private static final List<Product> products = new CopyOnWriteArrayList<>();
    private static final AtomicInteger nextCategoryId = new AtomicInteger(1);
    private static final AtomicInteger nextSupplierId = new AtomicInteger(1);
    private static final AtomicInteger nextProductId = new AtomicInteger(1);

    static {
        try {
            // Load the MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish connection to the database
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/inventory_db", "your_username", "your_pswd");
            System.out.println("Database connection established successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to connect to the database.");
        }
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(6090)) {
            System.out.println("Server is running on port 6090...");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if (connection != null && !connection.isClosed()) {
                        connection.close();
                        System.out.println("Database connection closed.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }));

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected!");

                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {
            while (true) {
                String request = (String) in.readObject();

                switch (request) {
                    case "ADD_CATEGORY":
                        addCategory(in, out);
                        break;

                    case "ADD_SUPPLIER":
                        addSupplier(in, out);
                        break;

                    case "ADD_PRODUCT":
                        addProduct(in, out);
                        break;

                    case "VIEW_PRODUCTS":
                        viewProducts(out);
                        break;

                    case "VIEW_CATEGORIES":
                        viewCategories(out);
                        break;

                    case "VIEW_SUPPLIERS":
                        viewSuppliers(out);
                        break;

                    default:
                        out.writeObject("Unknown request.");
                }

                out.flush();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Client disconnected or error occurred.");
        }
    }

    private static void addCategory(ObjectInputStream in, ObjectOutputStream out)
            throws IOException, ClassNotFoundException {
        String categoryName = (String) in.readObject();
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO categories (name) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, categoryName);
            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int id = generatedKeys.getInt(1);
                out.writeObject("Category Added with ID: " + id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            out.writeObject("Error adding category.");
        }
    }

    private static void addSupplier(ObjectInputStream in, ObjectOutputStream out)
            throws IOException, ClassNotFoundException {
        String supplierName = (String) in.readObject();
        String supplierContact = (String) in.readObject();
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO suppliers (name, contact) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, supplierName);
            stmt.setString(2, supplierContact);
            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int id = generatedKeys.getInt(1);
                out.writeObject("Supplier Added with ID: " + id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            out.writeObject("Error adding supplier.");
        }
    }

    private static void addProduct(ObjectInputStream in, ObjectOutputStream out)
            throws IOException, ClassNotFoundException {
        String productName = (String) in.readObject();
        int categoryId = (Integer) in.readObject();
        int supplierId = (Integer) in.readObject();
        double price = (Double) in.readObject();

        try (PreparedStatement stmt = connection
                .prepareStatement("INSERT INTO products (name, category_id, supplier_id, price) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, productName);
            stmt.setInt(2, categoryId);
            stmt.setInt(3, supplierId);
            stmt.setDouble(4, price);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                out.writeObject("Product added successfully!");
            } else {
                out.writeObject("Error adding product.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            out.writeObject("Error adding product.");
        }
    }

    private static void viewProducts(ObjectOutputStream out) throws IOException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, category_id, supplier_id, price FROM products")) {
            List<Object[]> productData = new ArrayList<>();
            while (rs.next()) {
                productData.add(new Object[] {
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("category_id"),
                        rs.getInt("supplier_id"),
                        rs.getDouble("price")
                });
            }
            out.writeObject(productData.toArray(new Object[0][]));
        } catch (SQLException e) {
            e.printStackTrace();
            out.writeObject("Error retrieving products.");
        }
    }

    private static void viewCategories(ObjectOutputStream out) throws IOException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name FROM categories")) {
            List<Object[]> categoryData = new ArrayList<>();
            while (rs.next()) {
                categoryData.add(new Object[] {
                        rs.getInt("id"),
                        rs.getString("name")
                });
            }
            out.writeObject(categoryData.toArray(new Object[0][]));
        } catch (SQLException e) {
            e.printStackTrace();
            out.writeObject("Error retrieving categories.");
        }
    }

    private static void viewSuppliers(ObjectOutputStream out) throws IOException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, contact FROM suppliers")) {
            List<Object[]> supplierData = new ArrayList<>();
            while (rs.next()) {
                supplierData.add(new Object[] {
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("contact")
                });
            }
            out.writeObject(supplierData.toArray(new Object[0][]));
        } catch (SQLException e) {
            e.printStackTrace();
            out.writeObject("Error retrieving suppliers.");
        }
    }

    private static boolean isValidCategory(int categoryId) {
        return categories.stream().anyMatch(c -> c.getId() == categoryId);
    }

    private static boolean isValidSupplier(int supplierId) {
        return suppliers.stream().anyMatch(s -> s.getId() == supplierId);
    }
}
