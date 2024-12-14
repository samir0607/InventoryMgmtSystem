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
    private static final List<Category> categories = new CopyOnWriteArrayList<>();
    private static final List<Supplier> suppliers = new CopyOnWriteArrayList<>();
    private static final List<Product> products = new CopyOnWriteArrayList<>();
    private static final AtomicInteger nextCategoryId = new AtomicInteger(1);
    private static final AtomicInteger nextSupplierId = new AtomicInteger(1);
    private static final AtomicInteger nextProductId = new AtomicInteger(1);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(6090)) {
            System.out.println("Server is running on port 6090...");

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
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())
        ) {
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
        categories.add(new Category(nextCategoryId.getAndIncrement(), categoryName));
        out.writeObject("Category Added with ID: " + (nextCategoryId.get() - 1));
    }

    private static void addSupplier(ObjectInputStream in, ObjectOutputStream out)
            throws IOException, ClassNotFoundException {
        String supplierName = (String) in.readObject();
        String supplierContact = (String) in.readObject();
        suppliers.add(new Supplier(nextSupplierId.getAndIncrement(), supplierName, supplierContact));
        out.writeObject("Supplier Added with ID: " + (nextSupplierId.get() - 1));
    }

    private static void addProduct(ObjectInputStream in, ObjectOutputStream out)
            throws IOException, ClassNotFoundException {
        String productName = (String) in.readObject();
        int categoryId = (Integer) in.readObject();
        int supplierId = (Integer) in.readObject();
        double price = (Double) in.readObject();

        if (isValidCategory(categoryId) && isValidSupplier(supplierId)) {
            products.add(new Product(nextProductId.getAndIncrement(), productName, categoryId, supplierId, price));
            out.writeObject("Product added successfully!");
        } else {
            out.writeObject("Invalid category or supplier ID.");
        }
    }

    private static void viewProducts(ObjectOutputStream out)
            throws IOException {
        Object[][] productData = products.stream()
                .map(p -> new Object[]{
                        p.getId(),
                        p.getName(),
                        p.getCategoryId(),
                        p.getSupplierId(),
                        p.getPrice()
                })
                .toArray(Object[][]::new);
        out.writeObject(productData);
    }

    private static void viewCategories(ObjectOutputStream out)
            throws IOException {
        Object[][] categoryData = categories.stream()
                .map(c -> new Object[]{
                        c.getId(),
                        c.getName()
                })
                .toArray(Object[][]::new);
        out.writeObject(categoryData);
    }

    private static void viewSuppliers(ObjectOutputStream out)
            throws IOException {
        Object[][] supplierData = suppliers.stream()
                .map(s -> new Object[]{
                        s.getId(),
                        s.getName(),
                        s.getContact()
                })
                .toArray(Object[][]::new);
        out.writeObject(supplierData);
    }

    private static boolean isValidCategory(int categoryId) {
        return categories.stream().anyMatch(c -> c.getId() == categoryId);
    }

    private static boolean isValidSupplier(int supplierId) {
        return suppliers.stream().anyMatch(s -> s.getId() == supplierId);
    }
}
