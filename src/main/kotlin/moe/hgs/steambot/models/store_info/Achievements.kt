package moe.hgs.steambot.models.store_info

import kotlinx.serialization.Serializable

@Serializable
data class Achievements(
    val total: Int,
    val highlighted: List<Achievement>
) {
    @Serializable
    data class Achievement(
        val name: String,
        val path: String
    )
}
