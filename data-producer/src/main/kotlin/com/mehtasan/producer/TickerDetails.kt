package com.mehtasan.producer

/**
 * @author mehtasan
 */
data class TickerDetails(
    val name: String,
    val sector: Sector,
    val price: Double
)

enum class Sector(val id: Int) {
    BANKING(0), AUTOMOBILE(1), CHEMICALS(2), FOOD(3), IT(4), MEDIA(5)
}