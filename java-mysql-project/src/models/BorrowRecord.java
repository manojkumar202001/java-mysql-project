package models;

import java.sql.Date;

public class BorrowRecord {
    private int recordId;
    private int userId;
    private int bookId;
    private Date borrowDate;
    private Date dueDate;
    private Date returnDate;
    private boolean isReturned;
    private boolean renewed;
    private double fineIncurred;

    public BorrowRecord(int recordId, int userId, int bookId, Date borrowDate, Date dueDate,
                        Date returnDate, boolean isReturned, boolean renewed, double fineIncurred) {
        this.recordId = recordId;
        this.userId = userId;
        this.bookId = bookId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.isReturned = isReturned;
        this.renewed = renewed;
        this.fineIncurred = fineIncurred;
    }

    public int getRecordId() {
        return recordId;
    }

    public int getUserId() {
        return userId;
    }

    public int getBookId() {
        return bookId;
    }

    public Date getBorrowDate() {
        return borrowDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public boolean isReturned() {
        return isReturned;
    }

    public boolean isRenewed() {
        return renewed;
    }

    public double getFineIncurred() {
        return fineIncurred;
    }
}
