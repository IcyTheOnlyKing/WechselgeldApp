package htl.steyr.wechselgeldapp.Database.Models;

public class PersonalInformation {

    public int id;
    public int seller_id;

    public String name;
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

    public int getSeller_id() {
        return seller_id;
    }

    public void setSeller_id(int seller_id) {
        this.seller_id = seller_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public PersonalInformation(int id, int seller_id, String name, String street, String houseNumber, String zipCode, String city) {
        this.id = id;
        this.seller_id = seller_id;
        this.name = name;
        this.street = street;
        this.houseNumber = houseNumber;
        this.zipCode = zipCode;
        this.city = city;

    }
}