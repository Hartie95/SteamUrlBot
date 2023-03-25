package moe.hgs.steambot.models.workshop_info

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PublishedFileDetails(
    @SerialName("publishedfileid")
    val publishedFileId: String,
    @SerialName("creator")
    val creatorId: String,
    @SerialName("creator_appid")
    val creatorAppId: Int,
    @SerialName("consumer_appid")
    val consumerAppId: Int,
    @SerialName("filename")
    val fileName: String,
    @SerialName("file_size")
    val fileSize: String,
    @SerialName("preview_file_size")
    val previewFileSize: String,
    @SerialName("preview_url")
    val previewUrl: String,
    val url: String,
    val title: String,
    @SerialName("file_description")
    val fileDescription: String? = null,
    @SerialName("short_description")
    val shortDescription: String? = null,
    @SerialName("time_created")
    val timeCreated: Int,
    @SerialName("time_updated")
    val timeUpdated: Int,
    val visibility: Int,
    val flags: Int,
    @SerialName("workshop_file")
    val workshopFile: Boolean,
    @SerialName("workshop_accepted")
    val workshopAccepted: Boolean,
    @SerialName("show_subscribe_all")
    val showSubscribeAll: Boolean,
    @SerialName("num_comments_public")
    val numCommentsPublic: Int,
    val banned: Boolean,
    @SerialName("ban_reason")
    val banReason: String,
    val banner: String,
    @SerialName("can_be_deleted")
    val canBeDeleted: Boolean,
    @SerialName("app_name")
    val appName: String,
    @SerialName("file_type")
    val fileType: Int,
    @SerialName("can_subscribe")
    val canSubscribe: Boolean,
    val subscriptions: Int,
    val favorited: Int,
    val followers: Int,
    @SerialName("lifetime_subscriptions")
    val lifetimeSubscriptions: Int,
    @SerialName("lifetime_favorited")
    val lifetimeFavorited: Int,
    @SerialName("lifetime_followers")
    val lifetimeFollowers: Int,
    @SerialName("lifetime_playtime")
    val lifetimePlaytime: String,
    @SerialName("lifetime_playtime_sessions")
    val lifetimePlaytimeSessions: String,
    val views: Int,
    @SerialName("num_children")
    val numChildren: Int,
    @SerialName("num_reports")
    val numReports: Int,
    @SerialName("tags")
    val tags: List<Tag>,
    @SerialName("children")
    val children: List<Child>? = null,
    @SerialName("language")
    val language: Int,
    @SerialName("maybe_inappropriate_sex")
    val maybeInappropriateSex: Boolean,
    @SerialName("maybe_inappropriate_violence")
    val maybeInappropriateViolence: Boolean,
    @SerialName("revision_change_number")
    val revisionChangeNumber: String,
    @SerialName("revision")
    val revision: Int,
    @SerialName("ban_text_check_result")
    val banTextCheckResult: Int,


    ) {
    @Serializable
    data class Tag(
        val tag: String,
        @SerialName("display_name")
        val displayName: String
    )

    @Serializable
    data class Child(
        @SerialName("publishedfileid")
        val publishedFileId: String,
        @SerialName("sort_order")
        val sortOrder: Int,
        @SerialName("file_type")
        val fileType: Int
    )
}
