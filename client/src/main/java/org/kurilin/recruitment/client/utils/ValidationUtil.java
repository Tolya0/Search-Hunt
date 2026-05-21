package org.kurilin.recruitment.client.utils;

import java.time.LocalDate;
import java.util.regex.Pattern;

public class ValidationUtil {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{9,15}$");
    private static final Pattern LOGIN_PATTERN = Pattern.compile("^[A-Za-z0-9]{3,20}$");

    private ValidationUtil() {}

    public static boolean isNullOrBlank(String str) {
        return str == null || str.isBlank();
    }

    public static boolean isValidLogin(String login) {
        if (isNullOrBlank(login)) return false;
        return LOGIN_PATTERN.matcher(login.strip()).matches();
    }

    public static boolean isValidPassword(String password) {
        return !isNullOrBlank(password) && password.length() >= 6;
    }
    public static boolean isValidPasswordConfirmation(String password, String passwordConfirmation) {
        return !isNullOrBlank(passwordConfirmation) && password.equals(passwordConfirmation);
    }

    public static boolean isValidEmail(String email) {
        if (isNullOrBlank(email)) return false;
        return EMAIL_PATTERN.matcher(email.strip()).matches();
    }

    public static boolean isValidFullName(String name) {
        return !isNullOrBlank(name) && name.strip().length() >= 2;
    }

     public static boolean isValidPhone(String phone) {
        if (isNullOrBlank(phone)) return false;
        String cleanedPhone = phone.strip().replaceAll("[\\s-]", "");
        return PHONE_PATTERN.matcher(cleanedPhone).matches();
    }

    public static boolean isValidVacancyText(String text) {
        return !isNullOrBlank(text) && text.strip().length() >= 3 && text.strip().length() <= 1000;
    }

    public static boolean isValidSalary(String minSalary, String maxSalary) {
        if (isNullOrBlank(minSalary) || isNullOrBlank(maxSalary)) return false;
        try {
            int min = Integer.parseInt(minSalary.strip());
            int max = Integer.parseInt(maxSalary.strip());
            return min >= 0 && min <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isComboSelected(Object selection) {
        return selection != null;
    }

    public static boolean isValidDate(LocalDate date) {
        if (date == null) return false;
        return date.isBefore(LocalDate.now().minusYears(14));
    }
}
