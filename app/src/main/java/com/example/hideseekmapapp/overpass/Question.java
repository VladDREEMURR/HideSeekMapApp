package com.example.hideseekmapapp.overpass;

import java.util.ArrayList;

public interface Question {
    // подготовка исходных данных, полученных из конструктора классов, применяющих этот интерфейс
    void prepare();

    // выполняем нужные запросы в overpass
    void exec_overpass();

    // создание нужных областей
    void create_areas();

    // применение ответа на вопрос
    void apply_answer();
}
