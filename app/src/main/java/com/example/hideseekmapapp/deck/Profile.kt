package com.example.hideseekmapapp.deck



/*
TODO: функионал профиля
 * 1) добавление/удаление вопроса
 * 2) манипуляция колодой
 */
class Profile() {
    public lateinit var questions : Array<Question>
}



data class Question (
    // тип вопроса (для изменения удалить вопрос)
    val type : String, // "match", "measure", "thermometer", "tentacle", "photo"
    // координаты точки
    var pointX : String,
    var pointY : String,
    // ответ
    var answer : Int, // индекс ответа в списке возможных ответов
    // список вариантов ответа (оставить изменяемость для тентаклей)
    var answerList : Array<String>,
    // список параметров вопроса
    val paramList : Array<String>
)