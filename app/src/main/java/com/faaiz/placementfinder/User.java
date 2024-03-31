package com.faaiz.placementfinder;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;

@Entity(tableName = "userTable")
public class User implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "mobile")
    private String mobile;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "university")
    private String university;

    @ColumnInfo(name = "degree")
    private String degree;

    @ColumnInfo(name = "field")
    private String field;

    @ColumnInfo(name = "location")
    private String location;

    @ColumnInfo(name = "profilePhotoUrl")
    private String profilePhotoUrl;

    @ColumnInfo(name = "grade")
    private String grade;

    @ColumnInfo(name = "year")
    private String year;

    @ColumnInfo(name = "hasEnteredPersonalDetails")
    private boolean hasEnteredPersonalDetails;

    @ColumnInfo(name = "isEmployer")
    private boolean isEmployer;

    @ColumnInfo(name = "companyName")
    private String companyName;

    @ColumnInfo(name = "jobTitle")
    private String jobTitle;
    @ColumnInfo(name = "startDate")
    private String startDate;

    @ColumnInfo(name = "endDate")
    private String endDate;

    @ColumnInfo(name = "experienceDescription")
    private String experienceDescription;

    @ColumnInfo(name = "projectTitle")
    private String projectTitle;

    @ColumnInfo(name = "projectDescription")
    private String projectDescription;

    @ColumnInfo(name = "skills")
    private List<String> skills;

    @ColumnInfo(name = "savedJobs")
    private List<String> savedJobs;

    @ColumnInfo(name = "appliedJobs")
    private List<String> appliedJobs;

    public List<String> getSavedJobs() {
        return savedJobs;
    }

    public void setSavedJobs(List<String> savedJobs) {
        this.savedJobs = savedJobs;
    }

    public List<String> getAppliedJobs() {
        return appliedJobs;
    }

    public void setAppliedJobs(List<String> appliedJobs) {
        this.appliedJobs = appliedJobs;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getExperienceDescription() {
        return experienceDescription;
    }

    public void setExperienceDescription(String experienceDescription) {
        this.experienceDescription = experienceDescription;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

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

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", mobile='" + mobile + '\'' +
                ", email='" + email + '\'' +
                ", university='" + university + '\'' +
                ", degree='" + degree + '\'' +
                ", field='" + field + '\'' +
                ", location='" + location + '\'' +
                ", profilePhotoUrl='" + profilePhotoUrl + '\'' +
                ", grade='" + grade + '\'' +
                ", year='" + year + '\'' +
                ", hasEnteredPersonalDetails=" + hasEnteredPersonalDetails +
                ", isEmployer=" + isEmployer +
                ", companyName='" + companyName + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", experienceDescription='" + experienceDescription + '\'' +
                ", projectTitle='" + projectTitle + '\'' +
                ", projectDescription='" + projectDescription + '\'' +
                ", skills=" + skills +
                '}';
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
