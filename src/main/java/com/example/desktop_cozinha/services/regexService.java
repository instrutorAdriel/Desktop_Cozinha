package com.example.desktop_cozinha.services;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class regexService {
    private static final  String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

    public static boolean emailValidation(String email) {
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

}
