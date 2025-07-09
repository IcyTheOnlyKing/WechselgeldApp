package htl.steyr.wechselgeldapp.Backup;

public class UserData {
    private String email;
    private String username;
    private double totalTransactions;
    private int transactionCount;
    private char[] totalAmmunt;

    public UserData() {}

    public UserData(String email, String username, double totalTransactions, int transactionCount) {
        this.email = email;
        this.username = username;
        this.totalTransactions = totalTransactions;
        this.transactionCount = transactionCount;
    }

    // Getter und Setter
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public double getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(double totalTransactions) { this.totalTransactions = totalTransactions; }

    public int getTransactionCount() { return transactionCount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }

    public char[] getTotalAmmunt() {
        return totalAmmunt;
    }

    public void setTotalAmmunt(char[] totalAmmunt) {
        this.totalAmmunt = totalAmmunt;
    }
}