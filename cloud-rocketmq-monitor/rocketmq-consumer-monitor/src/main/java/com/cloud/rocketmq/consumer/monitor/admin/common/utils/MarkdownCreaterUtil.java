package com.cloud.rocketmq.consumer.monitor.admin.common.utils;

import java.util.Map;

public class MarkdownCreaterUtil {
    public static String listMarkdown(Map<String /*title*/, Map<String, String>/*values*/> map) {
        if (map.isEmpty()) return null;
        int size = map.values().iterator().next().size();
        if (size == 0) return null;

        StringBuffer table = new StringBuffer();
        StringBuffer titleSplit = new StringBuffer();
        String newLine = "\n";
        for (Map.Entry<String /*title*/, Map<String, String>/*values*/> entry : map.entrySet()) {
            String title = entry.getKey();
            table.append(newLine);
            table.append("# ").append(title);
            int i = 1;
            for (Map.Entry<String, String> stringEntry : entry.getValue().entrySet()) {
                table.append(newLine);
                table.append(String.valueOf(i) + ". ").append(stringEntry.getKey()).append(": ").append(stringEntry.getValue());
                i++;
            }
            table.append(newLine);
        }
        return table.toString();
    }
}
