package com.example.addon.util;

import com.example.addon.AddonTemplate;

public class LogUtil {
    public static void info(String msg) {
        AddonTemplate.LOG.info("{} {}", MsgUtil.getRawPrefix(), msg);
    }
    public static void info(String msg, String module) {
        AddonTemplate.LOG.info("{}{} {}", MsgUtil.getRawPrefix(), MsgUtil.getRawPrefix(module), msg);
    }
    public static void warn(String msg) {
        AddonTemplate.LOG.warn("{} {}", MsgUtil.getRawPrefix(), msg);
    }
    public static void warn(String msg, String module) {
        AddonTemplate.LOG.warn("{}{} {}", MsgUtil.getRawPrefix(), MsgUtil.getRawPrefix(module), msg);
    }
    public static void error(String msg) {
        AddonTemplate.LOG.error("{} {}", MsgUtil.getRawPrefix(), msg);
    }
    public static void error(String msg, String module) {
        AddonTemplate.LOG.error("{}{} {}", MsgUtil.getRawPrefix(), MsgUtil.getRawPrefix(module), msg);
    }
    public static void debug(String msg) {
        AddonTemplate.LOG.debug("{} {}", MsgUtil.getRawPrefix(), msg);
    }
    public static void debug(String msg, String module) {
        AddonTemplate.LOG.debug("{}{} {}", MsgUtil.getRawPrefix(), MsgUtil.getRawPrefix(module), msg);
    }
}
