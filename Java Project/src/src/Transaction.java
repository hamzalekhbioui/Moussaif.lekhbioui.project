package src;
import java.util.Date;

public class Transaction {
    private Date date;
    private String type; // "Deposit" or "Withdraw"
    private double amount;

    public Transaction(Date date, String type, double amount) {
        this.date = date;
        this.type = type;
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }
}