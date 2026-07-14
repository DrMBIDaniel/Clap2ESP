package com.clap2esp.app


data class SignalFeatures(

    // Максимальная амплитуда
    val peak: Int,


    // Средняя энергия сигнала
    val rms: Double,


    // Переходы через ноль
    val zeroCrossings: Int,


    // Скорость подъёма
    val attack: Int,


    // Скорость затухания
    val decay: Int,


    // Длина импульса
    val impulseWidth: Int,


    // Подготовка под FFT
    val highFrequencyRatio: Double = 0.0

)
