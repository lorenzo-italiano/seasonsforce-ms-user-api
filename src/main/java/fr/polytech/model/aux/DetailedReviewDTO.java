package fr.polytech.model.aux;

import fr.polytech.model.response.user.BaseUserResponse;
import fr.polytech.model.response.user.RecruiterUserResponse;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class DetailedReviewDTO {

    private UUID id;
    private int grade;
    private String message;
    private BaseUserResponse sender;
    private List<Response> responseList;
    private Date date;
    private OfferDTO offer;

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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BaseUserResponse getSender() {
        return sender;
    }

    public void setSender(BaseUserResponse sender) {
        this.sender = sender;
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

    public OfferDTO getOffer() {
        return offer;
    }

    public void setOffer(OfferDTO offer) {
        this.offer = offer;
    }
}
