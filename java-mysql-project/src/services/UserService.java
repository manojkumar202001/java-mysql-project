package services;

import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class UserService {
    private static final int MAX_BORROW_LIMIT = 4;
    private static final int BORROW_DAYS = 5;
    private static final int RENEW_DAYS = 3;
    private static final int FINE_PER_DAY = 10;

    private Connection conn;

    public UserService() {
        conn = DatabaseConnection.getConnection();
    }

    //Register user
    public void registerUser(Scanner scanner) {
    System.out.print("Enter your name to register: ");
    String name = scanner.nextLine().trim();

    if (name.isEmpty()) {
        System.out.println("Name cannot be empty.");
        return;
    }

    String sql = "INSERT INTO users (name) VALUES (?)";

    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        stmt.setString(1, name);
        int rowsInserted = stmt.executeUpdate();

        if (rowsInserted > 0) {
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int newUserId = rs.getInt(1);
                    System.out.println("Registration successful!");
                    System.out.println("Your User ID is: " + newUserId);
                    System.out.println("Please note this User ID for logging in.");
                }
            }
        } else {
            System.out.println("Registration failed. Try again.");
        }

    } catch (SQLException e) {
        System.out.println("SQL Error during registration: " + e.getMessage());
    }
}


    // Borrow book interactively - pass scanner
    public void borrowBook(int userId, Scanner scanner) {
        try {
            // Check current borrowed book count
            String countSql = "SELECT COUNT(*) FROM borrow_records WHERE user_id = ? AND return_date IS NULL";
            try (PreparedStatement stmt = conn.prepareStatement(countSql)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next() && rs.getInt(1) >= MAX_BORROW_LIMIT) {
                    System.out.println("You have already borrowed the maximum of 4 books.");
                    return;
                }
            }

            // Show available books
            System.out.println("\nAvailable Books:");
            String sql = "SELECT id, title, author FROM books WHERE copies_available > 0";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    System.out.printf("ID: %d | Title: %s | Author: %s%n",
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("author"));
                }
            }

            System.out.print("Enter the ID of the book you want to borrow: ");
            int bookId = Integer.parseInt(scanner.nextLine());
            borrowBook(userId, bookId);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Borrow book by ID - no scanner needed here
    public void borrowBook(int userId, int bookId) {
        try {
            // Check borrow count
            String countSql = "SELECT COUNT(*) FROM borrow_records WHERE user_id = ? AND return_date IS NULL";
            try (PreparedStatement stmt = conn.prepareStatement(countSql)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                rs.next();
                if (rs.getInt(1) >= MAX_BORROW_LIMIT) {
                    System.out.println("⚠️ You have reached the borrow limit.");
                    return;
                }
            }

            // Check book availability
            String availSql = "SELECT copies_available FROM books WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(availSql)) {
                stmt.setInt(1, bookId);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next() || rs.getInt("copies_available") <= 0) {
                    System.out.println("❌ Book not available.");
                    return;
                }
            }

            // Insert borrow record
            String insertSql = """
                INSERT INTO borrow_records (user_id, book_id, borrow_date, due_date, renewed)
                VALUES (?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 5 DAY), false)
            """;
            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, bookId);
                stmt.executeUpdate();
            }

            // Decrement copies
            String updateSql = "UPDATE books SET copies_available = copies_available - 1 WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setInt(1, bookId);
                stmt.executeUpdate();
            }

            System.out.println("Book borrowed successfully for 5 days.");

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
    }

    public void renewBook(int recordId) {
        String sql = "SELECT due_date, renewed FROM borrow_records WHERE id = ? AND return_date IS NULL";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, recordId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                if (rs.getBoolean("renewed")) {
                    System.out.println("Already renewed once.");
                    return;
                }

                LocalDate newDue = rs.getDate("due_date").toLocalDate().plusDays(RENEW_DAYS);
                String updateSql = "UPDATE borrow_records SET due_date = ?, renewed = true WHERE id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setDate(1, Date.valueOf(newDue));
                    updateStmt.setInt(2, recordId);
                    updateStmt.executeUpdate();
                }

                System.out.println("Book renewed. New due date: " + newDue);
            } else {
                System.out.println("Record not found or already returned.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
    }

    public void returnBook(int recordId) {
        String fetchSql = "SELECT book_id, due_date FROM borrow_records WHERE id = ? AND return_date IS NULL";

        try (PreparedStatement stmt = conn.prepareStatement(fetchSql)) {
            stmt.setInt(1, recordId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int bookId = rs.getInt("book_id");
                LocalDate due = rs.getDate("due_date").toLocalDate();
                LocalDate today = LocalDate.now();

                long lateDays = java.time.temporal.ChronoUnit.DAYS.between(due, today);
                double fine = Math.max(0, lateDays * FINE_PER_DAY);

                // Update borrow record
                String updateBorrow = """
                    UPDATE borrow_records 
                    SET return_date = ?, fine_incurred = ? 
                    WHERE id = ?
                """;
                try (PreparedStatement updateStmt = conn.prepareStatement(updateBorrow)) {
                    updateStmt.setDate(1, Date.valueOf(today));
                    updateStmt.setDouble(2, fine);
                    updateStmt.setInt(3, recordId);
                    updateStmt.executeUpdate();
                }

                // Update book copies
                String updateBook = "UPDATE books SET copies_available = copies_available + 1 WHERE id = ?";
                try (PreparedStatement stmt2 = conn.prepareStatement(updateBook)) {
                    stmt2.setInt(1, bookId);
                    stmt2.executeUpdate();
                }

                System.out.println("Book returned. Fine: ₹" + fine);

            } else {
                System.out.println("Invalid record or already returned.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
    }

    // Fix SQL here: remove return_status, use return_date
    public void viewUserStatus(int userId) {
        String sql = """
            SELECT br.id AS record_id, b.title, br.borrow_date, br.due_date, br.renewed,
                   br.return_date, br.fine_incurred
            FROM borrow_records br
            JOIN books b ON br.book_id = b.id
            WHERE br.user_id = ?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\nUser Borrow Records:");
            while (rs.next()) {
                System.out.println("Record ID : " + rs.getInt("record_id"));
                System.out.println("Title     : " + rs.getString("title"));
                System.out.println("Borrowed  : " + rs.getDate("borrow_date"));
                System.out.println("Due Date  : " + rs.getDate("due_date"));
                System.out.println("Renewed   : " + rs.getBoolean("renewed"));
                System.out.println("Returned  : " + (rs.getDate("return_date") != null));
                System.out.println("Fine      : ₹" + rs.getDouble("fine_incurred"));
                System.out.println("-----------------------------");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
    }

    public void calculateAndUpdateFines() {
        String sql = """
            UPDATE users u
            JOIN (
                SELECT user_id,
                       SUM(CASE 
                               WHEN return_date IS NULL AND DATEDIFF(CURDATE(), due_date) > 0
                               THEN DATEDIFF(CURDATE(), due_date) * ?
                               ELSE 0
                           END) AS fine
                FROM borrow_records
                GROUP BY user_id
            ) f ON u.id = f.user_id
            SET u.total_fine = f.fine
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, FINE_PER_DAY);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating fines: " + e.getMessage());
        }
    }
}
