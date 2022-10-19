
rootProject.name = "rest-to-rest-proxy"
include("rest-to-rest-proxy-model","rest-to-ws-gw","ws-to-rest-agent")


pluginManagement {
    val quarkusPluginVersion: String by settings
    val quarkusPluginId: String by settings
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
    plugins {
        id(quarkusPluginId) version quarkusPluginVersion
    }
}
