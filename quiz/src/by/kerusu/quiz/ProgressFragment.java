package by.kerusu.quiz;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;

public class ProgressFragment extends DialogFragment {

    private static final String FRAGMENT_TAG = ProgressFragment.class.getCanonicalName();

    private static final String ARGS_TITLE_RES_ID = "titleResId";

    private static ProgressFragment newInstance(int titleResId) {

        Bundle args = new Bundle();
        args.putInt(ARGS_TITLE_RES_ID, titleResId);

        ProgressFragment fragment = new ProgressFragment();
        fragment.setArguments(args);

        return fragment;
    }

    private View view;
    private View icon;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_progress, container, false);
        icon = view.findViewById(R.id.icon);
        getDialog().setTitle(getArguments().getInt(ARGS_TITLE_RES_ID));
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().setCancelable(false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        final ScaleAnimation growAnim = new ScaleAnimation(.7f, 1f, .7f, 1f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        final ScaleAnimation shrinkAnim = new ScaleAnimation(1f, .7f, 1f, .7f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);

        growAnim.setDuration(700);
        growAnim.setFillBefore(true);
        shrinkAnim.setDuration(700);

        icon.setAnimation(growAnim);
        growAnim.start();

        growAnim.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                icon.setAnimation(shrinkAnim);
                shrinkAnim.start();
            }
        });
        shrinkAnim.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                icon.setAnimation(growAnim);
                growAnim.start();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        icon.clearAnimation();
    }

    public static void show(FragmentActivity activity, int titleResId) {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment prev = fragmentManager.findFragmentByTag(FRAGMENT_TAG);
        if (prev != null) {
            fragmentTransaction.remove(prev);
        }
        fragmentTransaction.addToBackStack(null);
        newInstance(titleResId).show(fragmentManager, FRAGMENT_TAG);
    }

    public static void dismiss(FragmentActivity activity) {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        Fragment prev = fragmentManager.findFragmentByTag(FRAGMENT_TAG);
        if (prev != null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(prev);
            fragmentTransaction.commit();
        }
    }
}
