package com.chessix.tickets.model;

/**
 * Represents a ticket to be sold
 * 
 * @author Mark Wigmans
 * 
 */
public class Ticket {

    private String event;
    private int number;

    public Ticket(String event, int number) {
        super();
        this.event = event;
        this.number = number;
    }

    public String getEvent() {
        return event;
    }

    void setEvent(String event) {
        this.event = event;
    }

    public int getNumber() {
        return number;
    }

    void setNumber(int number) {
        this.number = number;
    }

}
