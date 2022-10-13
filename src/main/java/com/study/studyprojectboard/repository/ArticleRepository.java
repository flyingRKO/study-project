package com.study.studyprojectboard.repository;

import com.study.studyprojectboard.domain.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {
}