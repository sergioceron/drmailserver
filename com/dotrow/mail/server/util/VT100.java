/*
 * Copyright DotRow.com (c) 2012.
 *
 * Este programa se distribuye segun la licencia GPL v.2 o posteriores y no
 * tiene garantias de ningun tipo. Puede obtener una copia de la licencia GPL o
 * ponerse en contacto con la Free Software Foundation en http://www.gnu.org
 */

package com.dotrow.mail.server.util;

/**
 * Created by IntelliJ IDEA.
 * User: sergio
 * Date: 7/07/12
 * Time: 11:02 PM
 * To change this template use File | Settings | File Templates.
 */

import java.io.*;
import java.util.Scanner;

public class VT100{

    static public enum Color {
        BLACK   ( 30 ),
        RED     ( 31 ),
        GREEN   ( 32 ),
        YELLOW  ( 33 ),
        BLUE    ( 34 ),
        MAGENTA ( 35 ),
        CYAN    ( 36 ),
        WHITE   ( 37 );

        private int code;

        private Color(int code){
            this.code = code;
        }

        public int getCode(){
            return code;
        }
    }

    public final static Character ESC = '\u001B';

    private PrintWriter out = null;

    public VT100(OutputStream out){
        this.out = new PrintWriter(out, true);
    }

    public void print(String s){
        out.print(s);
    }

    public void gotoxy(int x, int y){
        out.print(ESC + "[" + y + ";" + x + "H");
    }

    public void setColor(Color color){
        out.print(ESC + "[" + color.getCode() + "m");
    }

    public void setBackground(Color color){
        out.print(ESC + "[" + (color.getCode() + 10) + "m");
    }

    public void clearLine(){
        out.print(ESC + "[2K");
    }

    public void clearScreen(){
        out.print(ESC + "[2J");
    }

    public void print(int x, int y, String s){
        out.print(ESC + "[" + y + ";" + x + "H" + s);
    }

    public void flush(){
        out.flush();
    }

    public void reset(){
        out.print(ESC + "[0m");
    }

    public void print(File file){
        try {
            InputStream is = new FileInputStream(file);
            Scanner in = new Scanner(is);
            while( in.hasNextLine() ) {
                String line =  in.nextLine();
                out.println(line);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}