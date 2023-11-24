package fr.polytech.model.aux;

import java.util.Date;
import java.util.UUID;

public class DetailedExperienceDTO {
    private UUID id;
    private String jobTitle;
    private JobCategoryDTO jobCategory;
    private Date startDate;
    private Date endDate;
    private CompanyDTO company;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public JobCategoryDTO getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(JobCategoryDTO jobCategory) {
        this.jobCategory = jobCategory;
    }

    public CompanyDTO getCompany() {
        return company;
    }

    public void setCompany(CompanyDTO company) {
        this.company = company;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

}
