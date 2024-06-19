package com.funky.funkyjobs.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class EmailScheduler {

    private final RestTemplate restTemplate;

    @Autowired
    public EmailScheduler(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Define the scheduled task to call the sendEmail endpoint
    @Scheduled(cron = "${scheduler.email.cron}")
    public void sendEmailJob() {
        String urlSaveExcel = "http://FUNKY-SERVICE:8082/funky/save-excel"; // Change URL as per your application
        restTemplate.postForObject(urlSaveExcel, null, String.class);
        String url = "http://FUNKY-SERVICE:8082/sendEmail"; // Change URL as per your application
        restTemplate.postForObject(url, null, String.class);
    }
}

