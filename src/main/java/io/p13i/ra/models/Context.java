package io.p13i.ra.models;

import java.util.Date;

public class Context {
    private String location;
    private String person;
    private String subject;
    private Date date;

    public Context(String location, String person, String subject, Date date) {
        this.location = location;
        this.person = person;
        this.subject = subject;
        this.date = date;
    }

    public static Context of(Document document) {
        return new Context(null, null, null, null);
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
