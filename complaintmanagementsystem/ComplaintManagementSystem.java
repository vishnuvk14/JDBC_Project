package com.java.jdbc.complaintmanagementsystem;
import java.util.*;
import java.sql.*;

class Database{
	static final String serverUrl = "jdbc:mysql://localhost:3306/";
	static final String URL = "jdbc:mysql://localhost:3306/complaint_management";
    static final String NAME = "complaint_management";
    static final String USER = "root";
    static final String PASS = "root123";
    
    public static Connection getConnection() {
    	Connection con=null;
    	try {
    		con = DriverManager.getConnection(serverUrl,USER,PASS);
    		try(Statement smt=con.createStatement()){
    			smt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + NAME);
    			smt.executeUpdate("USE "+NAME);
    			
    			String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                        "user_id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "name VARCHAR(50) NOT NULL, " +
                        "phone VARCHAR(15) NOT NULL)";
                smt.executeUpdate(createUsersTable);
                
                String createOfficersTable = "CREATE TABLE IF NOT EXISTS officers (" +
                        "officer_id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "name VARCHAR(50) NOT NULL, " +
                        "department VARCHAR(30) NOT NULL)";
                smt.executeUpdate(createOfficersTable);
                
                String createComplaintsTable = "CREATE TABLE IF NOT EXISTS complaints (" +
                        "complaint_id INT PRIMARY KEY AUTO_INCREMENT, " +
                        "user_id INT, " +
                        "officer_id INT, " +
                        "description TEXT NOT NULL, " +
                        "status VARCHAR(20) DEFAULT 'Pending', " +
                        "FOREIGN KEY (user_id) REFERENCES users(user_id), " +
                        "FOREIGN KEY (officer_id) REFERENCES officers(officer_id))";
                smt.executeUpdate(createComplaintsTable);
    		}
    		con = DriverManager.getConnection(URL,USER,PASS);
    	}catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
    }
    	return con;
    
 }
}

class Validation{
	public static boolean isValidString(String input) {
        if(input != null && !input.trim().isEmpty()) {
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
	
	public static boolean isUserExists(Connection conn, int userId) {
        String sql = "SELECT 1 FROM users WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }
	
	public static boolean isOfficerExists(Connection conn, int officerId) {
        String sql = "SELECT 1 FROM officers WHERE officer_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, officerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }
	
	public static boolean isComplaintExists(Connection conn, int complaintId) {
        String sql = "SELECT 1 FROM complaints WHERE complaint_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, complaintId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }
}
public class ComplaintManagementSystem {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		try(Connection con=Database.getConnection(); Scanner sc =new Scanner(System.in)){
			if(con==null) {
				System.out.println("Database Connection failed...");
                return;
			}
			
			while(true) {
				System.out.println("\n COMPLAINT SYSTEM :");
                System.out.println("1. Register User");
                System.out.println("2. Register Officer");
                System.out.println("3. Register Complaint");
                System.out.println("4. Assign Officer & Update Status");
                System.out.println("5. View All Complaints");
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
                        registerUser(con, sc);
                        break;
                    case 2:
                        registerOfficer(con, sc);
                        break;
                    case 3:
                        registerComplaint(con, sc);
                        break;
                    case 4:
                        updateComplaintStatus(con, sc);
                        break;
                    case 5:
                        viewComplaints(con);
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

	private static void registerUser(Connection con, Scanner sc) {
		// TODO Auto-generated method stub
		System.out.print("Enter User Name: ");
        String name = sc.nextLine();
        if (!Validation.isValidString(name)) {
            System.out.println("Name cannot be empty.");
            return;
        }

        System.out.print("Enter Phone Number: ");
        String phone = sc.nextLine();
        if (!Validation.isValidPhone(phone)) {
            System.out.println("Invalid phone number.");
            return;
        }

        String sql = "INSERT INTO users (name, phone) VALUES (?, ?)";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.executeUpdate();
            System.out.println("User registered successfully!");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
		
	}

	private static void registerOfficer(Connection con, Scanner sc) {
		// TODO Auto-generated method stub
		System.out.print("Enter Officer Name: ");
        String name = sc.nextLine();
        if (!Validation.isValidString(name)) {
            System.out.println("Name cannot be empty.");
            return;
        }

        System.out.print("Enter Department: ");
        String dept = sc.nextLine();
        if (!Validation.isValidString(dept)) {
            System.out.println("Department cannot be empty.");
            return;
        }

        String sql = "INSERT INTO officers (name, department) VALUES (?, ?)";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, dept);
            pstmt.executeUpdate();
            System.out.println("Officer registered successfully!");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
		
	}

	private static void registerComplaint(Connection con, Scanner sc) {
		// TODO Auto-generated method stub
		System.out.print("Enter User ID: ");
        int userId = sc.nextInt();
        sc.nextLine();

        if (!Validation.isUserExists(con, userId)) {
            System.out.println("User ID does not exist! Please register user first.");
            return;
        }

        System.out.print("Enter Complaint Description: ");
        String description = sc.nextLine();
        if (!Validation.isValidString(description)) {
            System.out.println("Description cannot be empty.");
            return;
        }

        String sql = "INSERT INTO complaints (user_id, description, status) VALUES (?, ?, 'Pending')";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, description);
            pstmt.executeUpdate();
            System.out.println("Complaint registered successfully!");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
	}

	private static void updateComplaintStatus(Connection con, Scanner sc) {
		// TODO Auto-generated method stub
		System.out.print("Enter Complaint ID: ");
        int complaintId = sc.nextInt();

        if (!Validation.isComplaintExists(con, complaintId)) {
            System.out.println("Complaint ID not found.");
            return;
        }

        System.out.print("Enter Officer ID to assign: ");
        int officerId = sc.nextInt();
        sc.nextLine();

        if (!Validation.isOfficerExists(con, officerId)) {
            System.out.println("Officer ID does not exist.");
            return;
        }

        System.out.print("Enter New Status (e.g., In Progress, Resolved): ");
        String status = sc.nextLine();
        if (!Validation.isValidString(status) && (status.equalsIgnoreCase("progress") || status.equalsIgnoreCase("resolved"))) {
            System.out.println("Status cannot be empty.");
            return;
        }

        String sql = "UPDATE complaints SET officer_id = ?, status = ? WHERE complaint_id = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, officerId);
            pstmt.setString(2, status);
            pstmt.setInt(3, complaintId);
            pstmt.executeUpdate();
            System.out.println("Complaint updated successfully!");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
		
	}

	private static void viewComplaints(Connection con) {
		// TODO Auto-generated method stub
		
		String sql = "SELECT c.complaint_id, u.name AS citizen, o.name AS officer, c.description, c.status " +
                "FROM complaints c " +
                "JOIN users u ON c.user_id = u.user_id " +
                "LEFT JOIN officers o ON c.officer_id = o.officer_id";

   try (Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {

       System.out.println("\n--- COMPLAINT LIST ---");
       boolean hasData = false;
       while (rs.next()) {
           hasData = true;
           System.out.println("S.NO: " + rs.getInt("complaint_id") +
                              " | User Name: " + rs.getString("citizen") +
                              " | Officer Name: " + (rs.getString("officer") != null ? rs.getString("officer") : "Unassigned") +
                              " | Desc: " + rs.getString("description") +
                              " | Status: " + rs.getString("status"));
       }
       if (!hasData) {
           System.out.println("No complaints found in the database.");
       }
   } catch (SQLException e) {
       System.out.println("Error: " + e.getMessage());
   }	
	}
           
	

		

	
}

