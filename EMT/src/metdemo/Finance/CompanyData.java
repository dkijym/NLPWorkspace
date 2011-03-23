/*CompanyData.java
 * Stores economic and social network indicators of a specific company.
 */

package metdemo.Finance;

public class CompanyData {
	private String name;
	private int degree;
	private double score;
	private double betweenness;
	private double HITS;
	private Double avgDist;
	private Double clustercoef;
	private Double car1, car2, size, frev, ltg, sue, sg, ta, capex, bp, ep, anfor, anforlag, felag, consensus;
	private int label;
	public CompanyData(String name, int degree, double score, double betweenness, double HITS, Double avgDist, Double clustercoef){
		this.name = name;
		this.degree = degree;
		this.score = score;
		this.betweenness = betweenness;
		this.HITS = HITS;
		this.avgDist = avgDist;
		this.clustercoef = clustercoef;
	}
	
	public void car1(String car1){
		if(!car1.equalsIgnoreCase("NA")){
			if(car1.substring(0,1).equals(" "))
				car1 = car1.substring(1, car1.length());
			this.car1 = new Double(car1);
		}
	}
	
	public void car2(String car2){
		if(!car2.equalsIgnoreCase("NA")){
			if(car2.substring(0,1).equals(" "))
				car2 = car2.substring(1, car2.length());
			this.car2 = new Double(car2);
		}
	}
	
	public void size(String size){
		if(!size.equalsIgnoreCase("NA")){
			if(size.substring(0,1).equals(" "))
				size = size.substring(1, size.length());
			this.size = new Double(size);
		}
	}
	
	public void frev(String frev){
		if(!frev.equalsIgnoreCase("NA")){
			if(frev.substring(0,1).equals(" "))
				frev = frev.substring(1, frev.length());
			this.frev = new Double(frev);
		}
	}
	public void ltg(String ltg){
		if(!ltg.equalsIgnoreCase("NA")){
			if(ltg.substring(0,1).equals(" "))
				ltg = ltg.substring(1, ltg.length());
			this.ltg = new Double(ltg);
		}
	}
	public void sue(String sue){
		if(!sue.equalsIgnoreCase("NA")){
			if(sue.substring(0,1).equals(" "))
				sue = sue.substring(1, sue.length());
			this.sue = new Double(sue);
		}
	}
	public void sg(String sg){
		if(!sg.equalsIgnoreCase("NA")){
			if(sg.substring(0,1).equals(" "))
				sg = sg.substring(1, sg.length());
			this.sg = new Double(sg);
		}
	}
	public void ta(String ta){
		if(!ta.equalsIgnoreCase("NA")){
			if(ta.substring(0,1).equals(" "))
				ta = ta.substring(1, ta.length());
			this.ta = new Double(ta);
		}
	}
	public void capex(String capex){
		if(!capex.equalsIgnoreCase("NA")){
			if(capex.substring(0,1).equals(" "))
				capex = capex.substring(1, capex.length());
			this.capex = new Double(capex);
		}
	}
	public void bp(String bp){
		if(!bp.equalsIgnoreCase("NA")){
			if(bp.substring(0,1).equals(" "))
				bp = bp.substring(1, bp.length());
			this.bp = new Double(bp);
		}
	}
	public void ep(String ep){
		if(!ep.equalsIgnoreCase("NA")){
			if(ep.substring(0,1).equals(" "))
				ep = ep.substring(1, ep.length());
			this.ep = new Double(ep);
		}
	}
	public void anfor(String anfor){
		if(!anfor.equalsIgnoreCase("NA")){
			if(anfor.substring(0,1).equals(" "))
				anfor = anfor.substring(1, anfor.length());
			this.anfor = new Double(anfor);
		}
	}
	public void anforlag(String anforlag){
		if(!anforlag.equalsIgnoreCase("NA")){
			if(anforlag.substring(0,1).equals(" "))
				anforlag = anforlag.substring(1, anforlag.length());
			this.anforlag = new Double(anforlag);
		}
	}
	public void felag(String felag){
		if(!felag.equalsIgnoreCase("NA")){
			if(felag.substring(0,1).equals(" "))
				felag = felag.substring(1, felag.length());
			this.anforlag = new Double(felag);
		}
	}
	public void consensus(String consensus){
		if(!consensus.equalsIgnoreCase("NA")){
			if(consensus.substring(0,1).equals(" "))
				consensus = consensus.substring(1, consensus.length());
			this.consensus = new Double(consensus);
		}
	}
	public void label(String label){
		if(label.substring(0,1).equals(" "))
			label = label.substring(1, label.length());
		this.label = Integer.parseInt(label);
	}
	
	public String toString(){
		String output = name + "," + degree + "," + score + "," + betweenness + "," + HITS + "," + avgDist+ "," +clustercoef;
		output += "," +car1+ "," +car2+ "," +size;
		output += "," + frev+ "," +ltg+ "," +sue+ "," +sg+ "," +ta+ "," +capex+ "," +bp+ "," +ep+ "," +anfor+ "," +anforlag+ "," +felag+ "," +consensus+ "," +label;
		return output;
	}
	
	public String toWeka(){
		String output = name + "," + degree + "," + score + "," + betweenness + "," + HITS + "," + avgDist+ "," +clustercoef;
		if(car1!=null)
			output += "," +car1;
		else
			output+= ",?";
		if(car2!=null)
			output += "," +car2;
		else
			output+= ",?";
		if(size!=null)
			output+= "," +size;
		else
			output+= ",?";
		if(frev!=null)
			output += "," + frev;
		else
			output+= ",?";
		if(ltg!=null)
			output +="," +ltg;
		else
			output+= ",?";
		if(sue!=null)
			output += "," +sue;
		else
			output+= ",?";
		if(sg!=null)
			output+="," +sg;
		else
			output+= ",?";
		if(ta!=null)
			output+="," +ta;
		else
			output+= ",?";
		if(capex!=null)
			output+="," +capex;
		else
			output+= ",?";
		if(bp!=null)
			output+="," +bp;
		else
			output+= ",?";
		if(ep!=null)
			output+="," +ep;
		else
			output+= ",?";
		if(anfor!=null)
			output+="," +anfor;
		else
			output+= ",?";
		if(anforlag!=null)
			output+="," +anforlag;
		else
			output+= ",?";
		if(felag!=null)
			output+="," +felag;
		else
			output+= ",?";
		if(consensus!=null)
			output+="," +consensus;
		else
			output+= ",?";
		
		output+="," +label;
			return output;
	}
}
