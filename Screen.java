

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;

import javax.swing.SwingUtilities;

import javax.swing.JPanel;
import java.util.*;

public class Screen extends JPanel implements Runnable{

	private static final long serialVersionUID = 1L;
	
	Thread thread;
			
	private static final int WIDTH = 600;
	private static final int HEIGHT = 600;
	private final int BOX_SIZE = 10;
	
	private boolean running;
	private boolean sourceBlock = false;
	private boolean endBlock = false;
	private boolean noMoreObstacles = false;
	
	private int sourceBlockX;
	private int sourceBlockY;
	private int endBlockX;
	private int endBlockY;
	
	HashSet<Integer> obstacles = new HashSet<>();
	HashMap<Integer, Boolean> tokens = new HashMap<>();
	
	private MouseAndKey mouseAndKey;	

	private int [][] matrix;
	private int N;
	private int oo = (int)1e9; //'infinity' i.e. large constant
	private int numCols;
	private int numRows;
	private int sourceID;
	private int startNode;
	private int endID;
	private int prevSourceID;
	int visitedTokens;
	private boolean [] visited;
	private ArrayList<Vertex> pathRecovery;
	private ArrayList<Integer> realPath =new ArrayList<>();
	int id;
	
	// Toggle this variable, if true then this assumes edge weights are unequal so Djikstra's algorithm in ran,
	// if false, then it assumes all edge weights are equal therefore it runs Breadth-First Search	
	// ** Disclaimer: tokens are ignored in Djikstra's algorithm
	boolean DJIKSTRA = true;
	
    Screen()
    {
    	
    	mouseAndKey = new MouseAndKey();
		addKeyListener(mouseAndKey);
		addMouseMotionListener(mouseAndKey);
		addMouseListener(mouseAndKey);		
        setFocusable(true);
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		numRows = (HEIGHT / BOX_SIZE);
        numCols = (WIDTH / BOX_SIZE);
		start();

    }
   
    // Runs djikstra's shortest path algorithm
    public void runDjikstra()
    {
    	
    	int [] dist = new int[N];
		Arrays.fill(dist, oo);
		dist[sourceID] = 0;
		PriorityQueue<Vertex> minheap = new PriorityQueue<>();	
		minheap.add(new Vertex(sourceID, dist[sourceID], sourceID));
		pathRecovery.set(sourceID, (new Vertex(sourceID, dist[sourceID], sourceID)));
    	
    	while(!minheap.isEmpty())
    	{
    		 Vertex v = minheap.remove();
    		 
    		if(obstacles.contains(v.id))
    			continue;

    		visited[v.id] = true;
    		repaint();
    		
    		// Delay to visualize
			try {
				Thread.sleep(1);
			} catch ( Exception e){
				Thread.currentThread().interrupt();
			}
    		
    		for(int i = 0; i < N; i++)    		    			
    			if (matrix[v.id][i] > 0 && dist[v.id] + matrix[v.id][i] < dist[i])
				{
					dist[i] = dist[v.id] + matrix[v.id][i];
					minheap.add(new Vertex(i, dist[i], v.id));
					if(pathRecovery.get(i) != null)
						continue;
					pathRecovery.set(i, new Vertex(i, dist[i], v.id));
				}
    		
    	}
    	
    	// Recover the shortest path and save it
		Vertex temp = pathRecovery.get(endID);
		ArrayList<Integer> buffer = new ArrayList<>();
		
		while(temp.id != sourceID)
		{
			buffer.add(temp.id);
			temp = pathRecovery.get(temp.prev);
		}
		buffer.add(temp.id);
		
		for(int i = 0; i < buffer.size(); i++)
			realPath.add(buffer.get(i));
		
		stop();
		
    }
    
    // Runs BFS for shortest path if edges weight are all equal in cost
    public void bfs()
    {
    	
    	Queue<Vertex> q = new LinkedList<>();	
    	visited[sourceID] = true;    	
    	q.add(new Vertex(sourceID, -1, prevSourceID));
    	pathRecovery.set(sourceID, (new Vertex(sourceID, -1, prevSourceID)));
    	
    	while(!q.isEmpty())
    	{
    		Vertex v = q.remove();
    		
    		if(obstacles.contains(v.id))
    			continue;    		

    		if(v.id == endID && visitedTokens == 0)
    		{
    			
    			Vertex temp = v;
    			ArrayList<Integer> buff = new ArrayList<>();
    			while(temp.id != sourceID)
    			{
    				
    				buff.add(temp.id);
    				temp = pathRecovery.get(temp.prev);
    			}
    			buff.add(temp.id);
    			for(int i = 0; i <buff.size(); i++)
    				realPath.add(buff.get(i));
    			
    			stop();
    			
    		}
    		if(tokens.containsKey(v.id) && !tokens.get(v.id))
    		{
    			
    			visitedTokens--;
    			tokens.put(v.id, true);
    			
    			Vertex temp = v;
    			ArrayList<Integer> buff = new ArrayList<>();
    			while(temp.id != sourceID)
    			{
    				
    				buff.add(temp.id);
    				temp = pathRecovery.get(temp.prev);
    			}
    			
    			sourceID = v.id;
    			prevSourceID = v.prev;
    		
    			for(int i = 0; i <buff.size(); i++)
    				realPath.add(buff.get(i));
    			
    			Arrays.fill(visited, false);
    			repaint();
    			visited[startNode] = true;
    			
    			bfs();
    			return;
    			
    		}
    		
    		visited[v.id] = true;
    		repaint();
    		
    		// Delay to visualize
			try {
				Thread.sleep(1);
			} catch ( Exception e) {
				Thread.currentThread().interrupt();
			}
    		
    		for(int i = 0; i < N; i++) 
    		{   			
    			if(i == endID && visitedTokens != 0)
    				continue;
    			
    			if(matrix[v.id][i] > 0 && !visited[i])
    			{
    				visited[i] = true;
    				q.add(new Vertex(i, -1, v.id));   				
    				pathRecovery.set(i, (new Vertex(i, -1, v.id)));
    			}
    		}
    	}
    }
    
  
    public void tick()
    {
    	// Wait until user has place start and end blocks
    	while (!sourceBlock || !endBlock)
        	repaint();        	
				 	
		N = (HEIGHT / BOX_SIZE) * (WIDTH / BOX_SIZE);		
		matrix = new int [N][N];
        
	
        // Turn screen grid into adjacency matrix representation
		for (int r = 0; r < N; r++)
		{
			int right = r + 1;
			int down = r + numCols;
			int up = r - numCols;
			int left = r - 1;			
			
			// Initializes path weights
			if (right < N && right % numCols != 0 && !obstacles.contains(right))
				matrix[r][right] = DJIKSTRA ? (int)(Math.random() * 50 + 1) : 1;;			
			
			if (down < N && !obstacles.contains(down))
				matrix[r][down] = DJIKSTRA ? (int)(Math.random() * 50 + 1) : 1;;			
		
			if (left >= 0 && r % numCols != 0 && !obstacles.contains(left))
				matrix[r][left] = DJIKSTRA ? (int)(Math.random() * 50 + 1) : 1;;			
			
			if (up >= 0 && !obstacles.contains(up))
				matrix[r][up] = DJIKSTRA ? (int)(Math.random() * 50 + 1) : 1;;
		}
		
		// If Vertex is obstacle remove its adjacent vertices
		for (int i = 0; i < N; i++)
			if (obstacles.contains(i))
				Arrays.fill(matrix[i], 0);
				
		// Initialize
		pathRecovery = new ArrayList<>(N);
    	for(int i = 0; i < N; i++)
    		pathRecovery.add(null);	
		prevSourceID = sourceID;
		visited = new boolean[N];
		visitedTokens = tokens.size();
		
		if(DJIKSTRA)
			runDjikstra();
		else
			bfs();

    }
   

    public void paintComponent(Graphics g)
    {
    	// Paint Grid
    	g.clearRect(0, 0, WIDTH, HEIGHT);
		g.setColor(new Color(200,230,200));    		
		g.setColor(Color.black);
		
		for (int i = 0; i < WIDTH/BOX_SIZE; i++)
			g.drawLine(i * BOX_SIZE, 0, i * BOX_SIZE, HEIGHT);
		for (int i = 0; i < HEIGHT/BOX_SIZE; i++)
			g.drawLine(0, i*BOX_SIZE, HEIGHT, i*BOX_SIZE);   		
    		
    		
    	// Paint the Visited Blocks
    	if(running && sourceBlock && endBlock && visited != null) 
    	{
    		for(int i = 0; i < N; i++)
    		{
    			if(visited == null)
    				return;
    			if(visited[i])
    			{
    				int rowV = i % numCols;
    				int colV = (i / numRows);
    				g.setColor(Color.black);
    				g.drawRect(rowV*BOX_SIZE, colV*BOX_SIZE, 11, 11);
    				g.setColor(Color.cyan);
    				g.fillRect(rowV*BOX_SIZE+1, colV*BOX_SIZE+1, 9, 9);
    			}    			
    		}
    	}    	
    	
    	
    	// Paint RecoveryPath  	
		if(!running) 
    	{
			int curr;				
			for(int i = realPath.size()-1; i >= 0; i--)	
			{
				curr = realPath.get(i);				
				int rowV = curr % numCols;
				int colV = (curr / numRows);
				g.setColor(Color.black);
				g.drawRect(rowV*BOX_SIZE, colV*BOX_SIZE, BOX_SIZE, BOX_SIZE);
				g.setColor(new Color(180, 100, 180));
				g.fillRect(rowV*BOX_SIZE+1, colV*BOX_SIZE+1, 9, 9);
				
			}
				
		}	
    		
    	// Paint Obstacles & Tokens    	
		for(Integer k : obstacles)
		{
			int rowV = k % numCols;
			int colV = (k / numRows);
			g.setColor(Color.black);
			g.drawRect(rowV*BOX_SIZE, colV*BOX_SIZE, 11, 11);
			g.setColor(Color.green);
			g.fillRect(rowV*BOX_SIZE+1, colV*BOX_SIZE+1, 9, 9);
		}
		
		for(Integer k : tokens.keySet())
		{
			int rowV = k % numCols;
			int colV = (k / numRows);
			g.setColor(Color.orange);
			g.drawOval(rowV*BOX_SIZE, colV*BOX_SIZE, BOX_SIZE, BOX_SIZE);
			g.setColor(Color.yellow);
			g.fillOval(rowV*BOX_SIZE+1, colV*BOX_SIZE+1, 9, 9);				
			g.setColor(Color.orange);
			g.fillOval(rowV*BOX_SIZE+3, colV*BOX_SIZE+3, 5, 5);
			
		}
    		
    	// Paint & maintain start and end blocks
		if(sourceBlock) 
    	{
    		g.setColor(Color.blue);  		
    		g.fillRect(sourceBlockY, sourceBlockX, BOX_SIZE, BOX_SIZE);   		
    	}    	
    	if(endBlock)
    	{
    		g.setColor(Color.red);
    		g.fillRect(endBlockY, endBlockX, BOX_SIZE, BOX_SIZE);
    	}
    	
    }
    
    public void start()
    {
    	running = true;
    	thread = new Thread(this, "Game Loop");
    	thread.start();    	
    }
    
    public void stop()
    {
    	running = false;
    	repaint();
    	try {
    		
			thread.join();
			
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
    	
    	System.exit(1);
    }
   
    public void run()
    {    	
		tick();
		stop();
    }
    
 
    /************************************************************************/
    class MouseAndKey implements MouseListener, MouseMotionListener, KeyListener
    {
    	int keyPressed = 0;  
    	
    	@Override
	    public void mouseClicked(MouseEvent e) {
	    	
    		System.out.println("herer");
	    	if (keyPressed == KeyEvent.VK_T && SwingUtilities.isLeftMouseButton(e) )
	    	{	    		
    			System.out.println("added token");
    			int row = e.getY()/BOX_SIZE;
	    		int col = e.getX()/BOX_SIZE;	    		
	    		int id = (row*numCols) + col;
	    		if(!obstacles.contains(id))
	    			tokens.put(id, false);   
    		
	    		repaint();
	    		keyPressed = 0; 
	    		
	    	}
	    	
	    	else if (!sourceBlock && SwingUtilities.isLeftMouseButton(e) && keyPressed == KeyEvent.VK_S)
	    	{
	    		System.out.println("herer");
	    		sourceBlockX = e.getY();
    	    	sourceBlockY = e.getX();
    	    	sourceBlockX = sourceBlockX/BOX_SIZE * BOX_SIZE;
    	    	sourceBlockY = sourceBlockY/BOX_SIZE * BOX_SIZE;
    	    	
    	    	sourceID = (sourceBlockX/BOX_SIZE * numCols) + sourceBlockY/BOX_SIZE;    	   
    	    	startNode = (sourceBlockX/BOX_SIZE * numCols) + sourceBlockY/BOX_SIZE;  
    	        
    	    	if(!obstacles.contains(sourceID))    	    		
    	    		sourceBlock = true;
    	    	
    	    	
	    	}
	    	else if (!endBlock && SwingUtilities.isLeftMouseButton(e) && keyPressed == KeyEvent.VK_E)
	    	{
	    		endBlockX = e.getY();
    	    	endBlockY = e.getX();
    	    	endBlockX = endBlockX/BOX_SIZE * BOX_SIZE;
    	    	endBlockY = endBlockY/BOX_SIZE * BOX_SIZE;    	    	
    	    	endID = (endBlockX/BOX_SIZE * numCols) + endBlockY/BOX_SIZE;
    	    	
    	    	if (!obstacles.contains(endID)) 
    	    	{
    	    		endBlock = true;
    	    		noMoreObstacles = true;
    	    	}    	    		
	    	}
	    }	  


		@Override
		public void mouseDragged(MouseEvent e) {			
			
			if (SwingUtilities.isRightMouseButton(e) && !noMoreObstacles) 
			{
				
				int row = e.getY()/BOX_SIZE;
	    		int col = e.getX()/BOX_SIZE;	    		
	    		int id = (row*numCols) + col;
	    		obstacles.add(id);   	
			}
			
		}

		@Override
		public void keyPressed(KeyEvent e) {
			
			keyPressed = e.getKeyCode();
		}

		@Override
		public void keyReleased(KeyEvent e) {}


		@Override
		public void keyTyped(KeyEvent e) {}


		@Override
		public void mouseMoved(MouseEvent e) {}


		@Override
		public void mousePressed(MouseEvent e) {}


		@Override
		public void mouseReleased(MouseEvent e) {}


		@Override
		public void mouseEntered(MouseEvent e) {}


		@Override
		public void mouseExited(MouseEvent e) {}	

    }
    
}






