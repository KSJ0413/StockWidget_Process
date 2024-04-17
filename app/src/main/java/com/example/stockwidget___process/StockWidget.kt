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

/**
 * Implementation of App Widget functionality.
 */
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
    } else {
        // 응답이 실패했을 때의 처리
        // ...
    }

    connection.disconnect()

    val widgetText = context.getString(R.string.appwidget_text)
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.stock_widget)
    views.setTextViewText(R.id.appwidget_text, widgetText)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}
