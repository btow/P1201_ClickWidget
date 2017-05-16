package com.example.samsung.p1201_clickwidget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class ConfigActivity extends AppCompatActivity {

    public final static String
        WIDGET_PREF = "widget_pref",
        WIDGET_TIME_FORMAT = "idget_time_format_",
        WIDGET_COUNT = "idget_count_";
    private int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Intent resultValue;
    private SharedPreferences sp;
    private EditText etFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Извлечение ID конфигурируемого виджета
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null) {
            widgetID = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
            );
        }
        //Проверка корректности извлечённого ID конфигурируемого  виджета
        if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
        //Формирование intent'a ответа
        resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
        //Формирование отрицательного результата
        setResult(RESULT_CANCELED, resultValue);

        setContentView(R.layout.activity_config);

        sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE);
        etFormat = (EditText) findViewById(R.id.etFormat);
        etFormat.setText(sp.getString(WIDGET_TIME_FORMAT + widgetID, "HH:mm:ss"));

        int cnt = sp.getInt(ConfigActivity.WIDGET_COUNT + widgetID, -1);
        if (cnt == -1) {
            sp.edit().putInt(WIDGET_COUNT + widgetID, 0);
        }
    }

    public void onClickBtnOk(View view) {
        sp.edit().putString(WIDGET_TIME_FORMAT + widgetID, etFormat.getText().toString()).commit();
        MyWidget.updateWidget(this, AppWidgetManager.getInstance(this), widgetID, MyWidget.UPDATE_ALL);
        setResult(RESULT_OK, resultValue);
        finish();
    }
}
