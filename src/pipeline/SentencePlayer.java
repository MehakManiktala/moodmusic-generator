package pipeline;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

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
	
public class SentencePlayer {
	
    private static WatchService watcher;
    
	public static void main(String[]args) {
		System.out.println("Running");

	    
		String path = System.getProperty("user.dir");
		if (args.length > 0) {
			if (args[0]!=null)
				path = args[0];
		}
		System.out.println("Attempting to watch directory "+path);
        try {
			watcher = FileSystems.getDefault().newWatchService();
	        Path dir = Paths.get(path);
	        WatchKey key = dir.register(watcher, ENTRY_CREATE);//, ENTRY_MODIFY);
			System.out.print("Watching directory "+dir.toAbsolutePath().toString());
	        
	        //put poller in new thread called mood-change reader
	        new Thread(new Runnable() {


				@Override
				public void run() {
			        for (int i = 10; i < 20; i++) {
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
			            	  
			            	  BufferedReader br;
							try {
								br = new BufferedReader(new FileReader(file));
				            	  String st; 
				            	  while ((st = br.readLine()) != null) 
				            	    System.out.println(st); 
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} 
			            	  
			            }
			        }
					
				}
	        }, "mood-change reader").start();
	        
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
}
