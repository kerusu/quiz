package by.kerusu.quiz;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import android.content.Context;

public class ComplementProvider {

    private List<String> complements;
    private Random random = new Random();

    public ComplementProvider(Context context) {
        complements = Arrays.asList(context.getResources().getStringArray(R.array.complement_list));
    }

    public String getRandomComplement() {
        return complements.get(random.nextInt(complements.size()));
    }
}
