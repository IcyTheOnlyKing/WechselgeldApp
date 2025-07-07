package htl.steyr.wechselgeldapp.Database.Models;

public class Customer {
    public int id;
    public String displayName;
    public String email;
    public String passwordHash;

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
