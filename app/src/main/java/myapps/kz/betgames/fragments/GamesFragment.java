package myapps.kz.betgames.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import myapps.kz.betgames.LoginActivity;
import myapps.kz.betgames.MainActivity;
import myapps.kz.betgames.PaymentActivity;
import myapps.kz.betgames.R;
import myapps.kz.betgames.adapters.GamesAdapter;
import myapps.kz.betgames.interfaces.RequestInterface;
import myapps.kz.betgames.models.Game;
import myapps.kz.betgames.models.User;
import myapps.kz.betgames.networks.JSONResponseGames;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by rauan on 24.06.17.
 */

public class GamesFragment extends Fragment {

    private String BASE_URL;

    private RelativeLayout loadingHorizontalGames;
    private RelativeLayout layoutTariffEnded;
    private Button btnExtendTariffGames;
    private ExpandableListView expandableListView;
    private GamesAdapter adapter;
    private ArrayList<Game> games;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private String token;
    private String userPhone;

    private int lastGroupPos;
    private int lastChildPos;

    private int mInterval; // 5 seconds by default, can be changed later
    private Handler mHandler;

    private int[] timesList;
    private int sel_time_position;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_games, container, false);
        ((MainActivity) getActivity()).setToolbar("Игры", false);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = preferences.edit();

        BASE_URL = getActivity().getResources().getString(R.string.base_url);

        timesList = getActivity().getResources().getIntArray(R.array.refresh_times);
        sel_time_position = preferences.getInt("sel_time_position", -1);
        mInterval = timesList[sel_time_position] * 1000;

        token = preferences.getString("user_token", "");
        userPhone = preferences.getString("user_phone", "");
        loadingHorizontalGames = (RelativeLayout) view.findViewById(R.id.loadingHorizontalGames);
        layoutTariffEnded = (RelativeLayout) view.findViewById(R.id.layoutTariffEnded);
        btnExtendTariffGames = (Button) view.findViewById(R.id.btnExtendTariffGames);
        btnExtendTariffGames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PaymentActivity.class);
                startActivity(intent);
            }
        });
        expandableListView = (ExpandableListView) view.findViewById(R.id.expandableListView);
        games = new ArrayList<>();
        adapter = new GamesAdapter(getActivity(), games, token, GamesFragment.this, BASE_URL);
        expandableListView.setAdapter(adapter);

        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                ((MainActivity)getActivity()).hideSoftKeyboard(getActivity());
                setLastFocused(-1, -1);
                if (parent.isGroupExpanded(groupPosition) == false) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        parent.expandGroup(groupPosition, true);
                    }
                    else {
                        parent.expandGroup(groupPosition);
                    }
                }
                else {
                    parent.collapseGroup(groupPosition);
                }
                return true;
            }
        });

        setLastFocused(-1, -1);

        mHandler = new Handler();
        startRepeatingTask();

        return view;
    }


    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                loadGames(); //this function can change value of mInterval.
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    public void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    public void startRepeatingTask() {
        mStatusChecker.run();
    }

    private void loadGames() {
        openLoading();

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request clientRequest = chain.request().newBuilder()
                                .addHeader("Accept", "Application/JSON")
                                .addHeader("auth-token", token)
                                .build();
                        return chain.proceed(clientRequest);
                    }
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RequestInterface request = retrofit.create(RequestInterface.class);
        Call<JSONResponseGames> call = request.getGames();
        call.enqueue(new Callback<JSONResponseGames>() {
            @Override
            public void onResponse(Call<JSONResponseGames> call, retrofit2.Response<JSONResponseGames> response) {
                if (response.isSuccessful()) {
                    JSONResponseGames jsonResponseGames = response.body();
                    int code = jsonResponseGames.getCode();
                    if (code == 100) {
                        stopRepeatingTask();
                        closeLoading();
                        resetUserDetails();
                        Toast.makeText(getActivity(), "Вы не вошли в систему", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                        //overridePendingTransition(0, 0);
                    } else if (code == 0) {

                        User user = jsonResponseGames.getUser();
                        editor.putString("user_tariff",user.getTariff());
                        editor.putInt("user_tariff_date", user.getTariff_date());
                        editor.apply();

                        if (getActivity() != null) {
                            ((MainActivity) getActivity()).setTariffInfo();
                        }

                        if (user.getTariff_date() == 0) {
                            expandableListView.setVisibility(View.GONE);
                            if (!userPhone.equals("+77001112233")) {
                                layoutTariffEnded.setVisibility(View.VISIBLE);
                            }
                            stopRepeatingTask();
                        } else {
                            expandableListView.setVisibility(View.VISIBLE);
                            layoutTariffEnded.setVisibility(View.GONE);
                            games = new ArrayList<>(Arrays.asList(jsonResponseGames.getGames()));

                            if (expandableListView.getAdapter() == null) {
                                adapter = new GamesAdapter(getActivity(), games, token, GamesFragment.this, BASE_URL);
                                expandableListView.setAdapter(adapter);
                            } else {
                                adapter.refill(games);
                            }


                            for (int i = 0; i < games.size(); i++) {
                                boolean isExpanded = preferences.getBoolean(games.get(i).getName(), true);
                                if (isExpanded) {
                                    expandableListView.expandGroup(i);
                                } else {
                                    expandableListView.collapseGroup(i);
                                }
                            }
                        }

                        closeLoading();
                    }
                } else {
                    if (getActivity() != null) {
                        closeLoading();
                        Toast.makeText(getActivity().getApplicationContext(),
                                "Неизвестная ошибка. Повторите попытку.",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                }
            }

            @Override
            public void onFailure(Call<JSONResponseGames> call, Throwable t) {
                if (getActivity() != null) {
                    closeLoading();
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Проблема с сетью... Проверьте подключение к сети и повторите попытку.",
                            Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
    }


//    Loading panel open-close methods
    public void closeLoading() {
        try {
            loadingHorizontalGames.setVisibility(View.GONE);
        } catch (Exception e) {
        }
    }

    public void openLoading() {
        try {
            loadingHorizontalGames.setVisibility(View.VISIBLE);
        } catch (Exception e) {
        }
    }

    public void resetUserDetails() {
        editor.putString("user_token","");
        editor.putString("user_phone","");
        editor.putString("user_id","");
        editor.putString("user_tariff","");
        editor.putInt("user_tariff_date", -1);
        editor.apply();
    }

    public void setLastFocused(int groupPos, int childPos) {
        lastGroupPos = groupPos;
        lastChildPos = childPos;
    }

    public Pair<Integer, Integer> getLastFocused() {
        return new Pair<>(lastGroupPos, lastChildPos);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopRepeatingTask();
        closeLoading();
    }

    @Override
    public void onResume() {
        super.onResume();
        startRepeatingTask();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }


}
