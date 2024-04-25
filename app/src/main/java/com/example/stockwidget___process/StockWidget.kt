package com.example.stockwidget___process

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues

class StockDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "stock.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_STOCKS = "stocks"
        private const val COLUMN_ID = "_id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_PRICE = "price"
        private const val COLUMN_PERCENT = "percent"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableSQL = "CREATE TABLE $TABLE_STOCKS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_NAME TEXT, " +
                "$COLUMN_PRICE REAL, " +
                "$COLUMN_PERCENT REAL)"
        db.execSQL(createTableSQL)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_STOCKS")
        onCreate(db)
    }

    fun insertStock(name: String, price: Double, percent: Double) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_PRICE, price)
            put(COLUMN_PERCENT, percent)
        }
        db.insert(TABLE_STOCKS, null, values)
        db.close()
    }
}

class StockWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }

        // Set up an alarm to update the widget every minute
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, StockWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 60000, pendingIntent)
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val endPoint = "https://apis.data.go.kr/1160100/service/GetStockSecuritiesInfoService"
    val apiKey = "B%2BSxs95%2F%2BrJUXfLvraUCT3Hz%2FGxcpKGpUEX17%2BQQ7cFJED07MQ34vQn%2F%2FWj54mgzQkKFH9pFuBKpI0zdu9eSIg%3D%3D"

    // API 요청을 위한 URL 생성
    val urlString = "$endPoint?serviceKey=$apiKey"

    // URL 객체 생성
    val url = URL(urlString)

    // HttpURLConnection 객체 생성
    val connection = url.openConnection() as HttpURLConnection

    // 요청 방식 설정 (GET 또는 POST)
    connection.requestMethod = "GET"

    // 연결 시도
    connection.connect()

    // 응답 코드 확인
    val responseCode = connection.responseCode
    if (responseCode == HttpURLConnection.HTTP_OK) {
        // 응답이 성공적으로 왔을 때의 처리
        val inputStream = connection.inputStream
        val reader = BufferedReader(InputStreamReader(inputStream))
        val response = reader.readText()

        // JSON 파싱
        val jsonObject = JSONObject(response)

        // 필요한 데이터 추출
        val stockPrice = jsonObject.getDouble("price")
        val changePercent = jsonObject.getDouble("changePercent")

        // SharedPreferences 객체 가져오기
        val sharedPreferences = context.getSharedPreferences("StockWidget", Context.MODE_PRIVATE)

        // SharedPreferences에 데이터 저장하기
        val editor = sharedPreferences.edit()
        editor.putFloat("stockPrice", stockPrice.toFloat())
        editor.putFloat("changePercent", changePercent.toFloat())
        editor.commit()
    } else {
        // 응답이 실패했을 때의 처리
        // ...
    }

    connection.disconnect()

    // SharedPreferences 객체 가져오기
    val sharedPreferences = context.getSharedPreferences("StockWidget", Context.MODE_PRIVATE)

    // SharedPreferences에서 데이터 불러오기
    val oldPrice = sharedPreferences.getFloat("oldPrice", 0.0f)
    val newPrice = sharedPreferences.getFloat("newPrice", 0.0f)

    // 퍼센트 계산
    val percentChange = ((newPrice - oldPrice) / oldPrice) * 100

    // 위젯 텍스트 업데이트
    val widgetText = "Price: $newPrice\\nChange: $percentChange%"
    val views = RemoteViews(context.packageName, R.layout.stock_widget)
    views.setTextViewText(R.id.appwidget_text, widgetText)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}
