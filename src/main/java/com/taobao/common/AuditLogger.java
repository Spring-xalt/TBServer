package com.taobao.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 业务审计日志工具
 * 用法：AuditLogger.log("用户登录 | userId={} | ip={}", userId, ip)
 * 输出到 logs/TBServer-audit.log
 */
public class AuditLogger {
    private static final Logger log = LoggerFactory.getLogger("AUDIT");

    public static void log(String format, Object... args) {
        log.info(format, args);
    }

    public static void warn(String format, Object... args) {
        log.warn(format, args);
    }

    public static void error(String format, Object... args) {
        log.error(format, args);
    }
}
