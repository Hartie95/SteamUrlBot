package moe.hgs.steambot.command_handlers

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.command.ApplicationCommandOption
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.ApplicationCommandRequest
import moe.hgs.steambot.*
import moe.hgs.steambot.models.store_info.AppDetails
import moe.hgs.steambot.models.store_info.Platforms
import moe.hgs.steambot.models.workshop_info.PublishedFileDetails
import org.reactivestreams.Publisher
import java.net.URI

class SteamUriCommandHandler : CommandHandler("steamUriCommand.json") {

    override fun handleCommand(event: ChatInputInteractionEvent): Publisher<*> {
        val commandInteraction = event.interaction.commandInteraction.get()
        val url = commandInteraction.getOption("url")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .orElse("")
        val uri = URI.create(url)

        val parsedUrl = URLParser.parseSteamUrl(uri)
        return replyCustom(event, parsedUrl)
    }


    enum class UrlGoal {
        OPEN_BROWSER,
        OPEN_STEAM,
        START_GAME_STEAM,
        INSTALL_GAME_STEAM
    }

    fun getWorkshopUrls(workshopID: String): Map<UrlGoal, String> {
        return mapOf(
            UrlGoal.OPEN_BROWSER to "https://steamcommunity.com/sharedfiles/filedetails/?id=$workshopID",
            UrlGoal.OPEN_STEAM to "steam://url/CommunityFilePage/$workshopID"
        )
    }

    fun getStoreUrls(appId: String): Map<UrlGoal, String> {
        return mapOf(
            UrlGoal.OPEN_BROWSER to "https://store.steampowered.com/app/$appId/",
            UrlGoal.OPEN_STEAM to "steam://store/$appId",
            UrlGoal.START_GAME_STEAM to "steam://run/$appId/",
            UrlGoal.INSTALL_GAME_STEAM to "steam://install/$appId/"
        )
    }

    fun getPlatformsString(platforms: Platforms): String {
        val platformsList = mutableListOf<String>()
        if (platforms.windows) platformsList.add("Windows")
        if (platforms.mac) platformsList.add("Mac")
        if (platforms.linux) platformsList.add("Linux")
        return platformsList.joinToString(", ")
    }

    fun buildStoreEmbed(appDetails: AppDetails, outputUrls: Map<UrlGoal, String>): EmbedCreateSpec {
        val builder = EmbedCreateSpec.builder()
            .title(appDetails.name)
            .description(appDetails.shortDescription)
            .image(appDetails.headerImage)
            .addField("Publishers", appDetails.publishers.joinToString(", "), true)
            .addField("Developers", appDetails.developers.joinToString(", "), true)
            .addField("Release Date", appDetails.releaseDate.date, true)
            .addField("Price", appDetails.priceOverview.finalFormatted, true)
            .addField("Platforms", getPlatformsString(appDetails.platforms), true)

        appDetails.recommendations?.let {
            builder.addField("Recommendations", it.total.toString(), true)
        }

        val mainUrl = outputUrls[UrlGoal.OPEN_BROWSER]
        val storeSteam = outputUrls[UrlGoal.OPEN_STEAM]
        mainUrl?.let {
            var storeFieldText = "[browser]($it)"
            storeSteam?.let { storeUri ->
                storeFieldText += " | steam client <$storeUri>"
            }
            builder.addField("Show in store", storeFieldText, false)
        }

        outputUrls[UrlGoal.START_GAME_STEAM]?.let { builder.addField("Run with Steam", "<${it}>", false) }
        outputUrls[UrlGoal.INSTALL_GAME_STEAM]?.let { builder.addField("Install with Steam", "<${it}>", false) }

        return builder.build()
    }

    fun buildWorkshopEmbed(itemDetails: PublishedFileDetails, outputUrls: Map<UrlGoal, String>): EmbedCreateSpec {
        val builder = EmbedCreateSpec.builder()
            .title(itemDetails.title)
            .description(itemDetails.shortDescription ?: "")
            .image(itemDetails.previewUrl)
            .addField("Game", itemDetails.appName, true)
            .addField("Subscriptions", itemDetails.subscriptions.toString(), true)

        val mainUrl = outputUrls[UrlGoal.OPEN_BROWSER]
        val steamUrl = outputUrls[UrlGoal.OPEN_STEAM]
        mainUrl?.let {
            var storeFieldText = "[browser]($it)"
            steamUrl?.let { storeUri ->
                storeFieldText += " | steam client <$storeUri>"
            }
            builder.addField("Show in workshop", storeFieldText, false)
        }
        return builder.build()
    }

    fun replyCustom(
        event: ChatInputInteractionEvent,
        parsedUrl: URLParser.ParsedUrl
    ): InteractionApplicationCommandCallbackReplyMono {
        try {
            val embed = when (parsedUrl.urlType) {
                URLParser.SteamUrlType.STORE -> {
                    val urls = getStoreUrls(parsedUrl.appId)
                    val storeInfo = SteamApiHandler.getStoreInfo(parsedUrl.appId)
                    storeInfo ?: return replyFallback(urls, event)
                    buildStoreEmbed(storeInfo, urls)
                }

                URLParser.SteamUrlType.WORKSHOP -> {
                    val urls = getWorkshopUrls(parsedUrl.appId)
                    val workshopInfo = SteamApiHandler.getWorkshopItemInfo(parsedUrl.appId)
                    workshopInfo ?: return replyFallback(urls, event)
                    buildWorkshopEmbed(workshopInfo, urls)
                }

                else -> {
                    return event.reply("unknown url type")
                }
            }
            return event.reply()
                .withEmbeds(embed)
        } catch (e: Exception) {
            e.printStackTrace()
            return event.reply("error: ${e.message}")
        }
    }

    fun replyFallback(
        outputUrls: Map<UrlGoal, String>,
        event: ChatInputInteractionEvent
    ): InteractionApplicationCommandCallbackReplyMono {
        var main = ""
        outputUrls.forEach { key ->
            main += "${key.value}\n"
            if (key.key == UrlGoal.OPEN_BROWSER) {
                println("${key.key} ${key.value}")
            }
        }
        return event.reply(main)
    }
}