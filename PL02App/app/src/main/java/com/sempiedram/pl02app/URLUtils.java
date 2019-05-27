
package com.sempiedram.pl02app;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

public class URLUtils {
    public static String urlEncode(String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return ""; // Should never happen, right?
    }

    public static String composeQueryParameters(Map<String, String> parameters) {
        StringBuilder builder = new StringBuilder();

        try {
            Set<String> parameterNames = parameters.keySet();
            int count = 0;

            for (String parameterName : parameterNames) {
                builder.append(URLEncoder.encode(parameterName, "UTF-8"));
                builder.append('=');
                builder.append(URLEncoder.encode(parameters.get(parameterName), "UTF-8"));

                if(count + 1 != parameterNames.size()) {
                    builder.append('&');
                }

                count++;
            }
        }catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return builder.toString();
    }
}
