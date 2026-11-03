package com.java.jdbc.inventorymanagementsystem;
import java.util.*;
import java.sql.*;

class Database{
	static final String serverUrl = "jdbc:mysql://localhost:3306/";
	static final String URL = "jdbc:mysql://localhost:3306/inventory_management";
	static final String NAME = "inventory_management";
    static final String USER = "root";
    static final String PASS = "root123";
    
    public static Connection getConnection() {
        Connection con = null;
        try {
            con = DriverManager.getConnection(serverUrl, USER, PASS);
            try(Statement smt = con.createStatement()){
                smt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + NAME);
            }
            con.close(); 

           
            con = DriverManager.getConnection(URL, USER, PASS);

            try(Statement smt = con.createStatement()){
                String createSuppliersTable = "CREATE TABLE IF NOT EXISTS suppliers (" +
                        "supplier_id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "supplier_name VARCHAR(50) NOT NULL, " +
                        "phone VARCHAR(15) NOT NULL)";
                smt.executeUpdate(createSuppliersTable);
                
                String createProductsTable = "CREATE TABLE IF NOT EXISTS products (" +
                        "product_id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "product_name VARCHAR(50) NOT NULL, " +
                        "price DECIMAL(10,2) NOT NULL, " +
                        "stock INT NOT NULL, " +
                        "supplier_id INT, " +
                        "FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id))";
                smt.executeUpdate(createProductsTable);
                
                String createSalesTable = "CREATE TABLE IF NOT EXISTS sales (" +
                        "sale_id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "product_id INT, " +
                        "quantity_sold INT NOT NULL, " +
                        "total_amount DECIMAL(10,2) NOT NULL, " +
                        "FOREIGN KEY (product_id) REFERENCES products(product_id))";
                smt.executeUpdate(createSalesTable);
            }
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
        return con;
    }
}

class Validation{
	
	public static boolean isValidString(String input) {
		if(input != null && !input.trim().isEmpty()) {
			return true;
		}
		else {
			return false;
		}
    }
	
	public static boolean isValidNumber(double number) {
        if(number>0) {
        	return true;
        }else {
        	return false;
        }
    }
	
	public static boolean isValidPhone(String phone) {
        if(phone != null && phone.matches("\\d{10}")) {
        	return true;
        }else {
        	return false;
        }
    }
	
	public static boolean isValidStock(int stock) {
        if(stock>=0) {
        	return true;
        }else {
        	return false;
        }
    }
	
	public static boolean isSupplierExists(Connection conn, int supplierId) {
        String sql = "SELECT 1 FROM suppliers WHERE supplier_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, supplierId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }
	
	public static boolean isProductExists(Connection conn, int productId) {
        String sql = "SELECT 1 FROM products WHERE product_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }
	
}

public class InventoryManagementSystem {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try(Connection con = Database.getConnection();
				Scanner sc = new Scanner(System.in)){
            if(con == null) {
                System.out.println("Database Connection failed...");
                return;
            }
		
            while(true) {
                System.out.println("\n INVENTORY SYSTEM :");
                System.out.println("1. Register Supplier");
                System.out.println("2. Add Product");
                System.out.println("3. Record Sale (Update Stock)");
                System.out.println("4. View All Products");
                System.out.println("5. View All Sales Reports");
                System.out.println("6. Exit");
                System.out.print("Choose an option: ");
                
                if (!sc.hasNextInt()) {
                    System.out.println("Invalid input! Please enter a number.");
                    sc.next(); 
                    continue;
                }
                
                int choice = sc.nextInt();
                sc.nextLine(); 

                switch (choice) {
                    case 1:
                        registerSupplier(con, sc);
                        break;
                    case 2:
                        addProduct(con, sc);
                        break;
                    case 3:
                        recordSale(con, sc);
                        break;
                    case 4:
                        viewProducts(con);
                        break;
                    case 5:
                        viewSalesReports(con);
                        break;
                    case 6:
                        System.out.println("Exiting application!");
                        return;
                    default:
                        System.out.println("Invalid choice! Choose between 1 and 6.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
	 }

	private static void registerSupplier(Connection con, Scanner sc) {
		// TODO Auto-generated method stub
		
		System.out.print("Enter Supplier Name: ");
        String name = sc.nextLine();
        if (!Validation.isValidString(name)) {
            System.out.println("Supplier name cannot be empty.");
            return;
        }

        System.out.print("Enter Phone Number: ");
        String phone = sc.nextLine();
        if (!Validation.isValidPhone(phone)) {
            System.out.println("Invalid Phone number!.");
            return;
        }

        String sql = "INSERT INTO suppliers (supplier_name, phone) VALUES (?, ?)";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.executeUpdate();
            System.out.println("Supplier registered successfully!");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
		
	}

	private static void addProduct(Connection con, Scanner sc) {
		// TODO Auto-generated method stub
		
		System.out.print("Enter Supplier ID: ");
        int supplierId = sc.nextInt();
        sc.nextLine();

        if (!Validation.isSupplierExists(con, supplierId)) {
            System.out.println("Supplier ID does not exist! Register supplier first.");
            return;
        }

        System.out.print("Enter Product Name: ");
        String productName = sc.nextLine();
        if (!Validation.isValidString(productName)) {
            System.out.println("Product name cannot be empty.");
            return;
        }

        System.out.print("Enter Product Price: ");
        double price = sc.nextDouble();
        if (!Validation.isValidNumber(price)) {
            System.out.println("Price must be greater than zero.");
            return;
        }

        System.out.print("Enter Initial Stock Quantity: ");
        int stock = sc.nextInt();
        sc.nextLine();
        if (!Validation.isValidStock(stock)) {
            System.out.println("Stock cannot be negative.");
            return;
        }

        String sql = "INSERT INTO products (product_name, price, stock, supplier_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, productName);
            pstmt.setDouble(2, price);
            pstmt.setInt(3, stock);
            pstmt.setInt(4, supplierId);
            pstmt.executeUpdate();
            System.out.println("Product added successfully!");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
		
	}

	private static void recordSale(Connection con, Scanner sc) {
		// TODO Auto-generated method stub
		
		System.out.print("Enter Product ID to Sell: ");
        int productId = sc.nextInt();

        if (!Validation.isProductExists(con, productId)) {
            System.out.println("Product ID not found.");
            return;
        }

        System.out.print("Enter Quantity to Sell: ");
        int qtySold = sc.nextInt();
        sc.nextLine();

        if (!Validation.isValidNumber(qtySold)) {
            System.out.println("Quantity sold must be greater than zero.");
            return;
        }

       
        String check = "SELECT stock, price FROM products WHERE product_id = ?";
        try (PreparedStatement pstmt = con.prepareStatement(check)) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int currentStock = rs.getInt("stock");
                double price = rs.getDouble("price");

                if (currentStock < qtySold) {
                    System.out.println("Insufficient stock! Available stock is: " + currentStock);
                    return;
                }

                double totalAmount = price * qtySold;

                String insertSale = "INSERT INTO sales (product_id, quantity_sold, total_amount) VALUES (?, ?, ?)";
                String updateStock = "UPDATE products SET stock = stock - ? WHERE product_id = ?";

                try (PreparedStatement psSale = con.prepareStatement(insertSale);
                     PreparedStatement psStock = con.prepareStatement(updateStock)) {
                    
                    psSale.setInt(1, productId);
                    psSale.setInt(2, qtySold);
                    psSale.setDouble(3, totalAmount);
                    psSale.executeUpdate();

                    psStock.setInt(1, qtySold);
                    psStock.setInt(2, productId);
                    psStock.executeUpdate();

                    System.out.println("Sale recorded successfully! Total Amount: " + totalAmount);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
		
	}

	private static void viewProducts(Connection con) {
		// TODO Auto-generated method stub
		String sql = "SELECT p.product_id, p.product_name, p.price, p.stock, s.supplier_name " +
                "FROM products p " +
                "JOIN suppliers s ON p.supplier_id = s.supplier_id";

   try (Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {

       System.out.println("\n--- PRODUCT INVENTORY LIST ---");
       boolean hasData = false;
       while (rs.next()) {
           hasData = true;
           System.out.println("ID: " + rs.getInt("product_id") +
                              " | Product: " + rs.getString("product_name") +
                              " | Price: " + rs.getDouble("price") +
                              " | Stock: " + rs.getInt("stock") +
                              " | Supplier: " + rs.getString("supplier_name"));
       }
       if (!hasData) {
           System.out.println("No products found in the inventory.");
       }
   } catch (SQLException e) {
       System.out.println("Error: " + e.getMessage());
   }
		
	}

	private static void viewSalesReports(Connection con) {
		// TODO Auto-generated method stub
		
		String sql = "SELECT sa.sale_id, p.product_name, sa.quantity_sold, sa.total_amount " +
                "FROM sales sa " +
                "JOIN products p ON sa.product_id = p.product_id";

   try (Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {

       System.out.println("\n--- SALES REPORTS ---");
       boolean hasData = false;
       while (rs.next()) {
           hasData = true;
           System.out.println("Sale ID: " + rs.getInt("sale_id") +
                              " | Product: " + rs.getString("product_name") +
                              " | Qty Sold: " + rs.getInt("quantity_sold") +
                              " | Total Earned: " + rs.getDouble("total_amount"));
       }
       if (!hasData) {
           System.out.println("No sales recorded yet.");
       }
   } catch (SQLException e) {
       System.out.println("Error: " + e.getMessage());
   }
		
	}

	}


