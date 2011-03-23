package nlp.Old;
import java.util.ArrayList;

import javax.mail.internet.MimeMessage;

public class Person {
		// unique identifier
		private String emailAddress;
		private String firstName;
		private String lastName;
		private ArrayList<String> nickNames;
		//public ArrayList<EmailCalculation> emails;
		
		//public EmailCalculation
		//{
	//		public MimeMessage email;
	//		public AceDocument NERemail;
		//}
		
		public Person(String email) {
			emailAddress = email;
	//		emails = new ArrayList<MimeMessage>();
		}
		public String toString()
		{
			return emailAddress;
		}
	}