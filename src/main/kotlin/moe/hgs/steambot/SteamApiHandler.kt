package moe.hgs.steambot

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import moe.hgs.steambot.config.ConfigLoader
import moe.hgs.steambot.models.store_info.AppDetailResult
import moe.hgs.steambot.models.store_info.AppDetails
import moe.hgs.steambot.models.workshop_info.PublishedFileDetails
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

object SteamApiHandler {
    val serializer = Json { ignoreUnknownKeys = true }
    fun getStoreInfo(appId: String, currency: String = "EUR"): AppDetails? {
        // get store info via  https://store.steampowered.com/api/appdetails?appids=&appId&currency=$currency
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://store.steampowered.com/api/appdetails?appids=$appId&currency=$currency"))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val responseBody = response.body()
        val config = AppDetailResult.serializer()
        val appDetailResponse = serializer.decodeFromString(JsonObject.serializer(), responseBody)
        if (!appDetailResponse.containsKey(appId))
            return null
        val appDetailResult = serializer.decodeFromJsonElement(config, appDetailResponse[appId]!!)
        if (!appDetailResult.success)
            return null
        return appDetailResult.data
    }

    fun getWorkshopItemInfo(
        itemId: String,
        includeChildren: Boolean = true,
        shortDescription: Boolean = true
    ): PublishedFileDetails? {
        // get workshop item info via https://api.steampowered.com/IPublishedFileService/GetDetails/v1/?publishedfileids[0]=$itemId&key=[steamtoken]&includechildren=true

        val steamToken = ConfigLoader.botConfig.steamApiKey
        steamToken ?: return null

        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.steampowered.com/IPublishedFileService/GetDetails/v1/?publishedfileids[0]=$itemId&includechildren=$includeChildren&short_description=$shortDescription&key=$steamToken"))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val responseBody = response.body()
        val fileDetailsResponse =
            serializer.decodeFromString(JsonObject.serializer(), responseBody)["response"]?.jsonObject
                ?.get("publishedfiledetails")?.jsonArray
                ?.get(0) ?: return null

        val config = PublishedFileDetails.serializer()
        return serializer.decodeFromJsonElement(config, fileDetailsResponse)
    }
}