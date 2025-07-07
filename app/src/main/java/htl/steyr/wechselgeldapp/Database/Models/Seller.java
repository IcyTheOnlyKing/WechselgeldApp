package htl.steyr.wechselgeldapp.Database.Models;

public class Seller {
    public int id;
    public String shopName;
    public String email;
    public String passwordHash;

    public Seller() {}

    public Seller(int id, String shopName, String email, String passwordHash) {
        this.id = id;
        this.shopName = shopName;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public Seller(String shopName, String email, String passwordHash) {
        this.shopName = shopName;
        this.email = email;
        this.passwordHash = passwordHash;
    }
}
