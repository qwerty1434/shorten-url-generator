package com.example.shorturl.service;

import com.example.shorturl.entity.ShortenUrl;
import com.example.shorturl.repository.ShortenUrlRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
class ShortenUrlServiceTest {
    private static final String ALREADY_SAVED_ORI_URL = "ALREADY_SAVED_ORI_URL";
    private static final String ALREADY_SAVED_SHORT_URL = "ALREADY_SAVED_SHORT_URL";

    @Autowired
    private ShortenUrlService shortenUrlService;

    @Autowired
    private ShortenUrlRepository shortenUrlRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void init() {
        ShortenUrl shortenUrl = ShortenUrl.of(ALREADY_SAVED_ORI_URL, ALREADY_SAVED_SHORT_URL);

        shortenUrlRepository.save(shortenUrl);

        entityManager.flush();
        entityManager.clear();

    }

    @Test
    @DisplayName("단축 URL로 원본 URL을 조회하는 요청을 받은 경우 원본 URL을 응답한다")
    public void returnOriginalUrlWhenRequestedByShortUrl() {

        String oriUrl = shortenUrlService.getOriginalUrlAndIncreaseRequestCount(ALREADY_SAVED_SHORT_URL);

        assertThat(oriUrl).isEqualTo(ALREADY_SAVED_ORI_URL);

    }

    @Test
    @DisplayName("단축 URL로 원본 URL을 조회하는 요청을 받은 경우 단축 URL의 조회 횟수가 증가한다")
    public void increaseRequestCountWhenRequestedByShortUrl() {

        shortenUrlService.getOriginalUrlAndIncreaseRequestCount(ALREADY_SAVED_SHORT_URL);

        ShortenUrl shortenUrl = shortenUrlRepository.findByShortUrl(ALREADY_SAVED_SHORT_URL).get();
        assertThat(shortenUrl.getShortUrlRequestCount()).isEqualTo(1);

    }

    @Test
    @DisplayName("단축 URL로 원본 URL을 조회하는 요청 받았지만 해당 단축 URL에 대한 정보가 존재하지 않으면 예외를 발생시킨다")
    public void throwExceptionWhenShortUrlDoesNotExist() {
        String nonExistShortUrl = "NON_EXIST_SHORT_URL";

        assertThatThrownBy(() -> shortenUrlService.getOriginalUrlAndIncreaseRequestCount(nonExistShortUrl))
                .isInstanceOf(EntityNotFoundException.class);

    }

    @Test
    @DisplayName("동일한 단축 URL로 DB 저장을 시도하면 DataIntegrityViolationException 예외가 발생한다")
    public void throwExceptionWhenShortenUrlDuplicated() {
        assertThatThrownBy(() -> shortenUrlRepository.save(ShortenUrl.of(ALREADY_SAVED_ORI_URL, ALREADY_SAVED_SHORT_URL)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }


}