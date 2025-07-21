package models;

public class User {
    private int userId;
    private String name;
    private double totalFine;

    public User(int userId, String name, double totalFine) {
        this.userId = userId;
        this.name = name;
        this.totalFine = totalFine;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public double getTotalFine() {
        return totalFine;
    }
}
