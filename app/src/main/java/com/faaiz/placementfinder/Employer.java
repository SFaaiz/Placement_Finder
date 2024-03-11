package com.faaiz.placementfinder;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "employerTable")
public class Employer {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "mobile")
    private String mobile;

    @ColumnInfo(name = "companyName")
    private String companyName;

    @ColumnInfo(name = "companyAddress")
    private String companyAddress;

    @ColumnInfo(name = "profilePhotoUrl")
    private String profilePhotoUrl;

    @ColumnInfo(name = "isEmployer")
    private boolean isEmployer;

    @ColumnInfo(name = "isMobileVerified")
    private boolean isMobileVerified;

    @ColumnInfo(name = "hasEnteredCompanyDetails")
    private boolean hasEnteredCompanyDetails;

    public Employer(String name, String email, String mobile, String companyName, String companyAddress, boolean isMobileVerified, boolean hasEnteredCompanyDetails) {
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.companyName = companyName;
        this.companyAddress = companyAddress;
        this.isEmployer = true;
        this.isMobileVerified = isMobileVerified;
        this.hasEnteredCompanyDetails = hasEnteredCompanyDetails;
    }

    public Employer(String name, String email, String mobile, boolean isMobileVerified, boolean hasEnteredCompanyDetails) {
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.isEmployer = true;
        this.isMobileVerified = isMobileVerified;
        this.hasEnteredCompanyDetails = hasEnteredCompanyDetails;
    }

    public Employer(){

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
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

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }

    public boolean isEmployer() {
        return isEmployer;
    }

    public void setEmployer(boolean employer) {
        isEmployer = employer;
    }

    public boolean isMobileVerified() {
        return isMobileVerified;
    }

    public void setMobileVerified(boolean mobileVerified) {
        isMobileVerified = mobileVerified;
    }

    public boolean isHasEnteredCompanyDetails() {
        return hasEnteredCompanyDetails;
    }

    public void setHasEnteredCompanyDetails(boolean hasEnteredCompanyDetails) {
        this.hasEnteredCompanyDetails = hasEnteredCompanyDetails;
    }
}
