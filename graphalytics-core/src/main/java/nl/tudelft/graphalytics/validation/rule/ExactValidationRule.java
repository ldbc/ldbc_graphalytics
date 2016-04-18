package nl.tudelft.graphalytics.validation.rule;

public class ExactValidationRule implements ValidationRule<Long> {

	@Override
	public Long parse(String val) {
		try {
			return Long.parseLong(val);
		} catch(NumberFormatException e) {
			return null;
		}
	}

	@Override
	public boolean match(Long lhs, Long rhs) {
		return lhs != null && rhs != null && lhs.equals(rhs);
	}
}
