package htl.steyr.wechselgeldapp.Database.Models;

public class Seller {
    public int id;
    public String shopName;
    public String email;
    public String passwordHash;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

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
