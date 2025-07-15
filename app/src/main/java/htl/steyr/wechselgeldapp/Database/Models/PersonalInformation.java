package htl.steyr.wechselgeldapp.Database.Models;

public class PersonalInformation {

    public int id;
    public int customer_id;

    public String name;
    public String email;
    public String street;
    public String houseNumber;
    public String zipCode;
    public String city;
    //public String profileImageUri; // Optional, falls ein Pfad gespeichert wird


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(int customer_id) {
        this.customer_id = customer_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public PersonalInformation(int id, int customer_id, String name, String email, String street, String houseNumber, String zipCode, String city) {
        this.id = id;
        this.customer_id = customer_id;
        this.name = name;
        this.email = email;
        this.street = street;
        this.houseNumber = houseNumber;
        this.zipCode = zipCode;
        this.city = city;

    }
}
