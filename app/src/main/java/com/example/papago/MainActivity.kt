package com.example.papago

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException


class MainActivity : AppCompatActivity() {
    var TAG = "Log"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val url = "https://openapi.naver.com/v1/papago/n2mt"
        val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
        val client = OkHttpClient()
        val json = JSONObject()

        button.setOnClickListener {
            var edit = editText.text.toString()
            Log.d("edit: ", "$edit")

            json.put("source", "ko")
            json.put("target", "en")
            json.put("text", "$edit")

            val body = RequestBody.create(JSON, json.toString())
            val request = Request.Builder()
                .header("X-Naver-Client-Id", "JWUruvHlY2ei3FaADncC")
                .addHeader("X-Naver-Client-Secret", "ON_r3ssOGN")
                .url(url)
                .post(body)
                .build();
            Log.d("request: ", "${request.toString()}")
            Log.d("json_body", "${body.toString()}")

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Toast.makeText(this@MainActivity, "요청을 실패했습니다.", Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call, response: Response) {
                    var str = response.body!!.string()
                    println(str)
                    var ppg = Gson().fromJson<PapagoDTO>(str,PapagoDTO::class.java)
                    println(ppg.message!!.result!!.translatedText)

                    textView.setText("번역 결과: ${ppg.message!!.result!!.translatedText}")
                }
            })
        }
    }
}