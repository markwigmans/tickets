package com.chessix.tickets.messages;

/**
 * Response of the create Event request.
 * 
 * @author Mark Wigmans
 * 
 */
public class EventCreated {

    /**
     * Name of the event
     */
    private final String event;
    private final boolean successful;
    private final String message;

    public EventCreated(final String event) {
        this(event, true, "created");
    }

    public EventCreated(final String event, final boolean successful, final String message) {
        super();
        this.event = event;
        this.successful = successful;
        this.message = message;
    }

    public String getEvent() {
        return event;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getMessage() {
        return message;
    }
}
