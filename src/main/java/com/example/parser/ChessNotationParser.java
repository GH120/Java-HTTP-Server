package com.example.parser;

import java.util.LinkedList;


public class ChessNotationParser{

    LinkedList<Token> tokens;
    TreeBuilder treeBuilder = new TreeBuilder();

    public ChessNotationParser parse(LinkedList<Token> tokens) {

        this.tokens = tokens;

        try {

            //Símbolos não terminais são métodos recursivos
            MOVE();
            
            System.out.println("Parsing concluído com sucesso.");
        } catch (ParseException e) {

            e.printStackTrace();
        } 

        return this;
    }

    //CONSOME UM TOKEN (TERMINAL) SE ELE FOR DO TIPO ESPERADO, SENÃO PARA EXECUÇÃO 
    private Token eat(String expected) throws ParseException {

        if (tokens.isEmpty()) throw new ParseException();

        Token token = tokens.poll();

        if (token.type.equals(expected)) {
            treeBuilder.insertToken(token);
            return token;
        } else 
            throw new ParseException();
        
    }

    /**********************************************************/
    /*MÉTODOS DOS SÍMBOLOS NÃO TERMINAIS DA NOTAÇÃO DO XADREZ */
    /**********************************************************/
    private void MOVE() throws ParseException{

        treeBuilder.startContext("MOVE");

        switch(nextToken().type){

            case "PIECE"     -> PIECE_MOVE();
            case "POSITION"  -> SIMPLE_MOVE();
            case "QUEENSIDE" -> CASTLE();
            case "KINGSIDE"  -> CASTLE();
        }   

        PROMOTION();

        if(!OPTIONAL("CHECK"))
            OPTIONAL("CHECKMATE");

        treeBuilder.endContext();

    }

    private void PIECE_MOVE() throws ParseException{

        treeBuilder.startContext("PIECE_MOVE");

        eat("PIECE");

        OPTIONAL("ATTACK");
        
        switch(nextToken().type){

            case "COLUMN"    -> eat("COLUMN");
            case "POSITION"  -> eat("POSITION");
        }  

        treeBuilder.endContext();
    }

    private void SIMPLE_MOVE() throws ParseException{

        treeBuilder.startContext("SIMPLE_MOVE");

        eat("POSITION");

        OPTIONAL("ATTACK");

        treeBuilder.endContext();

    }

    private void CASTLE() throws ParseException{

        treeBuilder.startContext("CASTLE");

        boolean isKingside = nextToken().type.equals("KINGSIDE");

        OPTIONAL("ATTACK");
        
        if(isKingside) eat("KINGSIDE");
        else           eat("QUEENSIDE");

        treeBuilder.endContext();
    }

    private void PROMOTION() throws ParseException{

        treeBuilder.startContext("PROMOTION");

        if(OPTIONAL("PROMOTION_EVENT")){
            eat("PIECE");
        }
        
        treeBuilder.endContext();
    }

    //Come o proximo token se for do tipo esperado, mas não joga erro caso contrário. 
    //Retorna se token era do tipo esperado
    private boolean OPTIONAL(String tokenType) throws ParseException{

        Token token = tokens.peek();

        if(token == null) return false;

        boolean isType = token.type.equals(tokenType);

        if(isType) eat(tokenType);

        return isType;
    }

    private Token nextToken() throws ParseException{
        Token next = tokens.peek();

        if(next == null) throw new ParseException();

        return next;
    }

    public TreeNode getTree() {
        return treeBuilder.getTree();
    }

    class ParseException extends Exception{

    }

}
