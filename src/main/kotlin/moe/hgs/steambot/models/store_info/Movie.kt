package moe.hgs.steambot.models.store_info

import kotlinx.serialization.Serializable

@Serializable
data class Movie(
    val id: Int,
    val name: String,
    val thumbnail: String,
    val webm: MovieFile,
    val mp4: MovieFile,
    val highlight: Boolean
) {

    @Serializable
    data class MovieFile(val max: String, val fallback: String)
}
