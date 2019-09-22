package io.p13i.ra.utils;

import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HTML {
    public static String text(String html) {
        Document document = Jsoup.parse(html);
        Elements paragraphs = document.select("p");

        StringBuilder sb = StringUtil.borrowBuilder();
        for (Element element : paragraphs) {
            if (sb.length() != 0)
                sb.append("\n");
            sb.append(element.text());
        }

        return StringUtil.releaseBuilder(sb);
    }
}
