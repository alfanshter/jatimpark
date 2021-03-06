package com.alfanshter.jatimpark.Session

import android.content.Context
import android.content.SharedPreferences
import com.alfanshter.jatimpark.Rombongan.NamaProfil
import com.alfanshter.jatimpark.Utils.Utils

class SessionManager(private val context: Context ?) {
    val privateMode = 0
    val privateName ="login"
    var Pref :SharedPreferences?=context?.getSharedPreferences(privateName,privateMode)
    var editor : SharedPreferences.Editor?=Pref?.edit()

    private val islogin = "login"
    fun setLogin(check: Boolean){
        editor?.putBoolean(islogin,check)
        editor?.commit()
    }

    fun getLogin():Boolean?
    {
        return Pref?.getBoolean(islogin,false)
    }

    private val iduser = "iduser"
    fun setIduser(check:String)
    {
        editor?.putString(iduser,check)
        editor?.commit()
    }

    fun getIduser():String?
    {
        return Pref?.getString(iduser,"")
    }

    private val longitude = "longitude"
    fun setlongitude(check:String)
    {
        editor?.putString(longitude,check)
        editor?.commit()
    }

    fun getlongitude():String?
    {
        return Pref?.getString(longitude,"")
    }

    private val latidude = "latidude"
    fun setLatidude(check:String)
    {
        editor?.putString(latidude,check)
        editor?.commit()
    }

    fun getLatidude():String?
    {
        return Pref?.getString(latidude,"")
    }

    private val kunci = "kunci"
    fun setKunci (check: String)
    {
        editor?.putString(kunci,check)
        editor?.commit()
    }

    fun getKunci():String?
    {
        return Pref?.getString(kunci,"")
    }

    //status nama
    private val isnama = "nama"
    fun setNama(check: Boolean){
        editor?.putBoolean(isnama,check)
        editor?.commit()
    }

    fun getNama():Boolean?
    {
        return Pref?.getBoolean(isnama,false)
    }

    //nama
    private val profil = "profil"
    fun setprofil(check:String)
    {
        editor?.putString(profil,check)
        editor?.commit()
    }

    fun getprofil():String?
    {
        return Pref?.getString(profil,"")
    }

    private val foto = "foto"
    fun setFoto(check:String)
    {
        editor?.putString(foto,check)
        editor?.commit()
    }

    fun getFoto():String?
    {
        return Pref?.getString(foto,"")
    }






    private val id_status_user = "id_status_user"
    fun setIDStatusUser(id_status_users:String?){
        editor?.putString(id_status_user, id_status_users)
        editor?.commit()
    }
    fun getIDStatusUser():String?{
        return Pref?.getString(id_status_user,"")
    }



    private val telfon = "telfon"
    fun setTelfon(telfoncheck:String?){
        editor?.putString(telfon, telfoncheck)
        editor?.commit()
    }
    fun getTelfon():String?{
        return Pref?.getString(telfon,"")
    }


    private val qrcode = "qrcode"
    fun setQrcode(telfon:String?){
        editor?.putString(qrcode, qrcode)
        editor?.commit()
    }
    fun getQrcode():String?{
        return Pref?.getString(qrcode,"")
    }


    private val id_Setting = "id_Setting"
    fun setSetting(id_Settings:String?){
        editor?.putString(id_Setting, id_Settings)
        editor?.commit()
    }
    fun getSetting():String?{
        return Pref?.getString(id_Setting,"")
    }


    private val id_Viewuser = "id_Viewuser"
    fun setviewuser(id_viewuser:String?){
        editor?.putString(id_Viewuser, id_viewuser)
        editor?.commit()
    }
    fun getviewuser():String?{
        return Pref?.getString(id_Viewuser,"")
    }




}