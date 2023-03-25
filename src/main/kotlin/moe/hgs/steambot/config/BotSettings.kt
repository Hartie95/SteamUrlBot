package moe.hgs.steambot.config

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class BotSettings(
    val discordToken: String,
    // used for some steam api requests, e.g. steam workshop information
    @EncodeDefault
    val steamApiKey: String? = null,
    @EncodeDefault
    val currency: String = "EUR"
)
