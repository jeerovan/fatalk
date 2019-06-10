package com.kaarss.fatalk;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class AppPreferences {
    public static final String preferenceFileName = "kaarss.freevideotalk";
    public static String internalDir = "dir";
    private static final String TAG = AppPreferences.class.getSimpleName();
    public static List<String> CountriesName = new ArrayList<>();
    public static List<String> CountriesIso = new ArrayList<>();

    public static String getString(String key,String defaultValue) {
        return App.sharedPref.getString(key,defaultValue);
    }
    public static void setString(String key, String value) {
        SharedPreferences.Editor editor = App.sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static int getInt(String key,int defValue) {
        return App.sharedPref.getInt(key, defValue);
    }
    public static void setInt(String key, int value) {
        SharedPreferences.Editor editor = App.sharedPref.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static float getFloat(String key, float defaultValue) {
        return App.sharedPref.getFloat(key, defaultValue);
    }
    public static void setFloat(String key, float value) {
        SharedPreferences.Editor editor = App.sharedPref.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    public static long getLong(String key, long defaultValue) {
        return App.sharedPref.getLong(key, defaultValue);
    }
    public static void setLong(String key, long value) {
        SharedPreferences.Editor editor = App.sharedPref.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return App.sharedPref.getBoolean(key, defaultValue);
    }
    public static void setBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = App.sharedPref.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static void unset(String Key) {
        SharedPreferences.Editor editor = App.sharedPref.edit();
        editor.remove(Key);
        editor.apply();
    }
    public static void clearPreferences() {
        SharedPreferences.Editor editor = App.sharedPref.edit();
        editor.clear();
        editor.apply();
    }

    //returns int between 1 and limit (Inclusive)
    public static int getRandomInt(int limit) {
        Random r = new Random();
        return r.nextInt(limit) + 1;
    }
    public static void increaseDpVersion(){
        setInt(Keys.dpVersion,getInt(Keys.dpVersion,0) + 1);
    }
    public static Bitmap getDrawableBitmap(String uid,int gender) {
        int exist = getInt(uid + "_dummy_image_id",0);
        int drawable;
        if (exist > 0) {
            drawable = getDummyDrawable(exist, gender);
        } else {
            int nextId = getNextDummyNumber(gender);
            setInt(uid + "_dummy_image_id",nextId);
            drawable = getDummyDrawable(nextId,gender);
        }
        return BitmapFactory.decodeResource(App.applicationContext.getResources(),drawable);
    }

    public static int getDummyDrawable(int id, int gender){
        int drawable = 0;
        switch (gender){
            case 1:
                switch (id){
                    case 1:
                        drawable = R.drawable.male1;
                        break;
                    case 2:
                        drawable = R.drawable.male2;
                        break;
                    case 3:
                        drawable = R.drawable.male3;
                        break;
                    case 4:
                        drawable = R.drawable.male4;
                        break;
                    case 5:
                        drawable = R.drawable.male5;
                        break;
                    case 6:
                        drawable = R.drawable.male6;
                        break;
                }
                break;
            case 0:
                switch (id){
                    case 1:
                        drawable = R.drawable.female1;
                        break;
                    case 2:
                        drawable = R.drawable.female2;
                        break;
                    case 3:
                        drawable = R.drawable.female3;
                        break;
                    case 4:
                        drawable = R.drawable.female4;
                        break;
                    case 5:
                        drawable = R.drawable.female5;
                        break;
                    case 6:
                        drawable = R.drawable.female6;
                        break;
                }
                break;
        }
        return drawable;
    }
    private static int getNextDummyNumber(int gender){
        int number = getInt("dummynumber"+gender,0);
        int next = 0;
        if(number == 0 || number == 6){
            next = 1;
        } else {
            next = number + 1;
        }
        setInt("dummynumber"+gender,next);
        return next;
    }

    private static void clearInternal(){
        File dir = App.getDirectory();
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(dir, children[i]).delete();
            }
        }
    }
    public static void setUser(JSONObject message) throws JSONException {
        String userName = message.getString(Keys.userName);
        String userId = message.getString(Keys.userId);
        int userState = message.getInt(Keys.userState);
        String country = message.getString(Keys.country);
        int age = message.getInt(Keys.userAge);
        int gender = message.getInt(Keys.userGender);
        int dpVersion = message.getInt(Keys.dpVersion);
        int profileType = message.getInt(Keys.profileType);
        String answerOne = message.getString(Keys.answerOne);
        String answerTwo = message.getString(Keys.answerTwo);
        String answerThree = message.getString(Keys.answerThree);
        String answerFour = message.getString(Keys.answerFour);
        String userBio = message.getString(Keys.userBio);
        AppPreferences.setString(Keys.userId,userId);
        AppPreferences.setString(Keys.userName,userName);
        AppPreferences.setInt(Keys.userState,userState);
        AppPreferences.setString(Keys.country,country);
        AppPreferences.setInt(Keys.userAge,age);
        AppPreferences.setInt(Keys.userGender,gender);
        AppPreferences.setInt(Keys.dpVersion,dpVersion);
        AppPreferences.setInt(Keys.profileType,profileType);
        AppPreferences.setString(Keys.answerOne,answerOne);
        AppPreferences.setString(Keys.answerTwo,answerTwo);
        AppPreferences.setString(Keys.answerThree,answerThree);
        AppPreferences.setString(Keys.answerFour,answerFour);
        AppPreferences.setString(Keys.userBio,userBio);
        if(userState == 1){
            AppPreferences.setString(Keys.securityState,Keys.settingSecurity);
        }
    }
    public static void saveImage(Bitmap bitmapImage, String filename){
        //Log.i(TAG,"Saving Downloaded Image For : "+ filename);
        File directory = App.getDirectory();
        // Create imageDir
        File mypath = new File(directory,filename);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void unsetUser(){
        AppPreferences.unset(Keys.userId);
        AppPreferences.unset(Keys.userName);
        AppPreferences.unset(Keys.userState);
        AppPreferences.unset(Keys.country);
        AppPreferences.unset(Keys.userAge);
        AppPreferences.unset(Keys.userGender);
        AppPreferences.unset(Keys.dpVersion);
        AppPreferences.unset(Keys.securityState);
        AppPreferences.unset(Keys.atAnswer);
        AppPreferences.unset(Keys.answerOne);
        AppPreferences.unset(Keys.answerTwo);
        AppPreferences.unset(Keys.answerThree);
        AppPreferences.unset(Keys.answerFour);
        AppPreferences.unset(Keys.userBio);
    }
    public static void initialize() {
        CountriesName.add("Afghanistan");
        CountriesIso.add("af");
        CountriesName.add("Albania");
        CountriesIso.add("al");
        CountriesName.add("Algeria");
        CountriesIso.add("dz");
        CountriesName.add("American Samoa");
        CountriesIso.add("as");
        CountriesName.add("Andorra");
        CountriesIso.add("ad");
        CountriesName.add("Angola");
        CountriesIso.add("ao");
        CountriesName.add("Antarctica");
        CountriesIso.add("aq");
        CountriesName.add("Argentina");
        CountriesIso.add("ar");
        CountriesName.add("Armenia");
        CountriesIso.add("am");
        CountriesName.add("Aruba");
        CountriesIso.add("aw");
        CountriesName.add("Australia");
        CountriesIso.add("au");
        CountriesName.add("Austria");
        CountriesIso.add("at");
        CountriesName.add("Azerbaijan");
        CountriesIso.add("az");
        CountriesName.add("Bahrain");
        CountriesIso.add("bh");
        CountriesName.add("Bangladesh");
        CountriesIso.add("bd");
        CountriesName.add("Belarus");
        CountriesIso.add("by");
        CountriesName.add("Belgium");
        CountriesIso.add("be");
        CountriesName.add("Belize");
        CountriesIso.add("bz");
        CountriesName.add("Benin");
        CountriesIso.add("bj");
        CountriesName.add("Bhutan");
        CountriesIso.add("bt");
        CountriesName.add("Bolivia");
        CountriesIso.add("bo");
        CountriesName.add("Bosnia-Herzegovina");
        CountriesIso.add("ba");
        CountriesName.add("Botswana");
        CountriesIso.add("bw");
        CountriesName.add("Brazil");
        CountriesIso.add("br");
        CountriesName.add("Brunei Darussalam");
        CountriesIso.add("bn");
        CountriesName.add("Bulgaria");
        CountriesIso.add("bg");
        CountriesName.add("Burkina Faso");
        CountriesIso.add("bf");
        CountriesName.add("Burundi");
        CountriesIso.add("bi");
        CountriesName.add("Cabo Verde");
        CountriesIso.add("cv");
        CountriesName.add("Cambodia");
        CountriesIso.add("kh");
        CountriesName.add("Cameroon");
        CountriesIso.add("cm");
        CountriesName.add("Canada");
        CountriesIso.add("ca");
        CountriesName.add("Central African Republic");
        CountriesIso.add("cf");
        CountriesName.add("Chad");
        CountriesIso.add("td");
        CountriesName.add("Chile");
        CountriesIso.add("cl");
        CountriesName.add("China");
        CountriesIso.add("cn");
        CountriesName.add("Christmas Island");
        CountriesIso.add("cx");
        CountriesName.add("Cocos (Keeling) Islands");
        CountriesIso.add("cc");
        CountriesName.add("Colombia");
        CountriesIso.add("co");
        CountriesName.add("Comoros");
        CountriesIso.add("km");
        CountriesName.add("Congo");
        CountriesIso.add("cg");
        CountriesName.add("Congo, Dem. Republic");
        CountriesIso.add("cd");
        CountriesName.add("Cook Islands");
        CountriesIso.add("ck");
        CountriesName.add("Costa Rica");
        CountriesIso.add("cr");
        CountriesName.add("Croatia");
        CountriesIso.add("hr");
        CountriesName.add("Cuba");
        CountriesIso.add("cu");
        CountriesName.add("Cyprus");
        CountriesIso.add("cy");
        CountriesName.add("Czechia");
        CountriesIso.add("cz");
        CountriesName.add("Denmark");
        CountriesIso.add("dk");
        CountriesName.add("Djibouti");
        CountriesIso.add("dj");
        CountriesName.add("Dominican Republic");
        CountriesIso.add("do");
        CountriesName.add("Ecuador");
        CountriesIso.add("ec");
        CountriesName.add("Egypt");
        CountriesIso.add("eg");
        CountriesName.add("El Salvador");
        CountriesIso.add("sv");
        CountriesName.add("Equatorial Guinea");
        CountriesIso.add("gq");
        CountriesName.add("Eritrea");
        CountriesIso.add("er");
        CountriesName.add("Estonia");
        CountriesIso.add("ee");
        CountriesName.add("Ethiopia");
        CountriesIso.add("et");
        CountriesName.add("Falkland Islands (Malvinas)");
        CountriesIso.add("fk");
        CountriesName.add("Faroe Islands");
        CountriesIso.add("fo");
        CountriesName.add("Fiji");
        CountriesIso.add("fj");
        CountriesName.add("Finland");
        CountriesIso.add("fi");
        CountriesName.add("France");
        CountriesIso.add("fr");
        CountriesName.add("French Guiana");
        CountriesIso.add("gf");
        CountriesName.add("Gabon");
        CountriesIso.add("ga");
        CountriesName.add("Gambia");
        CountriesIso.add("gm");
        CountriesName.add("Georgia");
        CountriesIso.add("ge");
        CountriesName.add("Germany");
        CountriesIso.add("de");
        CountriesName.add("Ghana");
        CountriesIso.add("gh");
        CountriesName.add("Gibraltar");
        CountriesIso.add("gi");
        CountriesName.add("Great Britain");
        CountriesIso.add("gb");
        CountriesName.add("Greece");
        CountriesIso.add("gr");
        CountriesName.add("Greenland");
        CountriesIso.add("gl");
        CountriesName.add("Guadeloupe (French)");
        CountriesIso.add("gp");
        CountriesName.add("Guatemala");
        CountriesIso.add("gt");
        CountriesName.add("Guinea");
        CountriesIso.add("gn");
        CountriesName.add("Guinea Bissau");
        CountriesIso.add("gw");
        CountriesName.add("Guyana");
        CountriesIso.add("gy");
        CountriesName.add("Haiti");
        CountriesIso.add("ht");
        CountriesName.add("Honduras");
        CountriesIso.add("hn");
        CountriesName.add("Hong Kong");
        CountriesIso.add("hk");
        CountriesName.add("Hungary");
        CountriesIso.add("hu");
        CountriesName.add("Iceland");
        CountriesIso.add("is");
        CountriesName.add("India");
        CountriesIso.add("in");
        CountriesName.add("Indonesia");
        CountriesIso.add("id");
        CountriesName.add("Iran");
        CountriesIso.add("ir");
        CountriesName.add("Iraq");
        CountriesIso.add("iq");
        CountriesName.add("Ireland");
        CountriesIso.add("ie");
        CountriesName.add("Israel");
        CountriesIso.add("il");
        CountriesName.add("Italy");
        CountriesIso.add("it");
        CountriesName.add("Ivory Coast");
        CountriesIso.add("ci");
        CountriesName.add("Japan");
        CountriesIso.add("jp");
        CountriesName.add("Jordan");
        CountriesIso.add("jo");
        CountriesName.add("Kazakhstan");
        CountriesIso.add("kz");
        CountriesName.add("Kenya");
        CountriesIso.add("ke");
        CountriesName.add("Kiribati");
        CountriesIso.add("ki");
        CountriesName.add("Korea-North");
        CountriesIso.add("kp");
        CountriesName.add("Korea-South");
        CountriesIso.add("kr");
        CountriesName.add("Kuwait");
        CountriesIso.add("kw");
        CountriesName.add("Kyrgyzstan");
        CountriesIso.add("kg");
        CountriesName.add("Laos");
        CountriesIso.add("la");
        CountriesName.add("Latvia");
        CountriesIso.add("lv");
        CountriesName.add("Lebanon");
        CountriesIso.add("lb");
        CountriesName.add("Lesotho");
        CountriesIso.add("ls");
        CountriesName.add("Liberia");
        CountriesIso.add("lr");
        CountriesName.add("Libya");
        CountriesIso.add("ly");
        CountriesName.add("Liechtenstein");
        CountriesIso.add("li");
        CountriesName.add("Lithuania");
        CountriesIso.add("lt");
        CountriesName.add("Luxembourg");
        CountriesIso.add("lu");
        CountriesName.add("Macau");
        CountriesIso.add("mo");
        CountriesName.add("Macedonia");
        CountriesIso.add("mk");
        CountriesName.add("Madagascar");
        CountriesIso.add("mg");
        CountriesName.add("Malawi");
        CountriesIso.add("mw");
        CountriesName.add("Malaysia");
        CountriesIso.add("my");
        CountriesName.add("Maldives");
        CountriesIso.add("mv");
        CountriesName.add("Mali");
        CountriesIso.add("ml");
        CountriesName.add("Malta");
        CountriesIso.add("mt");
        CountriesName.add("Marshall Islands");
        CountriesIso.add("mh");
        CountriesName.add("Martinique (French)");
        CountriesIso.add("mq");
        CountriesName.add("Mauritania");
        CountriesIso.add("mr");
        CountriesName.add("Mauritius");
        CountriesIso.add("mu");
        CountriesName.add("Mayotte");
        CountriesIso.add("yt");
        CountriesName.add("Mexico");
        CountriesIso.add("mx");
        CountriesName.add("Micronesia");
        CountriesIso.add("fm");
        CountriesName.add("Moldova");
        CountriesIso.add("md");
        CountriesName.add("Monaco");
        CountriesIso.add("mc");
        CountriesName.add("Mongolia");
        CountriesIso.add("mn");
        CountriesName.add("Montenegro");
        CountriesIso.add("me");
        CountriesName.add("Morocco");
        CountriesIso.add("ma");
        CountriesName.add("Mozambique");
        CountriesIso.add("mz");
        CountriesName.add("Myanmar");
        CountriesIso.add("mm");
        CountriesName.add("Namibia");
        CountriesIso.add("na");
        CountriesName.add("Nauru");
        CountriesIso.add("nr");
        CountriesName.add("Nepal");
        CountriesIso.add("np");
        CountriesName.add("Netherlands");
        CountriesIso.add("nl");
        CountriesName.add("Netherlands Antilles");
        CountriesIso.add("an");
        CountriesName.add("New Caledonia (French)");
        CountriesIso.add("nc");
        CountriesName.add("New Zealand");
        CountriesIso.add("nz");
        CountriesName.add("Nicaragua");
        CountriesIso.add("ni");
        CountriesName.add("Niger");
        CountriesIso.add("ne");
        CountriesName.add("Nigeria");
        CountriesIso.add("ng");
        CountriesName.add("Niue");
        CountriesIso.add("nu");
        CountriesName.add("Norfolk Island");
        CountriesIso.add("nf");
        CountriesName.add("Northern Mariana Islands");
        CountriesIso.add("mp");
        CountriesName.add("Norway");
        CountriesIso.add("no");
        CountriesName.add("Oman");
        CountriesIso.add("om");
        CountriesName.add("Pakistan");
        CountriesIso.add("pk");
        CountriesName.add("Palau");
        CountriesIso.add("pw");
        CountriesName.add("Panama");
        CountriesIso.add("pa");
        CountriesName.add("Papua New Guinea");
        CountriesIso.add("pg");
        CountriesName.add("Paraguay");
        CountriesIso.add("py");
        CountriesName.add("Peru");
        CountriesIso.add("pe");
        CountriesName.add("Philippines");
        CountriesIso.add("ph");
        CountriesName.add("Poland");
        CountriesIso.add("pl");
        CountriesName.add("Polynesia (French)");
        CountriesIso.add("pf");
        CountriesName.add("Portugal");
        CountriesIso.add("pt");
        CountriesName.add("Qatar");
        CountriesIso.add("qa");
        CountriesName.add("Reunion (French)");
        CountriesIso.add("re");
        CountriesName.add("Romania");
        CountriesIso.add("ro");
        CountriesName.add("Russia");
        CountriesIso.add("ru");
        CountriesName.add("Rwanda");
        CountriesIso.add("rw");
        CountriesName.add("Saint Helena");
        CountriesIso.add("sh");
        CountriesName.add("Saint Pierre and Miquelon");
        CountriesIso.add("pm");
        CountriesName.add("Samoa");
        CountriesIso.add("ws");
        CountriesName.add("San Marino");
        CountriesIso.add("sm");
        CountriesName.add("Sao Tome and Principe");
        CountriesIso.add("st");
        CountriesName.add("Saudi Arabia");
        CountriesIso.add("sa");
        CountriesName.add("Senegal");
        CountriesIso.add("sn");
        CountriesName.add("Serbia");
        CountriesIso.add("rs");
        CountriesName.add("Seychelles");
        CountriesIso.add("sc");
        CountriesName.add("Sierra Leone");
        CountriesIso.add("sl");
        CountriesName.add("Singapore");
        CountriesIso.add("sg");
        CountriesName.add("Slovakia");
        CountriesIso.add("sk");
        CountriesName.add("Slovenia");
        CountriesIso.add("si");
        CountriesName.add("Solomon Islands");
        CountriesIso.add("sb");
        CountriesName.add("Somalia");
        CountriesIso.add("so");
        CountriesName.add("South Africa");
        CountriesIso.add("za");
        CountriesName.add("Spain");
        CountriesIso.add("es");
        CountriesName.add("Sri Lanka");
        CountriesIso.add("lk");
        CountriesName.add("Sudan");
        CountriesIso.add("sd");
        CountriesName.add("Suriname");
        CountriesIso.add("sr");
        CountriesName.add("Swaziland");
        CountriesIso.add("sz");
        CountriesName.add("Sweden");
        CountriesIso.add("se");
        CountriesName.add("Switzerland");
        CountriesIso.add("ch");
        CountriesName.add("Syria");
        CountriesIso.add("sy");
        CountriesName.add("Taiwan");
        CountriesIso.add("tw");
        CountriesName.add("Tajikistan");
        CountriesIso.add("tj");
        CountriesName.add("Tanzania");
        CountriesIso.add("tz");
        CountriesName.add("Thailand");
        CountriesIso.add("th");
        CountriesName.add("Togo");
        CountriesIso.add("tg");
        CountriesName.add("Tokelau");
        CountriesIso.add("tk");
        CountriesName.add("Tonga");
        CountriesIso.add("to");
        CountriesName.add("Tunisia");
        CountriesIso.add("tn");
        CountriesName.add("Turkey");
        CountriesIso.add("tr");
        CountriesName.add("Turkmenistan");
        CountriesIso.add("tm");
        CountriesName.add("Tuvalu");
        CountriesIso.add("tv");
        CountriesName.add("U.K.");
        CountriesIso.add("uk");
        CountriesName.add("Uganda");
        CountriesIso.add("ug");
        CountriesName.add("Ukraine");
        CountriesIso.add("ua");
        CountriesName.add("United Arab Emirates");
        CountriesIso.add("ae");
        CountriesName.add("Uruguay");
        CountriesIso.add("uy");
        CountriesName.add("USA");
        CountriesIso.add("us");
        CountriesName.add("Uzbekistan");
        CountriesIso.add("uz");
        CountriesName.add("Vanuatu");
        CountriesIso.add("vu");
        CountriesName.add("Vatican");
        CountriesIso.add("va");
        CountriesName.add("Venezuela");
        CountriesIso.add("ve");
        CountriesName.add("Vietnam");
        CountriesIso.add("vn");
        CountriesName.add("Wallis and Futuna Islands");
        CountriesIso.add("wf");
        CountriesName.add("Yemen");
        CountriesIso.add("ye");
        CountriesName.add("Zambia");
        CountriesIso.add("zm");
        CountriesName.add("Zimbabwe");
        CountriesIso.add("zw");
    }

    public static long getGeneralRequestTimeoutMillis() {
        return getLong(Keys.generalRequestTimeoutMillis,5000);
    }
    public static void addMissedCall(String from) {
        List<String> missedFroms = new ArrayList<>(Arrays.asList(getString("missed_calls","").split("\n")));
        if (!missedFroms.contains(from)) {
            missedFroms.add(from);
        }
        setString("missed_calls", TextUtils.join("\n", missedFroms));
    }
    public static void removeMissedCall(String from) {
        List<String> missedFroms = new ArrayList<>(Arrays.asList(getString("missed_calls","").split("\n")));
        missedFroms.remove(from);
        setString("missed_calls", TextUtils.join("\n", missedFroms));
    }
}
