package com.example.papago

import android.os.Message
import java.lang.reflect.Type

data class PapagoDTO(var message : Message? = null) {
    data class Message(var result : Result? = null) {
        data class Result(var translatedText : String? = null)
    }
}