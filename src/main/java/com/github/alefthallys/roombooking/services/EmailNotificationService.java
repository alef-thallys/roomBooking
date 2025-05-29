package com.github.alefthallys.roombooking.services;

import com.github.alefthallys.roombooking.config.RabbitMQConfig;
import com.github.alefthallys.roombooking.dtos.Email.ReservationConfirmationEmailDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailNotificationService {
	
	private final RabbitTemplate rabbitTemplate;
	
	public EmailNotificationService(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}
	
	public void sendReservationConfirmationEmail(ReservationConfirmationEmailDTO emailDTO) {
		log.info("Sending reservation confirmation email message for reservation ID: {}", emailDTO.reservationId());
		rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, emailDTO);
	}
}
