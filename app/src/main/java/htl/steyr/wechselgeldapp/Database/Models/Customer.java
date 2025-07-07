package htl.steyr.wechselgeldapp.Database.Models;

public class Customer {
    public int id;
    public String displayName;
    public String email;
    public String passwordHash;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public Customer() {}

    public Customer(int id, String displayName, String email, String passwordHash) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
        this.passwordHash = passwordHash;
    }


    public Customer(String displayName, String email, String passwordHash) {
        this.displayName = displayName;
        this.email = email;
        this.passwordHash = passwordHash;
    }
}
