# URL 단축기 구현

## 1. 요구사항
1. 원본 URL을 요청받아 8자 이내의 짧은 URL을 응답해야 한다
2. 짧은 URL은 유일해야 한다
3. 동일한 원본 URL을 요청하면 동일한 짧은 URL을 응답해야 한다
4. 짧은 URL을 요청받으면 원본 URL을 응답해야 한다
5. 짧은 URL의 요청 횟수를 기록해야 한다

## 2. 시스템 흐름
<img width="775" alt="image" src="https://github.com/user-attachments/assets/19c32a6c-b3f1-400b-88b6-0b207d38d27a" />

1. 원본 URL을 입력받는다
2. 요청 URL에 대한 짧은 URL이 이미 생성되어 DB에 저장되어 있는지 확인한다
- 2-1. DB에 저장되어 있다면 해당 값을 그대로 반환한다
- 2-2. DB에 저장되어 있지 않다면 URL단축기를 이용해 짧은 URL을 생성한다. 이후 `(원본 URL, 짧은 URL)` 값을 DB에 저장하고, 짧은 URL을 반환한다


## 3. URL 단축기 객체 생성
```java
@Component
public class ShortenUrlGenerator {
    private static final int URL_MAX_LENGTH = 8;

    public String generate(String oriUrl) {
        byte[] bytes = Sha256.hash(oriUrl); // SHA-256알고리즘으로 해싱한다
        return Base62.encrypt(bytes) // Base62로 인코딩한다
                .substring(0, URL_MAX_LENGTH); // 결과값을 8자리 값으로 자른다
    }

}
```
1. 원본 URL을 SHA-256알고리즘으로 해싱한다
2. Base62로 인코딩한다
3. 결과값을 8자리 값으로 자른다

### 확장성에 대해
SHA-256알고리즘과 Base62인코딩을 활용한 URL 단축기를 만들면서 `확장성을 고려해 해시 알고리즘과 인코딩 객체를 인터페이스로 선언하고 빈으로 주입받도록 설계함으로써 구현체를 갈아끼울 수 있도록 설계하는건 어떨까?` 라는 고민을 했었습니다.

```java
@Component
@RequiredArgsConstructor
public class ShortenUrlGenerator {
	private final Hash hash;
    private final Encoder encoder;

    public String generate(String oriUrl) {
    }
}
```
*	Hash와 Encoder는 인터페이스입니다. ShortenUrlGenerator는 스프링 컨테이너에 의해 Hash와 Encoder의 구현체 빈을 주입받아 사용하게 됩니다.
*	SHA-256알고리즘과 Base62인코딩이 아닌 다른 알고리즘과 인코딩을 사용하려 할 때 주입할 구현체만 바꿔주면 되기 때문에 코드의 변경을 최소화할 수 있습니다. 

이러한 설계가 좋아보이기도 하지만, 확장성 있는 설계 이전에 `여기에 확장성이 반드시 필요한가`에 대한 고민이 필요합니다. URL 단축기는 오히려 알고리즘과 인코딩 방식이 변경됐을 때 문제가 발생할 수 있습니다.

URL 단축기가 생성한 짧은 URL은 유일해야 합니다. 입력값이 다르다면 출력값 역시 달라야 합니다. 
> *	A -> f(x) -> B
> *	A' -> f(x) -> B'

하지만 짧은 URL을 생성하는 방식이 서로 다르다면 입력값이 달라도 출력값이 같아질 가능성이 존재합니다.
> *	A -> f(x) -> B
> *	A' -> g(x) -> B

이러한 이유로 URL 단축기는 고정된 알고리즘과 인코딩 방법을 사용하는 게 더 적합하다 판단했고, 알고리즘과 인코딩 방법을 확장 가능하게 설계하지 않았습니다.

### static과 abstract
```java
public abstract class Sha256 {}
```
```java
public abstract class Base62 {}
```
*	저는 SHA-256알고리즘과 Base62인코딩 객체를 모두 static abstract class로 정의했습니다.
*	두 객체 모두 내부에 상태를 가지지 않아 static class로 정의했고, 클래스의 인스턴스를 생성해 사용하는 걸 원치 않아 abstract class로 정의했습니다.



## 4. SHA-256알고리즘과 Base62인코딩
### SHA-256
*	어떤 입력값이든 256비트 길이의 해시값으로 변환하는 단방향 해시 알고리즘입니다.
*	SHA-256알고리즘은 256비트를 모두 16진수(0~f(15))값으로 표현합니다. 16진수는 하나의 값을 표현하기 위해 4비트((0)0000~(f)1111)를 사용하기 때문에, SHA-256알고리즘은 64(256/4)자리의 값을 반환합니다.
*	단방향 알고리즘으로 해시값을 통해 원래값을 찾는 건 불가능합니다.
	*	해싱 과정에서 역연산이 불가능한 연산(비트 쉬프트, XOR, 논리 연산 등)을 사용하기 때문에 공식을 통한 계산이 불가능합니다.
	*	어떤 값을 넣더라도 그 결과로 64자리의 값이 나온다는 건 압축 과정에서 정보의 손실이 발생한다는 의미입니다. 
    	*	64자리의 결과값만으로는 원래의 입력값이 몇자리의 글자인지를 유추하는 것조차 불가능합니다.
        *	정보의 손실이 발생하기 때문에 해시 충돌이 발생할 가능성이 존재합니다. 정보의 손실이 없다면 입력값이 달랐을 때 무조건 다른 결과값이 나와 충돌이 발생할 수 없습니다.
*	SHA-256 결과값의 경우의 수는 2^256 =  1.16 × 10⁷⁷으로 사실상 충돌이 불가능한 것으로 간주합니다.


#### 구현
```java
public abstract class Sha256 {
    private static final String ALGORITHM = "SHA-256";

    public static byte[] hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            return digest.digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error initializing SHA-256 Algorithm", e);
        }
    }

}
```

### Base62
*	Base62는 62개의 문자(숫자(10), 알파벳 소문자(26), 알파벳 대문자(26))로 데이터를 인코딩하는 방식입니다. Base62 인코딩은 10진수의 숫자를 62진수로 변환합니다. 
*	예시) 999999₍₁₀₎를 Base62로 인코딩해 4c91₍₆₂₎값을 얻는 과정
	* <img width="700" alt="image" src="https://github.com/user-attachments/assets/80494f9d-e678-45c7-b37b-ad8084e8a080" />

*	Base62와 Base64의 차이 : Base64는 Base62에 사용되는 62개의 문자 외에 `+`,`/`를 추가로 사용해 총 64개의 문자를 사용합니다. 이때 `/`는 URL에서 경로 구분자로 사용되기 때문에 URL단축기의 결과값으로 `/`가 포함되는 건 바람직하지 않아 Base62를 사용했습니다.

#### 구현
```java
public abstract class Base62 {
    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int POSITIVE_SIGNUM = 1;

    public static String encrypt(byte[] bytes) {
        BigInteger number = new BigInteger(POSITIVE_SIGNUM, bytes);
        StringBuilder sb = new StringBuilder();
        BigInteger base = BigInteger.valueOf(62);

        while (number.compareTo(BigInteger.ZERO) > 0) {
            int remainder = number.mod(base).intValue();
            sb.append(BASE62_CHARS.charAt(remainder));
            number = number.divide(base);
        }

        return sb.reverse().toString();
    }
}
```

## 5. 8자리 이내의 URL 만들기

Base62는 62^8이하의 정수가 입력으로 들어오면 8자리 이내의 값으로 인코딩하지만 그 이상의 값이 들어왔을 때는 8자리를 초과하는 값을 생성하게 됩니다.

우리가 SHA-256알고리즘으로 만든 256비트 크기의 값을 Base62로 인코딩했을 때는 8자리를 초과하는 값이 만들어 집니다.

따라서 8자리 이내의 URL을 만들기 위해서는 결과값을 자르는 작업(substring)이 필요하게 됩니다. 하지만 substring을 하게 되면 SHA-256알고리즘의 (사실상) 유일한 값을 이용하는게 아니게 되므로 중복된 값이 생성될 가능성이 높아지게 됩니다.

이러한 문제를 해결하는 방법은 크게 2가지가 있습니다.

### 5-1. SHA-256 해싱을 사용하지 않는다
간단하고 명확한 방법입니다. SHA-256으로 해싱하지 않고 62^8이하의 정수로 인코딩 한다면 8자리 이내의 URL을 만들어낼 수 있습니다.

이때 62^8이하의 정수로 사용하기 좋은 값으로 DB의 Auto Increment PK가 있습니다. 이 PK는 유일하기 때문에 이를 활용하면 Base62로 생성한 짧은 URL도 유일한 값이 됩니다. (Base62는 입력값이 다르면 항상 다른 출력값을 반환합니다)

꽤 좋은 방법처럼 보이지만 저는 이 방법을 선택하지 않았는데요. 그 이유는 생성되는 짧은 URL이 너무 규칙적이었기 때문입니다.

PK가 1인 첫번째 URL은 00000001, 그 다음 입력으로 들어온 URL은 00000002 이런 식으로 생성됩니다. 물론 충분히 많은 값이 생성됐을 때는 X02c1aA8과 같이 불규칙적인 값이 생성될 것이고, 단축 URL값의 규칙을 유추할 수 있다고 해서 큰 문제가 발생하는 건 아닙니다.

하지만 사용자 입장에서는 URL 단축기를 사용하면 그 결과로 불규칙적인 값을 받는게 더 직관적이고 익숙할 것이라 판단해 해당 방법을 사용하지 않았습니다.

또 다른 이유로 Auto Increment를 사용하는 방법에서 URL 단축기가 생성하는 값은 원본 URL값과 무관합니다. 사용자가 서비스에 `https://github.com/qwerty1434/` 라는 URL을 단축해 달라고 요청했지만, URL 단축기는 `https://github.com/qwerty1434/`라는 값은 사용하지 않고 Auto Increment 값을 Base62로 단축해 그 결과값을 반환합니다. 

이로 인해 동일한 URL에 대한 단축을 시도한다면 동일한 URL에 대해 다른 단축 URL을 반환할 위험이 존재합니다. (물론 시스템 전체 로직상 동일한 URL을 URL단축기에 여러번 전달할 일은 없습니다)

<img width="792" alt="image" src="https://github.com/user-attachments/assets/823b8c41-f0c4-4ca1-8698-45a1c82b44cf" />

> Auto-Increment값을 이용하는 방법은 URL 단축기가 동일한 원본 URL에 대한 동일한 짧은 URL의 반환을 보장하지 못합니다. 따라서 사용자의 동일한 요청에 대한 동일한 결과값은 URL 단축기가 아닌 ServiceLayer에서 DB를 조회하는 작업을 통해 보장해야 합니다.



### 5-2. 충돌이 발생하면 새로운 단축 URL 생성을 시도한다
substring으로 인한 중복 발생의 가능성을 인정하고, 중복이 발생하면 재생성을 시도하는 현실적인 방법입니다.

중복이 발생했다면 이전과는 다른 입력값을 넣어줘야 하는데요. 저는 원래의 입력값에 임의의 문자열을 덧붙이는 Salting이라는 기법을 활용했습니다.

```java
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
```
*	DB의 짧은 URL컬럼에 UNIQUE속성을 걸어뒀습니다. 저장을 시도하는데 짧은 URL이 UNIQUE하지 않다면(중복이 발생한다면) salting한 뒤 재시도합니다.
*	직접 조회쿼리를 이용해 중복된 짧은 URL이 존재하는지 확인하는 방법도 존재합니다. 하지만 중복이 발생할 가능성은 매우 낮은데 이를 피하기 위해 매번 조회를 실행하는 건 비효율적이라 판단해 직접 조회쿼리로 중복을 판단하는 방법을 선택하지 않았습니다.
