package kz.kazpost.vpn.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kz.kazpost.vpn.acts.models.ActModel
import kz.kazpost.vpn.enums.LabelsModel
import kz.kazpost.vpn.enums.ItemModel
import kz.kazpost.vpn.enums.Step
import kz.kazpost.vpn.models.Road
import kz.kazpost.vpn.models.StationData
import kz.kazpost.vpn.models.VPNData

class PreferenceHelper {

    companion object Factory {
        private const val PREF_NAME = "preferences"
        private val gson = Gson()

        private const val CURRENT_STEP = "current_step"

        private const val AUTH = "auth"
        private const val LOGIN = "login"
        private const val TRANSPORT_ID = "transport_id"
        private const val ROAD_INDEX = "road_index"
        private const val DATABASE = "database"
        private const val LABELS = "labels"
        private const val ITEMS = "items"
        private const val SCANS = "scans"
        private const val SCANS2 = "scans2"
        private const val TRANSFERS = "transfers"
        private const val LEFTS = "lefts"
        private const val ACTS = "acts"
        private const val ROADS = "roads"

        fun setAuth(context: Context) {
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val editor = pref.edit()
            editor.putString(AUTH, "auth")
            editor.apply()
        }

        fun getAuth(context: Context): String? {
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return pref.getString(AUTH, "") ?: ""
        }

        fun saveLogin(context: Context, login: String) {
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val editor = pref.edit()
            editor.putString(LOGIN, login)
            editor.apply()
        }

        fun getLogin(context: Context): String {
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return pref.getString(LOGIN, "") ?: ""
        }

        fun saveStep(context: Context, step: Step) {
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val editor = pref.edit()
            editor.putString(CURRENT_STEP, step.toString())
            editor.apply()
        }

        fun getStep(context: Context): Step {
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return Step.valueOf(pref.getString(CURRENT_STEP, "ACCEPT_INVOICE") ?: "ACCEPT_INVOICE")
        }

        fun saveTId(context: Context, id: String) {
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val editor = pref.edit()
            editor.putString(TRANSPORT_ID, id)
            editor.apply()
        }

        fun getTId(context: Context): String {
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return pref.getString(TRANSPORT_ID, "") ?: ""
        }

        fun saveIndex(context: Context, index: Int) {
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val editor = pref.edit()
            editor.putInt(ROAD_INDEX, index)
            editor.apply()
        }

        fun getIndex(context: Context): Int {
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return pref.getInt(ROAD_INDEX, 1)
        }

        fun saveDB(context: Context, database: StationData) {
            val json = gson.toJson(database)
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val editor = pref.edit()
            editor.putString(DATABASE, json)
            editor.apply()
        }

        fun getDB(context: Context): StationData {
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val json = pref.getString(DATABASE, "")
            return gson.fromJson(json, StationData::class.java)
        }

        fun saveLabels(context: Context, labels: MutableList<LabelsModel>) {
            val json = gson.toJson(labels)
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val editor = pref.edit()
            editor.putString(LABELS, json)
            editor.apply()
        }

        fun getLabels(context: Context): MutableList<LabelsModel> {
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val json = pref.getString(LABELS, "")
            val labelsType = object: TypeToken<MutableList<LabelsModel>>(){}.type
            return gson.fromJson(json, labelsType)
        }

        fun saveScans(context: Context, scans: MutableList<ItemModel>) {
            val json = gson.toJson(scans)
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val editor = pref.edit()
            editor.putString(SCANS, json)
            editor.apply()
        }

        fun getScans(context: Context): MutableList<ItemModel>? {
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val json = pref.getString(SCANS, "")
            return if (json == "") {
                null
            } else {
                val scanType = object: TypeToken<MutableList<ItemModel>>(){}.type
                gson.fromJson(json, scanType)
            }
        }

        fun saveScans2(context: Context, scans2: MutableList<ItemModel>) {
            val json = gson.toJson(scans2)
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val editor = pref.edit()
            editor.putString(SCANS2, json)
            editor.apply()
        }

        fun getScans2(context: Context): MutableList<ItemModel>? {
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val json = pref.getString(SCANS2, "")
            return if (json == "") {
                null
            } else {
                val scan2Type = object: TypeToken<MutableList<ItemModel>>(){}.type
                gson.fromJson(json, scan2Type)
            }
        }

        fun saveItems(context: Context, items: MutableList<ItemModel>) {
            val json = gson.toJson(items)
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val editor = pref.edit()
            editor.putString(ITEMS, json)
            editor.apply()
        }

        fun getItems(context: Context): MutableList<ItemModel>? {
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val json = pref.getString(ITEMS, "")
            return if (json == "") {
                null
            } else {
                val itemType = object: TypeToken<MutableList<ItemModel>>(){}.type
                gson.fromJson(json, itemType)
            }
        }

        fun saveTransfers(context: Context, transfers: MutableList<ItemModel>) {
            val json = gson.toJson(transfers)
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val editor = pref.edit()
            editor.putString(TRANSFERS, json)
            editor.apply()
        }

        fun getTransfers(context: Context): MutableList<ItemModel>? {
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val json = pref.getString(TRANSFERS, "")
            return if (json == "") {
                null
            } else {
                val transferType = object : TypeToken<MutableList<ItemModel>>() {}.type
                gson.fromJson(json, transferType)
            }
        }

        fun saveLefts(context: Context, lefts: MutableList<ItemModel>) {
            val json = gson.toJson(lefts)
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val editor = pref.edit()
            editor.putString(LEFTS, json)
            editor.apply()
        }

        fun getLefts(context: Context): MutableList<ItemModel>? {
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val json = pref.getString(LEFTS, "")
            return if (json == "") {
                null
            } else {
                val leftType = object : TypeToken<MutableList<ItemModel>>() {}.type
                return gson.fromJson(json, leftType)
            }
        }

        fun saveActs(context: Context, acts: MutableList<ActModel>) {
            val json = gson.toJson(acts)
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val editor = pref.edit()
            editor.putString(ACTS, json)
            editor.apply()
        }

        fun getActs(context: Context): MutableList<ActModel>? {
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val json = pref.getString(ACTS, "") ?: ""
            return if (json == "") {
                null
            } else {
                val actType = object : TypeToken<MutableList<ActModel>>() {}.type
                return gson.fromJson(json, actType)
            }
        }

        fun saveRoads(context: Context, acts: ArrayList<Road>) {
            val json = gson.toJson(acts)
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val editor = pref.edit()
            editor.putString(ROADS, json)
            editor.apply()
        }

        fun getRoads(context: Context): ArrayList<Road>? {
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val json = pref.getString(ROADS, "") ?: ""
            return if (json == "") {
                null
            } else {
                val actType = object : TypeToken<ArrayList<ActModel>>() {}.type
                return gson.fromJson(json, actType)
            }
        }

        fun clearPreference(context: Context) {
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = pref.edit()
            //sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            editor.clear()
            editor.apply()
        }

        fun removeValue(context: Context, KEY_NAME: String) {
            val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val editor: SharedPreferences.Editor = pref.edit()
            editor.remove(KEY_NAME)
            editor.apply()
        }
    }

}