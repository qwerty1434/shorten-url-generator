package com.example.shorturl.domain;

import com.example.shorturl.domain.encoding.Base62;
import com.example.shorturl.domain.hash.Sha256;
import org.springframework.stereotype.Component;

@Component
public class ShortenUrlGenerator {
    private static final int URL_MAX_LENGTH = 8;

    public String generate(String oriUrl) {
        byte[] bytes = Sha256.hash(oriUrl);
        return Base62.encrypt(bytes)
                .substring(0, URL_MAX_LENGTH);
    }

}
