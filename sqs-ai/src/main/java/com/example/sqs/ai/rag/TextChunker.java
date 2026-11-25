package com.example.sqs.ai.rag;

import java.util.ArrayList;
import java.util.List;

/**
 * 文本分块工具：按段落聚合到目标长度，块间保留一定重叠字符。
 */
public final class TextChunker {

    private TextChunker() {
    }

    /**
     * 将文本切分为若干块。
     *
     * @param text     原始文本
     * @param maxChars 单块目标最大长度
     * @param overlap  相邻块重叠字符数（小于 maxChars）
     */
    public static List<String> chunk(String text, int maxChars, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }
        int safeMax = Math.max(50, maxChars);
        int safeOverlap = Math.min(Math.max(0, overlap), safeMax / 2);

        StringBuilder current = new StringBuilder();
        for (String line : text.split("\n")) {
            String trimmed = line.strip();
            if (trimmed.isEmpty()) {
                continue;
            }
            // 单行超长时先整体硬切
            for (String piece : splitLongLine(trimmed, safeMax)) {
                if (current.length() + piece.length() + 1 > safeMax && current.length() > 0) {
                    chunks.add(flush(current));
                    int start = Math.max(0, current.length() - safeOverlap);
                    String tail = current.substring(start);
                    current = new StringBuilder(tail.strip());
                    if (current.length() > 0) {
                        current.append('\n');
                    }
                }
                if (current.length() > 0) {
                    current.append('\n');
                }
                current.append(piece);
            }
        }
        if (current.length() > 0) {
            chunks.add(flush(current));
        }
        return chunks;
    }

    private static List<String> splitLongLine(String line, int maxChars) {
        if (line.length() <= maxChars) {
            return List.of(line);
        }
        List<String> pieces = new ArrayList<>();
        for (int from = 0; from < line.length(); from += maxChars) {
            pieces.add(line.substring(from, Math.min(from + maxChars, line.length())));
        }
        return pieces;
    }

    private static String flush(StringBuilder sb) {
        String flushed = sb.toString().strip();
        sb.setLength(0);
        return flushed;
    }
}
