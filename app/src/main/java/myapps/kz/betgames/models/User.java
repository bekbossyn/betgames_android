package myapps.kz.betgames.models;

/**
 * Created by rauan on 25.06.17.
 */

public class User {

    private String phone;
    private int user_id;
    private String tariff;
    private int tariff_date;
    private String sound_name;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getTariff() {
        return tariff;
    }

    public void setTariff(String tariff) {
        this.tariff = tariff;
    }

    public int getTariff_date() {
        return tariff_date;
    }

    public void setTariff_date(int tariff_date) {
        this.tariff_date = tariff_date;
    }

    public String getSound_name() {
        return sound_name;
    }

    public void setSound_name(String sound_name) {
        this.sound_name = sound_name;
    }
}
