package myapps.kz.betgames.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import myapps.kz.betgames.MainActivity;
import myapps.kz.betgames.PaymentActivity;
import myapps.kz.betgames.R;

/**
 * Created by rauan on 28.07.17.
 */

public class CalcEmptyFragment extends Fragment {

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private String userPhone;

    private Button btnExtendTariffCalc;
    private LinearLayout layoutExtendCalc;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calcempty, container, false);

        ((MainActivity) getActivity()).setToolbar("Калькулятор", false);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = preferences.edit();
        userPhone = preferences.getString("user_phone", "");

        layoutExtendCalc = (LinearLayout) view.findViewById(R.id.layoutExtendCalc);
        if (userPhone.equals("+77001112233")) {
            layoutExtendCalc.setVisibility(View.GONE);
        }
        btnExtendTariffCalc = (Button) view.findViewById(R.id.btnExtendTariffCalc);
        btnExtendTariffCalc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PaymentActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_refresh).setVisible(false);
        menu.findItem(R.id.action_silent).setVisible(false);
        menu.findItem(R.id.action_reset).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

}
