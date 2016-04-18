package nl.tudelft.graphalytics.validation.rule;

public interface ValidationRule<E> {
	public E parse(String val);
	public boolean match(E lhs, E rhs);
}
