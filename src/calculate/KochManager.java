/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calculate;

import calculate.KochFractal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventHandler;
import jsf31kochfractalfx.JSF31KochFractalFX;
import timeutil.TimeStamp;

/**
 *
 * @author Noah Scharrenberg
 */

public class KochManager {

    private KochFractal koch;
    private JSF31KochFractalFX application;
    private ArrayList<Edge> edgeList;
    public int count = 0;
    private ExecutorService executor = Executors.newFixedThreadPool(3);
    TimeStamp ts = new TimeStamp();
    
    TaskRight tr;
    TaskLeft tl;
    TaskBottom tb;
            
    
    public KochManager(JSF31KochFractalFX application) {
        this.application = application;
        koch = new KochFractal();
        edgeList = new ArrayList<Edge>();
    }
    
    public JSF31KochFractalFX getApplication() {
        return application;
    }
    
    public ArrayList<Edge> getEdgeList() {
        return edgeList;
    }

    public synchronized void increaseCount() 
    {
        count++;
        if(count == 3)
        {
            ts.setEnd();
            application.requestDrawEdges();
            count = 0;
        }
    }
    
    
    public  void changeLevel(int nxt) {
        interrupt();
        edgeList.clear();
        koch.setLevel(nxt);
        ts.init();
        ts.setBegin();
        
        KochFractal koch1 = new KochFractal();
        KochFractal koch2 = new KochFractal();
        KochFractal koch3 = new KochFractal();
        koch1.setLevel(nxt);
        koch2.setLevel(nxt);
        koch3.setLevel(nxt);
        
        tr = new TaskRight(koch1, this);
        tl = new TaskLeft(koch2, this);
        tb = new TaskBottom(koch3, this);
        
        EventHandler doneHandler = new EventHandler() {
            @Override
            public void handle(Event event) {
                if (tl.isDone() && tr.isDone() && tb.isDone()) {

                    tl.setOnSucceeded(null);
                    tb.setOnSucceeded(null);
                    tr.setOnSucceeded(null);

                    try {
                        edgeList.addAll(tl.get());
                        edgeList.addAll(tb.get());
                        edgeList.addAll(tr.get());
                        // draw stuff
                        ts.setEnd();
                        application.clearKochPanel();
                        application.requestDrawEdges();
                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(KochManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
        
        tl.setOnSucceeded(doneHandler);
        tb.setOnSucceeded(doneHandler);
        tr.setOnSucceeded(doneHandler);
        
        executor.submit(tr);
        executor.submit(tl);
        executor.submit(tb);
    }

    public synchronized void addEdge(Edge edge)
    {
         edgeList.add(edge);
         drawEdge();
    }
    
    public synchronized void drawEdge() {
        application.drawEdge(edgeList.get(edgeList.size() - 1));
    }
    
    public synchronized void drawEdges() {
        TimeStamp tsDraw = new TimeStamp();
        tsDraw.setBegin();
        
        
        for(Edge e : edgeList)
        {
            application.drawEdge(e);
        }
        tsDraw.setEnd();
        
        application.setTextDraw(tsDraw.toString());
        application.setTextCalc(ts.toString());
        application.setTextNrEdges(String.valueOf(koch.getNrOfEdges()));
    } 
    
    public void interrupt() {
        try {
            tr.cancel();
            tb.cancel();
            tl.cancel();
            tr = null;
            tb = null;
            tl = null;
        } catch(Exception e) {
            
        }
    }

}
