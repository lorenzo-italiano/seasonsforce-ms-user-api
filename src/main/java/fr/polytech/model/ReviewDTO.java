package fr.polytech.model;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ReviewDTO {
    private UUID id;
    private Integer grade;
    private String message;
    private UUID senderId;
    private UUID offerId;
    private List<Response> responseList;
    private Date date;

    private static class Response {
        private UUID id;
        private String message;
        private UUID senderId;
        private Date date;

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public UUID getSenderId() {
            return senderId;
        }

        public void setSenderId(UUID senderId) {
            this.senderId = senderId;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        @Override
        public String toString() {
            return "Response{" +
                    "id=" + id +
                    ", message='" + message + '\'' +
                    ", senderId=" + senderId +
                    ", date=" + date +
                    '}';
        }
    }

    public ReviewDTO() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public UUID getOfferId() {
        return offerId;
    }

    public void setOfferId(UUID offerId) {
        this.offerId = offerId;
    }

    public List<Response> getResponseList() {
        return responseList;
    }

    public void setResponseList(List<Response> responseList) {
        this.responseList = responseList;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "ReviewDTO{" +
                "id=" + id +
                ", grade=" + grade +
                ", message='" + message + '\'' +
                ", senderId=" + senderId +
                ", offerId=" + offerId +
                ", responseList=" + responseList +
                ", date=" + date +
                '}';
    }
}
