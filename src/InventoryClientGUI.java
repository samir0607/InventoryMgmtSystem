import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
        frame.setSize(800, 900);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(20, 20));  // Use BorderLayout with spacing

        JLabel titleLabel = new JLabel("Inventory Management System", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.BLUE);
        panel.add(titleLabel, BorderLayout.NORTH);

        // Panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));  // Stack buttons vertically
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Add padding around button panel

        JButton addCategoryButton = createStyledButton("Add Category");
        JButton addSupplierButton = createStyledButton("Add Supplier");
        JButton addProductButton = createStyledButton("Add Product");
        JButton viewProductsButton = createStyledButton("View Products");

        buttonPanel.add(addCategoryButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Space between buttons
        buttonPanel.add(addSupplierButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(addProductButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        buttonPanel.add(viewProductsButton);

        panel.add(buttonPanel, BorderLayout.WEST);  // Place buttons on the left

        // Table setup
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Category ID", "Supplier ID", "Price"}, 0);
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setIntercellSpacing(new Dimension(5, 5));
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane tableScrollPane = new JScrollPane(table);
        panel.add(tableScrollPane, BorderLayout.CENTER);  // Table takes the center space

        frame.add(panel);

        addCategoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = JOptionPane.showInputDialog(frame, "Enter category name:");
                if (name != null && !name.trim().isEmpty()) {
                    sendRequest("ADD_CATEGORY", name.trim());
                } else {
                    JOptionPane.showMessageDialog(frame, "Category name cannot be empty.");
                }
            }
        });

        addSupplierButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
            }
        });

        addProductButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
            }
        });

        viewProductsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendRequest("VIEW_PRODUCTS");
            }
        });

        frame.setVisible(true);

        sendRequest("VIEW_PRODUCTS");
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 16));
        button.setBackground(new Color(135, 206, 250));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(150, 40));
        return button;
    }

    private void sendRequest(String command, Object... args) {
        try (Socket socket = new Socket("localhost", 12345);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(command);

            for (Object arg : args) {
                out.writeObject(arg);
            }
            out.flush();

            if ("VIEW_PRODUCTS".equals(command)) {
                Object response = in.readObject();
                if (response instanceof Object[][]) {
                    Object[][] data = (Object[][]) response;
                    tableModel.setRowCount(0);

                    for (Object[] row : data) {
                        tableModel.addRow(row);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Unexpected response format from server.");
                }
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(InventoryClientGUI::new);
    }
}
