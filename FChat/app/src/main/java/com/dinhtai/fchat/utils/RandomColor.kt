package com.dinhtai.fchat.utils

class RandomColor{
    private var recycle = mutableListOf<Int>(-0xbbcca, -0x16e19d, -0x63d850, -0x98c549,
        -0xc0ae4b, -0xde690d, -0xfc560c, -0xff432c,
        -0xff6978, -0xb350b0, -0x743cb6, -0x3223c7,
        -0x14c5, -0x3ef9, -0x6800, -0xa8de,
        -0x86aab8, -0x616162, -0x9f8275, -0xcccccd)
//    fun getRandomColor(): Int {
//        return recycle.random()
//    }
    fun getColor(text : String) = when(text.toUpperCase()) {
        "A","S" -> recycle[0]
        "B","T" -> recycle[1]
        "C","U" -> recycle[2]
        "D","V" -> recycle [3]
        "E","W"-> recycle [4]
        "F","X" -> recycle [5]
        "G","Y" -> recycle[6]
        "H","Z" -> recycle[7]
        "I","1" -> recycle[8]
        "J","0" -> recycle[9]
        "K","2" -> recycle[10]
        "L","3" -> recycle[11]
        "L","4" -> recycle[12]
        "M","5" -> recycle[13]
        "N","6" -> recycle[14]
        "O","7" -> recycle[15]
        "P","8" -> recycle[16]
        "Q","9" -> recycle[17]
        "R" -> recycle[18]
        else -> recycle[19]
    }
}


