/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package calculate;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;

/**
 *
 * @author Noah Scharrenberg
 */
public class TaskLeft extends Task<List<Edge>> implements Observer {
    private KochFractal k;
    private KochManager km;
    private ArrayList<Edge> edgeList;
    private int max;
    private boolean allowDrawLine;
    private int current;
    
    public TaskLeft(KochFractal k, KochManager km) {
        this.k = k;
        this.km = km;
        k.addObserver(this);
        max = (int)Math.pow(4, (k.getLevel() - 1));
        allowDrawLine = true;
        current = 0;     
        edgeList = new ArrayList<Edge>();
        
        changeProgressBarFromTask();
    }

    @Override
    public void update(Observable o, Object arg) {
        edgeList.add((Edge)arg);
         drawLine((Edge)arg);
    }

    @Override
    protected List<Edge> call() throws Exception {
        for(int i = 1; i <= max; i++) {
           if(isCancelled()) {
               break;
           }
       }
       
       k.generateLeftEdge();      
       return edgeList;
    }
    
    public void drawLine(final Edge de) {
        if (allowDrawLine) {
            try {
                Thread.sleep(20);
                
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        km.getApplication().drawEdge(de);
                    }
                });
                
                current++;
                if(current == max)
                {
                    for(Edge edge : edgeList)
                    {
                        edge.color = Color.hsb(edge.color.getHue(), 1.0, 1.0);
                    }
                }
                updateProgress(current, max);
                updateMessage(String.valueOf("Nr of Edges: " + current));
            } catch(InterruptedException e) {
                System.out.println("Error: Interrupted");
                allowDrawLine = false;
            }
        }
    }
    
    private void changeProgressBarFromTask() {
        final ProgressBar pl = km.getApplication().getProgressBarLeft();
        final Label ll = km.getApplication().getLabelLeft();
        
        final ReadOnlyDoubleProperty progProp = this.progressProperty();
        final ReadOnlyStringProperty mesProp = this.messageProperty();
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                pl.progressProperty().bind(progProp);
                ll.textProperty().bind(mesProp);
            }
        
        });
    }
    
}
