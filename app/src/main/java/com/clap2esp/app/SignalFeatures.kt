package com.clap2esp.app

data class SignalFeatures(

    /*
     * Максимальная амплитуда
     */
    val peak: Int,

    /*
     * RMS (средняя энергия)
     */
    val rms: Double,

    /*
     * Peak / RMS
     * Один из лучших признаков хлопка.
     */
    val crestFactor: Double,

    /*
     * Количество переходов через ноль
     */
    val zeroCrossings: Int,

    /*
     * Скорость появления импульса
     */
    val attack: Int,

    /*
     * Скорость затухания
     */
    val decay: Int,

    /*
     * Реальная ширина импульса
     */
    val impulseWidth: Int,

    /*
     * Текущий уровень фонового шума
     */
    val noiseFloor: Double,

    /*
     * Доля высоких частот
     */
    val highFrequencyRatio: Double

)
