package com.example.community_4am_kotlin.feature.article.dto.comment

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class CommentResponse(
    var articleId:Long,
    var commentAuthor:String,
    var commentContent:String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    var commentCreatedAt: LocalDateTime,
    var commentId:Long,
    var parentCommentId:Long,
    var commentIsHidden:Boolean,
    var commentIsDeleted:Boolean
)