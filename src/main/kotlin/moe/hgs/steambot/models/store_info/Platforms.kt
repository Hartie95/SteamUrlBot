package moe.hgs.steambot.models.store_info

import kotlinx.serialization.Serializable

@Serializable
data class Platforms(
    val windows: Boolean,
    val mac: Boolean,
    val linux: Boolean
)
