package nl.tudelft.graphalytics.validation.rule;

public class EpsilonValidationRule implements ValidationRule<Double> {

	private final static double COMPARISON_THRESHOLD = 0.0001;

	@Override
	public Double parse(String val) {
		try {
			return Double.parseDouble(val);
		} catch(NumberFormatException e) {
			return null;
		}
	}

	@Override
	public boolean match(Double outputValue, Double correctValue) {
		try {
			double a = outputValue;
			double b = correctValue;

			if (Double.isNaN(a) && Double.isNaN(b)) {
				return true;
			} else if (Double.isInfinite(a) && Double.isInfinite(b) && a * b < 0) {
				return true;
			} else if (Math.abs(a - b) < COMPARISON_THRESHOLD * a) {
				return true;
			}

			return false;
		} catch(NumberFormatException e) {
			return false;
		}
	}
}
