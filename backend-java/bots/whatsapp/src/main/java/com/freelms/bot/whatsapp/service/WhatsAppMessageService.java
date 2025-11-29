package com.freelms.bot.whatsapp.service;

import com.freelms.bot.whatsapp.config.TwilioConfig;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppMessageService {

    private final TwilioConfig twilioConfig;

    public void sendMessage(String to, String body) {
        try {
            if (twilioConfig.getAccountSid() == null || twilioConfig.getAuthToken() == null) {
                log.info("Demo mode - Would send to {}: {}", to, body);
                return;
            }

            String fromNumber = "whatsapp:" + twilioConfig.getWhatsappNumber();
            String toNumber = to.startsWith("whatsapp:") ? to : "whatsapp:" + to;

            Message message = Message.creator(
                    new PhoneNumber(toNumber),
                    new PhoneNumber(fromNumber),
                    body
            ).create();

            log.info("Message sent to {}, SID: {}", to, message.getSid());
        } catch (Exception e) {
            log.error("Error sending WhatsApp message to {}", to, e);
        }
    }
}
