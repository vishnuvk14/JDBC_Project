package com.java.jdbc.employeemanagementsystem;

import java.util.*;
import java.sql.*;

class Database {
	static final String serverUrl = "jdbc:mysql://localhost:3306/";
	static final String URL = "jdbc:mysql://localhost:3306/employee_leave_management";
	static final String NAME = "employee_leave_management";
	static final String USER = "root";
	static final String PASS = "root123";

	public static Connection getConnection() {
		Connection con = null;
		try {
			con = DriverManager.getConnection(serverUrl, USER, PASS);
			try (Statement smt = con.createStatement()) {
				smt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + NAME);
			}
			con.close();

			con = DriverManager.getConnection(URL, USER, PASS);

			try (Statement smt = con.createStatement()) {
				String createEmployeesTable = "CREATE TABLE IF NOT EXISTS employees ("
						+ "emp_id INT PRIMARY KEY AUTO_INCREMENT, " + "name VARCHAR(50) NOT NULL, "
						+ "department VARCHAR(30) NOT NULL)";
				smt.executeUpdate(createEmployeesTable);

				String createLeaveBalanceTable = "CREATE TABLE IF NOT EXISTS leave_balance ("
						+ "balance_id INT PRIMARY KEY AUTO_INCREMENT, " + "emp_id INT, "
						+ "total_leaves INT DEFAULT 20, " + "leaves_taken INT DEFAULT 0, "
						+ "FOREIGN KEY (emp_id) REFERENCES employees(emp_id))";
				smt.executeUpdate(createLeaveBalanceTable);

				String createLeaveRequestsTable = "CREATE TABLE IF NOT EXISTS leave_requests ("
						+ "leave_id INT PRIMARY KEY AUTO_INCREMENT, " + "emp_id INT, "
						+ "leave_type VARCHAR(20) NOT NULL, " + "days INT NOT NULL, "
						+ "status VARCHAR(20) DEFAULT 'Pending', "
						+ "FOREIGN KEY (emp_id) REFERENCES employees(emp_id))";
				smt.executeUpdate(createLeaveRequestsTable);
			}
		} catch (SQLException e) {
			System.out.println("Database Error: " + e.getMessage());
		}
		return con;
	}

}

class Validation {
	public static boolean isValidString(String input) {
		if (input != null && !input.trim().isEmpty()) {
			return true;

		} else {
			return false;
		}
	}

	public static boolean isValidDays(int days) {
		if (days > 0 && days <= 30) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isEmployeeExists(Connection conn, int empId) {
		String sql = "SELECT 1 FROM employees WHERE emp_id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, empId);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		} catch (SQLException e) {
			return false;
		}
	}

	public static boolean isLeaveRequestExists(Connection conn, int leaveId) {
		String sql = "SELECT 1 FROM leave_requests WHERE leave_id = ? AND status = 'Pending'";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, leaveId);
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		} catch (SQLException e) {
			return false;
		}
	}
}

public class EmployeeLeaveManagementSystem {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		try (Connection con = Database.getConnection(); Scanner sc = new Scanner(System.in)) {
			if (con == null) {
				System.out.println("Database Connection failed...");
				return;
			}

			while (true) {
				System.out.println("\n EMPLOYEE LEAVE MANAGEMENT SYSTEM :");
				System.out.println("1. Register Employee");
				System.out.println("2. Apply Leave");
				System.out.println("3. Approve/Reject Leave");
				System.out.println("4. View Leave Balances");
				System.out.println("5. View Leave Requests");
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
					registerEmployee(con, sc);
					break;
				case 2:
					applyLeave(con, sc);
					break;
				case 3:
					processLeaveRequest(con, sc);
					break;
				case 4:
					viewLeaveBalances(con);
					break;
				case 5:
					viewLeaveRequests(con);
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

	private static void registerEmployee(Connection con, Scanner sc) {
		// TODO Auto-generated method stub

		System.out.print("Enter Employee Name: ");
		String name = sc.nextLine();
		if (!Validation.isValidString(name)) {
			System.out.println("Employee name cannot be empty.");
			return;
		}

		System.out.print("Enter Department: ");
		String dept = sc.nextLine();
		if (!Validation.isValidString(dept)) {
			System.out.println("Department cannot be empty.");
			return;
		}

		String insertEmp = "INSERT INTO employees (name, department) VALUES (?, ?)";
		String insertBalance = "INSERT INTO leave_balance (emp_id, total_leaves, leaves_taken) VALUES (?, 20, 0)";

		try {
			con.setAutoCommit(false);

			try (PreparedStatement psEmp = con.prepareStatement(insertEmp, Statement.RETURN_GENERATED_KEYS)) {
				psEmp.setString(1, name);
				psEmp.setString(2, dept);
				psEmp.executeUpdate();

				ResultSet rs = psEmp.getGeneratedKeys();
				if (rs.next()) {
					int empId = rs.getInt(1);

					try (PreparedStatement psBal = con.prepareStatement(insertBalance)) {
						psBal.setInt(1, empId);
						psBal.executeUpdate();
					}
				}
			}

			con.commit();
			System.out.println("Employee registered successfully with 20 default leave balance!");
		} catch (SQLException e) {
			try {
				con.rollback();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			System.out.println("Error registering employee: " + e.getMessage());
		} finally {
			try {
				con.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	private static void applyLeave(Connection con, Scanner sc) {
		// TODO Auto-generated method stub
		System.out.print("Enter Employee ID: ");
		int empId = sc.nextInt();
		sc.nextLine();

		if (!Validation.isEmployeeExists(con, empId)) {
			System.out.println("Employee ID does not exist!");
			return;
		}

		System.out.print("Enter Leave Type (Casual, Sick): ");
		String leaveType = sc.nextLine();
		if (!Validation.isValidString(leaveType)) {
			System.out.println("Leave type cannot be empty.");
			return;
		}

		System.out.print("Enter Number of Days: ");
		int days = sc.nextInt();
		sc.nextLine();
		if (!Validation.isValidDays(days)) {
			System.out.println("Invalid number of days. Must be between 1 and 30.");
			return;
		}

		String checkBal = "SELECT (total_leaves - leaves_taken) AS remaining FROM leave_balance WHERE emp_id = ?";
		try (PreparedStatement pstmt = con.prepareStatement(checkBal)) {
			pstmt.setInt(1, empId);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				int remaining = rs.getInt("remaining");
				if (days > remaining) {
					System.out.println("Insufficient leave balance! Remaining leaves: " + remaining);
					return;
				}
			}
		} catch (SQLException e) {
			System.out.println("Error checking balance: " + e.getMessage());
			return;
		}

		String sql = "INSERT INTO leave_requests (emp_id, leave_type, days, status) VALUES (?, ?, ?, 'Pending')";
		try (PreparedStatement pstmt = con.prepareStatement(sql)) {
			pstmt.setInt(1, empId);
			pstmt.setString(2, leaveType);
			pstmt.setInt(3, days);
			pstmt.executeUpdate();
			System.out.println("Leave applied successfully!");
		} catch (SQLException e) {
			System.out.println("Error: " + e.getMessage());
		}

	}

	private static void processLeaveRequest(Connection con, Scanner sc) {
		// TODO Auto-generated method stub
		System.out.print("Enter Pending Leave Request ID: ");
		int leaveId = sc.nextInt();
		sc.nextLine();

		if (!Validation.isLeaveRequestExists(con, leaveId)) {
			System.out.println("Pending Leave Request ID not found.");
			return;
		}

		System.out.print("Action (Enter 'Approve' or 'Reject'): ");
		String action = sc.nextLine().trim();
		if (!action.equalsIgnoreCase("Approve") && !action.equalsIgnoreCase("Reject")) {
			System.out.println("Invalid action entered.");
			return;
		}

		try {
			con.setAutoCommit(false);

			if (action.equalsIgnoreCase("Approve")) {

				int empId = 0;
				int days = 0;
				String getReq = "SELECT emp_id, days FROM leave_requests WHERE leave_id = ?";
				try (PreparedStatement ps = con.prepareStatement(getReq)) {
					ps.setInt(1, leaveId);
					ResultSet rs = ps.executeQuery();
					if (rs.next()) {
						empId = rs.getInt("emp_id");
						days = rs.getInt("days");
					}
				}

				String updateBal = "UPDATE leave_balance SET leaves_taken = leaves_taken + ? WHERE emp_id = ?";
				try (PreparedStatement psBal = con.prepareStatement(updateBal)) {
					psBal.setInt(1, days);
					psBal.setInt(2, empId);
					psBal.executeUpdate();
				}

				String updateReq = "UPDATE leave_requests SET status = 'Approved' WHERE leave_id = ?";
				try (PreparedStatement psReq = con.prepareStatement(updateReq)) {
					psReq.setInt(1, leaveId);
					psReq.executeUpdate();
				}

				con.commit();
				System.out.println("Leave request Approved successfully and balance updated!");

			} else {
				String updateReq = "UPDATE leave_requests SET status = 'Rejected' WHERE leave_id = ?";
				try (PreparedStatement psReq = con.prepareStatement(updateReq)) {
					psReq.setInt(1, leaveId);
					psReq.executeUpdate();
				}
				con.commit();
				System.out.println("Leave request Rejected.");
			}

		} catch (SQLException e) {
			try {
				con.rollback();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			System.out.println("Error processing leave request: " + e.getMessage());
		} finally {
			try {
				con.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private static void viewLeaveBalances(Connection con) {
		// TODO Auto-generated method stub
		String sql = "SELECT e.emp_id, e.name, e.department, lb.total_leaves, lb.leaves_taken, "
				+ "(lb.total_leaves - lb.leaves_taken) AS remaining_leaves " + "FROM employees e "
				+ "JOIN leave_balance lb ON e.emp_id = lb.emp_id";

		try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

			System.out.println("\n--- EMPLOYEE LEAVE BALANCES ---");
			boolean hasData = false;
			while (rs.next()) {
				hasData = true;
				System.out.println("Emp ID: " + rs.getInt("emp_id") + " | Name: " + rs.getString("name") + " | Dept: "
						+ rs.getString("department") + " | Total: " + rs.getInt("total_leaves") + " | Taken: "
						+ rs.getInt("leaves_taken") + " | Remaining: " + rs.getInt("remaining_leaves"));
			}
			if (!hasData) {
				System.out.println("No records found.");
			}
		} catch (SQLException e) {
			System.out.println("Error: " + e.getMessage());
		}

	}

	private static void viewLeaveRequests(Connection con) {
		// TODO Auto-generated method stub
		String sql = "SELECT lr.leave_id, e.name AS employee_name, lr.leave_type, lr.days, lr.status "
				+ "FROM leave_requests lr " + "JOIN employees e ON lr.emp_id = e.emp_id";

		try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

			System.out.println("\n--- LEAVE REQUESTS ---");
			boolean hasData = false;
			while (rs.next()) {
				hasData = true;
				System.out.println("Request ID: " + rs.getInt("leave_id") + " | Employee Name: "
						+ rs.getString("employee_name") + " | Type: " + rs.getString("leave_type") + " | Days: "
						+ rs.getInt("days") + " | Status: " + rs.getString("status"));
			}
			if (!hasData) {
				System.out.println("No leave requests found.");
			}
		} catch (SQLException e) {
			System.out.println("Error: " + e.getMessage());
		}

	}

}
