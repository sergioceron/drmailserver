/*
 * ViewTraffic.java
 *
 * Created on 5 de julio de 2006, 19:21
 */

import java.awt.*;
import java.util.*;
/**
 *
 * @author  fernando
 */
public class ViewTraffic extends Thread implements UpdateTraffic {
    
    private Traffic tr = null;
    private int bytes = 0;
    private static ViewTraffic _instance = null;
    
    public ViewTraffic( Traffic _tr ){
        tr = _tr;
        _instance = this;
    }
    
    static public ViewTraffic getInstance(){
        return _instance != null ? _instance : null;
    }
    
    public void run(){
        int xt = 0, yt = 0,
            y = 0, x = 0;
        
        Graphics gr = tr.view.getGraphics();
        //gr.setColor(new Color( 000000 ));
        
        while( true ){
            
            x = new Date().getSeconds();
            y = bytes + 20;
            System.out.println(" x --> " + x + " y --> " + y );
            gr.drawLine( xt, yt, x, y );
            
            xt = x;
            yt = y;
            
        }
    }
    
    public void getData( int _bytes ){
        bytes = _bytes;
    }
}

