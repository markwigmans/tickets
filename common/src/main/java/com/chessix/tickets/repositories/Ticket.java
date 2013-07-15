package com.chessix.tickets.repositories;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "c_dbId")
    private Integer dbId;

    @Column(name = "c_event", nullable = false, updatable = false)
    private String event;

    @Column(name = "c_number", nullable = false, updatable = false)
    private String number;

    /**
     * Needed by Hibernate
     */
    Ticket() {
        super();
    }

    public Ticket(final String event, final String number) {
        super();
        this.event = event;
        this.number = number;
    }

    public Integer getDbId() {
        return dbId;
    }

    void setDbId(Integer dbId) {
        this.dbId = dbId;
    }

    public String getEvent() {
        return event;
    }

    void setEvent(String event) {
        this.event = event;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

}
