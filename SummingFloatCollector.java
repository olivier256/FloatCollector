package stream;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import stream.SummingFloatCollector.FloatAccumulator;

/**
 * A Collector that produces the sum of a float-valued function applied to the
 * input elements. If no elements are present, the result is 0.
 * 
 * @see Collectors#summingDouble
 *
 */
public class SummingFloatCollector implements Collector<Float, FloatAccumulator, Float> {
	static class FloatAccumulator {
		private float value;

		public FloatAccumulator() {
			value = 0f;
		}

		public float getValue() {
			return value;
		}

		public void addValue(float value) {
			this.value += value;
		}

	}

	private static final Set<Characteristics> CHARACTERISTICS = Collections.singleton(Characteristics.UNORDERED);
	private boolean hasBeenCalledAtLeastOnce;

	public SummingFloatCollector() {
		hasBeenCalledAtLeastOnce = false;
	}

	@Override
	public Supplier<FloatAccumulator> supplier() {
		return new Supplier<FloatAccumulator>() {

			@Override
			public FloatAccumulator get() {
				System.out.println("supplier: I've heard someone needed an accumulator?");
				return new FloatAccumulator();
			}

		};
	}

	@Override
	public BiConsumer<FloatAccumulator, Float> accumulator() {
		System.out.println("Creating the accumulator function");
		return new BiConsumer<FloatAccumulator, Float>() {

			@Override
			public void accept(FloatAccumulator accumulator, Float value) {
				System.out.println("accumulator: then you add");
				accumulator.addValue(value.floatValue());
			}

		};
	}

	@Override
	public BinaryOperator<FloatAccumulator> combiner() {
		return new BinaryOperator<FloatAccumulator>() {

			@Override
			public FloatAccumulator apply(FloatAccumulator acc1, FloatAccumulator acc2) {
				System.out.println("combiner");
				acc1.addValue(acc2.getValue());
				return acc1;
			}
		};
	}

	@Override
	public Function<FloatAccumulator, Float> finisher() {
		return new Function<FloatAccumulator, Float>() {

			@Override
			public Float apply(FloatAccumulator t) {
				System.out.println("finisher: Finish him!!!");
				return t.getValue();
			}

		};
	}

	@Override
	public Set<Characteristics> characteristics() {
		if (!hasBeenCalledAtLeastOnce) {
			System.out.println("characteristics : can I process it parallel way?");
			hasBeenCalledAtLeastOnce = true;
		} else {
			System.out.println("characteristics : should I finish it myself?");
		}
		return CHARACTERISTICS;
	}
}
