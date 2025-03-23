package com.like.likecard.webViewProject

import android.content.Context

const val SHARED_PREF_NAME = "GAME_DEMO"
const val last_url = "last_url"
class SharedPrefManager {
    companion object {



        fun getUrl(mContext: Context): String {
            val sharedPreferences =
                mContext.getSharedPreferences(
                    SHARED_PREF_NAME, 0
                )
            return sharedPreferences.getString(last_url, "") ?: ""
        }
        fun setUrl(url: String?, mContext: Context) {
            val sharedPreferences =
                mContext!!.getSharedPreferences(
                    SHARED_PREF_NAME,
                    0
                )
            val editor = sharedPreferences.edit()
            editor.putString(last_url, (url ?: ""))
            editor.apply()
        }

}
}