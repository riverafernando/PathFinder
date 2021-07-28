
import java.awt.Color;
import java.awt.Graphics;
import java.util.Arrays;
import java.io.*;
import java.util.*;


class Vertex implements Comparable<Vertex>
{
	int id;
	int dist;
	int prev;
	int x; 
	int y;
	
	Vertex(int id, int dist, int prev)
	{
		this.id = id;
		this.dist = dist;
		this.prev = prev;
	}
	
	public int compareTo(Vertex v)
	{
		return this.dist - v.dist;
	}
	
}

