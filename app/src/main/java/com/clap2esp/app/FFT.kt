package com.clap2esp.app

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object FFT {

    fun magnitude(buffer: ShortArray): DoubleArray {

        val n = Integer.highestOneBit(buffer.size)

        val real = DoubleArray(n)
        val imag = DoubleArray(n)

        for (i in 0 until n) {
            real[i] = buffer[i].toDouble()
        }

        fft(real, imag)

        val result = DoubleArray(n / 2)

        for (i in result.indices) {
            result[i] = sqrt(
                real[i] * real[i] +
                imag[i] * imag[i]
            )
        }

        return result
    }

    private fun fft(real: DoubleArray, imag: DoubleArray) {

        val n = real.size

        var j = 0

        for (i in 0 until n) {

            if (i < j) {

                val tr = real[i]
                real[i] = real[j]
                real[j] = tr

                val ti = imag[i]
                imag[i] = imag[j]
                imag[j] = ti
            }

            var m = n shr 1

            while (j >= m && m >= 2) {
                j -= m
                m = m shr 1
            }

            j += m
        }

        var len = 2

        while (len <= n) {

            val angle =
                -2.0 * Math.PI / len

            val wlenCos =
                cos(angle)

            val wlenSin =
                sin(angle)

            var i = 0

            while (i < n) {

                var wCos = 1.0
                var wSin = 0.0

                for (k in 0 until len / 2) {

                    val uReal =
                        real[i + k]

                    val uImag =
                        imag[i + k]

                    val vReal =
                        real[i + k + len / 2] * wCos -
                        imag[i + k + len / 2] * wSin

                    val vImag =
                        real[i + k + len / 2] * wSin +
                        imag[i + k + len / 2] * wCos

                    real[i + k] =
                        uReal + vReal

                    imag[i + k] =
                        uImag + vImag

                    real[i + k + len / 2] =
                        uReal - vReal

                    imag[i + k + len / 2] =
                        uImag - vImag

                    val nextCos =
                        wCos * wlenCos -
                        wSin * wlenSin

                    wSin =
                        wCos * wlenSin +
                        wSin * wlenCos

                    wCos =
                        nextCos
                }

                i += len
            }

            len = len shl 1
        }
    }
}
