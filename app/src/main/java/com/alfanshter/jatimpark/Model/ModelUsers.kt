package com.alfanshter.jatimpark.Model
class ModelUsers {

    /// MOdel class
    var name : String? = null
    var email : String? = null
    var saldo : String? = null
    var photo : String? = null
    var nama : String? = null
    var postimage : String? = null
    var gambar:String? = null
    var id : String? = null
    var image: String? = null
    var password:String? = null
    constructor(){

    }

    constructor(email: String?,name : String?,saldo :String?,photo:String?,nama:String?,postimage:String?,gambar:String?,id:String?,image:String?,password:String?) {
        this.email = email
        this.name = name
        this.saldo = saldo
        this.photo = photo
        this.nama = nama
        this.postimage = postimage
        this.gambar = gambar
        this.id = id
        this.image = image
        this.password = password
    }
}