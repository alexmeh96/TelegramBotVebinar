package org.itmo.config;

import java.util.HashMap;
import java.util.Map;

public class BotProperty {
    //колонки новой таблицы
    public static final byte SHEET_NAME_COL=0;
    public static final byte SHEET_EMAIL_COL=1;
    public static final byte SHEET_PASS_COL=2;
    public static final byte SHEET_PHONE_COL=3;
    public static final byte SHEET_USERNAME_COL=4;
    public static final byte SHEET_ROLE_COL=5;
    public static final byte SHEET_CASH_COL=6;
    public static final byte SHEET_TARIF_COL=7;
    public static final byte SHEET_CHAT_ID_COL=8;

    public static int ID_ROW=2;

    public static final String TOKEN = "1158197395:AAGIe0V25U0FgH9SuYkuFfz80EYii76cd7Q";
    public static final String PATH = "src/main";

    public static final String ADMIN = "@MarkStav";
    public static final String SPIKER = "@GPlesovskikh";

    public static final String SPREADSHEET_ID = "1wOOgK2KK6OE7tmLPsJR-_Jt_sBVfCtD0Qk-n1CqZpbc";

    // Время существования вопроса
    public static final Long TIME_QUESTION = 86_400_000L;

    // Срок сдачи дз
    public static Map<String, Long> map = new HashMap<>();
    static {
        map.put("1", 86_400_000L);   //1
        map.put("2", 259_200_000L);   //3
        map.put("3", 86_400_000L);    //1
        map.put("4", 345_600_000L);    //4

    }

    public static final Long TIME_OTHER_HW = 86_400_000L;

    // Успешное выполнение домашки
    //основа
    public static final int CASH_HW = 0;
    //доп
    public static final int CASH_OTHER_HW = 1;

    // Если не вовремя
    public static final int MINUS_CASH_HW = -5;
    public static final int MINUS_CASH_OTHER_HW = 0;






}
