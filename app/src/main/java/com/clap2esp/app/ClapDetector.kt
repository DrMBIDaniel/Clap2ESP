package com.clap2esp.app

import kotlin.math.abs


enum class ClapType {
    NONE,
    SINGLE_CLAP,
    DOUBLE_CLAP
}



class ClapDetector {


    private val threshold = 12000


    // Минимальный промежуток между двумя хлопками
    private val minDoubleDelay = 120L


    // Максимальный промежуток между двумя хлопками
    private val maxDoubleDelay = 800L


    private var lastDetectionTime = 0L


    private var waitingForSecondClap = false


    private var firstClapTime = 0L



    fun detect(buffer: ShortArray): ClapType {


        var maxAmplitude = 0


        for (sample in buffer) {

            val amplitude = abs(sample.toInt())


            if (amplitude > maxAmplitude) {
                maxAmplitude = amplitude
            }

        }



        val currentTime = System.currentTimeMillis()



        if (maxAmplitude < threshold) {
            return ClapType.NONE
        }



        if (currentTime - lastDetectionTime < 250L) {
            return ClapType.NONE
        }



        lastDetectionTime = currentTime



        /*
        Первый хлопок.
        Запоминаем и ждём второй.
        */

        if (!waitingForSecondClap) {


            waitingForSecondClap = true


            firstClapTime = currentTime


            Logger.log(
                "First clap detected amplitude=$maxAmplitude"
            )


            return ClapType.NONE

        }




        /*
        Второй хлопок.
        Проверяем интервал.
        */


        val delay = currentTime - firstClapTime



        if (
            delay >= minDoubleDelay &&
            delay <= maxDoubleDelay
        ) {


            waitingForSecondClap = false


            Logger.log(
                "DOUBLE CLAP delay=${delay}ms"
            )


            return ClapType.DOUBLE_CLAP

        }



        /*
        Слишком поздний второй хлопок.
        Новый хлопок становится первым.
        */


        firstClapTime = currentTime


        return ClapType.NONE

    }





    fun checkSingleClapTimeout(): ClapType {


        if (
            waitingForSecondClap &&
            System.currentTimeMillis() - firstClapTime > maxDoubleDelay
        ) {


            waitingForSecondClap = false


            Logger.log(
                "SINGLE CLAP"
            )


            return ClapType.SINGLE_CLAP

        }


        return ClapType.NONE

    }

}
