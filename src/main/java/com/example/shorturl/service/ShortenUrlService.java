package com.example.shorturl.service;

import com.example.shorturl.controller.response.ShortenUrlResponse;
import com.example.shorturl.domain.ShortenUrlGenerator;
import com.example.shorturl.entity.ShortenUrl;
import com.example.shorturl.repository.ShortenUrlRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShortenUrlService {
    private final ShortenUrlRepository shortenUrlRepository;
    private final ShortenUrlGenerator shortenUrlGenerator;

    private static final int MAX_RETRY_COUNT = 3;

    @Transactional
    public String shortenUrl(String oriUrl) {
        return shortenUrlRepository.findByOriUrl(oriUrl)
                .orElseGet(() -> attemptSaveShortenUrlWithRetries(oriUrl))
                .getShortUrl();
    }

    @Transactional
    public String getOriginalUrlAndIncreaseRequestCount(String shortUrl) {
        ShortenUrl url = shortenUrlRepository.findByShortUrl(shortUrl)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Shortened URL '%s' does not exist.", shortUrl)));
        url.increaseShortUrlRequestCount();
        return url.getOriUrl();
    }

    @Transactional(readOnly = true)
    public Page<ShortenUrlResponse> getAllShortenUrlWithPaging(Pageable pageable) {
        return shortenUrlRepository.findAll(pageable)
                .map(ShortenUrlResponse::from);
    }

    private ShortenUrl attemptSaveShortenUrlWithRetries(String oriUrl) {
        String shortenUrl = shortenUrlGenerator.generate(oriUrl);
        ShortenUrl url = ShortenUrl.of(oriUrl, shortenUrl);

        int retryCount = 0;

        while(retryCount <= MAX_RETRY_COUNT){
            try{
                return shortenUrlRepository.save(url);
            }catch(DataIntegrityViolationException e){
                String saltedOriUrl = saltingOriUrl(oriUrl);
                shortenUrl = shortenUrlGenerator.generate(saltedOriUrl);
                url = ShortenUrl.of(oriUrl, shortenUrl);
                retryCount++;
            }
        }

        throw new RuntimeException("Shorten Url Generator's Maximum retry count exceeded");

    }

    private String saltingOriUrl(String oriUrl) {
        return oriUrl + UUID.randomUUID();
    }
}
