package moe.hgs.steambot.models.store_info

import kotlinx.serialization.Serializable

@Serializable
data class Screenshot(
    val id: Int,
    val path_thumbnail: String,
    val path_full: String
)
