package com.example.shorturl.service;

import com.example.shorturl.domain.ShortenUrlGenerator;
import com.example.shorturl.entity.ShortenUrl;
import com.example.shorturl.repository.ShortenUrlRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;

@SpringBootTest
@Transactional
public class ShortenUrlServiceWithMockingTest {
    private static final String ALREADY_SAVED_ORI_URL = "ALREADY_SAVED_ORI_URL";
    private static final String ALREADY_SAVED_SHORT_URL = "ALREADY_SAVED_SHORT_URL";

    @Autowired
    private ShortenUrlService shortenUrlService;

    @MockitoBean
    private ShortenUrlGenerator shortenUrlGenerator;

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
    @DisplayName("새로운 URL의 단축을 요청받으면 단축 URL 정보를 DB에 저장하고 단축 URL 결과를 반환한다")
    public void saveAndReturnShortUrlWhenRequestedNewUrl() {
        String newUrl = "https://www.google.com";
        String shortenMockUrl = "SHORTEN_MOCK_URL";
        BDDMockito.given(shortenUrlGenerator.generate(newUrl)).willReturn(shortenMockUrl);
        shortenUrlService.shortenUrl(newUrl);

        entityManager.flush();
        entityManager.clear();

        Optional<ShortenUrl> savedUrl = shortenUrlRepository.findByOriUrl(newUrl);

        assertThat(savedUrl).isPresent();
        assertThat(savedUrl.get().getOriUrl()).isEqualTo(newUrl);
        assertThat(savedUrl.get().getShortUrl()).isEqualTo(shortenMockUrl);

    }

    @Test
    @DisplayName("이미 존재하는 URL의 단축을 요청받으면 단축기를 거치지 않고 DB에 저장된 단축 URL을 곧바로 반환한다")
    public void returnExistingShortUrlWithoutGeneration() {
        String generatorResult = "YOU_USED_SHORTEN_URL_GENERATOR";

        BDDMockito.given(shortenUrlGenerator.generate(ALREADY_SAVED_ORI_URL)).willReturn(generatorResult);

        String shortenUrl = shortenUrlService.shortenUrl(ALREADY_SAVED_ORI_URL);

        assertThat(shortenUrl).isEqualTo(ALREADY_SAVED_SHORT_URL);
        assertThat(shortenUrl).isNotEqualTo(generatorResult);

    }

    @Test
    @DisplayName("생성기로 생성한 단축 URL이 중복되면 원본 URL에 임의의 값을 추가해 새로운 단축 URL 생성을 시도한다")
    public void tryNewShortenUrlWhenDuplicated() {
        String oriUrl1 = "https://www.google.com";
        String oriUrl2 = "https://www.GOOGLE.com";

        String generatorResult = "SAME_SHORTEN_URL";
        BDDMockito.given(shortenUrlGenerator.generate(oriUrl1)).willReturn(generatorResult);
        BDDMockito.given(shortenUrlGenerator.generate(oriUrl2)).willReturn(generatorResult);
        shortenUrlService.shortenUrl(oriUrl1);
        shortenUrlService.shortenUrl(oriUrl2);

    }

    @Test
    @DisplayName("단축 URL 생성 재시도는 최대 재시도 횟수만큼만 시도한다")
    public void stopGenerateAfterMaximumRetries() {
        String newUrl = "https://www.google.com";

        BDDMockito.given(shortenUrlGenerator.generate(anyString())).willReturn(ALREADY_SAVED_SHORT_URL);
        assertThatThrownBy(() -> shortenUrlService.shortenUrl(newUrl))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Shorten Url Generator's Maximum retry count exceeded");

    }

}
