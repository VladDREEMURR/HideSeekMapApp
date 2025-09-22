package com.example.hideseekmapapp.overpass;

import java.util.ArrayList;

public interface Question {
    void prepare();
    void exec_overpass();
    void create_areas();
    void apply_answer();
    QuestionResult return_result();
}


class QuestionResult {
    public ArrayList <Polygon> area;
    public boolean inside;
}
