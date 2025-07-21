package services;

import models.Book;
import utils.DatabaseConnection;

import java.sql.*;

public class BookService {
    private final Connection conn;

    public BookService() {
        conn = DatabaseConnection.getConnection();
    }

    // View all books that have available copies
    public void viewAvailableBooks() {
        String sql = "SELECT * FROM books WHERE copies_available > 0";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("\nAvailable Books:");
            boolean found = false;
            while (rs.next()) {
                found = true;
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                int available = rs.getInt("copies_available");

                System.out.println("\nID       : " + id);
                System.out.println("Title    : " + title);
                System.out.println("Author   : " + author);
                System.out.println("Available: " + available);
            }

            if (!found) {
                System.out.println("No books currently available.");
            }

        } catch (SQLException e) {
            System.err.println("Error fetching available books.");
            e.printStackTrace();
        }
    }

    // Add a new book
    public void addBook(String title, String author, int copies) {
        if (copies < 0) {
            System.out.println("Number of copies cannot be negative.");
            return;
        }
        String sql = "INSERT INTO books (title, author, copies_available) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.setInt(3, copies);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Book added successfully.");
            } else {
                System.out.println("Failed to add book.");
            }
        } catch (SQLException e) {
            System.err.println("Error adding book.");
            e.printStackTrace();
        }
    }

    // Check if a book is available (copies_available > 0)
    public boolean isBookAvailable(int bookId) {
        String sql = "SELECT copies_available FROM books WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("copies_available") > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking book availability.");
            e.printStackTrace();
        }
        return false;
    }

    // Update copies_available by delta (+ or -)
    public void updateCopies(int bookId, int delta) {
        String sql = "UPDATE books SET copies_available = GREATEST(copies_available + ?, 0) WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, delta);
            stmt.setInt(2, bookId);
            int updatedRows = stmt.executeUpdate();
            if (updatedRows == 0) {
                System.out.println("Book ID " + bookId + " not found.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating book copies.");
            e.printStackTrace();
        }
    }

    // View all books (for librarian)
    public void viewAllBooks() {
        String sql = "SELECT * FROM books";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("\nAll Books:");
            boolean found = false;
            while (rs.next()) {
                found = true;
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                int available = rs.getInt("copies_available");

                System.out.println("\nID       : " + id);
                System.out.println("Title    : " + title);
                System.out.println("Author   : " + author);
                System.out.println("Available: " + available);
            }

            if (!found) {
                System.out.println("No books in the library yet.");
            }

        } catch (SQLException e) {
            System.err.println("Error fetching all books.");
            e.printStackTrace();
        }
    }

    // Optional: Fetch a Book object by bookId
    public Book getBookById(int bookId) {
        String sql = "SELECT * FROM books WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getInt("copies_available")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching book by ID.");
            e.printStackTrace();
        }
        return null;
    }
}
