package com.example.hideseekmapapp.overpass;

public interface Question {
    // выполняем нужные запросы в overpass
    void exec_overpass();

    // создание нужных областей
    void create_areas();

    // применение ответа на вопрос
    void generate_answer(double x, double y);
}
