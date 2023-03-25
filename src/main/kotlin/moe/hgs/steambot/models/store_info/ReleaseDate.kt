package moe.hgs.steambot.models.store_info

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReleaseDate(
    @SerialName("coming_soon")
    val comingSoon: Boolean,
    val date: String
)
