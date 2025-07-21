package models;

public class Book {
    private int bookId;
    private String title;
    private String author;
    private int copiesAvailable;

    public Book(int bookId, String title, String author, int copiesAvailable) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.copiesAvailable = copiesAvailable;
    }

    public int getBookId() {
        return bookId;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getCopiesAvailable() {
        return copiesAvailable;
    }
}
