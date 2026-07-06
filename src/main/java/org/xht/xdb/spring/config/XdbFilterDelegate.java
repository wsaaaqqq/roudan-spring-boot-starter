package org.xht.xdb.spring.config;

import org.xht.xdb.Xdb;

public class XdbFilterDelegate {

    private XdbFilterDelegate() {
    }

    public static void doFilter() {
        Xdb.selectDataSourceDefault();
    }
}
