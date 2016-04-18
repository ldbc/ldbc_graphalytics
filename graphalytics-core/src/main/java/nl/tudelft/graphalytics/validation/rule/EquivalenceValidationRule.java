package nl.tudelft.graphalytics.validation.rule;

import java.util.HashMap;
import java.util.Map;

public class EquivalenceValidationRule implements ValidationRule<Long> {
	private Map<Long, Long> left2right;
	private Map<Long, Long> right2left;

	public EquivalenceValidationRule() {
		left2right = new HashMap<Long, Long>();
		right2left = new HashMap<Long, Long>();
	}

	@Override
	public Long parse(String val) {
		try {
			return Long.parseLong(val);
		} catch(NumberFormatException e) {
			return null;
		}
	}

	@Override
	public boolean match(Long left, Long right) {
		Long a = left2right.get(left);
		Long b = right2left.get(right);

		if (a == null && b == null) {
			left2right.put(left, right);
			right2left.put(right, left);
			return true;
		}

		return a != null && b != null & a.equals(b);
	}
}
