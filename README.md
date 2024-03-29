# 학습용으로 만들어 본 게시판 서비스

가장 기본적이고 보편적인 게시판 기능을 둘러볼 수 있는 서비스. 스프링 부트와 관련 기술들, 자바 17 기능들, 개발 도구들을 경험할 수 있도록 만들어졌다.

## 데모 페이지

* https://project-board-rko.herokuapp.com

## 어플리케이션 아키텍처

![어플리케이션 아키텍처](./src/main/resources/static/images/Application%20Architecture.png)

## ERD
![ERD](src/main/resources/static/images/erd.png)

## 개발 환경

* Intellij IDEA Ultimate
* Java 17
* Gradle 7.4.1
* Spring Boot 2.7.0

## 기술 세부 스택

Spring Boot

* Spring Boot Actuator
* Spring Web
* Spring Data JPA
* Rest Repositories
* Rest Repositories HAL Explorer
* Thymleaf
* Spring Security
* H2 Database
* MySQL Driver
* Lombok
* Spring Boot DevTools
* Spring Configuration Processor

그 외

* QueryDSL 5.0.0
* Bootstrap 5.2.0-Beta1
* Heroku

## 주요 기능
- 카카오 OAuth 로그인
- 게시글 작성, 검색, 수정, 삭제
- 댓글 작성, 삭제
- 대댓글 작성, 삭제
- 해시태그 작성, 검색

## 시퀀스 다이어그램
- 카카오 OAuth 로그인
```mermaid
sequenceDiagram
    actor 사용자
    participant 클라이언트 as 클라이언트
    participant 카카오인증서버 as 카카오 인증 서버
    participant 리소스서버 as 리소스 서버

    사용자->>클라이언트: 로그인 요청
    클라이언트->>카카오인증서버: 카카오 로그인 페이지로 리다이렉트
    note over 사용자,카카오인증서버: 사용자 인증
    카카오인증서버->>사용자: 카카오 로그인 폼
    사용자->>카카오인증서버: 인증 정보 제공(로그인)
    카카오인증서버->>클라이언트: 인증 코드 반환
    클라이언트->>카카오인증서버: 인증 코드를 사용하여 액세스 토큰 요청
    카카오인증서버->>클라이언트: 액세스 토큰 발급
    클라이언트->>리소스서버: 액세스 토큰을 사용하여 리소스 요청
    리소스서버->>클라이언트: 보호된 리소스 응답
    클라이언트->>사용자: 요청된 데이터 표시
```

- 게시글 작성
```mermaid
sequenceDiagram
    participant 클라이언트
    participant 컨트롤러 as "Controller"
    participant 아티클서비스 as "ArticleService"
    participant 아티클리포지토리 as "ArticleRepository"
    participant 해시태그서비스 as "HashtagService"
    participant 해시태그리포지토리 as "HashtagRepository"
    participant 데이터베이스 as "Database"

    클라이언트->>+컨트롤러: POST /form (ArticleRequest)
    컨트롤러->>+아티클서비스: saveArticle(ArticleDto)
    아티클서비스->>+아티클리포지토리: getReferenceById(userId)
    아티클리포지토리->>+데이터베이스: Query User by ID
    데이터베이스-->>-아티클리포지토리: UserAccount
    아티클서비스->>+해시태그서비스: renewHashtagsFromContent(content)
    해시태그서비스->>해시태그서비스: parseHashtagNames(content)
    해시태그서비스->>+해시태그리포지토리: findByHashtagNameIn(hashtagNames)
    해시태그리포지토리->>+데이터베이스: Query Hashtags by Names
    데이터베이스-->>-해시태그리포지토리: Set<Hashtag>
    해시태그서비스-->>-아티클서비스: Set<Hashtag>
    아티클서비스->>+아티클리포지토리: save(Article)
    아티클리포지토리->>+데이터베이스: Save Article
    데이터베이스-->>-아티클리포지토리: Confirmation
    아티클리포지토리-->>-아티클서비스: Confirmation
    아티클서비스-->>-컨트롤러: Confirmation
    컨트롤러-->>-클라이언트: redirect:/articles
```

- 게시글 리스트 검색 및 조회 
```mermaid
sequenceDiagram
    participant 클라이언트
    participant 컨트롤러 as "Controller"
    participant 아티클서비스 as "ArticleService"
    participant 아티클리포지토리 as "ArticleRepository"
    participant 페이지네이션서비스 as "PaginationService"
    participant 데이터베이스 as "Database"

    클라이언트->>+컨트롤러: GET /articles (searchType, searchValue, pageable)
    컨트롤러->>+아티클서비스: searchArticles(searchType, searchValue, pageable)
    alt searchKeyword is null or blank
        아티클서비스->>+아티클리포지토리: findAll(pageable)
        아티클리포지토리->>+데이터베이스: Query All Articles
        데이터베이스-->>-아티클리포지토리: Page<ArticleDto>
    else search by Title, Content, ID, Nickname, or Hashtag
        아티클서비스->>+아티클리포지토리: findBy...(searchKeyword, pageable)
        아티클리포지토리->>+데이터베이스: Query Articles by Criteria
        데이터베이스-->>-아티클리포지토리: Page<ArticleDto>
    end
    아티클리포지토리-->>-아티클서비스: Page<ArticleDto>
    아티클서비스-->>-컨트롤러: Page<ArticleResponse>
    컨트롤러->>+페이지네이션서비스: getPaginationBarNumbers(currentPageNumber, totalPages)
    페이지네이션서비스-->>-컨트롤러: List<Integer>
    컨트롤러-->>-클라이언트: "articles/index" 페이지 및 데이터
```

- 게시글 단 건 조회
```mermaid
sequenceDiagram
    participant 클라이언트
    participant 컨트롤러 as "Controller"
    participant 아티클서비스 as "ArticleService"
    participant 아티클리포지토리 as "ArticleRepository"
    participant 데이터베이스 as "Database"

    클라이언트->>+컨트롤러: GET /{articleId}
    컨트롤러->>+아티클서비스: getArticleWithComments(articleId)
    아티클서비스->>+아티클리포지토리: findById(articleId)
    아티클리포지토리->>+데이터베이스: Query Article by ID
    데이터베이스-->>-아티클리포지토리: ArticleWithCommentsDto
    아티클리포지토리-->>-아티클서비스: ArticleWithCommentsDto
    아티클서비스-->>-컨트롤러: ArticleWithCommentsResponse
    컨트롤러->>+아티클서비스: getArticleCount()
    아티클서비스->>+아티클리포지토리: count()
    아티클리포지토리->>+데이터베이스: Count Articles
    데이터베이스-->>-아티클리포지토리: TotalCount
    아티클리포지토리-->>-아티클서비스: TotalCount
    아티클서비스-->>-컨트롤러: TotalCount
    컨트롤러-->>-클라이언트: "articles/detail" 페이지 및 데이터
```

- 해시태그 검색
```mermaid
sequenceDiagram
    participant 클라이언트
    participant 컨트롤러 as "Controller"
    participant 아티클서비스 as "ArticleService"
    participant 아티클리포지토리 as "ArticleRepository"
    participant 페이지네이션서비스 as "PaginationService"
    participant 해시태그리포지토리 as "HashtagRepository"
    participant 데이터베이스 as "Database"

    클라이언트->>+컨트롤러: GET /search-hashtag (searchValue, pageable)
    컨트롤러->>+아티클서비스: searchArticlesViaHashtag(searchValue, pageable)
    아티클서비스->>+아티클리포지토리: findByHashtagNames(hashtagName, pageable)
    아티클리포지토리->>+데이터베이스: Query Articles by Hashtag
    데이터베이스-->>-아티클리포지토리: Page<ArticleDto>
    아티클리포지토리-->>-아티클서비스: Page<ArticleDto>
    아티클서비스-->>-컨트롤러: Page<ArticleResponse>
    컨트롤러->>+페이지네이션서비스: getPaginationBarNumbers(pageNumber, totalPages)
    페이지네이션서비스-->>-컨트롤러: List<Integer>
    컨트롤러->>+아티클서비스: getHashtags()
    아티클서비스->>+해시태그리포지토리: findAllHashtagNames()
    해시태그리포지토리->>+데이터베이스: Query All Hashtag Names
    데이터베이스-->>-해시태그리포지토리: List<String>
    해시태그리포지토리-->>-아티클서비스: List<String>
    아티클서비스-->>-컨트롤러: List<String>
    컨트롤러-->>-클라이언트: "articles/search-hashtag" 페이지 및 데이터
```

- 게시글 수정
```mermaid
sequenceDiagram
    participant 클라이언트
    participant 컨트롤러 as "Controller"
    participant 아티클서비스 as "ArticleService"
    participant 아티클리포지토리 as "ArticleRepository"
    participant 유저어카운트리포지토리 as "UserAccountRepository"
    participant 해시태그서비스 as "HashtagService"
    participant 해시태그리포지토리 as "HashtagRepository"
    participant 데이터베이스 as "Database"

    클라이언트->>+컨트롤러: POST /{articleId}/form (ArticleRequest)
    컨트롤러->>+아티클서비스: updateArticle(articleId, ArticleDto)
    아티클서비스->>+아티클리포지토리: getReferenceById(articleId)
    아티클리포지토리->>+데이터베이스: Query Article by ID
    데이터베이스-->>-아티클리포지토리: Article
    아티클서비스->>+유저어카운트리포지토리: getReferenceById(userId)
    유저어카운트리포지토리->>+데이터베이스: Query User by ID
    데이터베이스-->>-유저어카운트리포지토리: UserAccount
    아티클서비스->>아티클리포지토리: Update Article
    아티클리포지토리->>+데이터베이스: Update Article in DB
    데이터베이스-->>-아티클리포지토리: Confirmation
    아티클서비스->>+해시태그서비스: deleteHashtagWithoutArticles(hashtagIds)
    해시태그서비스->>+해시태그리포지토리: findById(hashtagId)
    해시태그리포지토리->>+데이터베이스: Query Hashtag by ID
    데이터베이스-->>-해시태그리포지토리: Hashtag
    해시태그리포지토리->>+데이터베이스: Delete Hashtag
    데이터베이스-->>-해시태그리포지토리: Confirmation
    아티클서비스->>+해시태그리포지토리: saveAll(New Hashtags)
    해시태그리포지토리->>+데이터베이스: Update Hashtags in DB
    데이터베이스-->>-해시태그리포지토리: Confirmation
    아티클서비스-->>-컨트롤러: Confirmation
    컨트롤러-->>-클라이언트: redirect:/articles/{articleId}
```

- 게시글 삭제
```mermaid
sequenceDiagram
    participant 클라이언트
    participant 컨트롤러 as "Controller"
    participant 아티클서비스 as "ArticleService"
    participant 아티클리포지토리 as "ArticleRepository"
    participant 해시태그서비스 as "HashtagService"
    participant 해시태그리포지토리 as "HashtagRepository"
    participant 데이터베이스 as "Database"

    클라이언트->>+컨트롤러: POST /{articleId}/delete
    컨트롤러->>+아티클서비스: deleteArticle(articleId, username)
    아티클서비스->>+아티클리포지토리: getReferenceById(articleId)
    아티클리포지토리->>+데이터베이스: Query Article by ID
    데이터베이스-->>-아티클리포지토리: Article
    아티클서비스->>+아티클리포지토리: deleteByIdAndUserAccount_UserId(articleId, userId)
    아티클리포지토리->>아티클리포지토리: flush()
    아티클리포지토리->>+데이터베이스: Delete Article
    데이터베이스-->>-아티클리포지토리: Confirmation
    loop for each hashtagId
        아티클서비스->>+해시태그서비스: deleteHashtagWithoutArticles(hashtagIds)
        해시태그서비스->>+해시태그리포지토리: findById(hashtagId)
        해시태그리포지토리->>+데이터베이스: Query Hashtag by ID
        데이터베이스-->>-해시태그리포지토리: Hashtag
        해시태그리포지토리->>+데이터베이스: Delete Hashtag
        데이터베이스-->>-해시태그리포지토리: Confirmation
    end
    아티클서비스-->>-컨트롤러: Confirmation
    컨트롤러-->>-클라이언트: redirect:/articles
```

- 댓글 작성
```mermaid
sequenceDiagram
    participant 클라이언트
    participant 아티클코멘트컨트롤러 as "ArticleCommentController"
    participant 아티클코멘트서비스 as "ArticleCommentService"
    participant 아티클코멘트리포지토리 as "ArticleCommentRepository"
    participant 아티클리포지토리 as "ArticleRepository"
    participant 유저어카운트리포지토리 as "UserAccountRepository"
    participant 데이터베이스 as "Database"

    클라이언트->>+아티클코멘트컨트롤러: POST /new (ArticleCommentRequest)
    아티클코멘트컨트롤러->>+아티클코멘트서비스: saveArticleComment(ArticleCommentDto)
    아티클코멘트서비스->>+아티클리포지토리: getReferenceById(articleId)
    아티클리포지토리->>+데이터베이스: Query Article by ID
    데이터베이스-->>-아티클리포지토리: Article
    아티클코멘트서비스->>+유저어카운트리포지토리: getReferenceById(userId)
    유저어카운트리포지토리->>+데이터베이스: Query User by ID
    데이터베이스-->>-유저어카운트리포지토리: UserAccount
    alt If parentCommentId is not null
        아티클코멘트서비스->>+아티클코멘트리포지토리: getReferenceById(parentCommentId)
        아티클코멘트리포지토리->>+데이터베이스: Query Parent Comment by ID
        데이터베이스-->>-아티클코멘트리포지토리: ArticleComment
        아티클코멘트리포지토리->>아티클코멘트리포지토리: addChildComment(articleComment)
    else
        아티클코멘트서비스->>+아티클코멘트리포지토리: save(articleComment)
        아티클코멘트리포지토리->>+데이터베이스: Save Comment
        데이터베이스-->>-아티클코멘트리포지토리: Confirmation
    end
    아티클코멘트서비스-->>-아티클코멘트컨트롤러: Confirmation
    아티클코멘트컨트롤러-->>-클라이언트: redirect:/articles/{articleId}
```

- 댓글 삭제
```mermaid
sequenceDiagram
    participant 클라이언트
    participant 컨트롤러 as "ArticleCommentController"
    participant 아티클코멘트서비스 as "ArticleCommentService"
    participant 아티클코멘트리포지토리 as "ArticleCommentRepository"
    participant 데이터베이스 as "Database"

    클라이언트->>+컨트롤러: POST /{commentId}/delete
    컨트롤러->>+아티클코멘트서비스: deleteArticleComment(commentId, username)
    아티클코멘트서비스->>+아티클코멘트리포지토리: deleteByIdAndUserAccount_UserId(commentId, userId)
    아티클코멘트리포지토리->>+데이터베이스: Delete Comment by ID and UserID
    데이터베이스-->>-아티클코멘트리포지토리: Confirmation
    아티클코멘트리포지토리-->>-아티클코멘트서비스: Confirmation
    아티클코멘트서비스-->>-컨트롤러: Confirmation
    컨트롤러-->>-클라이언트: redirect:/articles/{articleId}
```