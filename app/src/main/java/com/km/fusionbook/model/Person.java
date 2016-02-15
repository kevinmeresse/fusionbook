package com.km.fusionbook.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Person extends RealmObject {

    public static final String EXTRA_PERSON_ID = "extra_person_id";

    @PrimaryKey
    private String id;
    private String firstname;
    private String lastname;
    private String pictureUrl;
    private long birthdate;
    private String mobilePhone;
    private String workPhone;
    private String email;
    private String addressStreet;
    private String addressCity;
    private String addressState;
    private String addressZipcode;
    private String addressCountry;
    private long createdAt;
    private long modifiedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public String getAddressZipcode() {
        return addressZipcode;
    }

    public void setAddressZipcode(String addressZipcode) {
        this.addressZipcode = addressZipcode;
    }

    public long getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(long birthdate) {
        this.birthdate = birthdate;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(long modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getWorkPhone() {
        return workPhone;
    }

    public void setWorkPhone(String workPhone) {
        this.workPhone = workPhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddressStreet() {
        return addressStreet;
    }

    public void setAddressStreet(String addressStreet) {
        this.addressStreet = addressStreet;
    }

    public String getAddressCity() {
        return addressCity;
    }

    public void setAddressCity(String addressCity) {
        this.addressCity = addressCity;
    }

    public String getAddressState() {
        return addressState;
    }

    public void setAddressState(String addressState) {
        this.addressState = addressState;
    }

    public String getAddressCountry() {
        return addressCountry;
    }

    public void setAddressCountry(String addressCountry) {
        this.addressCountry = addressCountry;
    }

    static public void addToRealm(Person personToAdd) {
        if (personToAdd != null && personToAdd.getId() != null) {
            // Get the default realm
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();

            // Check if person already in Realm
            Person person = null;
            try {
                person = realm
                        .where(Person.class)
                        .equalTo("id", personToAdd.getId())
                        .findFirst();
            } catch (Exception e) {
                // Unable to query local DB
                // Do nothing
            }

            // If person not already in Realm, create a new entry
            if (person == null) {
                person = realm.createObject(Person.class);
                person.setId(personToAdd.getId());
            }

            // Update person's details
            person.setFirstname(personToAdd.getFirstname());
            person.setLastname(personToAdd.getLastname());
            person.setPictureUrl(personToAdd.getPictureUrl());
            person.setBirthdate(personToAdd.getBirthdate());
            person.setMobilePhone(personToAdd.getMobilePhone());
            person.setWorkPhone(personToAdd.getWorkPhone());
            person.setEmail(personToAdd.getEmail());
            person.setAddressStreet(personToAdd.getAddressStreet());
            person.setAddressCity(personToAdd.getAddressCity());
            person.setAddressState(personToAdd.getAddressState());
            person.setAddressZipcode(personToAdd.getAddressZipcode());
            person.setAddressCountry(personToAdd.getAddressCountry());
            person.setCreatedAt(personToAdd.getCreatedAt());
            person.setModifiedAt(personToAdd.getModifiedAt());

            // Commit changes to Realm
            realm.commitTransaction();
        }
    }
}
