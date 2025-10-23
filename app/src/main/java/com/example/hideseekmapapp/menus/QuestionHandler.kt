package com.example.hideseekmapapp.menus

import android.widget.GridLayout
import android.widget.LinearLayout

import com.example.hideseekmapapp.overpass.Question

class QuestionHandler {
    // список вопросов
    public var questions : Array<Question> = arrayOf()

    // интерфейс
    public lateinit var ui_question_list : LinearLayout
    // интерфейс данных вопроса
    public lateinit var ui_question_tablet : GridLayout





    constructor (_ui_question_list : LinearLayout) {
        ui_question_list = _ui_question_list
    }





    public fun create_new_question () {

    }
}