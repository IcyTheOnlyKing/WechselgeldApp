package htl.steyr.wechselgeldapp.Backup;

public class UserData {
    private String email;
    private String username;
    private double totalAmount;
    private int transactionCount;
    private String sellerName;
    private double transactionAmount;

    public UserData() {
    }

    public UserData(String email, String username, double totalAmount, int transactionCount, String sellerName, double transactionAmount) {
        this.email = email;
        this.username = username;
        this.totalAmount = totalAmount;
        this.transactionCount = transactionCount;
        this.sellerName = sellerName;
        this.transactionAmount = transactionAmount;
    }

    // Getter und Setter...
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public int getTransactionCount() { return transactionCount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public double getTransactionAmount() { return transactionAmount; }
    public void setTransactionAmount(double transactionAmount) { this.transactionAmount = transactionAmount; }
}