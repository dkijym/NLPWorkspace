package metdemo.CliqueTools;

import java.awt.Color;
import java.util.ArrayList;

public class VCNodeOperator{

    public VCNodeOperator(){}

    public static VCNode computeWeight(VCNode n1, VCNode n2){
    	ArrayList su = new ArrayList();
	double rate = 0.0;
	for(int i=0;i<n1.users.size();i++){
	    Object obj = (Object)n1.users.get(i);
	    if(n2.users.contains(obj)){
		rate++;
		su.add(obj);
	    }
	}

	double total = (double)((n1.users.size()+n2.users.size())-rate);
	rate = rate/total;

	Color color = Color.black;
	if(rate == 0)color = Color.white;
	else if(rate<=0.1)color = Color.orange;
	else if(rate<=0.2)color = Color.yellow;
	else if(rate<=0.3)color = Color.red;
	else if(rate<=0.4)color = Color.cyan;
	else if(rate<=0.5)color = Color.blue;
	else if(rate<=0.6)color = Color.magenta;
	else if(rate<=0.7)color = Color.green;
	else if(rate<=0.8)color = Color.gray;
	else if(rate<=0.9)color = Color.pink;
	else color = Color.black;

	VCNode n = null;
	if(!color.equals(Color.white))
	    n = new VCNode(n1.xPos+4,n1.yPos+4,n2.xPos+4,n2.yPos+4,color,su);
	return n;
    }

    public static Color getColor(int i){
	if(i%7==0)return Color.orange;
	else if(i%7==1)return Color.gray;
	else if(i%7==2)return Color.green;
	else if(i%7==3)return Color.magenta;
	else if(i%7==4)return Color.pink;
	else if(i%7==5)return Color.yellow;
	else return Color.cyan;
    }

}
