import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

class Category {
    int id;
    String name;

    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }
}

class Supplier {
    int id;
    String name;
    String contact;

    public Supplier(int id, String name, String contact) {
        this.id = id;
        this.name = name;
        this.contact = contact;
    }
}

class Product {
    int id;
    String name;
    int categoryId;
    int supplierId;
    double price;

    public Product(int id, String name, int categoryId, int supplierId, double price) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.supplierId = supplierId;
        this.price = price;
    }
}

public class InventoryMgmtSys {
    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;

    private List<Category> categories = new ArrayList<>();
    private List<Supplier> suppliers = new ArrayList<>();
    private List<Product> products = new ArrayList<>();

    private int nextCategoryId = 1;
    private int nextSupplierId = 1;
    private int nextProductId = 1;

    public InventoryMgmtSys() {
        frame = new JFrame("Inventory Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JButton addCategoryButton = new JButton("Add Category");
        JButton addSupplierButton = new JButton("Add Supplier");
        JButton addProductButton = new JButton("Add Product");
        JButton viewProductsButton = new JButton("View Products");
        JButton updateProductButton = new JButton("Update Product");
        JButton deleteProductButton = new JButton("Delete Product");

        panel.add(addCategoryButton);
        panel.add(addSupplierButton);
        panel.add(addProductButton);
        panel.add(viewProductsButton);
        panel.add(updateProductButton);
        panel.add(deleteProductButton);

        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Category ID", "Supplier ID", "Price"}, 0);
        table = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(table);
        panel.add(tableScrollPane);

        frame.add(panel);

        addCategoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = JOptionPane.showInputDialog(frame, "Enter category name:");
                if (name != null && !name.isEmpty()) {
                    categories.add(new Category(nextCategoryId++, name));
                    JOptionPane.showMessageDialog(frame, "Category added successfully!");
                }
            }
        });

        addSupplierButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = JOptionPane.showInputDialog(frame, "Enter supplier name:");
                if (name == null || name.isEmpty()) return;

                String contact = JOptionPane.showInputDialog(frame, "Enter supplier contact:");
                if (contact != null && !contact.isEmpty()) {
                    suppliers.add(new Supplier(nextSupplierId++, name, contact));
                    JOptionPane.showMessageDialog(frame, "Supplier added successfully!");
                }
            }
        });

        addProductButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (categories.isEmpty() || suppliers.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please add categories and suppliers first.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String name = JOptionPane.showInputDialog(frame, "Enter product name:");
                if (name == null || name.isEmpty()) return;

                String categoryIdStr = JOptionPane.showInputDialog(frame, "Enter category ID:");
                int categoryId;
                try {
                    categoryId = Integer.parseInt(categoryIdStr);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid category ID.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String supplierIdStr = JOptionPane.showInputDialog(frame, "Enter supplier ID:");
                int supplierId;
                try {
                    supplierId = Integer.parseInt(supplierIdStr);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid supplier ID.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String priceStr = JOptionPane.showInputDialog(frame, "Enter product price:");
                double price;
                try {
                    price = Double.parseDouble(priceStr);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid price.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                products.add(new Product(nextProductId++, name, categoryId, supplierId, price));
                JOptionPane.showMessageDialog(frame, "Product added successfully!");
            }
        });

        viewProductsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableModel.setRowCount(0);

                for (Product product : products) {
                    tableModel.addRow(new Object[]{
                            product.id,
                            product.name,
                            product.categoryId,
                            product.supplierId,
                            String.format("%.2f", product.price)
                    });
                }
            }
        });

        updateProductButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String productIdStr = JOptionPane.showInputDialog(frame, "Enter product ID to update:");
                int productId;
                try {
                    productId = Integer.parseInt(productIdStr);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid product ID.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Product product = products.stream().filter(p -> p.id == productId).findFirst().orElse(null);
                if (product == null) {
                    JOptionPane.showMessageDialog(frame, "Product not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String name = JOptionPane.showInputDialog(frame, "Enter new product name:", product.name);
                if (name != null && !name.isEmpty()) product.name = name;

                String priceStr = JOptionPane.showInputDialog(frame, "Enter new product price:", product.price);
                try {
                    product.price = Double.parseDouble(priceStr);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid price.", "Error", JOptionPane.ERROR_MESSAGE);
                }

                JOptionPane.showMessageDialog(frame, "Product updated successfully!");
            }
        });

        deleteProductButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String productIdStr = JOptionPane.showInputDialog(frame, "Enter product ID to delete:");
                int productId;
                try {
                    productId = Integer.parseInt(productIdStr);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid product ID.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                products.removeIf(product -> product.id == productId);
                JOptionPane.showMessageDialog(frame, "Product deleted successfully!");
            }
        });

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InventoryGUI());
    }
}
