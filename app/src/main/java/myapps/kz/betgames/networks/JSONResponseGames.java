package myapps.kz.betgames.networks;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import myapps.kz.betgames.models.Game;
import myapps.kz.betgames.models.User;

/**
 * Created by rauan on 25.06.17.
 */

public class JSONResponseGames {
    @SerializedName("code")
    @Expose
    private int code;

    @SerializedName("user")
    @Expose
    private User user;

    @SerializedName("games")
    @Expose
    private Game[] games;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Game[] getGames() {
        return games;
    }

    public void setGames(Game[] games) {
        this.games = games;
    }
}
