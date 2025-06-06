package com.example.shorturl.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ShortenUrlGeneratorTest {
    @Autowired
    private ShortenUrlGenerator shortenUrlGenerator;

    @ParameterizedTest(name = "원본URL : {0}")
    @ValueSource(strings = {
            "https://www.google.com"
            , "https://www.google.com/search?q=google"
            , "https://www.long-url/ontheotherhandwedenouncewithrighteousindignationanddislikemenwhoaresobeguiledanddemoralizedbythecharmsofpleasureofthemomentsoblindedbydesirethattheycannotforeseethepainandtroublethatareboundtoensueandequalblamebelongstothosewhofailintheirdutythroughweaknessofwillwhichisthesameassayingthroughshrinkingfromtoilandpainthesecasesareperfectlysimpleandeasytodistinguishinafreehourwhenourpowerofchoiceisuntrammelledandwhennothingpreventsourbeingabletodowhatwelikebesteverypleasureistobewelcomedandeverypainavoidedbutincertaincircumstancesandowingtotheclaimsofdutyor theobligationsofbusinessitwillfrequentlyoccurthatpleasureshavetoberepudiatedandannoyancesacceptedthewisemanthereforealwaysholdsinthesematterstothisprincipleofselectionherejectspleasurestosecureothergreaterpleasuresorelseheendurespainstoavoidworsepains"
    })
    @DisplayName("URL단축기가 생성한 단축 URL은 8자 이하다")
    public void shortenUrlLengthIsLessThanOrEqualToEight(String oriUrl){
        String shortenUrl = shortenUrlGenerator.generate(oriUrl);

        assertThat(shortenUrl.length()).isLessThanOrEqualTo(8);
    }

    @Test
    @DisplayName("원본 URL이 다르면 생성된 단축 URL도 다르다")
    public void generatesDifferentShortUrlForDifferentOriginalUrl() {
        String oriUrl = "https://www.google.com";
        String oriUrl2 = "https://www.Google.com";

        String shortenUrl1 = shortenUrlGenerator.generate(oriUrl);
        String shortenUrl2 = shortenUrlGenerator.generate(oriUrl2);

        assertThat(shortenUrl1).isNotEqualTo(shortenUrl2);
    }

    @Test
    @DisplayName("원본 URL이 같으면 생성된 단축 URL도 같다")
    public void generatesSameShortUrlForSameOriginalUrl() {
        String oriUrl = "https://www.google.com";

        String shortenUrl1 = shortenUrlGenerator.generate(oriUrl);
        String shortenUrl2 = shortenUrlGenerator.generate(oriUrl);

        assertThat(shortenUrl1).isEqualTo(shortenUrl2);
    }



}