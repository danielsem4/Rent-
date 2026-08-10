package org.example.rent

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform