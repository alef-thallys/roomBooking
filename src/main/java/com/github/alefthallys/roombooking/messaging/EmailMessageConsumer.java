package com.github.alefthallys.roombooking.messaging;

import com.github.alefthallys.roombooking.config.RabbitMQConfig;
import com.github.alefthallys.roombooking.dtos.Email.ReservationConfirmationEmailDTO;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class EmailMessageConsumer {
	
	private final JavaMailSender mailSender;
	
	public EmailMessageConsumer(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}
	
	@RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
	public void receiveMessage(ReservationConfirmationEmailDTO emailDTO) {
		log.info("Received reservation confirmation email for recipient: {}", emailDTO.recipientEmail());
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			
			helper.setTo(emailDTO.recipientEmail());
			helper.setSubject("Reserva Confirmada - " + emailDTO.roomName());
			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
			String formattedStartDate = emailDTO.startDate().format(formatter);
			String formattedEndDate = emailDTO.endDate().format(formatter);
			
			String htmlContent = String.format("""
							<!DOCTYPE html>
							<html>
							<head>
							<style>
							    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
							    .container { max-width: 600px; margin: 20px auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px; background-color: #f9f9f9; }
							    .header { background-color: #007bff; color: white; padding: 10px 20px; border-radius: 8px 8px 0 0; text-align: center; }
							    .content { padding: 20px; }
							    .footer { text-align: center; font-size: 0.8em; color: #777; margin-top: 20px; padding-top: 10px; border-top: 1px solid #eee; }
							    .highlight { font-weight: bold; color: #007bff; }
							</style>
							</head>
							<body>
							    <div class="container">
							        <div class="header">
							            <h2>Confirmação de Reserva</h2>
							        </div>
							        <div class="content">
							            <p>Olá <span class="highlight">%s</span>,</p>
							            <p>Sua reserva para a sala '<span class="highlight">%s</span>' está confirmada com sucesso!</p>
							            <p>Detalhes da sua reserva:</p>
							            <ul>
							                <li><strong>Sala:</strong> %s</li>
							                <li><strong>Início:</strong> %s</li>
							                <li><strong>Fim:</strong> %s</li>
							                <li><strong>ID da Reserva:</strong> <span class="highlight">%d</span></li>
							            </ul>
							            <p>Agradecemos por usar nosso serviço.</p>
							        </div>
							        <div class="footer">
							            <p>&copy; 2025 RoomBooking. Todos os direitos reservados.</p>
							        </div>
							    </div>
							</body>
							</html>
							""",
					emailDTO.userName(),
					emailDTO.roomName(),
					emailDTO.roomName(),
					formattedStartDate,
					formattedEndDate,
					emailDTO.reservationId()
			);
			
			helper.setText(htmlContent, true);
			
			mailSender.send(message);
			log.info("HTML email successfully sent to {} for reservation ID {}", emailDTO.recipientEmail(), emailDTO.reservationId());
		} catch (Exception e) {
			log.error("Failed to send HTML email to {} for reservation ID {}", emailDTO.recipientEmail(), emailDTO.reservationId(), e);
		}
	}
}