package com.github.alefthallys.roombooking.messaging;

import com.github.alefthallys.roombooking.config.RabbitMQConfig;
import com.github.alefthallys.roombooking.dtos.Email.ReservationConfirmationEmailDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EmailMessageConsumer {
	
	@RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
	public void receiveReservationConfirmationEmail(ReservationConfirmationEmailDTO emailDTO) {
		log.info("Received reservation confirmation email for recipient: {}", emailDTO.recipientEmail());
		log.info("Sending email to {} for reservation ID {} for room '{}' from {} to {}",
				emailDTO.recipientEmail(),
				emailDTO.reservationId(),
				emailDTO.roomName(),
				emailDTO.startDate(),
				emailDTO.endDate());
		
		// Simulate email sending process (e.g., call an external email service)
		try {
			Thread.sleep(2000); // Simulate network delay or complex email processing
			log.info("Email successfully sent to {} for reservation ID {}", emailDTO.recipientEmail(), emailDTO.reservationId());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error("Email sending interrupted for reservation ID {}: {}", emailDTO.reservationId(), e.getMessage());
		}
	}
}
