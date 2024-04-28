package org.skriptlang.skript.lang.arithmetic;

public abstract class ArithmeticNumber extends Number implements Comparable<Number> {

	public abstract ArithmeticNumber add(ArithmeticNumber other);

	public abstract ArithmeticNumber subtract(ArithmeticNumber other);

	public abstract ArithmeticNumber multiply(ArithmeticNumber other);

	public abstract ArithmeticNumber divide(ArithmeticNumber other);

	public abstract ArithmeticNumber exponentiate(ArithmeticNumber power);

	public abstract ArithmeticNumber abs();

	public abstract ArithmeticNumber negate();

	public ArithmeticNumber not() {
		throw new UnsupportedOperationException();
	}

	public ArithmeticNumber and(ArithmeticNumber other) {
		throw new UnsupportedOperationException();
	}

	public ArithmeticNumber or(ArithmeticNumber other) {
		throw new UnsupportedOperationException();
	}

	public ArithmeticNumber xor(ArithmeticNumber other) {
		throw new UnsupportedOperationException();
	}

	public ArithmeticNumber shiftLeft(ArithmeticNumber other) {
		throw new UnsupportedOperationException();
	}

	public ArithmeticNumber shiftRight(ArithmeticNumber other) {
		throw new UnsupportedOperationException();
	}

	public ArithmeticNumber uShiftRight(ArithmeticNumber other) {
		throw new UnsupportedOperationException();
	}

}
