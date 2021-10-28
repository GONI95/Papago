package com.example.papago

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private val REQUESTCODE = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) // 하드웨어에 마이크가 있는지 확인
            imageView3.setEnabled(true)
        else
            imageView3.setEnabled(false)

        imageView2.setOnClickListener {
            var intent = Intent(this, Papago_Translation::class.java)
            startActivity(intent)
        }
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) // 하드웨어에 마이크가 있는지 확인
        {
          imageView3.setEnabled(true)
          imageView3.setOnClickListener {
              val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
              intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
              intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
              intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Need to Speak")
              startActivityForResult(intent, REQUESTCODE) // onActivityResult 메서드의 request 코드에 추가되는 것으로 알고있음
            }
        }
        else {
            imageView3.setEnabled(false)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && null != data) {
            val result: ArrayList<String> = data.getStringArrayListExtra(RecognizerIntent .EXTRA_RESULTS)
            intent = Intent(this, Papago_Translation::class.java)
            intent.putExtra("result", result[0])
            startActivity(intent)
        }
    }
}