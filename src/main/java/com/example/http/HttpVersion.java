package com.example.http;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum HttpVersion {
    HTTP_1_1("HTTP/1.1", 1, 1);


    public final String LITERAL;
    public final int MAJOR;
    public final int MINOR;

    HttpVersion(String LITERAL, int MAJOR, int MINOR){
        this.LITERAL = LITERAL;
        this.MAJOR   = MAJOR;
        this.MINOR   = MINOR;
    }

    //Dúvida, o que esse ?< faz e qual sua relação com o groupCount
    private static final Pattern httpVersionRegex = Pattern.compile("HTTP/(?<major>\\d+).(?<minor>\\d+)*");

    public static HttpVersion getBestCompatibleVersion(String literalVersion) throws HttpParseException {
        Matcher matcher = httpVersionRegex.matcher(literalVersion);

        if(!matcher.find() || matcher.groupCount() != 2){
            throw new HttpParseException(HttpStatusCode.SERVER_ERROR_500_INTERNAL_SERVER_ERROR);
        }
        int major = Integer.parseInt(matcher.group("major"));
        int minor = Integer.parseInt(matcher.group("minor"));

        HttpVersion tempBestCompatible = null;
        for (HttpVersion version: HttpVersion.values()){

            if (version.LITERAL.equals(literalVersion)) 
                return version;

            if(version.MAJOR == major)
                if(version.MINOR < minor)
                    tempBestCompatible = version;
        }

        return tempBestCompatible;
    }
}
