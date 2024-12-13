import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class InventoryClientGUI {
    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private String serverAddress = "localhost";
    private int serverPort = 12345;

    public InventoryClientGUI() {
        frame = new JFrame("Inventory Management System - Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(20, 20));
        panel.setBackground(new Color(99, 151, 99));

        JLabel titleLabel = new JLabel("Inventory Management System", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(23, 66, 23));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buttonPanel.setBackground(new Color(99, 151, 99));

        // Create buttons with rounded corners and dark green text
        JButton addCategoryButton = createStyledButton("Add Category");
        JButton addSupplierButton = createStyledButton("Add Supplier");
        JButton addProductButton = createStyledButton("Add Product");
        JButton viewProductsButton = createStyledButton("View Products");
        JButton viewCategoriesButton = createStyledButton("View Categories");
        JButton viewSuppliersButton = createStyledButton("View Suppliers");

        buttonPanel.add(addCategoryButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(addSupplierButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(addProductButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(viewProductsButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(viewCategoriesButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(viewSuppliersButton);

        panel.add(buttonPanel, BorderLayout.WEST);

        // Table setup with modern styling
        tableModel = new DefaultTableModel(new String[]{}, 0);
        table = new JTable(tableModel);
        table.setRowHeight(35);
        table.setIntercellSpacing(new Dimension(5, 5));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setGridColor(new Color(122, 172, 122));
        table.setSelectionBackground(new Color(30, 81, 52));
        table.setSelectionForeground(new Color(99, 151, 99));
        JScrollPane tableScrollPane = new JScrollPane(table);
        panel.add(tableScrollPane, BorderLayout.CENTER);

        frame.add(panel);

        // Add action listeners for buttons
        addCategoryButton.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(frame, "Enter category name:");
            if (name != null && !name.trim().isEmpty()) {
                sendRequest("ADD_CATEGORY", name.trim());
            } else {
                JOptionPane.showMessageDialog(frame, "Category name cannot be empty.");
            }
        });

        addSupplierButton.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(frame, "Enter supplier name:");
            if (name == null || name.trim().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Supplier name cannot be empty.");
                return;
            }

            String contact = JOptionPane.showInputDialog(frame, "Enter supplier contact:");
            if (contact != null && !contact.trim().isEmpty()) {
                sendRequest("ADD_SUPPLIER", name.trim(), contact.trim());
            } else {
                JOptionPane.showMessageDialog(frame, "Supplier contact cannot be empty.");
            }
        });

        addProductButton.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(frame, "Enter product name:");
            if (name == null || name.trim().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Product name cannot be empty.");
                return;
            }

            String categoryIdStr = JOptionPane.showInputDialog(frame, "Enter category ID:");
            String supplierIdStr = JOptionPane.showInputDialog(frame, "Enter supplier ID:");
            String priceStr = JOptionPane.showInputDialog(frame, "Enter product price:");

            try {
                int categoryId = Integer.parseInt(categoryIdStr.trim());
                int supplierId = Integer.parseInt(supplierIdStr.trim());
                double price = Double.parseDouble(priceStr.trim());
                sendRequest("ADD_PRODUCT", name.trim(), categoryId, supplierId, price);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Invalid numeric input for category, supplier, or price.");
            }
        });

        viewProductsButton.addActionListener(e -> sendRequest("VIEW_PRODUCTS"));
        viewCategoriesButton.addActionListener(e -> sendRequest("VIEW_CATEGORIES"));
        viewSuppliersButton.addActionListener(e -> sendRequest("VIEW_SUPPLIERS"));

        frame.setVisible(true);
        sendRequest("VIEW_PRODUCTS");
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(new Color(23, 66, 23));
        button.setBackground(new Color(78, 126, 78));
        button.setSize(160, 70);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(160, 50));
        button.setBorder(BorderFactory.createBevelBorder(2, new Color(221, 225, 77),new Color(221, 225, 0)));

        // Add rounded corners to the button
        button.setBorder(BorderFactory.createCompoundBorder(
                button.getBorder(),
                BorderFactory.createEmptyBorder(20, 20, 20, 20) // Padding inside the button
        ));

        // Add hover effect for buttons
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(221, 225, 77));
                button.setForeground(new Color(76, 198, 76));
                button.setOpaque(true);
                button.setSize(165, 75);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setForeground(new Color(23, 66, 23));
                button.setOpaque(false);
                button.setSize(160, 70);
            }
        });

        return button;
    }

    private void sendRequest(String command, Object... args) {
        try (Socket socket = new Socket(serverAddress, serverPort);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(command);

            for (Object arg : args) {
                out.writeObject(arg);
            }
            out.flush();

            if ("VIEW_PRODUCTS".equals(command)) {
                updateTable(in, new String[]{"Product ID", "Product Name", "Category ID", "Supplier ID", "Price"});
            } else if ("VIEW_CATEGORIES".equals(command)) {
                updateTable(in, new String[]{"Category ID", "Category Name"});
            } else if ("VIEW_SUPPLIERS".equals(command)) {
                updateTable(in, new String[]{"Supplier ID", "Supplier Name", "Contact"});
            } else {
                Object response = in.readObject();
                if (response instanceof String) {
                    JOptionPane.showMessageDialog(frame, (String) response);
                } else {
                    JOptionPane.showMessageDialog(frame, "Unexpected response from server.");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(frame, "Error communicating with server: " + e.getMessage());
        }
    }

    private void updateTable(ObjectInputStream in, String[] columnNames) throws IOException, ClassNotFoundException {
        Object response = in.readObject();
        if (response instanceof Object[][]) {
            Object[][] data = (Object[][]) response;
            tableModel.setColumnIdentifiers(columnNames);
            tableModel.setRowCount(0);

            for (Object[] row : data) {
                tableModel.addRow(row);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Unexpected response format from server.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(InventoryClientGUI::new);
    }
}
