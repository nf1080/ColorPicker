package com.fesefeldt.neal.colorpicker2

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.appcompat.R.id.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import android.text.InputType
import android.view.*
import android.widget.*
import com.fesefeldt.neal.colorpicker2.MainActivity.ColorObj
import java.util.*
import java.util.Collections.list
import kotlin.collections.ArrayList
import android.R.id.edit
import android.content.SharedPreferences
import android.os.Environment
import android.preference.PreferenceManager
import java.io.File.separator
import android.os.Environment.getExternalStorageDirectory
import android.util.Log
import java.io.*

class MainActivity : AppCompatActivity() {

    private var cText = ""
    var rValue = 0
    var gValue = 0
    var bValue = 0

    private var list: ArrayList<ColorObj> = ArrayList()

    private var savedColors = File("/data/user/0/com.fesefeldt.neal.colorpicker2/files", "savedColors")

    class ColorObj (val name: String, val redVal: String, val greenVal: String, val blueVal: String)

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)
            setSupportActionBar(toolbar)

            //var temp = filesDir
            //var saveFile = File(temp, "savedColors")
            //Log.i("ColorPicker", saveFile.toString())

            setUpSeekBar(redSeekBar, rVal)
            setUpSeekBar(greenSeekBar, gVal)
            setUpSeekBar(blueSeekBar, bVal)
            
            colorWindow.setBackgroundColor(Color.rgb(rValue, gValue, bValue))

            saveButtonClick()
            recallButtonClick()

            val info = intent
            if (info != null) {

                if(info.action == "myDesign.intent.ACTION_COLOR"){
                    sendButton.visibility = View.VISIBLE
                    sendButton.setOnClickListener {
                        sendResult()
                    }
                }
            }
        }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {

        super.onPause()
        saveArrayList(list)
    }

    override fun onResume(){
        super.onResume()

        var tempList: ArrayList<String> = getSavedArrayList()

        if(tempList.size > 3) {
            Log.i("ColorPicker", "List not empty. size of tempList is: " + tempList.size)
            Log.i("ColorPicker", "tempList[0] value contains: " + tempList[0])
            Log.i("ColorPicker", "tempList[1] value contains: " + tempList[1])
            for (i in 0 until tempList.size  step 4) {
                val newColor = ColorObj(tempList[i], tempList[i + 1], tempList[i + 2], tempList[i + 3])
                list.add(newColor)
                Log.i("ColorPicker", "for loop executed. value of i is: " + i)
            }
        }
        else {
            Log.i("ColorPicker", "List is empty")
        }
    }

    private fun setUpSeekBar(seekBar: SeekBar, textView: TextView) {

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int,
                                           fromUser: Boolean) {

                textView.text = progress.toString()

                if(seekBar == redSeekBar) {
                    colorWindow.setBackgroundColor(Color.rgb(progress, gValue, bValue))
                    rValue = progress
                }

                if(seekBar == greenSeekBar) {
                    colorWindow.setBackgroundColor(Color.rgb(rValue, progress, bValue))
                    gValue = progress
                }

                if(seekBar == blueSeekBar) {
                    colorWindow.setBackgroundColor(Color.rgb(rValue, gValue, progress))
                    bValue = progress
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

     private fun saveButtonClick() {

            saveButton.setOnClickListener {

                var redVal = rValue.toString()
                var greenVal = gValue.toString()
                var blueVal = bValue.toString()

                val builder = AlertDialog.Builder(this)
                builder.setTitle("Save Color")

                val input = EditText(this)
                input.inputType = InputType.TYPE_CLASS_TEXT
                builder.setView(input)

                // Set up the buttons
                builder.setPositiveButton("Save") { dialog, which ->
                    cText = input.text.toString()
                    val newColor = ColorObj(cText, redVal, greenVal, blueVal)

                    list.add(newColor)

                   Toast.makeText(this,"current data " + cText + redVal + " "
                            + greenVal + " " + blueVal, Toast.LENGTH_LONG).show()
                }

                builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                builder.show()
            }
     }

     private fun listToCharSeq(list: List<ColorObj>): Array<CharSequence?> {
            val myArray = arrayOfNulls<CharSequence>(list.size)
            for (i in 0 until list.size) {
                myArray[i] = list[i].name + list[i].redVal + list[i].greenVal + list[i].blueVal
            }
            return myArray
     }

     private fun recallButtonClick() {

           recallButton.setOnClickListener {

                var colorArray = listToCharSeq(list)

                val builder = AlertDialog.Builder(this)
                builder.setTitle("Select Color")
                builder.setItems(colorArray, DialogInterface.OnClickListener { dialog, i  ->

                    val selected = list[i]

                    colorWindow.setBackgroundColor(Color.rgb(selected.redVal.toInt(), selected.greenVal.toInt(),
                                                    selected.blueVal.toInt()))
                    redSeekBar.progress = selected.redVal.toInt()
                    greenSeekBar.progress = selected.greenVal.toInt()
                    blueSeekBar.progress = selected.blueVal.toInt()

                    dialog.dismiss()
                })
                builder.show()
           }
     }

     private fun saveArrayList(list: ArrayList<ColorObj>) {

         listToCharSeq(list)

         try {
           File(savedColors.toString()).printWriter().use { out ->
             list.forEach {
                 //out.println(it.name + " " + it.redVal + " " + it.blueVal + " " + it.greenVal)
                 out.println(it.name)
                 out.println(it.redVal)
                 out.println(it.greenVal)
                 out.println(it.blueVal)
             }
               Log.i("ColorPicker","List Written")
           }
             } catch (e: Exception) {
                 Log.e("InternalStorage", "Write fail.")
             }
     }

     private fun getSavedArrayList(): ArrayList<String> {

        var toReturn: ArrayList<String> = ArrayList()

         if(savedColors.exists()) {
             File(savedColors.toString()).inputStream().use {

                 Log.i("ColorPicker", "File Exists ")
                 try {
                     val file = InputStreamReader(openFileInput("savedColors"))
                     val br = BufferedReader(file)
                     Log.i("ColorPicker", "File Located")
                     var line = br.readLine()

                     if (line == null)
                         Log.i("ColorPicker", "File is Empty")

                     while (line != null) {

                         toReturn.add(line)
                         Log.i("ColorPicker", "line read and added $line to list")
                         line = br.readLine()
                     }

                     file.close()
                     br.close()

                 } catch (e: FileNotFoundException) {
                     Log.e("InternalStorage", "File not found Exception")
                 } catch (e: IOException) {
                     Log.e("InternalStorage", "IO Exception")
                 }
             }
         }
         return toReturn
     }

     fun sendResult(){

        intent!!.putExtra("Red Value", rValue)
        intent!!.putExtra("Green Value", gValue)
        intent!!.putExtra("Blue Value", bValue)

        setResult(68, intent)
        super.finish()
     }
}

