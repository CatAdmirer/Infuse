package com.catadmirer.infuseSMP;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.ToIntFunction;

public class WeightedRandom {
        public static <T> int getTotalWeight(List<T> list, ToIntFunction<T> getWeight) {
        long i = 0L;

        for (T t : list) {
            i += getWeight.applyAsInt(t);
        }

        if (i > 2147483647L) {
            throw new IllegalArgumentException("Sum of weights must be <= 2147483647");
        } else {
            return (int)i;
        }
    }

    public static <T> Optional<T> getRandomItem(Random random, List<T> list, int totalWeight, ToIntFunction<T> getWeight) {
        if (totalWeight < 0) {
            throw new IllegalArgumentException("Negative total weight in getRandomItem");
        }

        if (totalWeight == 0) return Optional.empty();

        int i = random.nextInt(totalWeight);
        return Optional.of(getWeightedItem(list, i, getWeight));
    }

    public static <T> T getWeightedItem(List<T> list, int weight, ToIntFunction<T> getWeight) {
        for (T t : list) {
            weight -= getWeight.applyAsInt(t);
            if (weight < 0) {
                return t;
            }
        }

        return null;
    }

    public static <T> Optional<T> getRandomItem(Random random, List<T> list, ToIntFunction<T> getWeight) {
        return getRandomItem(random, list, getTotalWeight(list, getWeight), getWeight);
    }

}
