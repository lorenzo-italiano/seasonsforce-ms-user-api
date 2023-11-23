package fr.polytech.model.aux;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class DetailedAvailabilityDTO {

    private UUID id;
    private String jobTitle;
    private JobCategoryDTO jobCategory;
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

    public JobCategoryDTO getJobCategory() {
        return jobCategory;
    }

    public void setJobCategory(JobCategoryDTO jobCategory) {
        this.jobCategory = jobCategory;
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
