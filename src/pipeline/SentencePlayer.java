package pipeline;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

import jm.util.Play;
import musicgenerator.MusicGenerator;
	
@SuppressWarnings("serial")
public class SentencePlayer extends javax.swing.JFrame  {
	
    private static WatchService watcher;
    MusicGenerator Generator = new MusicGenerator();
    private SwingWorker<Void,String> worker;
    
    //UI Components
    private JTextArea textArea;
    private Highlighter highlighter;
    HighlightPainter painter;
    private StringBuilder inputString;
    
    private void initComponents() {

        textArea = new JTextArea(10, 30);

        String text = "hello world. How are you?";

        textArea.setText(text);

        highlighter = textArea.getHighlighter();
        painter = new DefaultHighlighter.DefaultHighlightPainter(Color.pink);
        int p0 = text.indexOf("world");
        int p1 = p0 + "world".length();
        try {
			highlighter.addHighlight(p0, p1, painter );
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

    }

    void Stop(){
        // TODO add your handling code here:
        worker.cancel(true);
        Generator.scr.empty();
    }

    void Start() {
        // TODO add your handling code here:
    
        worker = new SwingWorker<Void, String>(){
            @Override
            protected Void doInBackground() throws Exception {
                Generator.scr.empty();
                Generator.Generate();
                Play.midi(Generator.scr);
                return null;
            }
        };
        worker.execute();
    }
    
    void SetEmotion(String mood) {
    	//TODO
    }
    
	public static void main(String[]args) {
		System.out.println("Running");
		
		SentencePlayer player = new SentencePlayer();
		player.initComponents();
	    
		String path = System.getProperty("user.dir");
		if (args.length > 0) {
			if (args[0]!=null)
				path = args[0];
		}
		System.out.println("Attempting to watch directory "+path);
        try {
			watcher = FileSystems.getDefault().newWatchService();
	        Path dir = Paths.get(path);
	        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY);//, ENTRY_MODIFY);
			System.out.print("Watching directory "+dir.toAbsolutePath().toString());
	        player.inputString = new StringBuilder();
	        //put poller in new thread called mood-change reader
	        new Thread(new Runnable() {

				@Override
				public void run() {
			        while(true){
			            for (WatchEvent<?> event: key.pollEvents()) {
			                WatchEvent.Kind<?> kind = event.kind();
			                if (kind == OVERFLOW) {
			                    continue;
			                }
			                @SuppressWarnings("unchecked")
							WatchEvent<Path> ev = (WatchEvent<Path>)event;
			                Path filename = ev.context();
			            	//change mood
			            	File file = new File(filename.toAbsolutePath().toString()); 
			            	System.out.println("New changes spotted in "+file.getAbsolutePath());
			            	  
			            	  BufferedReader br;
							try {
								br = new BufferedReader(new FileReader(file));
				            	  String st; 
				            	  while ((st = br.readLine()) != null) {
				            	    System.out.println(st); 

				            	  try {
					          			player.highlighter.addHighlight(0, player.inputString.length(), player.painter );
					          		} catch (BadLocationException e) {
					          			e.printStackTrace();
					          		}
				            	  player.inputString.append(st);
				            	  

				            	  }
				            	  player.textArea.setText(player.inputString.toString());

				                  
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} 
			            	  
			            }
			        }
					
				}
	        }, "mood-change reader").start();
	        SwingWorker<Void, String> worker = new SwingWorker<Void, String>(){
	            @Override
	            protected Void doInBackground() throws Exception {
	    	        JOptionPane.showMessageDialog(null, new JScrollPane(player.textArea));  
	                return null;
	            }
	        };
	        worker.execute();
	        
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
	}

}
