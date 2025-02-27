package stream;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import stream.KahanSummingFloatCollector.IntermediateSum;

/**
 * A Collector that produces the sum of a float-valued function applied to the
 * input elements. The sum is computed with the Kahan summation algorithm. If no
 * elements are present, the result is 0.
 * 
 * @see Collectors#summingDouble
 * @see https://en.wikipedia.org/wiki/Kahan_summation_algorithm
 */
public class KahanSummingFloatCollector implements Collector<Float, IntermediateSum, Float> {
	static class IntermediateSum {
		/** High-order bits of the running sum */
		float highOrderBits;
		/**
		 * Low-order bits of the sum computed via compensated summation. A running
		 * compensation for lost low-order bits.
		 */
		float lowOrderBits;
		/**
		 * Simple sum used to compute the proper result if the stream contains infinite
		 * values of the same sign
		 */
		float simpleSum;

	}

	@Override
	public Supplier<IntermediateSum> supplier() {
		return new Supplier<IntermediateSum>() {

			@Override
			public IntermediateSum get() {
				return new IntermediateSum();
			}

		};
	}

	@Override
	public BiConsumer<IntermediateSum, Float> accumulator() {
		return new BiConsumer<IntermediateSum, Float>() {

			@Override
			public void accept(IntermediateSum intermediateSum, Float t) {
				float val = t.floatValue();
				sumWithCompensation(intermediateSum, val);
				intermediateSum.simpleSum += val;
			}

		};

	}

	private static IntermediateSum sumWithCompensation(IntermediateSum intermediateSum, float value) {
		float y = value - intermediateSum.lowOrderBits; // lowOrderBits is zero the first time around.
		float velvel = intermediateSum.highOrderBits + y; // highOrderBits >>> y => low-order digits of y are lost.
		intermediateSum.lowOrderBits = (velvel - intermediateSum.highOrderBits) - y;
		intermediateSum.highOrderBits = velvel;
		return intermediateSum;
	}

	@Override
	public BinaryOperator<IntermediateSum> combiner() {
		return new BinaryOperator<IntermediateSum>() {

			@Override
			public IntermediateSum apply(IntermediateSum a, IntermediateSum b) {
				sumWithCompensation(a, b.highOrderBits);
				a.simpleSum += b.simpleSum;
				return sumWithCompensation(a, b.lowOrderBits);
			}
		};
	}

	@Override
	public Function<IntermediateSum, Float> finisher() {
		return new Function<IntermediateSum, Float>() {

			@Override
			public Float apply(IntermediateSum a) {
				return computeFinalSum(a);
			}

		};
	}

	private static final float computeFinalSum(IntermediateSum summands) {
		// Better error bounds to add both terms as the final sum
		float tmp = summands.highOrderBits + summands.lowOrderBits;
		float simpleSum = summands.simpleSum;
		if (Float.isNaN(tmp) && Float.isInfinite(simpleSum))
			return simpleSum;
		else
			return tmp;
	}

	/**
	 * @see Collector.Characteristics#CONCURRENT
	 * @see Collector.Characteristics#UNORDERED
	 * @see Collector.Characteristics#IDENTITY_FINISH
	 */
	@Override
	public Set<Characteristics> characteristics() {
		return Collections.emptySet();
	}
}
