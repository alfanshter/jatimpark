package com.alfanshter.jatimpark.Model

data class ModelBaru(
    val nama:String,
    val latidude:Double,
    val longitude:Double,
    val foto:String


) {
    constructor() : this("", 0.00, 0.00,"https://cdn0-production-images-kly.akamaized.net/RxUo9GGJA_1oDBlykD64OHEuVVg=/640x360/smart/filters:quality(75):strip_icc():format(jpeg)/kly-media-production/medias/2396228/original/055603500_1540900672-lion-3040797_1920.jpg")
}