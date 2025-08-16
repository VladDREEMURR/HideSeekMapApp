package com.example.hideseekmapapp.deck



/*
TODO: заполнить начальную колоду + функционал колоды
 * 1) взятие карт
 * 2) использование карт
 * 2.1) сброс при использовании карты
 * 2.2) поделиться фоткой карты при её использовании
 */
class Deck() {
    // начальная колода <Карта, Начальное количество>
    // пока комментируем, потом заполним колоду
    // public val initial_deck : Map<Card, Int>
    // колода <Карта, Количество>
    public lateinit var deck : MutableMap<Card, Int>
}



/*
TODO: посмотреть, как реализуется хранение фотки карты
 */
data class Card (
    /* Нет необходимости, будет изображение, параметры ниже для ленивых
    // название карты
    val name : String,
    // описание карты
    val description : String,
    */

    // val image : ImageView, ????

    // количество сбросов при использовании
    val discard_count : Int = 0, // по умолчанию = 0
    // тип сбрасываемых при использовании карт
    val discard_type : String = "any", // "any", "time", "powerup", "curse"

    // количество прибавляемых минут (для прибавок времени)
    val time_value : Int = 0
)