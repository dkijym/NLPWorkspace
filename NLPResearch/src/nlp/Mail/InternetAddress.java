package nlp.Mail;

import java.util.regex.Pattern;

/**
 * Use as a wrapper for a name, address, domain and zone.
 * 
 * @author reuben
 * 
 */
public class InternetAddress {
	public String address, name, domain, zone;

	public InternetAddress(String address) {
		this.address = address;
		init();
	}

	private void init() {

		final Pattern p_address1 = Pattern
				.compile("^.*?([^\"'\\s<]*)@([^>\\s'\"\\);]*).*$");
		java.util.regex.Matcher matcher;
		if(address != null)
		{
			matcher = p_address1.matcher(address);
			if (matcher.matches()) {
				name = matcher.group(1).trim().toLowerCase();
				domain = matcher.group(2).trim().toLowerCase();

				// now to get zone
				zone = domain.substring((domain.lastIndexOf(".")) + 1);
			}
		}
	}

}
