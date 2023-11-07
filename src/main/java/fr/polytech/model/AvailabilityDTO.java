package fr.polytech.model;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class AvailabilityDTO {
    private UUID id;
    private String jobTitle;
    private UUID jobCategoryId;
    private Date startDate;
    private Date endDate;
    private List<String> placeList;

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

    public List<String> getPlaceList() {
        return placeList;
    }

    public void setPlaceList(List<String> placeList) {
        this.placeList = placeList;
    }
}
