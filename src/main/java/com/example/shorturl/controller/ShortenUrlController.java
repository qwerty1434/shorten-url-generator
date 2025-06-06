package com.example.shorturl.controller;

import com.example.shorturl.controller.response.ShortenUrlResponse;
import com.example.shorturl.service.ShortenUrlService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping
@RequiredArgsConstructor
public class ShortenUrlController {
    private final ShortenUrlService shortenUrlService;

    @PostMapping("/shorten")
    public ResponseEntity<String> shortenUrl(@RequestBody String oriUrl, HttpServletRequest request) {
        return ResponseEntity.ok(makeUrlFormat(shortenUrlService.shortenUrl(oriUrl), request));
    }

    @GetMapping("/original/{shortUrl}")
    public ResponseEntity<String> getOriginalUrl(@PathVariable("shortUrl") String shortUrl) {
        return ResponseEntity.ok(shortenUrlService.getOriginalUrlAndIncreaseRequestCount(shortUrl));
    }

    @GetMapping("/short-urls")
    public ResponseEntity<Page<ShortenUrlResponse>> getShortenUrls(Pageable pageable) {
        return ResponseEntity.ok(shortenUrlService.getAllShortenUrlWithPaging(pageable));
    }

    private String makeUrlFormat(String shortenUrl, HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() + "/" + shortenUrl;
    }

}
