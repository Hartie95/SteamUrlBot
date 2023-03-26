package moe.hgs.steambot

import discord4j.core.DiscordClient
import discord4j.core.event.ReactiveEventAdapter
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import moe.hgs.steambot.command_handlers.ServerCommandHandler
import moe.hgs.steambot.command_handlers.SteamUriCommandHandler
import moe.hgs.steambot.config.ConfigLoader
import org.reactivestreams.Publisher

// store rest api: https://wiki.teamfortress.com/wiki/User:RJackson/StorefrontAPI#Known_methods
// user stats api: https://developer.valvesoftware.com/wiki/Steam_Web_API#GetNewsForApp_.28v0001.29
// steam browser protocol: https://developer.valvesoftware.com/wiki/Steam_browser_protocol
// discord applications: https://discord.com/developers/applications
// discord4j embeds doku: https://docs.discord4j.com/embeds


fun main() {
    val client = DiscordClient.create(ConfigLoader.botConfig.discordToken)
        .login()
        .block()

    if (client == null) {
        println("Failed to get client, token wrong?")
        return
    }

    val urlCommand = SteamUriCommandHandler()

    val serverCommand = ServerCommandHandler()
    val restClient = client.restClient
    val applicationID = restClient.applicationId.block()

    val commands = mapOf(serverCommand.commandRequest.name() to serverCommand,
        urlCommand.commandRequest.name() to urlCommand)

    applicationID?.let {
        commands.forEach{ command ->
            restClient.applicationService
                .createGlobalApplicationCommand(it, command.value.commandRequest)
                .doOnError { println("Unable to create ${command.key} command") }
                .block()
        }
    }
    client.on(object : ReactiveEventAdapter() {
        override fun onChatInputInteraction(event: ChatInputInteractionEvent): Publisher<*> {
            commands.forEach{
                if (event.commandName == it.key) {
                    return it.value.handleCommand(event)
                }
            }
            return event.reply("unknown command")
        }
    }).blockLast()

    client.applicationInfo.block()
}



