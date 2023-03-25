package moe.hgs.steambot.config

import kotlinx.serialization.json.Json
import java.io.File

object ConfigLoader {
    private const val configFile = "config.json"
    lateinit var botConfig: BotSettings
    private val serializer = Json { ignoreUnknownKeys = true }

    fun loadConfig() {
        val config = BotSettings.serializer()
        val file = File(configFile)
        if (!file.exists()) {
            file.createNewFile()
            file.writeText(Json.encodeToString(config, BotSettings("")))
        }
        val configText = file.readText()
        botConfig = serializer.decodeFromString(config, configText)
        if (botConfig.discordToken.isBlank()) {
            throw Exception("Please enter your discord token in the config file")
        }
        println("Loaded config")
    }
}