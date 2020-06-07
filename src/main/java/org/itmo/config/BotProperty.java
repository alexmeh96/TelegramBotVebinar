package org.itmo.config;

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

    public static final String TOKEN = "1283464616:AAGdDzAlPb_JxQ72VRic9_rN-6WRG7lM2S4";
    public static final String PATH = "src/main";

    public static final String ADMIN = "@MarkStav";
    public static final String SPIKER = "@GPlesovskikh";

    public static final String SPREADSHEET_ID = "1wOOgK2KK6OE7tmLPsJR-_Jt_sBVfCtD0Qk-n1CqZpbc";

    // Время существования вопроса
    public static final Long TIME_QUESTION = 120_000L;

    // Срок сдачи дз
    public static final Long TIME_HW = 120_000L;
    public static final Long TIME_OTHER_HW = 120_000L;

    // Успешное выполнение домашки
    //основа
    public static final int CASH_HW = 0;
    //доп
    public static final int CASH_OTHER_HW = 1;

    // Если не вовремя
    public static final int MINUS_CASH_HW = -5;
    public static final int MINUS_CASH_OTHER_HW = 0;






}
