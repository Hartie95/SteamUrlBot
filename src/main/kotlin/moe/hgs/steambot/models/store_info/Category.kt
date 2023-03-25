package moe.hgs.steambot.models.store_info

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: Int,
    val description: String
)
