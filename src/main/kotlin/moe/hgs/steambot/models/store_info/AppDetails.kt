package moe.hgs.steambot.models.store_info

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppDetails(
    val name: String,
    val type: String,
    @SerialName("steam_appid")
    val steamAppId: Int,
    @SerialName("header_image")
    val headerImage: String,
    val website: String,
    @SerialName("short_description")
    val shortDescription: String,
    @SerialName("detailed_description")
    val detailedDescription: String,
    @SerialName("about_the_game")
    val aboutTheGame: String,
    @SerialName("supported_languages")
    val supportedLanguages: String,
    val developers: List<String>,
    val publishers: List<String>,
    @SerialName("price_overview")
    val priceOverview: PriceOverview,
    val packages: List<Int>,
    val platforms: Platforms,
    val categories: List<Category>,
    val genres: List<Genre>,
    val screenshots: List<Screenshot>? = null,
    val movies: List<Movie>? = null,
    val recommendations: Recommendations? = null,
    val achievements: Achievements? = null,
    @SerialName("release_date")
    val releaseDate: ReleaseDate,
    val background: String
)
