package com.chessix.tickets.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Mark Wigmans
 */
public interface TicketRepository extends JpaRepository<Ticket, Long> {
     
}
