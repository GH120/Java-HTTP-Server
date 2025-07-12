package com.example.parser;

import java.util.*;
import java.util.regex.*;
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
    public LinkedList<Token> tokenize(String input) {

        LinkedList<Token> tokens = tokenizePhrase(input);

        // System.out.println(input);
        
        return tokens;
    }

    //Transformar isso num iterator?
    private LinkedList<Token> tokenizePhrase(String phrase) {
        LinkedList<Token> tokens = new LinkedList<>();
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
            String type = rules.entrySet()
                                .stream()
                                .filter(entry -> entry.getValue().pattern().equals(bestRule.pattern()))
                                .findFirst()
                                .map(Map.Entry::getKey)
                                .orElse("undefined");

            tokens.add(new Token(substring, type, index));

            index += substring.length();

            // System.out.println(new Token(substring, type, index));

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

                //indice do match encontrado
                int index = matcher.start();

                //Tamanho do match
                int length = matcher.group().length();

                //Se o índice for menor que o melhor índice
                //Se o índice for igual ao melhor índice e o tamanho é maior que o melhor 
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
        Map<String, Pattern> compiled = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : rawRules.entrySet()) {
            compiled.put(entry.getKey(), Pattern.compile(entry.getValue()));
        }
        return compiled;
    }

    public String testCase(){
        return "";
    }
}