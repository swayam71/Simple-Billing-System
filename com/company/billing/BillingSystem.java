package com.company.billing;

import com.company.products.Product;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

public class BillingSystem {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/billing_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "swayam";

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
            return;
        }

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("Connected to the database successfully.");

            Scanner scanner = new Scanner(System.in);
            System.out.println("Enter customer name:");
            String customerName = scanner.nextLine();
            System.out.println("Enter customer phone number:");
            String phoneNumber = scanner.nextLine();

            Bill bill = new Bill(customerName, phoneNumber);

            while (true) {
                try {
                    System.out.println("Enter product name (or 'done' to finish):");
                    String productName = scanner.nextLine();
                    if (productName.equalsIgnoreCase("done")) {
                        break;
                    }

                    System.out.println("Enter product price:");
                    double productPrice = scanner.nextDouble();
                    scanner.nextLine();

                    Product product = new Product(productName, productPrice);
                    bill.addProduct(product);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid price. Please enter a valid number.");
                    scanner.nextLine();
                }
            }

            System.out.println("\nBilling Details:");
            System.out.println("Customer Name: " + bill.getCustomerName());
            System.out.println("Phone Number: " + bill.getPhoneNumber());
            bill.printBill();

            saveBillToDatabase(connection, bill);
            System.out.println("Billing information saved to database.");
            scanner.close();

        } catch (SQLException e) {
            System.out.println("Failed to connect to the database.");
            e.printStackTrace();
            return;
        }
    }

    private static void saveBillToDatabase(Connection connection, Bill bill) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO bill (customer_name, phone_number, items_purchased, total_amount) VALUES (?, ?, ?, ?)")) {
            statement.setString(1, bill.getCustomerName());
            statement.setString(2, bill.getPhoneNumber());

            StringBuilder itemsPurchased = new StringBuilder();
            for (Product product : bill.getProducts()) {
                itemsPurchased.append(product.getName()).append(", ");
            }

            String items = itemsPurchased.toString().trim();
            if (items.endsWith(",")) {
                items = items.substring(0, items.length() - 1);
            }

            statement.setString(3, items);

            double totalAmount = bill.calculateTotal();
            statement.setDouble(4, totalAmount);

            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error saving bill to database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
