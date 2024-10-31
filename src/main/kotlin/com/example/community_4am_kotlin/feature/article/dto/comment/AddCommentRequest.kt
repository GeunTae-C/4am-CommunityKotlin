package com.example.community_4am_kotlin.feature.article.dto.comment

import com.example.community_4am_kotlin.domain.article.Article
import com.example.community_4am_kotlin.domain.article.Comment

data class AddCommentRequest(
    val commentContent: String = "",
    val parentCommentId: Long? = null // 대댓글일 경우 부모 댓글의 ID
) {
    // Article과 Comment 객체를 이용해 Comment 엔티티 생성
    fun toEntity(commentAuthor: String, article: Article, parentComment: Comment): Comment {
        return Comment(
            commentAuthor = commentAuthor,
            commentContent = commentContent,
            article = article,
            parentComment = parentComment // parentComment는 대댓글일 경우에 사용됨
        )
    }
}