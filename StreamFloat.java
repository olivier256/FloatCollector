package stream;

import java.util.Arrays;
import java.util.List;

public class StreamFloat {

	public static void main(String[] args) {
		List<Float> floats = Arrays.asList(1_000_000f, 3.141_5f, 2.718_2f); // 1 000 005,8597 ~ 1 000 005,9
		float sum = 0;
		for (Float f : floats) {
			sum += f.floatValue();
		}
		System.out.printf("%.10f%n", sum); // 1000005,8125000000
		System.out.println(sum + "\n"); // 1000005.8

		Float collect = floats.stream().collect(new SummingFloatCollector()); // Shorter. As long as you don't delve
																				// into the source
		System.out.printf("%.10f%n", collect); // 1000005,8125000000
		System.out.println(collect + "\n"); // 1000005.8

		collect = floats.stream().collect(new KahanSummingFloatCollector());
		System.out.printf("%.10f%n", collect); // 1000005,8750000000
		System.out.println(collect + "\n"); // 1000005.9

	}
}
