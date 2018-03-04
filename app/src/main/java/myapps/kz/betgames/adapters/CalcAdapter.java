package myapps.kz.betgames.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;

import myapps.kz.betgames.R;
import myapps.kz.betgames.models.Bet;

/**
 * Created by rauan on 23.07.17.
 */

public class CalcAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Bet> bets;

    public CalcAdapter(Context context, ArrayList<Bet> bets) {
        this.context = context;
        this.bets = bets;
    }

    @Override
    public int getCount() {
        return bets.size();
    }

    @Override
    public Object getItem(int position) {
        return bets.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Bet bet = (Bet) getItem(position);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.item_bet, null);
        }

        LinearLayout betLayout = (LinearLayout) convertView.findViewById(R.id.betLayout);
        if (bet.getNumber() % 2 == 0) {
            betLayout.setBackgroundResource(R.drawable.bg_bet_item);
        } else {
            betLayout.setBackgroundResource(R.drawable.bg_bet_item_dark);
        }

        TextView txtBetNumber = (TextView) convertView.findViewById(R.id.txtBetNumber);
        txtBetNumber.setText(String.valueOf(bet.getNumber()));

        TextView txtBetAmount = (TextView) convertView.findViewById(R.id.txtBetAmount);
        txtBetAmount.setText(String.valueOf(bet.getAmount()));

        TextView txtBetCoef = (TextView) convertView.findViewById(R.id.txtBetCoef);
        txtBetCoef.setText(String.valueOf(bet.getCoef()));

        TextView txtBetProfit = (TextView) convertView.findViewById(R.id.txtBetProfit);
        txtBetProfit.setText(String.valueOf(bet.getProfit()));

        return convertView;
    }
}
