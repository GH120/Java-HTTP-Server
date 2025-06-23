package com.example.parser;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class ChessNotationLexer extends Lexer{

    {
        //Obs: ordem de inserção importa. Em caso de empate, a primeira inserida é a escolhida
        Map<String, String> rules = new LinkedHashMap<>();
        rules.put("PIECE", "N|Q|K|B|R");
        rules.put("POSITION", "[a-h][1-8]");
        rules.put("COLUMN", "[a-h]");
        rules.put("KINGSIDE", "O-O");
        rules.put("QUEENSIDE", "O-O-O");
        rules.put("CHECK", "\\+");
        rules.put("CHECKMATE", "#");
        rules.put("ATTACK", "x");
        rules.put("PROMOTION_EVENT", "=");
       

        this.rules = this.toRegex(rules);
        this.separators = Arrays.asList(",");
    }

}
