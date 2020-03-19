package com.alfanshter.jatimpark.ui.shareRombongan.listuser.Model


data class copy(
    val name:String,
    val token_id:String,
    val image:String

) {
    constructor() : this("", "","")
}