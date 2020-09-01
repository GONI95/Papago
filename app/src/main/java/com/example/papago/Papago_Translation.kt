package com.example.papago

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeechService
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.papago_translation.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException


class Papago_Translation : AppCompatActivity() {
    var TAG = "Log"
    var source : String = ""
    var target : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.papago_translation)
        
        // 텍스트 뷰에 스크롤 활성화
        textView.movementMethod = ScrollingMovementMethod()
        
        // 현재 문자 수를 출력하기 위한 TextWatcher
        val mTextWatcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                editTextLiveLength.setText( "글자 수: ${s!!.length.toString()}" )
            }
            override fun afterTextChanged(s: Editable?) { }
        }
        editText.addTextChangedListener(mTextWatcher)

        // 문자 수를 제한하기 위한 lengthfilter
        val maxTextLength = 500
        /* val filterArray = arrayOfNulls<InputFilter>(1)
        filterArray[0] = LengthFilter(maxTextLength)
        editText.setFilters(filterArray) */
        editText.filters =
            arrayOf<InputFilter>(LengthFilter(maxTextLength))    // android:maxlength xml파일에서 재정의하므로 lengthfilter 사용이 좋음

        // 소스 스피너, 타겟 스피너
        val spinner_source = findViewById(R.id.spinner_source) as Spinner
        val spinner_target = findViewById(R.id.spinner_target) as Spinner
        spinner(spinner_source, spinner_target)

        if(intent.hasExtra("result"))
            editText.setText(intent.getStringExtra("result"))

        button_request.setOnClickListener {     // 요청 버튼 클릭
           if(source.equals("") && target.equals(""))
               Toast.makeText(this, "언어를 선택해주세요", Toast.LENGTH_SHORT).show()
           else
               translation()
        }

        button_speech.setOnClickListener {
        }
    }

    // 스피너에서 선택한 소스, 타겟 값의 키워드를 가져오기위한 함수
    fun keyword(source: String?) : String {
        when (source) {
            "독일어" -> return "de"
            "러시아어" -> return "ru"
            "베트남어" -> return "vi"
            "스페인어" -> return "es"
            "이탈리아어" -> return "it"
            "인도네시아어" -> return "id"
            "일본어" -> return "ja"
            "영어" -> return "en"
            "중국어 간체" -> return "zh-CN"
            "중국어 번체" -> return "zh-TW"
            "태국어" -> return "th"
            "프랑스" -> return "fr"
            "한국어" -> return "ko"
            else -> return "ko"
        }
    }

    fun spinner(spinner_source: Spinner, spinner_target: Spinner) {
        ArrayAdapter.createFromResource(this, R.array.source_target_list, R.layout.custom_spinner_item)
            .also { adapter ->
                adapter.setDropDownViewResource(R.layout.custom_spinner)
                spinner_source.adapter = adapter
                spinner_source.setSelection(12)
            }
        spinner_source.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                source = spinner_source.getItemAtPosition(position).toString()
                Log.d("source", "$source")

                if(source != null) {
                    var target_list : Array<String>
                    when (source) {
                        "한국어" -> target_list = resources.getStringArray(R.array.source_ko)
                        "영어" -> target_list = resources.getStringArray(R.array.source_en)
                        "일본어" -> target_list = resources.getStringArray(R.array.source_ja)
                        "중국어 간체" -> target_list = resources.getStringArray(R.array.source_zh_ch)
                        "중국어 번체" -> target_list = resources.getStringArray(R.array.source_zh_tw)
                        "베트남어" -> target_list = resources.getStringArray(R.array.source_vi)
                        "인도네시아어" -> target_list = resources.getStringArray(R.array.source_id)
                        "태국어" -> target_list = resources.getStringArray(R.array.source_th)
                        "독일어" -> target_list = resources.getStringArray(R.array.source_de)
                        "러시아어" -> target_list = resources.getStringArray(R.array.source_ru)
                        "스페인어" -> target_list = resources.getStringArray(R.array.source_es)
                        "이탈리아어" -> target_list = resources.getStringArray(R.array.source_it)
                        "프랑스어" -> target_list = resources.getStringArray(R.array.source_fr)
                        else -> target_list = resources.getStringArray(R.array.source_fr)
                    }
                    val adapter = ArrayAdapter(this@Papago_Translation, R.layout.custom_spinner_item, target_list)
                    adapter.setDropDownViewResource(R.layout.custom_spinner)
                    spinner_target.adapter = adapter

                    spinner_target.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            target = target_list[position]
                            Log.d("target", "$target")
                        }
                        override fun onNothingSelected(parent: AdapterView<*>?) {
                        }
                    }
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    fun translation() {
        var edit = editText.text.toString() // editText의 값을 읽어와 edit에 저장(자바에선 setText() 사용했는데 코틀린에선 text도 사용함)
        Log.d("edit: ", "$edit")

        // 파파고 restapi에 요청, 응답을 위한 코드
        val url = "https://openapi.naver.com/v1/papago/n2mt"    // papago 개발자센터 주소
        val JSON: MediaType = "application/json; charset=utf-8".toMediaType()   // 언어 포맷
        val client = OkHttpClient() // okhttp 사용하기 위해 선언
        val json = JSONObject() // JSONObject 사용을 위해 선언

        json.put("source", "${keyword(source)}")    // 요청할 때 언어
        json.put("target", "${keyword(target)}")    // 응답할 때 언어
        json.put("text", "$edit")   // 번역하고자하는 구문

        val body = RequestBody.create(JSON, json.toString())
        val request = Request.Builder()
            .header("X-Naver-Client-Id", "yourid")
            .addHeader("X-Naver-Client-Secret", "yourpassword")
            .url(url)
            .post(body)
            .build();
        Log.d("request: ", "${request.toString()}")
        Log.d("json_body", "${body.toString()}")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Toast.makeText(this@Papago_Translation, "요청을 실패했습니다.", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onResponse(call: Call, response: Response) {
                var str = response.body!!.string()
                println(str)
                var ppg = Gson().fromJson<PapagoDTO>(str, PapagoDTO::class.java)
                textView.setText("번역 결과: ${ppg.message!!.result!!.translatedText}")
            }
        })
    }
}