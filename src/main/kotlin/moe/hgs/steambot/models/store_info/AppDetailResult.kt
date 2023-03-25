package moe.hgs.steambot.models.store_info

import kotlinx.serialization.Serializable

@Serializable
data class AppDetailResult(
    val success: Boolean,
    val data: AppDetails
)
