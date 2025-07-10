package htl.steyr.wechselgeldapp.Backup;

public class UserData {
    private String email;
    private String username;
    private double totalAmount;  // Korrigiert: war totalTransactions
    private int transactionCount;

    public UserData() {
    }

    public UserData(String email, String username, double totalAmount, int transactionCount) {
        this.email = email;
        this.username = username;
        this.totalAmount = totalAmount;
        this.transactionCount = transactionCount;
    }

    // Getter und Setter
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getTotalAmount() {
        return totalAmount;
    }  // Korrigiert: war getTotalTransactions

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    // Entfernt: getTotalAmmunt() mit Tippfehler

    public int getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }
}