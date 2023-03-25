package moe.hgs.steambot

import discord4j.core.DiscordClient
import discord4j.core.event.ReactiveEventAdapter
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.`object`.command.ApplicationCommandOption
import discord4j.core.spec.EmbedCreateSpec
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono
import discord4j.discordjson.json.ApplicationCommandOptionData
import discord4j.discordjson.json.ApplicationCommandRequest
import moe.hgs.steambot.config.ConfigLoader
import moe.hgs.steambot.models.store_info.AppDetails
import moe.hgs.steambot.models.store_info.Platforms
import moe.hgs.steambot.models.workshop_info.PublishedFileDetails
import org.reactivestreams.Publisher
import java.net.URI

// store rest api: https://wiki.teamfortress.com/wiki/User:RJackson/StorefrontAPI#Known_methods
// user stats api: https://developer.valvesoftware.com/wiki/Steam_Web_API#GetNewsForApp_.28v0001.29
// steam browser protocol: https://developer.valvesoftware.com/wiki/Steam_browser_protocol
// discord applications: https://discord.com/developers/applications
// discord4j embeds doku: https://docs.discord4j.com/embeds
fun getGetParametersMap(query: String): Map<String, String> {
    return query.split("&").associate {
        val (left, right) = it.split("=")
        left to right
    }
}

fun getWorkshopUrls(workshopID: String): Map<String, String> {
    return mapOf(
        "main" to "https://steamcommunity.com/sharedfiles/filedetails/?id=$workshopID",
        "open" to "steam://url/CommunityFilePage/$workshopID"
    )
}

fun getStoreUrls(appId: String): Map<String, String> {
    return mapOf(
        "main" to "https://store.steampowered.com/app/$appId/",
        "open" to "steam://store/$appId",
        "start" to "steam://run/$appId/",
        "install" to "steam://install/$appId/"
    )
}

fun main() {
    ConfigLoader.loadConfig()
    val client = DiscordClient.create(ConfigLoader.botConfig.discordToken)
        .login()
        .block()

    if (client == null) {
        println("Failed to get client, token wrong?")
        return
    }

    val urlCommand = ApplicationCommandRequest.builder()
        .name("steam")
        .description("Posts the link as web and steam urls")
        .addOption(
            ApplicationCommandOptionData.builder()
                .name("url")
                .description("url of the store or workshop content")
                .type(ApplicationCommandOption.Type.STRING.value)
                .required(true)
                .build()
        )
        .build()
    val testUrlCommand = ApplicationCommandRequest.builder()
        .name("test")
        .description("Posts the link as web and steam urls with test features")
        .addOption(
            ApplicationCommandOptionData.builder()
                .name("url")
                .description("url of the store or workshop content")
                .type(ApplicationCommandOption.Type.STRING.value)
                .required(true)
                .build()
        )
        .build()

    val serverCommand = ApplicationCommandRequest.builder()
        .name("steamserver")
        .description("Posts the link as web and steam urls")
        .addOption(
            ApplicationCommandOptionData.builder()
                .name("address_port")
                .description("server address:port")
                .type(ApplicationCommandOption.Type.STRING.value)
                .required(true)
                .build()
        )
        .addOption(
            ApplicationCommandOptionData.builder()
                .name("password")
                .description("server password")
                .type(ApplicationCommandOption.Type.STRING.value)
                .required(false)
                .build()
        )
        .build()
    val restClient = client.restClient
    val applicationID = restClient.applicationId.block()

    applicationID?.let {
        restClient.applicationService
            .createGlobalApplicationCommand(it, urlCommand)
            .doOnError { println("Unable to create url command") }
            .block()
        restClient.applicationService
            .createGlobalApplicationCommand(it, testUrlCommand)
            .doOnError { println("Unable to create server command") }
            .block()
        restClient.applicationService
            .createGlobalApplicationCommand(it, serverCommand)
            .doOnError { println("Unable to create server command") }
            .block()
    }
    client.on(object : ReactiveEventAdapter() {
        override fun onChatInputInteraction(event: ChatInputInteractionEvent): Publisher<*> {
            return when (event.commandName) {
                "steam" -> {
                    handleUriCommand(event, false)
                }

                "test" -> {
                    handleUriCommand(event, true)
                }

                "steamserver" -> {
                    handleServerCommand(event)
                }

                else -> {
                    event.reply("unknown command")
                }
            }
        }
    }).blockLast()

    client.applicationInfo.block()
}

enum class SteamUrlType {
    WORKSHOP,
    STORE,
    UNKNOWN
}

fun handleUriCommand(event: ChatInputInteractionEvent, testFeatures: Boolean): Publisher<*> {
    val commandInteraction = event.interaction.commandInteraction.get()
    val url = commandInteraction.getOption("url")
        .flatMap(ApplicationCommandInteractionOption::getValue)
        .map(ApplicationCommandInteractionOptionValue::asString)
        .orElse("")
    val uri = URI.create(url)
    var id = "0"
    var urlType = SteamUrlType.UNKNOWN
    /* steam workshop
        web:   https://steamcommunity.com/sharedfiles/filedetails/?id=485936923
        steam: steam://url/CommunityFilePage/485936923

        steam store
        web:     https://store.steampowered.com/app/1118310/RetroArch/
        open:    steam://store/<id>
        launch:  steam://run/<id>//<args>/
        install: steam://install/<id>

        add friend
        add: steam://friends/add/<id>
    */
    var outputUrls: Map<String, String> = mapOf("main" to "Unknown")
    val path = uri.path.split("/")
    when (uri.scheme) {
        "steam" -> {
            when (uri.host) {
                "url" -> {
                    if (path.size > 2 && path[1] == "CommunityFilePage") {
                        id = path[2]
                        urlType = SteamUrlType.WORKSHOP
                        outputUrls = getWorkshopUrls(path[2])
                    }
                }

                "store" -> {
                    if (path.size > 1) {
                        id = path[1]
                        urlType = SteamUrlType.STORE
                        outputUrls = getStoreUrls(path[1])
                    }
                }

                "run" -> {
                    if (path.size > 1) {
                        id = path[1]
                        urlType = SteamUrlType.STORE
                        outputUrls = getStoreUrls(path[1])
                    }
                }

                else -> {
                    return event.reply("unknown url type")
                }
            }
        }

        "https" -> {
            when (uri.host) {
                "steamcommunity.com" -> {
                    if (path.size > 2 && (path[1] == "sharedfiles" || path[1] == "workshop") &&
                        path[2] == "filedetails"
                    ) {
                        val getParams = getGetParametersMap(uri.query)
                        getParams["id"]?.let {
                            id = it
                            urlType = SteamUrlType.WORKSHOP
                            outputUrls = getWorkshopUrls(it)
                        }
                    }
                }

                "store.steampowered.com" -> {
                    if (path.size > 2 && path[1] == "app") {
                        id = path[2]
                        urlType = SteamUrlType.STORE
                        outputUrls = getStoreUrls(id)
                    }
                }

                else -> {
                    return event.reply("unknown url type")
                }
            }
        }

        else -> {
            return event.reply("unknown url type")
        }
    }

    return if (testFeatures) {
        replyCustom(outputUrls, event, id, urlType)
    } else {
        reply(outputUrls, event)
    }
}

fun reply(
    outputUrls: Map<String, String>,
    event: ChatInputInteractionEvent
): InteractionApplicationCommandCallbackReplyMono {
    var main = ""
    outputUrls.forEach { key ->
        main += "${key.value}\n"
        if (key.key == "open") {
            println("${key.key} ${key.value}")
        }
    }
    return event.reply(main)
}

fun getPlatformsString(platforms: Platforms): String {
    val platformsList = mutableListOf<String>()
    if (platforms.windows) platformsList.add("Windows")
    if (platforms.mac) platformsList.add("Mac")
    if (platforms.linux) platformsList.add("Linux")
    return platformsList.joinToString(", ")
}

fun buildStoreEmbed(appDetails: AppDetails, outputUrls: Map<String, String>): EmbedCreateSpec {
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

    val mainUrl = outputUrls["main"]
    val storeSteam = outputUrls["open"]
    mainUrl?.let {
        var storeFieldText = "[browser]($it)"
        storeSteam?.let { storeUri ->
            storeFieldText += " | steam client <$storeUri>"
        }
        builder.addField("Show in store", storeFieldText, false)
    }

    outputUrls["start"]?.let { builder.addField("Run with Steam", "<${it}>", false) }
    outputUrls["install"]?.let { builder.addField("Install with Steam", "<${it}>", false) }

    return builder.build()
}

fun buildWorkshopEmbed(itemDetails: PublishedFileDetails, outputUrls: Map<String, String>): EmbedCreateSpec {
    val builder = EmbedCreateSpec.builder()
        .title(itemDetails.title)
        .description(itemDetails.shortDescription ?: "")
        .image(itemDetails.previewUrl)
        .addField("Game", itemDetails.appName, true)
        .addField("Subscriptions", itemDetails.subscriptions.toString(), true)

    val mainUrl = outputUrls["main"]
    val steamUrl = outputUrls["open"]
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
    outputUrls: Map<String, String>,
    event: ChatInputInteractionEvent,
    appIdString: String,
    urlType: SteamUrlType
): InteractionApplicationCommandCallbackReplyMono {
    val embed = when (urlType) {
        SteamUrlType.STORE -> {
            val storeInfo = SteamApiHandler.getStoreInfo(appIdString)
            storeInfo ?: return reply(outputUrls, event)
            buildStoreEmbed(storeInfo, outputUrls)
        }

        SteamUrlType.WORKSHOP -> {
            val workshopInfo = SteamApiHandler.getWorkshopItemInfo(appIdString)
            workshopInfo ?: return reply(outputUrls, event)
            buildWorkshopEmbed(workshopInfo, outputUrls)
        }

        else -> {
            return reply(outputUrls, event)
        }
    }

    return event.reply()
        .withEmbeds(embed)
}


fun handleServerCommand(event: ChatInputInteractionEvent): Publisher<*> {
    val commandInteraction = event.interaction.commandInteraction.get()
    val address = commandInteraction.getOption("address_port")
        .flatMap(ApplicationCommandInteractionOption::getValue)
        .map(ApplicationCommandInteractionOptionValue::asString)
        .orElse("")
    val password = commandInteraction.getOption("password")
        .flatMap(ApplicationCommandInteractionOption::getValue)
        .map(ApplicationCommandInteractionOptionValue::asString)
        .orElse("")

    /* steam workshop
        connect to server:
        steam://connect/<IP or DNS name>[:<port>][/<password>]
    */
    var url = "steam://connect/$address"
    if (password.isNotEmpty()) {
        url += "/$password"
    }

    return event.reply(url)
}
