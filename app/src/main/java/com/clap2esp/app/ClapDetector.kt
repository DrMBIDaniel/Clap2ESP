package com.clap2esp.app


enum class ClapType {
    NONE,
    SINGLE_CLAP,
    DOUBLE_CLAP
}



class ClapDetector {


    private val history = SignalHistory(12)


    private val analyzer = SignalAnalyzer()



    private var waitingSecondClap = false

    private var firstClapTime = 0L


    private val doubleTimeout = 700L


    private var lastDetection = 0L


    private val cooldown = 150L





    fun detect(buffer: ShortArray): ClapType {


        val signal =
            analyzer.analyze(buffer)



        history.add(signal)



        // Пока мало данных — ничего не решаем

        if (!history.isFull()) {

            return ClapType.NONE

        }



        val score =
            calculateScore()



        if (score < 70) {

            return ClapType.NONE

        }



        val now =
            System.currentTimeMillis()



        if (now - lastDetection < cooldown) {

            return ClapType.NONE

        }



        lastDetection = now



        Logger.log(
            "CLAP candidate score=$score peak=${signal.peak}"
        )




        if (!waitingSecondClap) {


            waitingSecondClap = true

            firstClapTime = now


            return ClapType.NONE

        }





        val delay =
            now - firstClapTime



        if (delay <= doubleTimeout) {


            waitingSecondClap = false


            Logger.log(
                "DOUBLE CLAP delay=${delay}ms"
            )


            return ClapType.DOUBLE_CLAP

        }



        firstClapTime = now



        return ClapType.NONE

    }







    fun checkSingleClapTimeout(): ClapType {



        if (
            waitingSecondClap &&
            System.currentTimeMillis()
            -
            firstClapTime
            >
            doubleTimeout
        ) {



            waitingSecondClap = false



            Logger.log(
                "SINGLE CLAP"
            )



            return ClapType.SINGLE_CLAP

        }



        return ClapType.NONE

    }









    private fun calculateScore(): Int {



        val signals =
            history.getAll()



        var score = 0



        val peaks =
            signals.map {
                it.peak
            }



        val averagePeak =
            peaks.average()



        val last =
            signals.last()





        // Резкий импульс

        if (
            last.peak >
            averagePeak * 2
        ) {

            score += 30

        }





        // Высокая энергия

        if (
            last.peak > 12000
        ) {

            score += 20

        }






        // Быстрый спад

        if (
            last.decay < 40
        ) {

            score += 15

        }





        // Много переходов через ноль

        if (
            last.zeroCrossings > 150
        ) {

            score += 15

        }





        // Хлопок должен быть коротким

        if (
            last.attack < 30
        ) {

            score += 10

        }



        return score

    }

}
