package app;

import services.BookService;
import services.UserService;
import services.LibrarianService;

import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        UserService userService = new UserService();
        BookService bookService = new BookService();
        LibrarianService librarianService = new LibrarianService();

        while (true) {
            System.out.println("\n====== Library Management System ======");
            System.out.println("0. Register as New User");
            System.out.println("1. User");
            System.out.println("2. Librarian");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");

            int choice = readInt(scanner);

            switch (choice) {
                case 0 -> userService.registerUser(scanner);  
                case 1 -> userMenu(userService, bookService, scanner);
                case 2 -> librarianMenu(librarianService, bookService, userService, scanner);
                case 3 -> {
                    System.out.println("Exiting system. Goodbye!");
                    scanner.close();
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void userMenu(UserService userService, BookService bookService, Scanner scanner) {
        System.out.print("\nEnter your User ID: ");
        int userId = readInt(scanner);

        userService.calculateAndUpdateFines();

        while (true) {
            System.out.println("\n--- User Menu ---");
            System.out.println("1. View My Borrowed Books & Fines");
            System.out.println("2. Borrow Book");
            System.out.println("3. Renew Book");
            System.out.println("4. Return Book");
            System.out.println("5. View All Books");
            System.out.println("6. Back to Main Menu");
            System.out.print("Enter your choice: ");

            int option = readInt(scanner);

            switch (option) {
                case 1 -> userService.viewUserStatus(userId);
                case 2 -> userService.borrowBook(userId, scanner);
                case 3 -> {
                    System.out.print("Enter Borrow Record ID to renew: ");
                    int recordId = readInt(scanner);
                    userService.renewBook(recordId);
                }
                case 4 -> {
                    System.out.print("Enter Borrow Record ID to return: ");
                    int recordId = readInt(scanner);
                    userService.returnBook(recordId);
                }
                case 5 -> bookService.viewAllBooks();
                case 6 -> {
                    System.out.println("Returning to main menu...");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void librarianMenu(LibrarianService librarianService, BookService bookService, UserService userService, Scanner scanner) {
        while (true) {
            System.out.println("\n--- Librarian Menu ---");
            System.out.println("1. Add Book");
            System.out.println("2. Check Book Availability");
            System.out.println("3. View All Users & Their Borrowed Books");
            System.out.println("4. View Specific User Status");
            System.out.println("5. View All Books");
            System.out.println("6. Back to Main Menu");
            System.out.print("Enter your choice: ");

            int option = readInt(scanner);

            switch (option) {
                case 1 -> librarianService.addBook(scanner);
                case 2 -> librarianService.checkBookAvailability();
                case 3 -> librarianService.viewAllUsersWithBorrowedBooks();
                case 4 -> {
                    System.out.print("Enter User ID: ");
                    int userId = readInt(scanner);
                    userService.viewUserStatus(userId);
                }
                case 5 -> bookService.viewAllBooks();
                case 6 -> {
                    System.out.println("Returning to main menu...");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static int readInt(Scanner scanner) {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number: ");
            }
        }
    }
}
