package com.udacity.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by NUSNAFIF on 1/22/2017.
 */

public class StockWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockRemoteViewFactory(this.getApplicationContext(), intent);
    }
}

class StockRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private Cursor data = null;

    public StockRemoteViewFactory(Context context, Intent intent) {
        mContext = context;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        if (data != null) {
            data.close();
        }

        final long identityToken = Binder.clearCallingIdentity();

        data = mContext.getContentResolver().query(Contract.Quote.URI,
                null,
                null,
                null,
                Contract.Quote.COLUMN_SYMBOL);
        Binder.restoreCallingIdentity(identityToken);
    }

    @Override
    public void onDestroy() {
        if (data != null) {
            data.close();
            data = null;
        }
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION || data == null || !data.moveToPosition(position)) {
            return null;
        }

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.stock_widget_list_item);
        views.setTextViewText(R.id.widget_symbol, data.getString(Contract.Quote.POSITION_SYMBOL));

        DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        DecimalFormat percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");
        views.setTextViewText(R.id.widget_price, dollarFormat.format(data.getFloat(Contract.Quote.POSITION_PRICE)));

        float rawAbsoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float percentageChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

        if (rawAbsoluteChange > 0) {
            views.setInt(R.id.widget_change, "setBackgroundResource", R.drawable.percent_change_pill_green);
        } else {
            views.setInt(R.id.widget_change, "setBackgroundResource", R.drawable.percent_change_pill_red);
        }

        String change = dollarFormatWithPlus.format(rawAbsoluteChange);
        String percentage = percentageFormat.format(percentageChange / 100);

        if (PrefUtils.getDisplayMode(mContext).equals(mContext.getString(R.string.pref_display_mode_absolute_key))) {
            views.setTextViewText(R.id.widget_change, change);
        } else {
            views.setTextViewText(R.id.widget_change, percentage);
        }

        // Set a fill-intent
        Intent fillInIntent = new Intent();
        fillInIntent.putExtra("symbol", data.getString(Contract.Quote.POSITION_SYMBOL));
        views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews(mContext.getPackageName(), R.layout.stock_widget_list_item);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
