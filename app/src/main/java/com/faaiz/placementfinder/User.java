package com.faaiz.placementfinder;

public class User {
    String name, mobile, email, university, degree, field, location, profilePhotoUrl;
    boolean hasEnteredPersonalDetails, isEmployer;

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public boolean isHasEnteredPersonalDetails() {
        return hasEnteredPersonalDetails;
    }

    public void setHasEnteredPersonalDetails(boolean hasEnteredPersonalDetails) {
        this.hasEnteredPersonalDetails = hasEnteredPersonalDetails;
    }

    public User() {
    }

    public User(String name, String mobile, String email, String university, String degree, String field, String location, boolean hasEnteredPersonalDetails) {
        this.name = name;
        this.mobile = mobile;
        this.email = email;
        this.university = university;
        this.degree = degree;
        this.field = field;
        this.location = location;
        this.hasEnteredPersonalDetails = hasEnteredPersonalDetails;
        this.isEmployer = false;
    }

    public User(String name, String mobile, String email, boolean hasEnteredPersonalDetails) {
        this.name = name;
        this.mobile = mobile;
        this.email = email;
        this.hasEnteredPersonalDetails = hasEnteredPersonalDetails;
        isEmployer = false;
    }

    public User(String name, String mobile, String email) {
        this.name = name;
        this.mobile = mobile;
        this.email = email;
        isEmployer = false;
    }

    public boolean isEmployer() {
        return isEmployer;
    }

    public void setEmployer(boolean employer) {
        isEmployer = employer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
