package com.bg.dataanalyzer;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;

import androidx.appcompat.app.AlertDialog;

import java.util.Locale;

public class ChangeLanguage {
    Context context;
    Activity activity;
    public ChangeLanguage(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;
    }

    public void chooseLanguage() {
        String en = context.getString(R.string.english);
        String pl = context.getString(R.string.polish);
        final String[] LANGUAGES = {en, pl};

        AlertDialog.Builder chooseLanguage = new AlertDialog.Builder(context);
        chooseLanguage.setTitle(context.getString(R.string.choose_language));

        chooseLanguage.setNegativeButton(context.getString(R.string.back), (dialog, which) -> dialog.dismiss());
        chooseLanguage.setItems(LANGUAGES, (dialog, which) -> switchLanguage(which));

        chooseLanguage.show();
    }

    void switchLanguage(int which) {
        Configuration config = new Configuration();
        switch (which) {
            case 0:
                config.locale = new Locale("en");
                break;
            case 1:
                config.locale = new Locale("pl");
                break;
        }
        context.getResources().updateConfiguration(config, null);
        activity.recreate();
    }
}
