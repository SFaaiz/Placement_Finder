package com.faaiz.placementfinder;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "jobs")
public class JobPost {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "role_to_hire")
    private String roleToHire;

    @ColumnInfo(name = "city")
    private String city;

    @ColumnInfo(name = "skills_required")
    private List<String> skillsRequired;

    @ColumnInfo(name = "min_salary")
    private String minSalary;

    @ColumnInfo(name = "max_salary")
    private String maxSalary;

    @ColumnInfo(name = "min_experience")
    private String minExperience;

    @ColumnInfo(name = "min_qualification")
    private String minQualification;

    @ColumnInfo(name = "job_description")
    private String jobDescription;

    public JobPost(){

    }

    public JobPost(String roleToHire, String city, List<String> skillsRequired, String minSalary, String maxSalary, String minExperience, String minQualification, String jobDescription) {
        this.roleToHire = roleToHire;
        this.city = city;
        this.skillsRequired = skillsRequired;
        this.minSalary = minSalary;
        this.maxSalary = maxSalary;
        this.minExperience = minExperience;
        this.minQualification = minQualification;
        this.jobDescription = jobDescription;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRoleToHire() {
        return roleToHire;
    }

    public void setRoleToHire(String roleToHire) {
        this.roleToHire = roleToHire;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public List<String> getSkillsRequired() {
        return skillsRequired;
    }

    public void setSkillsRequired(List<String> skillsRequired) {
        this.skillsRequired = skillsRequired;
    }

    public String getMinSalary() {
        return minSalary;
    }

    public void setMinSalary(String minSalary) {
        this.minSalary = minSalary;
    }

    public String getMaxSalary() {
        return maxSalary;
    }

    public void setMaxSalary(String maxSalary) {
        this.maxSalary = maxSalary;
    }

    public String getMinExperience() {
        return minExperience;
    }

    public void setMinExperience(String minExperience) {
        this.minExperience = minExperience;
    }

    public String getMinQualification() {
        return minQualification;
    }

    public void setMinQualification(String minQualification) {
        this.minQualification = minQualification;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }
}
