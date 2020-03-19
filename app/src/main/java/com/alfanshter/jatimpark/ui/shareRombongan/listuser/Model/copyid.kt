package com.alfanshter.jatimpark.ui.shareRombongan.listuser.Model

import androidx.annotation.NonNull


open class copyid{
    var copyId: String? = null
    fun <T : copyid?> withId(@NonNull id: String?): T {
        copyId = id
        return this as T
    }
}
