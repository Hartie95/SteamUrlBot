package moe.hgs.steambot.models.store_info

import kotlinx.serialization.Serializable

@Serializable
data class Movie(
    val id: Int,
    val name: String,
    val thumbnail: String,
    val webm: MovieFile?=null,
    val mp4: MovieFile?= null,
    val highlight: Boolean
) {

    @Serializable
    data class MovieFile(val max: String, val fallback: String?= null)
}
