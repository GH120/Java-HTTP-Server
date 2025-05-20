package com.example.parser;

import java.util.*;
import java.util.regex.*;

class Token {
    String string;
    String type;
    int index;

    public Token(String string, String type, int index) {
        this.string = string;
        this.type = type;
        this.index = index;
    }

    @Override
    public String toString() {
        return "Token(" + string + ", " + type + ", " + index + ")";
    }
}

//Classe traduzida do javascript
//Tentar depois fazer em java do zero, maior empecilho foi sintaxe e conhecimento de bibliotecas regex
class Lexer {
    Map<String, Pattern> rules;
    List<String> separators;

    public Lexer(){

    }

    public Lexer(Map<String, String> rawRules) {
        this(rawRules, Arrays.asList(","));
    }

    public Lexer(Map<String, String> rawRules, List<String> separators) {
        this.rules = toRegex(rawRules);
        this.separators = separators;
    }

    //Implementar uma otimização depois para fazer em chunks de strings
    public List<Token> parse(String input) {
        List<Token> tokens = parsePhrase(input);
        
        return tokens;
    }

    //Transformar isso num iterator?
    private List<Token> parsePhrase(String phrase) {
        List<Token> tokens = new ArrayList<>();
        int index = 0;
        int loop = 0;

        while (index < phrase.length()) {
            loop++;
            Pattern bestRule = longestMatch(phrase.substring(index));

            Matcher searchMatcher = bestRule.matcher(phrase.substring(index));
            if (!searchMatcher.find() || searchMatcher.start() > 0) {
                throw new RuntimeException("Caractere não reconhecido: " + phrase.substring(index));
            }

            String substring = searchMatcher.group();
            String type = rules.entrySet().stream()
                    .filter(entry -> entry.getValue().pattern().equals(bestRule.pattern()))
                    .findFirst()
                    .map(Map.Entry::getKey)
                    .orElse("undefined");

            tokens.add(new Token(substring, type, index));
            index += substring.length();

            System.out.println(new Token(substring, type, index));

            if (loop > 1000) {
                throw new RuntimeException("Loop infinito");
            }
        }

        return tokens;
    }

    private Pattern longestMatch(String phrase) {
        Pattern bestRule = null;
        int bestIndex = Integer.MAX_VALUE;
        int bestLength = -1;

        for (Pattern rule : rules.values()) {
            Matcher matcher = rule.matcher(phrase);
            if (matcher.find()) {
                int index = matcher.start();
                int length = matcher.group().length();

                if (index < bestIndex || (index == bestIndex && length > bestLength)) {
                    bestIndex = index;
                    bestLength = length;
                    bestRule = rule;
                }
            }
        }

        if (bestRule == null) {
            throw new RuntimeException("Nenhuma regra corresponde");
        }

        return bestRule;
    }

    public Map<String, Pattern> toRegex(Map<String, String> rawRules) {
        Map<String, Pattern> compiled = new HashMap<>();
        for (Map.Entry<String, String> entry : rawRules.entrySet()) {
            compiled.put(entry.getKey(), Pattern.compile(entry.getValue()));
        }
        return compiled;
    }
}

public class HttpLexer extends Lexer{

    HttpLexer(){
        super();

        Map<String, String> rules = new HashMap<>();
        rules.put("GET", "GET");
        rules.put("PUT", "PUT");
        rules.put("HEAD", "HEAD");
        rules.put("POST", "POST");
        rules.put("DELETE", "DELETE");
        rules.put("CONNECT", "CONNECT");
        rules.put("OPTIONS", "OPTIONS");
        rules.put("TRACE", "TRACE");
        rules.put("SPACE", "\\s+");
        rules.put("CRLF", "\\r\\n");
        rules.put("VERSION", "HTTP/[0-9]\\.[0-9]");
        rules.put("BAR", "/");
        rules.put("LOCALHOST", "localhost");
        rules.put("NUMBER", "[0-9]+");
        rules.put("CONNECTION", "Connection");
        rules.put("EQUALS", "=");
        rules.put("HOST", "Host");
        rules.put("WORD", "[a-zA-Z0-9\\-_.]+"); // simples e abrangente
        rules.put("DOT", "\\.");
        rules.put("COLON", ":");
        rules.put("ADDRESS", "([0-9a-fA-F]{1,4}:){2,7}[0-9a-fA-F]{1,4}"); // IPv6 básico
        rules.put("SYMBOL", "[;\\+\\*,\\?!]");
        rules.put("LPAR", "\\(");
        rules.put("RPAR", "\\)");
        rules.put("LBRACKET", "\\[");
        rules.put("RBRACKET", "\\]");
        rules.put("QUOTES", "\"");

        this.rules = this.toRegex(rules);
        this.separators = Arrays.asList(",");
    }
}

class TestCase {
    public static void main(String[] args) {

        String input = "GET / HTTP/1.1\r\n" + //
                        "Host:Connection accepted/0:0:0:0:0:0:0:1\r\n" + //
                        " localhost:8080\r\n" + //
                        "Connection: keep-alive\r\n" + //
                        "Cache-Control: max-age=0\r\n" + //
                        "sec-ch-ua: \"Chromium\";v=\"136\", \"Google Chrome\";v=\"136\", \"Not.A/Brand\";v=\"99\"\r\n" + //
                        "sec-ch-ua-mobile: ?0\r\n" + //
                        "sec-ch-ua-platform: \"Windows\"\r\n" + //
                        "Upgrade-Insecure-Requests: 1\r\n" + //
                        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.0.0 Safari/537.36\r\n" + //
                        "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7\r\n" + //
                        "Sec-Fetch-Site: none\r\n" + //
                        "Sec-Fetch-Mode: navigate\r\n" + //
                        "Sec-Fetch-User: ?1\r\n" + //
                        "Sec-Fetch-Dest: document\r\n" + //
                        "Accept-Encoding: gzip, deflate, br, zstd\r\n" + //
                        "Accept-Language: pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7";

        Lexer lexer = new HttpLexer();
        lexer.parse(input);
    }
}
