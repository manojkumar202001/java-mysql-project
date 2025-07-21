package services;

import utils.DatabaseConnection;

import java.sql.*;
import java.util.Scanner;

public class LibrarianService {
    private Connection conn;

    public LibrarianService() {
        conn = DatabaseConnection.getConnection();
    }

    // Interactive addBook with passed Scanner
    public void addBook(Scanner scanner) {
        try {
            System.out.print("Enter Book Title: ");
            String title = scanner.nextLine().trim();

            System.out.print("Enter Author Name: ");
            String author = scanner.nextLine().trim();

            System.out.print("Enter Number of Copies: ");
            int copies = Integer.parseInt(scanner.nextLine().trim());

            addBook(title, author, copies);

        } catch (NumberFormatException e) {
            System.out.println("Invalid number input.");
        }
    }

    // Core addBook logic
    public void addBook(String title, String author, int copies) {
        String sql = "INSERT INTO books (title, author, copies_available) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.setInt(3, copies);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Book added successfully.");
            } else {
                System.out.println("Failed to add book.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
    }

    public void viewAllUsersWithBorrowedBooks() {
        String sql = """
            SELECT u.id AS user_id, u.name AS user_name, u.total_fine,
                   b.title AS book_title, br.borrow_date, br.due_date
            FROM users u
            LEFT JOIN borrow_records br ON u.id = br.user_id AND br.is_returned = FALSE
            LEFT JOIN books b ON br.book_id = b.id
            ORDER BY u.id, br.due_date
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            int lastUserId = -1;
            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String userName = rs.getString("user_name");
                double totalFine = rs.getDouble("total_fine");
                String bookTitle = rs.getString("book_title");
                Date borrowDate = rs.getDate("borrow_date");
                Date dueDate = rs.getDate("due_date");

                if (userId != lastUserId) {
                    System.out.println("\n-----------------------------------");
                    System.out.println("User ID: " + userId);
                    System.out.println("Name   : " + userName);
                    System.out.println("Fine   : Rs." + totalFine);
                    System.out.println("Borrowed Books:");
                    lastUserId = userId;
                }

                if (bookTitle != null) {
                    System.out.println(" - " + bookTitle + " | Borrowed on: " + borrowDate + " | Due: " + dueDate);
                } else {
                    System.out.println(" - No books currently borrowed.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error fetching borrowed books: " + e.getMessage());
        }
    }

    public void checkBookAvailability() {
        String sql = "SELECT id, title, author, copies_available FROM books";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\nBook Availability:");
            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                int available = rs.getInt("copies_available");

                System.out.println("\nBook ID         : " + id);
                System.out.println("Title           : " + title);
                System.out.println("Author          : " + author);
                System.out.println("Available Copies: " + available);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving book availability: " + e.getMessage());
        }
    }
}
