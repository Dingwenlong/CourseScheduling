package com.paike.common.constants;

public class Constants {

    private Constants() {
    }

    public static final String DEFAULT_PASSWORD = "123456";

    public static final String UTF8 = "UTF-8";

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final Integer STATUS_ENABLE = 1;
    public static final Integer STATUS_DISABLE = 0;

    public static final Integer PAGE_SIZE_DEFAULT = 10;
    public static final Integer PAGE_SIZE_MAX = 100;

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_HEADER = "Authorization";

    public static final String REDIS_PREFIX = "course:scheduling:";
    public static final String REDIS_USER_TOKEN = REDIS_PREFIX + "user:token:";
    public static final String REDIS_USER_INFO = REDIS_PREFIX + "user:info:";
    public static final String REDIS_CACHE = REDIS_PREFIX + "cache:";
}
