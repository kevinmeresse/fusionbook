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
    private long birthdate;
    private String zipcode;
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

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
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
            person.setBirthdate(personToAdd.getBirthdate());
            person.setZipcode(personToAdd.getZipcode());
            person.setCreatedAt(personToAdd.getCreatedAt());
            person.setModifiedAt(personToAdd.getModifiedAt());

            // Commit changes to Realm
            realm.commitTransaction();
        }
    }
}
