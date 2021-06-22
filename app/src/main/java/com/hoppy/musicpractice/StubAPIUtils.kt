package com.hoppy.musicpractice

import android.content.Context
import java.io.IOException

object StubAPIUtils {
    fun loadJsonFromAssets(context: Context,fileName:String):String? {
        return try {
            val ins = context.assets.open(fileName)
            val buffer = ByteArray(ins.available())
            ins.read(buffer)
            ins.close()
            String(buffer)
        }catch (e:IOException){
            null
        }
    }
}