package com.newbieandy.commons;

/**
 * 命名转换
 * Created by andy on 2016/10/27.
 */
public class NameCaseUtil {

    private static final char SEPARATOR = '_';

    /**
     * 驼峰命名转全大写下划线命名
     */
    public static String camelToUpperUnderlineCase(String name) {
        return camelToUnderlineCase(name).toUpperCase();
    }

    /**
     * 驼峰命名转全小写下划线命名
     */
    public static String camelToUnderlineCase(String name) {
        if (name == null) {
            return null;
        }
        StringBuilder nameBuilder = new StringBuilder();
        boolean upperCase = false;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);

            boolean nextUpperCase = true;

            if (i < (name.length() - 1)) {
                nextUpperCase = Character.isUpperCase(name.charAt(i + 1));
            }

            if ((i >= 0) && Character.isUpperCase(c)) {
                if (!upperCase || !nextUpperCase) {
                    if (i > 0) nameBuilder.append(SEPARATOR);
                }
                upperCase = true;
            } else {
                upperCase = false;
            }

            nameBuilder.append(Character.toLowerCase(c));
        }

        return nameBuilder.toString();
    }

    /**
     * 下划线格式转驼峰格式
     */
    public static String underlineToCamelCase(String name) {
        if (name == null) {
            return null;
        }

        name = name.toLowerCase();

        StringBuilder nameBuilder = new StringBuilder(name.length());
        boolean upperCase = false;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);

            if (c == SEPARATOR) {
                upperCase = true;
            } else if (upperCase) {
                nameBuilder.append(Character.toUpperCase(c));
                upperCase = false;
            } else {
                nameBuilder.append(c);
            }
        }
        return nameBuilder.toString();
    }

    /**
     * 下划线格式转首字母大写的驼峰格式
     */
    public static String underlineToCapitalizeCamelCase(String name) {
        if (name == null) {
            return null;
        }
        name = underlineToCamelCase(name);
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
