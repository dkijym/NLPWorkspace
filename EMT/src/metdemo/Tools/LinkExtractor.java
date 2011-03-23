/*
 * Created on Jan 17, 2005
 *
 * Class to help extract links from messages
 */
package metdemo.Tools;

import java.util.ArrayList;

import metdemo.dataStructures.LinkInfoDS;

/**
 * Class to work with extracting and manipulating links in text data
 * 
 * @author Shlomo Hershkop
 * 
 *  
 */

public class LinkExtractor {
	//<a href\\s*=\\s*(.*)<\\/a>
	//<a href[\\s]*=[\\s]*(.*)<\\/a>
	//public static Pattern anchorPattern = Pattern.compile(".*<a
	// href[\\s]*=.*(</a>)*",Pattern.CASE_INSENSITIVE);
	//public static Pattern imagePattern = Pattern.compile("<img
	// (.*)>",Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ);
	//public static Pattern linkPattern = Pattern.compile("<link
	// (.*)>",Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ);
	//public static Pattern formPattern =
	// Pattern.compile("<form>(.*)</form>",Pattern.CASE_INSENSITIVE |
	// Pattern.MULTILINE | Pattern.CANON_EQ);
	//public static Pattern stylesheetPattern = Pattern.compile("
	// (.*)\\.css(.*) ",Pattern.CASE_INSENSITIVE | Pattern.MULTILINE |
	// Pattern.CANON_EQ);

	/**
	 * Method to parse out any links from the given data.
	 * 
	 * @param data
	 *            to parse
	 * @return extracted array of links in the LinkInfoDS
	 */
	public static LinkInfoDS[] extractAllLinksFromData(String datap) {
		
		if(datap==null)
		{
			return new LinkInfoDS[0];
		}
		
		
		String data = datap.replaceAll("[\n|\r|\u0085]+", " ").toLowerCase();
		data = data.replaceAll("\\'", "\"");
		ArrayList foundlinks = new ArrayList();

		int mainStartOffset, mainEndOffset;
		int offset1, offset2;
		//1 extract/remove all link parts
		//Matcher mm ;
		mainStartOffset = data.indexOf("<link");

		while (mainStartOffset >= 0) {
			//try to find end of image tag
			mainEndOffset = data.indexOf(">", mainStartOffset + 1);
			//will exit if problems
			if (mainEndOffset < 0) {
				break;
			}
			String subPart = data.substring(mainStartOffset + 5, mainEndOffset)
					.trim();
			//now to look for name of url
			offset1 = data.indexOf("href");

			//if not present then just get next tag
			if (offset1 >= 0) {

				offset1 = data.indexOf("\"", offset1+3);

				if (offset1 >= 0) {
					offset2 = subPart.indexOf("\"", offset1 + 1);
					if (offset2 >= 0) {

						//can grab url of image
						LinkInfoDS ll = new LinkInfoDS(LinkInfoDS.RELATED_LINK);
						ll.setLink(subPart.substring(0, offset2));

						//look for alterante text
						offset1 = subPart.indexOf("title");
						if (offset1 >= 0) {
							offset1 = subPart.indexOf("\"", offset1);
							offset2 = subPart.indexOf("\"", offset1 + 1);
							if (offset1 >= 0 && offset2 >= 0) {
								ll.setLabel(subPart.substring(offset1 + 1,
										offset2));
							}

						}//done look for alt text
						if(ll.getLink()!=null)
						foundlinks.add(ll);
					}
				}
			}
			//clean out stuff we were looking at
			//even if we cant parse it
			data = data.substring(0, mainStartOffset - 1)
					+ data.substring(mainEndOffset + 1);
			mainStartOffset = data.indexOf("<link");
		}

		/*
		 * mm = linkPattern.matcher(data); if(mm.matches()){
		 * System.out.println("in link match"); for(int i=0;i
		 * <mm.groupCount();i++){ String subpart = mm.group(i); //extract href[
		 * ]=[ ]"..." //extract title[ ]=[ ]".." // String sub_href =
		 * System.out.println("1"+ subpart); } } mm.replaceAll("");//clean it
		 * out of the data
		 * 
		 * //2 style sheets
		 */
		//form data
		mainStartOffset = data.indexOf("<form");

		while (mainStartOffset >= 0) {
			//try to find end of image tag
			mainEndOffset = data.indexOf("</form>", mainStartOffset + 3);
			//will exit if problems
			if (mainEndOffset < 0) {
				break;
			}
			String subPart = data.substring(mainStartOffset, mainEndOffset)
					.trim();
			//now to look for name of url
			offset1 = data.indexOf("action");

			//if not present then just get next tag
			if (offset1 >= 0 && offset1 < data.length()) {

				offset1 = data.indexOf("\"", offset1);

				if (offset1 >= 0) {
					offset2 = subPart.indexOf("\"", offset1 + 1);
					if (offset2 >= 0) {

						//can grab url of image
						LinkInfoDS ll = new LinkInfoDS(
								LinkInfoDS.FORM_SUBMISSION);
						ll.setLink(subPart.substring(0, offset2));
						if(ll.getLink()!=null)
						foundlinks.add(ll);
					}
				}
			} else {
				offset1 = data.indexOf("source");

				//if not present then just get next tag
				if (offset1 >= 0) {

					offset1 = data.indexOf("\"", offset1);

					if (offset1 >= 0) {
						offset2 = subPart.indexOf("\"", offset1 + 1);
						if (offset2 >= 0) {

							//can grab url of image
							LinkInfoDS ll = new LinkInfoDS(
									LinkInfoDS.FORM_SUBMISSION);
							ll.setLink(subPart.substring(0, offset2));
							if(ll.getLink()!=null)
							foundlinks.add(ll);
						}
					}
				}
			}
			//clean out stuff we were looking at
			//even if we cant parse it
			data = data.substring(0, mainStartOffset - 1)
					+ data.substring(mainEndOffset + 6);
			mainStartOffset = data.indexOf("<form");
		}

		/*
		 * //3 extract and remove form data
		 * 
		 * //4 extract remove all img data mm = imagePattern.matcher(data);
		 */

		//start the image tag
		mainStartOffset = data.indexOf("<img ");
		if(mainStartOffset>=0){
			mainStartOffset = data.indexOf("src",mainStartOffset);
				
		}
		while (mainStartOffset >= 0) {
			//try to find end of image tag
			mainEndOffset = data.indexOf(">", mainStartOffset + 1);
			//will exit if problems
			if (mainEndOffset < 0) {
				break;
			}
			//now to look for name of image
			offset1 = data.indexOf("\"", mainStartOffset);
			//if not present then just get next tag
			if (offset1 >= 0 && offset1 < mainEndOffset) {

				String subPart = data.substring(offset1 + 1, mainEndOffset)
						.trim();

				offset2 = subPart.indexOf("\"");
				if (offset2 >= 0) {

					//can grab url of image
					LinkInfoDS ll = new LinkInfoDS(LinkInfoDS.IMAGE);
					ll.setLink(subPart.substring(0, offset2));

					//look for alterante text
					offset1 = subPart.indexOf("alt");
					if (offset1 >= 0) {
						offset1 = subPart.indexOf("\"", offset1);
						offset2 = subPart.indexOf("\"", offset1 + 1);
						if (offset1 >= 0 && offset2 >= 0) {
							ll
									.setLabel(subPart.substring(offset1 + 1,
											offset2));
						}

					}//done look for alt text
					if(ll.getLink()!=null)
					foundlinks.add(ll);
				}
			}
			//clean out stuff we were looking at
			//even if we cant parse it
			data = data.substring(0, mainStartOffset)
					+ data.substring(mainEndOffset + 1);
			mainStartOffset = data.indexOf("<img ");
			if(mainStartOffset>=0){
				mainStartOffset = data.indexOf("src",mainStartOffset);
			}
		}

		/*
		 * if(mm.matches()){
		 * 
		 * System.out.println("in image match"); for(int i=0;i
		 * <mm.groupCount();i++){ String subpart = mm.group(i);
		 * System.out.println("2"+ subpart); } } mm.replaceAll("");//clean it
		 * out of the data
		 */
		//5 extract remove all anchor data
		mainStartOffset = data.indexOf("<a href");

		while (mainStartOffset >= 0) {
			//try to find end of href tag
			mainEndOffset = data.indexOf("</a>", mainStartOffset);
			//stop if problems
			if (mainEndOffset < 0) {
				break;
			}

			//at this point we know we have <a href......</a>
			//but not sure about spaces around the equal and quote
			offset1 = data.indexOf("\"", mainStartOffset);
			//extract the href.... but check for problems
			if (offset1 >= 0 && offset1 < mainEndOffset) {
				//cut it off so we deal with less
				String subPart = data.substring(offset1 + 1, mainEndOffset)
						.trim();
				//create a link
				LinkInfoDS ll = new LinkInfoDS(LinkInfoDS.ANCHOR);
				//get link
				offset1 = subPart.indexOf("\"", 1);
				if (offset1 >= 0) {
					ll.setLink(subPart.substring(0, offset1));
					subPart = subPart.substring(offset1 + 1);
				}
				//get the stuff which shows in the webpage if any
				offset1 = subPart.lastIndexOf(">");
				if (offset1 >= 0) {
					ll.setLabel(subPart.substring(offset1 + 1));
					subPart = subPart.substring(0, offset1);
				}
				//read the target out
				offset1 = subPart.indexOf("target");
				//check if target label here
				if (offset1 >= 0) {
					offset1 = subPart.indexOf("\"", offset1 + 6);
					if (offset1 >= 0) {
						offset2 = subPart.indexOf("\"", offset1 + 1);
						if (offset2 >= 0) {
							ll.setTarget(subPart
									.substring(offset1 + 1, offset2));
							//	System.out.println("target:"+subPart.substring(offset1+1,offset2));
						}
					}
				}
				if(ll.getLink()!=null)
				foundlinks.add(ll);
			}
			data = data.substring(0, mainStartOffset)
					+ data.substring(mainEndOffset + 4);
			mainStartOffset = data.indexOf("<a href=\"");
		}

		//look for free standing http
		mainStartOffset = data.indexOf("http://");
		while (mainStartOffset >= 0) {
			mainEndOffset = data.indexOf(" ", mainStartOffset);
			if (mainEndOffset < 0) {
				mainEndOffset = mainStartOffset + 2;
			} else {
				LinkInfoDS ll = new LinkInfoDS(LinkInfoDS.FREE_WEB);
				ll.setLink(data.substring(mainStartOffset, mainEndOffset));
				if(ll.getLink()!=null)
				foundlinks.add(ll);
			}
			data = data.substring(0, mainStartOffset)
					+ data.substring(mainEndOffset);
			mainStartOffset = data.indexOf("http://");
		}

		//https
		mainStartOffset = data.indexOf("https://");
		while (mainStartOffset >= 0) {
			mainEndOffset = data.indexOf(" ", mainStartOffset+2);
			if (mainEndOffset < 0) {
				mainEndOffset = mainStartOffset + 2;
			} else {
					
				LinkInfoDS ll = new LinkInfoDS(LinkInfoDS.FREE_WEB);
				ll.setLink(data.substring(mainStartOffset, mainEndOffset));
				if(ll.getLink()!=null)
				foundlinks.add(ll);
			}

			data = data.substring(0, mainStartOffset)
					+ data.substring(mainEndOffset);
			mainStartOffset = data.indexOf("https://");
		}
		//www links
		mainStartOffset = data.indexOf("www.");
		while (mainStartOffset >= 0) {
			mainEndOffset = data.indexOf(" ", mainStartOffset);
			if (mainEndOffset < 0) {
				mainEndOffset = mainStartOffset + 2;
			} else {
				LinkInfoDS ll = new LinkInfoDS(LinkInfoDS.FREE_WEB);
				ll.setLink(data.substring(mainStartOffset, mainEndOffset));
				if(ll.getLink()!=null)
				foundlinks.add(ll);
			}

			data = data.substring(0, mainStartOffset)
					+ data.substring(mainEndOffset);
			mainStartOffset = data.indexOf("www.");
		}

		//ftp
		mainStartOffset = data.indexOf("ftp.");
		while (mainStartOffset >= 0) {
			mainEndOffset = data.indexOf(" ", mainStartOffset);
			if (mainEndOffset < 0) {
				mainEndOffset = mainStartOffset + 2;
			} else {
				LinkInfoDS ll = new LinkInfoDS(LinkInfoDS.FTP);
				ll.setLink(data.substring(mainStartOffset, mainEndOffset));
				if(ll.getLink()!=null)
				foundlinks.add(ll);
			}

			data = data.substring(0, mainStartOffset)
					+ data.substring(mainEndOffset);
			mainStartOffset = data.indexOf("ftp.");
		}

		
		
		
		return (LinkInfoDS[]) foundlinks.toArray(new LinkInfoDS[foundlinks
				.size()]);
	}

	/**
	 * Test if given link is an html link
	 * 
	 * @param unknownLink
	 * @return
	 */
	//static final boolean isHTMLLink(LinkInfoDS unknownLink){
	//TODO: fill me in
	//basically check for
	//www.
	//http://
	//maybe a regular expression ^[\s]*\\..*[\\.htm[l]+[/]+]
	//test if it really works :)
	//}

	static public final int urlDistance(final String url1, final String url2) {
		int distance = 0;
		char seq1[], seq2[];
		if (url1.length() <= url2.length()) {
			seq1 = url1.toCharArray();
			seq2 = url2.toCharArray();
		} else {
			seq1 = url2.toCharArray();
			seq2 = url1.toCharArray();

		}

		for (int i = 0; i < seq1.length; i++) {
			if (seq1[i] != seq2[i]) {
				distance++;
			}
		}
		//if domain the same should half the total distance
//		int a = url1.indexOf("/");
	//	int b = url2.indexOf("/");

		//now to add half the length diff
		distance += (seq2.length - seq1.length) / 2;

		
		//if(a>0 && b>0){
			
			//if(url1.substring(a).equals(url2.substring(b))){
			//	distance = distance/2;
		//	}
			
			
		//}
	
		return 500 * distance / seq2.length;
	}

	/*public static void main(String args[]) {

		String data = new String(
	"Dear Mr. Hershkop,\n"+
    "This e-mail is to notify you that there has been a change to your credit file. To view your notification, please click on the following link, which will take you to the MyEquifax login page.\n"+
"\n\nhttps://www.econsumer.equifax.com/CreditWatchNotify\n\n"+
"NOTE:  This e-mail is for your information, and does not require a response. If you need assistance on this or any other Equifax product, please contact us at customer.care@equifax.com or call us at 1-866-4CWATCH.  We kindly ask that you DO NOT REPLY to this e-mail."
);				
				/*"This is a short test\nyou can check out>\n>http://www.shlom1o.com or not\nof the \n <A HREF = \"www.this.com\"target=\"#sss\">top </a>\n"
						+ " <IMG src=\"http://graphics8.nytimes.com/images/misc/spacer.gif\" width=\"1\" height=\"3\"/> </td></tr> "
						+ " <tr><td bgcolor=\"#999999\"> <IMG src=\"http://graphics8.nytimes.com/images/misc/spacer.gif\" width=\"1\" height=\"1\"/> </td></tr> "
						+ " <tr><td> <IMG src=\"http://graphics8.nytimes.com/images/misc/spacer234.gif\" ALT=\"A space\" width=\"1\" height=\"2\"/> </td></tr> "
						+ " <tr><td class=\"globalnav\"> "
						+ " <a href=\"/ref/membercenter/textversion.html\">Text Version </a> <br> "
						+ "</td></tr> "
						+ "\nextraction system\n\n\n\nany I was looking at <a href=\"http://www.nice.com\">this is the top </a>some stuff afterwards\n\nand this");
/
		LinkInfoDS[] test = LinkExtractor.extractAllLinksFromData((data));
		BagOfLinks bag = new BagOfLinks(test);
		
		
		for (int i = 0; i < test.length; i++) {
			System.out.println(i+ " " +test[i]);
		}
		
		LinkInfoDS tstlink = new LinkInfoDS("www.shlomo.org","","",LinkInfoDS.ANCHOR);
		
		System.out.println("test:" + tstlink);
		System.out.println("distance: " + bag.getDistance(tstlink));
	}
*/
}