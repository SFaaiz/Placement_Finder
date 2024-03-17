package com.faaiz.placementfinder.Database;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.faaiz.placementfinder.Employer;
import com.faaiz.placementfinder.JobPost;
import com.faaiz.placementfinder.User;

import java.util.List;

@Dao
public interface DAO {

    @Insert
    void insertUser(User user);

    @Delete
    void deleteUser(User user);

    @Query("SELECT * FROM userTable LIMIT 1")
    User getUser();

    @Update
    void updateUser(User user);

    @Query("UPDATE userTable SET companyName = :companyName, jobTitle = :jobTitle, startDate = :startDate, endDate = :endDate, experienceDescription = :experienceDescription")
    void updateUserExperience(String companyName, String jobTitle, String startDate, String endDate, String experienceDescription);

    @Query("UPDATE userTable SET profilePhotoUrl = :imageUrl")
    void updateProfile(String imageUrl);

    @Query("UPDATE userTable SET name = :name, mobile = :mobile, location = :location")
    void updateUserPersonal(String name, String mobile, String location);

    @Query("UPDATE userTable SET degree = :degree, university = :university, field = :field, grade = :grade, year = :year")
    void updateUserEducation(String degree, String university, String field, String grade, String year);

    @Query("UPDATE userTable SET skills = :skills")
    void updateSkills(List<String> skills);

    @Query("UPDATE userTable SET projectTitle = :projectTitle, projectDescription = :projectDescription")
    void updateUserProject(String projectTitle, String projectDescription);


    // DAO methods for Employer entity
    @Insert
    void insertEmployer(Employer employer);

    @Delete
    void deleteEmployer(Employer employer);

    @Query("SELECT * FROM employerTable LIMIT 1")
    Employer getEmployer();

    @Update
    void updateEmployer(Employer employer);

    @Query("UPDATE employerTable SET name = :name, mobile = :mobile, companyName = :companyName, companyAddress = :companyAddress, companyDescription = :companyDescription")
    void updateEmpCompDetails(String name, String mobile, String companyName, String companyAddress, String companyDescription);

    // Jobs

    @Insert
    void insertJob(JobPost jobPost);

    @Query("SELECT * FROM jobs")
    List<JobPost> getAllJobPosts();
}
