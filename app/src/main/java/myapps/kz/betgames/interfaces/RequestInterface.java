package myapps.kz.betgames.interfaces;

import myapps.kz.betgames.networks.JSONResponseGames;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by rauan on 25.06.17.
 */

public interface RequestInterface {
    @GET("api/game/info")
    Call<JSONResponseGames> getGames();
}
