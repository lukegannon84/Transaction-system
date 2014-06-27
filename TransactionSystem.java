import java.util.*;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/////////////////////////////////////////////////////////////////////////
////////////////////////CUSTOMER ////////////////////////////////////
////////////////////////////////////////////////////////////////////////
 class customer{
	static int nameDim = 50;
	static int recordLen = 12+nameDim+4+8;
	private String cardNum;
	private String name;
	private int date;
	private double amount;
	public customer(String c,String n, int d, double a){


		cardNum =c;
		name = n;
		date = d;
		amount = a;


	}
	public customer(){

		cardNum = "";
		name = "";
	    date = 0;
	    amount = 0.0;

	}
	public void setExpiry(int d){ date = d;}
	public void setName(String n){name = n;}
	public void setLimit(double a){amount = a;}
	public void setCardNum(String c){cardNum = c;}

	public String name(){return name;}
	public String cardNum(){return cardNum;}
	public int date(){return date;}
	public double amount(){return amount;}


	public String toString(){
		return cardNum+" "+name+" "+date+" "+amount;
	}
	public boolean equals(Object ob){
		customer p = (customer)ob;
		return(cardNum.equals(p.cardNum));
	}
	public int hashCode(){
		return cardNum.hashCode();
	}
	public void write(RandomAccessFile f){
		try{
			f.writeUTF(cardNum);
			String n = String.format("%-50s",name);
			f.writeUTF(n);
			f.writeInt(date);
			f.writeDouble(amount);
		}
		catch(IOException e){e.printStackTrace();}
	}
	public void read(RandomAccessFile f){
		try{
			cardNum = f.readUTF();
			name = f.readUTF().trim();
			date = f.readInt();
			amount = f.readDouble();

		}
		catch(IOException e){e.printStackTrace();}
	}
}
///////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

 class IndexedGroupFile{
	RandomAccessFile fh = null;
	private long length = 0;
	private HashList<IndexKey> hTable = null;
	public IndexedGroupFile(String fName){
		try{
      fh = new RandomAccessFile(fName,"rw");
      length = fh.length()/customer.recordLen;
      fh.seek(0);
      //create hash table
      if(fh.length() == 0)
      	hTable = new HashList<IndexKey>(1000);
      else{
      	//create index table from file of existing data
      	hTable = new HashList<IndexKey>(1000);
		    int j = 0;
		    while(j < length){
			    customer per = new customer();
			    per.read(fh);
			    IndexKey ky = new IndexKey(per.cardNum(),j);
			    hTable.add(ky);
			    j++;
			  }
		  }
		}catch(IOException e){e.printStackTrace();}
	}
	public void add(customer p){
		try{
	    fh.seek(fh.length());
	    p.write(fh);
	    length++;
	    hTable.add(new IndexKey(p.cardNum(),length-1));
		}catch(IOException e){e.printStackTrace();}
	}
	public boolean contains(customer p){ //just check for key in internal table
      IndexKey ky = hTable.get(new IndexKey(p.cardNum()));
      if(ky != null) return true;
      else return false;
	}
	public customer get(String cardNum){
		customer per = null;
		IndexKey ky = hTable.get(new IndexKey(cardNum));
    if(ky == null) return null;
    else{
    	try{
    		fh.seek(ky.getFileIndex()*customer.recordLen);
			  per = new customer();
			  per.read(fh);
    	}
    	catch(IOException e){e.printStackTrace();}
		  return per;
    }
	}
	public boolean replace(customer p, customer np){
		try{
		  fh.seek(0);
		  int j = 0; boolean found = false;
		  while(j < length && !found){
			  customer per = new customer();
			  per.read(fh);
			  if(per.equals(p)) found = true;
			  else j++;
		  }
		  if(found){
			  fh.seek(j*customer.recordLen);
			  np.write(fh);
			  return true;
		  }
		  else
			 return false;
		}catch(IOException e){e.printStackTrace();return false;}
	}
	public long size(){
		return length;
	}
	public long length(){
		long l = 0;
		try{
		 l = fh.length();
		} catch(IOException e){e.printStackTrace();}
		return l;
	}
	public void close(){
		try{
		 fh.close();
		}catch(IOException e){e.printStackTrace();}
	}
	public void display(){
		try{
		  fh.seek(0);
		  int j = 0;
		  while(j < length){
			  customer p = new customer();
			  p.read(fh);
			  System.out.println(p.toString());
			  j++;
		  }
		}catch(IOException e){e.printStackTrace();}
	}

}

/////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////

class TransactionTest {
    public static void main(String[] args) {
     IndexedGroupFile f1 = new IndexedGroupFile("history.dat");
    // f1.add(new customer("00000001","Luke Bentaher",2012,1100.0));


	System.out.println("Size"+f1.size());
	System.out.println("Length"+f1.length());
     f1.display();
     System.out.println();
     customer p = f1.get("00000001");

     System.out.println(p.toString());

    }
}












