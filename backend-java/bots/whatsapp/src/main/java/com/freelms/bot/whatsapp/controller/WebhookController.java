package com.freelms.bot.whatsapp.controller;

import com.freelms.bot.whatsapp.handler.MessageHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final MessageHandler messageHandler;

    /**
     * Twilio webhook endpoint for incoming WhatsApp messages.
     * Configure this URL in Twilio Console: https://console.twilio.com
     */
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> handleIncomingMessage(
            @RequestParam("From") String from,
            @RequestParam("Body") String body,
            @RequestParam(value = "ProfileName", required = false) String profileName,
            @RequestParam(value = "MessageSid", required = false) String messageSid) {

        log.info("Received WhatsApp message from {} ({}): {}", from, profileName, body);

        try {
            messageHandler.handleMessage(from, body);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("Error handling message", e);
            return ResponseEntity.internalServerError().body("Error processing message");
        }
    }

    /**
     * Twilio webhook status callback for delivery confirmations.
     */
    @PostMapping(value = "/status", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> handleStatusCallback(
            @RequestParam("MessageSid") String messageSid,
            @RequestParam("MessageStatus") String messageStatus,
            @RequestParam(value = "ErrorCode", required = false) String errorCode) {

        log.info("Message {} status: {} (error: {})", messageSid, messageStatus, errorCode);

        return ResponseEntity.ok("OK");
    }

    /**
     * Health check endpoint for the webhook.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("WhatsApp Bot is running");
    }
}
