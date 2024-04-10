# 학습용으로 만들어 본 게시판 서비스

이 프로젝트는 학습용으로 만들어보는 게시판 클론 코딩 프로젝트입니다. 

가장 기본적이고 보편적인 게시판 기능을 둘러볼 수 있습니다. 

스프링 부트와 관련 기술들, 자바 17 기능들, 개발 도구들을 경험할 수 있었습니다.

## 결과물

* https://project-board-rko.herokuapp.com

![초기화면](./src/main/resources/static/images/초기화면.png)

![게시물](./src/main/resources/static/images/게시물.png)

![해시태그](./src/main/resources/static/images/해시태그.png)

## 어플리케이션 아키텍처

![게시판아키텍처](./src/main/resources/static/images/게시판아키텍처.png)

## ERD
![ERD](src/main/resources/static/images/ERD.png)

## 개발 환경

* `IntelliJ`
* `Java 17`
* `Gradle`
* `git/github`

## 기술 스택

* `Spring Boot MVC`
* `Spring Data JPA`
* `Spring Security`
* `MySQL`
* `QueryDSL`
* `Thymleaf`
* `Bootstrap`
* `Heroku`

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
    actor User
    participant Client as Client
    participant KakaoAuthServer as Kakao Auth Server
    participant ResourceServer as Resource Server

    User->>Client: Login request
    Client->>KakaoAuthServer: Redirect to Kakao login page
    note over User,KakaoAuthServer: User authentication
    KakaoAuthServer->>User: Kakao login form
    User->>KakaoAuthServer: Provide authentication information (login)
    KakaoAuthServer->>Client: Return authorization code
    Client->>KakaoAuthServer: Request access token using authorization code
    KakaoAuthServer->>Client: Issue access token
    Client->>ResourceServer: Request resources using access token
    ResourceServer->>Client: Respond with protected resources
    Client->>User: Display requested data
```

- 게시글 작성
```mermaid
sequenceDiagram
    participant 클라이언트 as Client
    participant 컨트롤러 as ArticleController
    participant 아티클서비스 as ArticleService
    participant 아티클리포지토리 as ArticleRepository
    participant 해시태그서비스 as HashtagService
    participant 해시태그리포지토리 as HashtagRepository
    participant 데이터베이스 as Database

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
    participant 클라이언트 as Client
    participant 컨트롤러 as ArticleController
    participant 아티클서비스 as ArticleService
    participant 아티클리포지토리 as ArticleRepository
    participant 페이지네이션서비스 as PaginationService
    participant 데이터베이스 as Database

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
    participant 클라이언트 as Client
    participant 컨트롤러 as ArticleController
    participant 아티클서비스 as ArticleService
    participant 아티클리포지토리 as ArticleRepository
    participant 데이터베이스 as Database

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
    participant 클라이언트 as Client
    participant 컨트롤러 as ArticleController
    participant 아티클서비스 as ArticleService
    participant 아티클리포지토리 as ArticleRepository
    participant 페이지네이션서비스 as PaginationService
    participant 해시태그리포지토리 as HashtagRepository
    participant 데이터베이스 as Database

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
    participant 클라이언트 as Client
    participant 컨트롤러 as ArticleController
    participant 아티클서비스 as ArticleService
    participant 아티클리포지토리 as ArticleRepository
    participant 유저어카운트리포지토리 as UserAccountRepository
    participant 해시태그서비스 as HashtagService
    participant 해시태그리포지토리 as HashtagRepository
    participant 데이터베이스 as Database

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
    participant 클라이언트 as Client
    participant 컨트롤러 as ArticleController
    participant 아티클서비스 as ArticleService
    participant 아티클리포지토리 as ArticleRepository
    participant 해시태그서비스 as HashtagService
    participant 해시태그리포지토리 as HashtagRepository
    participant 데이터베이스 as Database
    
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
    participant 클라이언트 as Client
    participant 아티클코멘트컨트롤러 as ArticleCommentController
    participant 아티클코멘트서비스 as ArticleCommentService
    participant 아티클코멘트리포지토리 as ArticleCommentRepository
    participant 아티클리포지토리 as ArticleRepository
    participant 유저어카운트리포지토리 as UserAccountRepository
    participant 데이터베이스 as Database

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
    participant 클라이언트 as Client
    participant 컨트롤러 as ArticleCommentController
    participant 아티클코멘트서비스 as ArticleCommentService
    participant 아티클코멘트리포지토리 as ArticleCommentRepository
    participant 데이터베이스 as Database

    클라이언트->>+컨트롤러: POST /{commentId}/delete
    컨트롤러->>+아티클코멘트서비스: deleteArticleComment(commentId, username)
    아티클코멘트서비스->>+아티클코멘트리포지토리: deleteByIdAndUserAccount_UserId(commentId, userId)
    아티클코멘트리포지토리->>+데이터베이스: Delete Comment by ID and UserID
    데이터베이스-->>-아티클코멘트리포지토리: Confirmation
    아티클코멘트리포지토리-->>-아티클코멘트서비스: Confirmation
    아티클코멘트서비스-->>-컨트롤러: Confirmation
    컨트롤러-->>-클라이언트: redirect:/articles/{articleId}
```