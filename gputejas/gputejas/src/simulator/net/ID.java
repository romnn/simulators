/**
 * 
 */
package net;

/**
 * @author eldhose
 *
 */
public class ID {
	int x;
	int y;
	public ID(int a, int b){
		x=a;
		y=b;
	}
	public ID(ID id)
	{
		x=id.getx();
		y=id.gety();
	}
	public void setx(int a)
	{
		x=a;
	}
	public void sety(int b)
	{
		y=b;
	}
	public int getx()
	{
		return x;
	}
	public int gety()
	{
		return y;
	}
	public ID clone()
	{
		return new ID(x,y);
	}
	public String toString()
	{
		return "["+this.x+" , "+this.y+"]";
	}
	public boolean equals(ID id)
	{
		return (this.x==id.x && this.y==id.y);
	}
}
