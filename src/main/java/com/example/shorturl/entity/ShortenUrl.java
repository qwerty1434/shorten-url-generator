package com.example.shorturl.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "shorten_url")
@NoArgsConstructor
public class ShortenUrl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String oriUrl;

    @Column(unique = true)
    private String shortUrl;

    private long shortUrlRequestCount;


    @Builder
    private ShortenUrl(String oriUrl, String shortUrl) {
        this.oriUrl = oriUrl;
        this.shortUrl = shortUrl;
    }

    public void increaseShortUrlRequestCount() {
        this.shortUrlRequestCount++;
    }

    public static ShortenUrl of(String oriUrl, String shortUrl) {
        return ShortenUrl.builder()
                .oriUrl(oriUrl)
                .shortUrl(shortUrl)
                .build();
    }
}

