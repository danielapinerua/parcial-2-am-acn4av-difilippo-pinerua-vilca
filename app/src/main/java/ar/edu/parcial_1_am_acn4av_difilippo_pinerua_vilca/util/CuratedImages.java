package ar.edu.parcial_1_am_acn4av_difilippo_pinerua_vilca.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fallback images used by the public home when the random image APIs fail.
 */
public final class CuratedImages {

    private static final List<String> DOGS = Arrays.asList(
            "https://images.dog.ceo/breeds/spaniel-cocker/n02102318_9670.jpg",
            "https://images.dog.ceo/breeds/springer-english/n02102040_2792.jpg",
            "https://images.dog.ceo/breeds/pembroke/n02113023_6702.jpg",
            "https://images.dog.ceo/breeds/brabancon/n02112706_1117.jpg",
            "https://images.dog.ceo/breeds/appenzeller/n02107908_314.jpg",
            "https://images.dog.ceo/breeds/spaniel-cocker/n02102318_7000.jpg"
    );

    private static final List<String> CATS = Arrays.asList(
            "https://cdn2.thecatapi.com/images/cj5.jpg",
            "https://cdn2.thecatapi.com/images/vXiNSY60T.jpg",
            "https://cdn2.thecatapi.com/images/d20.jpg",
            "https://cdn2.thecatapi.com/images/efe.jpg",
            "https://cdn2.thecatapi.com/images/1ts.jpg",
            "https://cdn2.thecatapi.com/images/MjA1NDI0MA.jpg"
    );

    private CuratedImages() {
    }

    public static List<String> dogs() {
        return new ArrayList<>(DOGS);
    }

    public static List<String> cats() {
        return new ArrayList<>(CATS);
    }
}
