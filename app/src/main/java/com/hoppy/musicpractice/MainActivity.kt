package com.hoppy.musicpractice

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.text.Html
import android.text.SpannableString
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {
    private lateinit var noteChords: NoteChord
    private val chordLevels = listOf("I级", "II级", "III级", "IV级", "V级", "VI级", "VII级")
    private val noteLevels = listOf("C调", "D调", "E调", "F调", "G调", "A调", "B调")
    private val noteChordMap: MutableMap<String, Note> = HashMap()
    private val chordList = mutableListOf<String>()

    private var selectedNote: Note? = null
    private var answer: String? = null

    private lateinit var mHandler:Handler
    private var millisecondsRecord = 0L
    private var startTime = 0L
    private var timeBuff = 0L
    private val runnable = object: Runnable{
        override fun run() {
            millisecondsRecord = SystemClock.uptimeMillis() - startTime
            val accumulatedTime = timeBuff + millisecondsRecord
            val milliseconds = accumulatedTime % 1000
            val seconds = accumulatedTime / 1000 % 60
            val minutes = accumulatedTime / 1000 / 60
            val time1 = String.format("%02d:%02d",minutes,seconds)
            val time2 = String.format(".%03d",milliseconds)

            tvStopwatch.text = time1
            tvStopwatchSecond.text = time2
            mHandler.postDelayed(this,0)
        }
    }

    private var produceQuestionTime = 0L
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initDataFromAsset()
        initSpinnerAdapter()
        initRadioGroup()
        initButton()
    }

    private fun initButton() {
        start.setOnClickListener { start() }
        pause.setOnClickListener { pause() }
        reset.setOnClickListener { reset() }
        reset()
        mHandler = Handler(Looper.getMainLooper())
    }


    private fun initDataFromAsset() {
        val json = StubAPIUtils.loadJsonFromAssets(this, "formula/formula.json")
        json?.let {
            noteChords = Gson().fromJson(json, NoteChord::class.java)
            for (item in noteChords.payload) {
                noteChordMap["${item.note}调"] = item
                chordList.add(item.chords.one)
                chordList.add(item.chords.two)
                chordList.add(item.chords.three)
                chordList.add(item.chords.four)
                chordList.add(item.chords.five)
                chordList.add(item.chords.six)
                chordList.add(item.chords.seven)
            }
            chordList.distinct()
        }
    }

    private fun initSpinnerAdapter() {

        ArrayAdapter(this, android.R.layout.simple_spinner_item, noteLevels).also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerNotes.adapter = it
        }
        spinnerNotes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedNote = noteChordMap[noteLevels[position]]
                produceNextQuestion()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }
        spinnerNotes.setSelection(0)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun produceNextQuestion() {
        produceQuestionTime = SystemClock.uptimeMillis()
        answer = produceQuestion()
        answer?.let { showOptions(it, chordList) }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initRadioGroup() {
        optionGroup.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = group.findViewById<RadioButton>(checkedId)
            answer?.let { showAnswer(radioButton.text.toString(), it) }
            produceNextQuestion()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    private fun showAnswer(actual: String, answer: String) {
         val milliSecs =  SystemClock.uptimeMillis() - produceQuestionTime
        val accumulatedTime = timeBuff + millisecondsRecord
        val seconds = milliSecs / 1000 % 60
        var textA = "Correct!"
        if(seconds <= 2){
            textA ="优秀！"
        }
        val text = SpannableString(Html.fromHtml(answer, Html.FROM_HTML_OPTION_USE_CSS_COLORS))
        textAnswer.setText(text, TextView.BufferType.SPANNABLE)
        if (actual == answer) {
            hintText.text = textA
            hintText.setTextColor(Color.GREEN)
            textAnswer.setTextColor(Color.GREEN)
        } else {
            hintText.text = "Wrong!"
            hintText.setTextColor(Color.RED)
            textAnswer.setTextColor(Color.RED)
        }
    }

    private fun produceQuestion(): String? {
        val index = (0..6).random()
        questionTitle.text = chordLevels[index]
        return selectedNote?.let {
            when (index) {
                0 -> {
                    it.chords.one
                }
                1 -> {
                    it.chords.two
                }
                2 -> {
                    it.chords.three
                }
                3 -> {
                    it.chords.four
                }
                4 -> {
                    it.chords.five
                }
                5 -> {
                    it.chords.six
                }
                else -> {
                    it.chords.seven
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun showOptions(answer: String, chordList: List<String>) {
        optionGroup.removeAllViews()
        val options = produceOption(answer, chordList)
        for (item in options) {
            val radioButton = RadioButton(this)
            val text = SpannableString(Html.fromHtml(item, Html.FROM_HTML_OPTION_USE_CSS_COLORS))
            radioButton.setText(text, TextView.BufferType.SPANNABLE)
            radioButton.setTextColor(Color.BLACK)
            val layoutParams = RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.WRAP_CONTENT,
                RadioGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.marginStart = 20
            layoutParams.marginEnd = 20
            radioButton.layoutParams = layoutParams
            radioButton.isClickable = true
            optionGroup.addView(radioButton)
        }
    }

    private fun produceOption(answer: String, chordList: List<String>): List<String> {
        val list = mutableListOf(answer)
        var count = 0
        while (count < 3) {
            val index = (chordList.indices).random()
            if (list.contains(chordList[index])) continue
            list.add(chordList[index])
            count++
        }
        return list.shuffled()
    }
    private fun start(){
        startTime = SystemClock.uptimeMillis()
        mHandler.postDelayed(runnable,0)
        reset.isEnabled = false
        start.isEnabled = false
        tvStopwatchSecond.visibility = View.VISIBLE
    }

    private fun pause(){
        timeBuff += millisecondsRecord
        mHandler.removeCallbacks(runnable)
        reset.isEnabled = true
        start.isEnabled = true
    }
    private fun reset(){
        millisecondsRecord = 0L
        timeBuff = 0L
        tvStopwatch.text = "00:00"
        tvStopwatchSecond.text = ".000"
        tvStopwatchSecond.visibility = View.INVISIBLE
    }
}

