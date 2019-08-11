package io.p13i.ra.models;

import java.util.Date;

/**
 * Represents all the contextual information surround a document
 */
public final class Context {
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

    public String getLocation() {
        return location;
    }

    public String getPerson() {
        return person;
    }

    public String getSubject() {
        return subject;
    }

    public Date getDate() {
        return date;
    }
}
