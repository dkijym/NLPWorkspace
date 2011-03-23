package metdemo.CliqueTools;

import java.awt.Color;
import java.util.ArrayList;

public class VCNode{
	
	ArrayList users;//String
    Color color = null;//dot color
    int xPos = 0;//x axis
    int yPos = 0;//y axis
    int xPos2 = 0;//second axis, for the right picture
    int yPos2 = 0;
    int size = 0;//dot size
    boolean isSelected = false;//for GWI(graphical weijen interface)
    int index;//a label for clique
    ArrayList commonwords = new ArrayList(5);

    //for second type of node
    String name;//name of user
    int count;//involves in how many cliques
    ArrayList clique;//Integer

    //for the lines
    ArrayList shareuser;
    
    /**
     * Constructor for a node point
     * @param colorNode
     * @param x
     * @param y
     * @param sizeNode
     * @param indexNode
     */
    public VCNode(Color colorNode, int x, int y, int sizeNode, int indexNode, ArrayList<String> userdata1){
	color = colorNode;
	xPos = x;
	yPos = y;
	size = sizeNode;
	index = indexNode;
	//folded into here to make things faster
	users = new ArrayList<String>(userdata1.size());
	for (int i = 0; i < userdata1.size(); i++) {
			users.add(userdata1.get(i));
		}
	
    }
    
    public VCNode(Color colorNode, int x, int y, int sizeNode, int indexNode, CliqueWithCommonWords userdata1){
    	color = colorNode;
    	xPos = x;
    	yPos = y;
    	size = sizeNode;
    	index = indexNode;
    	//folded into here to make things faster
    	users = new ArrayList<String>(userdata1.getNumUsers());
    	for (int j = 0; j < userdata1.getNumUsers(); j++) {
			users.add(userdata1.getUser(j));
			// System.out.println("user:"+oneclique.users.get(j));
		}
    	commonwords = new ArrayList<String>(userdata1.getNumCommon());
		for (int j = 0; j < userdata1.getNumCommon(); j++) {
			commonwords.add(userdata1.getCommon(j));
			// System.out.println("words:"+oneclique.commonWords.get(j));
		}
    }
    
    
    
    

    public VCNode(Color colorNode, int x, int y, int sizeNode, int indexNode){
    	color = colorNode;
    	xPos = x;
    	yPos = y;
    	size = sizeNode;
    	index = indexNode;
    	users = new ArrayList(2);
    }
    
    /**
     * Constructor with only a string
     * @param n name of point/line
     */
    public VCNode(String n){
	clique = new ArrayList();
	count = 1;
	name = n;
	users = new ArrayList(5);
    }

    /**
     * Constructor to point a point to point line
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param colorNode
     * @param arrays
     */
    public VCNode(int x1, int y1, int x2, int y2, Color colorNode, ArrayList arrays){
	xPos = x1;
	yPos = y1;
	xPos2 = x2;
	yPos2 = y2;
	color = colorNode;
	shareuser = arrays;
	users = new ArrayList(5);
    }

}



