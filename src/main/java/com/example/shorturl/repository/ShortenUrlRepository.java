package com.example.shorturl.repository;

import com.example.shorturl.entity.ShortenUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ShortenUrlRepository extends JpaRepository<ShortenUrl,Long> {

    Optional<ShortenUrl> findByOriUrl(String oriUrl);

    Optional<ShortenUrl> findByShortUrl(String shortUrl);

}
