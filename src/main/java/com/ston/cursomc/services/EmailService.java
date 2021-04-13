package com.ston.cursomc.services;

import org.springframework.mail.SimpleMailMessage;

import com.ston.cursomc.domain.Pedido;

public interface EmailService {
	
	void sendOrderConfirmationEmail(Pedido pedido);
	
	void sendEmail(SimpleMailMessage msg);
}
