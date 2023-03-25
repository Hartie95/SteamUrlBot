package moe.hgs.steambot.models.store_info

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PriceOverview(
    val currency: String,
    val initial: Int,
    val final: Int,
    @SerialName("discount_percent")
    val discountPercent: Int,
    @SerialName("initial_formatted")
    val initialFormatted: String,
    @SerialName("final_formatted")
    val finalFormatted: String
)
