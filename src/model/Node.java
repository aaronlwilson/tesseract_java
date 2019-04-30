package model;

import hardware.*;

public class Node  {
  public int x;
  public int y; 
  public int index; //0-143
  public Tile parentTile;
  public int nodeType;
  
  public int r;
  public int g;
  public int b;

  //constructor
  Node(int theX, int theY, int theIndex, Tile theParentTile) {
    x = theX;
    y = theY;
    index = theIndex;
    parentTile = theParentTile;
  }    

} 
