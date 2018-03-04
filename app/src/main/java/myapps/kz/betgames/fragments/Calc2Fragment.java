package myapps.kz.betgames.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import myapps.kz.betgames.R;
import myapps.kz.betgames.adapters.CalcAdapter;
import myapps.kz.betgames.models.Bet;

/**
 * Created by rauan on 23.07.17.
 */

public class Calc2Fragment extends Fragment {

    private ArrayList<Bet> bets;
    private ListView listCalculator;
    private CalcAdapter adapter;

    private EditText editProfit;
    private EditText editCoef;
    private EditText editAmount;
    private Button plusProfit, minusProfit;
    private Button plusCoef, minusCoef;
    private Button btnCopy;
    private TextView textTotalSpent;


    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    // Setting values
    long val_profit_increment, val_profit_initial, val_last_profit,
            val_total_spent;
    double val_coef_increment, val_coef_initial,
            val_last_coef;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calc, container, false);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = preferences.edit();

        getSettingValues();
        initialiseViews(view);

        return view;
    }

    private void getSettingValues() {
        val_profit_increment = preferences.getLong("val_profit_increment", -1);
        val_coef_increment = Double.longBitsToDouble(preferences.getLong("val_coef_increment", Double.doubleToLongBits(-1)));
        val_profit_initial = preferences.getLong("val_profit_initial", -1);
        val_coef_initial = Double.longBitsToDouble(preferences.getLong("val_coef_initial", Double.doubleToLongBits(-1)));
        val_last_profit = preferences.getLong("val_last_profit2", -1);
        val_last_coef = Double.longBitsToDouble(preferences.getLong("val_last_coef2", Double.doubleToLongBits(-1)));
        val_total_spent = preferences.getLong("val_total_spent2", -1);
    }

    private void initialiseViews(View view) {
        listCalculator = (ListView) view.findViewById(R.id.listCalculator);
        fillListView();

        textTotalSpent = (TextView) view.findViewById(R.id.textTotalSpent);
        textTotalSpent.setText("Общий расход: " + String.valueOf(val_total_spent));

        editProfit = (EditText) view.findViewById(R.id.editProfit);
        editCoef = (EditText) view.findViewById(R.id.editCoef);
        editProfit.setText(String.valueOf(val_last_profit));
        editCoef.setText(String.valueOf(val_last_coef));

        editAmount = (EditText) view.findViewById(R.id.editAmount);

        changeAmountValue();

        plusProfit = (Button) view.findViewById(R.id.plusProfit);
        minusProfit = (Button) view.findViewById(R.id.minusProfit);

        plusCoef = (Button) view.findViewById(R.id.plusCoef);
        minusCoef = (Button) view.findViewById(R.id.minusCoef);

        btnCopy = (Button) view.findViewById(R.id.btnCopy);


        // Listeners
        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editAmount.getText().toString().isEmpty()) {
                    ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", editAmount.getText().toString());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getActivity(), "Скопировано", Toast.LENGTH_LONG).show();
                }

            }
        });

        editProfit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String editProfitText = s.toString();
                if (!editProfitText.isEmpty()) {
                    val_last_profit = Long.parseLong(editProfitText);
                    editor.putLong("val_last_profit2", val_last_profit);
                    editor.apply();
                    changeAmountValue();
                }
            }
        });

        plusProfit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                val_last_profit += val_profit_increment;
                editor.putLong("val_last_profit2", val_last_profit);
                editor.apply();

                editProfit.setText(String.valueOf(val_last_profit));
                changeAmountValue();
            }
        });

        minusProfit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                val_last_profit -= val_profit_increment;
                if (val_last_profit < 0) {
                    val_last_profit = 0;
                }
                editor.putLong("val_last_profit2", val_last_profit);
                editor.apply();

                editProfit.setText(String.valueOf(val_last_profit));
                changeAmountValue();
            }
        });

        editCoef.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String editCoefText = s.toString();
                if (!editCoefText.isEmpty()) {
                    val_last_coef = Double.parseDouble(editCoefText);
                    editor.putLong("val_last_coef2", Double.doubleToRawLongBits(val_last_coef));
                    editor.apply();
                    changeAmountValue();
                }
            }
        });

        plusCoef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double coef = Math.round((val_last_coef + val_coef_increment) * 100.0) / 100.0;
                val_last_coef = coef;
                editor.putLong("val_last_coef2", Double.doubleToRawLongBits(val_last_coef));
                editor.apply();

                editCoef.setText(String.valueOf(val_last_coef));
                changeAmountValue();
            }
        });

        minusCoef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double coef = Math.round((val_last_coef - val_coef_increment) * 100.0) / 100.0;
                val_last_coef = coef;
                if (val_last_coef < 1.5) {
                    val_last_coef = 1.5;
                }
                editor.putLong("val_last_coef2", Double.doubleToRawLongBits(val_last_coef));
                editor.apply();

                editCoef.setText(String.valueOf(val_last_coef));
                changeAmountValue();
            }
        });
    }

    private void changeAmountValue() {
        double amountDouble = (val_total_spent + val_last_profit) / (val_last_coef - 1);
        long amount = Math.round(amountDouble);
        editAmount.setText(String.valueOf(amount));
    }

    public void updateList() {
        String jsonBets = preferences.getString("json_bets2", "");
        if (jsonBets.isEmpty()) {
            bets = new ArrayList<>();
            double amountDouble = (val_total_spent + val_last_profit) / (val_last_coef - 1);
            long amount = Math.round(amountDouble);
            Bet bet = new Bet(bets.size()+1, val_last_profit, val_last_coef, amount);
            bets.add(bet);

            val_total_spent += amount;
            editor.putLong("val_total_spent2", val_total_spent);
            textTotalSpent.setText("Общий расход: " + String.valueOf(val_total_spent));

            changeAmountValue();

            Gson gson = new Gson();
            jsonBets = gson.toJson(bets);
            editor.putString("json_bets2", jsonBets);
            editor.apply();
        } else {
            Type type = new TypeToken<List<Bet>>(){}.getType();
            Gson gson = new Gson();
            bets = gson.fromJson(jsonBets, type);
            double amountDouble = (val_total_spent + val_last_profit) / (val_last_coef - 1);
            long amount = Math.round(amountDouble);
            Bet bet = new Bet(bets.size()+1, val_last_profit, val_last_coef, amount);
            bets.add(bet);

            val_total_spent += amount;
            editor.putLong("val_total_spent2", val_total_spent);
            textTotalSpent.setText("Общий расход: " + String.valueOf(val_total_spent));

            changeAmountValue();

            jsonBets = gson.toJson(bets);
            editor.putString("json_bets2", jsonBets);
            editor.apply();
        }

        fillListView();
    }

    public void fillListView() {
        String jsonBets = preferences.getString("json_bets2", "");
        if (jsonBets.isEmpty()) {
            bets = new ArrayList<>();
            ArrayList<Bet> betsReverse = bets;
            Collections.reverse(betsReverse);
            adapter = new CalcAdapter(getActivity(), betsReverse);
            listCalculator.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } else {
            Type type = new TypeToken<List<Bet>>(){}.getType();
            Gson gson = new Gson();
            bets = gson.fromJson(jsonBets, type);
            ArrayList<Bet> betsReverse = bets;
            Collections.reverse(betsReverse);
            adapter = new CalcAdapter(getActivity(), betsReverse);
            listCalculator.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    public void reset() {
        bets.clear();
        adapter = new CalcAdapter(getActivity(), bets);
        listCalculator.setAdapter(adapter);

        editor.putString("json_bets2", "");
        val_last_profit = val_profit_initial;
        val_last_coef = val_coef_initial;
        val_total_spent = 0;

        editor.putLong("val_last_profit2", val_last_profit);
        editor.putLong("val_last_coef2", Double.doubleToRawLongBits(val_last_coef));
        editor.putLong("val_total_spent2", val_total_spent);
        editor.apply();

        editProfit.setText(String.valueOf(val_last_profit));
        editCoef.setText(String.valueOf(val_last_coef));
        textTotalSpent.setText("Общий расход: " + String.valueOf(val_total_spent));

    }
}
