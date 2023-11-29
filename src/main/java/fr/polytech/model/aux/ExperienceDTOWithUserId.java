package fr.polytech.model.aux;

import java.util.Date;
import java.util.UUID;

public class ExperienceDTOWithUserId {
    private UUID id;
    private UUID userId;
    private String jobTitle;
    private UUID jobCategoryId;
    private Date startDate;
    private Date endDate;
    private UUID companyId;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public UUID getJobCategoryId() {
        return jobCategoryId;
    }

    public void setJobCategoryId(UUID jobCategoryId) {
        this.jobCategoryId = jobCategoryId;
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

    public UUID getCompanyId() {
        return companyId;
    }

    public void setCompanyId(UUID companyId) {
        this.companyId = companyId;
    }
}


