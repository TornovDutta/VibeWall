package org.example.vibewall.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.vibewall.DTO.HelplineResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/support")
@Tag(name = "Crisis Support", description = "Confidential 24/7 crisis hotlines, student helplines, and mental wellness resources")
public class CrisisSupportController {

    private static final List<HelplineResource> HELPLINES = Arrays.asList(
            HelplineResource.builder()
                    .name("Crisis Text Line")
                    .contact("Text HOME to 741741")
                    .availability("24/7, Free & Confidential")
                    .description("Free text line connecting anyone with a trained crisis counselor.")
                    .region("US, UK, Canada")
                    .website("https://www.crisistextline.org")
                    .build(),
            HelplineResource.builder()
                    .name("988 Suicide & Crisis Lifeline")
                    .contact("Call or Text 988")
                    .availability("24/7, Free & Confidential")
                    .description("Immediate support for anyone experiencing mental health distress or suicidal crisis.")
                    .region("US & Canada")
                    .website("https://988lifeline.org")
                    .build(),
            HelplineResource.builder()
                    .name("iCall Psychosocial Helpline")
                    .contact("9152987821")
                    .availability("Mon - Sat, 8:00 AM - 10:00 PM")
                    .description("Free psychological counseling service run by the Tata Institute of Social Sciences.")
                    .region("India")
                    .website("https://icallhelpline.org")
                    .build(),
            HelplineResource.builder()
                    .name("Vandrevala Foundation")
                    .contact("+91 9999 666 555")
                    .availability("24/7, Free & Confidential")
                    .description("Mental health crisis support and counseling available round the clock.")
                    .region("India")
                    .website("https://www.vandrevalafoundation.com")
                    .build(),
            HelplineResource.builder()
                    .name("Samaritans")
                    .contact("Call 116 123")
                    .availability("24/7, Free to call")
                    .description("Non-judgmental emotional support for anyone in distress or struggling to cope.")
                    .region("UK & Ireland")
                    .website("https://www.samaritans.org")
                    .build(),
            HelplineResource.builder()
                    .name("Befrienders Worldwide")
                    .contact("Directory of 300+ Centers")
                    .availability("24/7 Global Network")
                    .description("Global network of listening services providing confidential suicide prevention.")
                    .region("Global (All Countries)")
                    .website("https://www.befrienders.org")
                    .build(),
            HelplineResource.builder()
                    .name("The Trevor Project (LGBTQ+ Youth)")
                    .contact("Call 1-866-488-7386 or Text START to 678-678")
                    .availability("24/7, Free & Confidential")
                    .description("Crisis intervention and suicide prevention services for LGBTQ young people.")
                    .region("Global / US")
                    .website("https://www.thetrevorproject.org")
                    .build()
    );

    @GetMapping("/helplines")
    @Operation(summary = "Get list of confidential 24/7 mental health and crisis helplines")
    public ResponseEntity<List<HelplineResource>> getHelplines() {
        return ResponseEntity.ok(HELPLINES);
    }
}
