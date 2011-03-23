/**
 * June 2006
 * Testing framework for database handlers
 */
package metdemo.Testing;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Shlomo
 *
 */
public class databaseTesting extends TestCase {

	
	protected void setUp() {
		//do pre test setup here
	}
	
	public static Test suite() {
			return new TestSuite(databaseTesting.class);
	}
	
//	public void testAdd() {
////		double result= fValue1 + fValue2;
////		// forced failure result == 5
////		assertTrue(result == 6);
//	}
	
//	public void testBagMultiply() {
//		// {[12 CHF][7 USD]} *2 == {[24 CHF][14 USD]}
//		IMoney expected= MoneyBag.create(new Money(24, "CHF"), new Money(14, "USD"));
//		assertEquals(expected, fMB1.multiply(2)); 
//		assertEquals(fMB1, fMB1.multiply(1));
//		assertTrue(fMB1.multiply(0).isZero());
//	}
	
	
	
	
	//since slow will have seperate tests just for this.
	public static void main (String[] args) {
		junit.textui.TestRunner.run(suite());
	}
	
}
