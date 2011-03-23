// -*- tab-width: 4 -*-
package Jet.Time;

import java.util.List;

import Jet.Tipster.Annotation;
import Jet.Tipster.Document;

public class NumberPattern extends PatternItem {
	private int min;
	private int max;

	public NumberPattern(int min, int max) {
		this.min = min;
		this.max = max;
	}

	public NumberPattern() {
		this(Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Override
	public PatternMatchResult match(Document doc, List<Annotation> tokens, int offset) {
		int start = tokens.get(offset).start();
		List<Annotation> numbers = doc.annotationsAt(start, "number");
		if (numbers == null || numbers.size() == 0) {
			return null;
		}

		Annotation number = numbers.get(0);
		Number value = (Number) number.get("value");

		if (min <= value.intValue() && value.intValue() <= max) {
			return new PatternMatchResult(value, number.span());
		} else {
			return null;
		}
	}
}
