package io.p13i.ra.models;

import java.util.Date;
import java.util.Objects;

/**
 * Represents all the contextual information surround a document
 */
public final class Context {
    public static Context NULL = new Context(null, null, null, null);

    private final String location;
    private final String person;
    private final String subject;
    private final Date date;

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

    @Override
    public int hashCode() {
        return Objects.hash(this.location, this.person, this.subject, this.date);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Context)) {
            return false;
        }

        Context other = (Context) obj;
        return Objects.equals(this.location, other.location) &&
                Objects.equals(this.person, other.person) &&
                Objects.equals(this.subject, other.subject) &&
                Objects.equals(this.date, other.date);
    }
}
