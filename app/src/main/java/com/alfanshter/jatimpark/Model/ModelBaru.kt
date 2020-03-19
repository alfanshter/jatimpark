package com.alfanshter.jatimpark.Model

data class ModelBaru(
    val email:String,
    val latidude:Double,
    val longitude:Double

) {
    constructor() : this("", 0.00, 0.00)
}