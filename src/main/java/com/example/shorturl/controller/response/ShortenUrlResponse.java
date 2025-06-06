package com.example.shorturl.controller.response;

import com.example.shorturl.entity.ShortenUrl;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortenUrlResponse {
    private Long id;

    private String oriUrl;

    private String shortUrl;

    private long shortUrlRequestCount;

    public static ShortenUrlResponse from(ShortenUrl shortenUrl) {
        return ShortenUrlResponse.builder()
                .id(shortenUrl.getId())
                .oriUrl(shortenUrl.getOriUrl())
                .shortUrl(shortenUrl.getShortUrl())
                .shortUrlRequestCount(shortenUrl.getShortUrlRequestCount())
                .build();
    }
}
