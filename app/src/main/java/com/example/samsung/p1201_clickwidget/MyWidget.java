package com.example.samsung.p1201_clickwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.SharedPreferences.*;

/**
 * Created by samsung on 16.05.2017.
 */

public class MyWidget extends AppWidgetProvider {

    private final static String ACTION_CHANGE = "com.example.samsung.p1201_clickwidget.change_count";

    @Override
    public void onUpdate(Context context,
                         AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        //Обновление всех экземпляров
        for (int i : appWidgetIds) {
            updateWidget(context, appWidgetManager, i);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        //Удаление Preferences
        Editor editor = context.getSharedPreferences(
                ConfigActivity.WIDGET_PREF,
                Context.MODE_PRIVATE)
                .edit();
        for (int widgetID : appWidgetIds) {
            editor.remove(ConfigActivity.WIDGET_TIME_FORMAT + widgetID);
            editor.remove(ConfigActivity.WIDGET_COUNT + widgetID);
        }
        editor.commit();
    }

    public static void updateWidget(
            final Context context,
            final AppWidgetManager appWidgetManager,
            final int widgetID) {

        SharedPreferences sp = context.getSharedPreferences(
                ConfigActivity.WIDGET_PREF,
                Context.MODE_PRIVATE
        );
        //Чтение формата времени и определение текущего
        String timeFormat = sp.getString(ConfigActivity.WIDGET_TIME_FORMAT + widgetID, null);

        if (timeFormat == null) return;

        SimpleDateFormat sdf = new SimpleDateFormat(timeFormat);
        String currentTime = sdf.format(new Date(System.currentTimeMillis()));
        //Чтение счётчика
        String count = String.valueOf(sp.getInt(ConfigActivity.WIDGET_COUNT + widgetID, 0));
        //Помещение данных в текстовые поля
        RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.widget);
        widgetView.setTextViewText(R.id.tvTime, currentTime);
        widgetView.setTextViewText(R.id.tvCount, count);

        //Реакция первой зоны - открытие конфигурационного экрана
        Intent configIntent = new Intent(context, ConfigActivity.class);
        configIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, widgetID, configIntent, 0);
        widgetView.setOnClickPendingIntent(R.id.tvPressConfig, pendingIntent);

        //Реакция второй зоны - обновление виджета
        Intent updateIntent = new Intent(context, MyWidget.class);
        updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] {widgetID});
        pendingIntent = PendingIntent.getBroadcast(
                context, widgetID, updateIntent, 0);
        widgetView.setOnClickPendingIntent(R.id.tvPressUpdate, pendingIntent);

        //Реакция третьей зоны - увеличение счётчика нажатий
        Intent countIntent = new Intent(context, MyWidget.class);
        countIntent.setAction(ACTION_CHANGE);
        countIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        pendingIntent = PendingIntent.getBroadcast(
                context, widgetID, countIntent, 0);
        widgetView.setOnClickPendingIntent(R.id.tvPressCount, pendingIntent);

        //Обновление виджета
        appWidgetManager.updateAppWidget(widgetID, widgetView);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        //Проверка, является вызов intent'a реакцией на нажатие третьей зоны виджета
        if (intent.getAction().equalsIgnoreCase(ACTION_CHANGE)) {
            //Извлечение ID экземпляра виджета
            int mAppWidgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
            Bundle extras = intent.getExtras();

            if (extras != null) {
                mAppWidgetID = extras.getInt(
                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                        AppWidgetManager.INVALID_APPWIDGET_ID
                );
            }
            if (mAppWidgetID != AppWidgetManager.INVALID_APPWIDGET_ID) {
                //Чтение значения счётика, увеличение его на 1 и запись обратно
                SharedPreferences sp = context.getSharedPreferences(
                        ConfigActivity.WIDGET_PREF,
                        Context.MODE_PRIVATE
                );
                int cnt = sp.getInt(ConfigActivity.WIDGET_COUNT + mAppWidgetID, 0);
                sp.edit().putInt(ConfigActivity.WIDGET_COUNT + mAppWidgetID, ++cnt).commit();
                //Обновление виджета
                updateWidget(context, AppWidgetManager.getInstance(context), mAppWidgetID);
            }
        }
    }
}
