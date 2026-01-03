package com.company.gui;

import com.company.products.Product;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BillingSystemGUI extends JFrame {
    private JTextField customerNameField;
    private JTextField phoneNumberField;
    private JTextField productNameField;
    private JTextField productPriceField;
    private JTextArea productArea;
    private List<Product> products;

    private static final String DB_URL = "jdbc:mysql://localhost:3306/billing_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "swayam";

    public BillingSystemGUI() {
        setTitle("Billing System");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(7, 2));

        products = new ArrayList<>();

        JLabel customerNameLabel = new JLabel("Customer Name:");
        customerNameField = new JTextField();
        JLabel phoneNumberLabel = new JLabel("Phone Number:");
        phoneNumberField = new JTextField();
        JLabel productNameLabel = new JLabel("Product Name:");
        productNameField = new JTextField();
        JLabel productPriceLabel = new JLabel("Product Price:");
        productPriceField = new JTextField();
        JLabel productLabel = new JLabel("Products:");
        productArea = new JTextArea();
        JButton addProductButton = new JButton("Add Product");
        JButton printBillButton = new JButton("Print Bill");

        add(customerNameLabel);
        add(customerNameField);
        add(phoneNumberLabel);
        add(phoneNumberField);
        add(productNameLabel);
        add(productNameField);
        add(productPriceLabel);
        add(productPriceField);
        add(productLabel);
        add(productArea);
        add(addProductButton);
        add(printBillButton);

        addProductButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addProduct();
            }
        });

        printBillButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                printBill();
            }
        });
    }

    private void addProduct() {
        String productName = productNameField.getText();
        double productPrice = Double.parseDouble(productPriceField.getText());
        Product product = new Product(productName, productPrice);
        products.add(product);
        updateProductArea();
        productNameField.setText("");
        productPriceField.setText("");
    }

    private void updateProductArea() {
        StringBuilder sb = new StringBuilder();
        for (Product product : products) {
            sb.append("- ").append(product.getName()).append(": $").append(product.getPrice()).append("\n");
        }
        productArea.setText(sb.toString());
    }

    private void printBill() {
        String customerName = customerNameField.getText();
        String phoneNumber = phoneNumberField.getText();
        double totalAmount = 0;

        // Construct items purchased string and calculate total amount
        StringBuilder itemsPurchased = new StringBuilder();
        for (Product product : products) {
            itemsPurchased.append(product.getName()).append(", ");
            totalAmount += product.getPrice();
        }

        // Remove the last comma and space
        String itemsPurchasedString = itemsPurchased.toString().trim();
        if (itemsPurchasedString.endsWith(",")) {
            itemsPurchasedString = itemsPurchasedString.substring(0, itemsPurchasedString.length() - 1);
        }

        // Display bill in dialog
        StringBuilder billText = new StringBuilder();
        billText.append("Customer Name: ").append(customerName).append("\n");
        billText.append("Phone Number: ").append(phoneNumber).append("\n");
        billText.append("Bill Details:\n");
        billText.append(productArea.getText()).append("\n");
        billText.append("Total: $").append(totalAmount);
        JOptionPane.showMessageDialog(this, billText.toString(), "Bill", JOptionPane.INFORMATION_MESSAGE);

        // Save bill to database
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String insertQuery = "INSERT INTO bill (customer_name, phone_number, items_purchased, total_amount) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
                statement.setString(1, customerName);
                statement.setString(2, phoneNumber);
                statement.setString(3, itemsPurchasedString);
                statement.setDouble(4, totalAmount);
                statement.executeUpdate();
                System.out.println("Billing information saved to database.");
            } catch (SQLException ex) {
                System.out.println("Error saving bill to database: " + ex.getMessage());
                ex.printStackTrace();
            }
        } catch (SQLException ex) {
            System.out.println("Failed to connect to the database.");
            ex.printStackTrace();
        }

        // Clear fields and products list after printing bill
        products.clear();
        productArea.setText("");
        customerNameField.setText("");
        phoneNumberField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new BillingSystemGUI().setVisible(true);
            }
        });
    }
}
