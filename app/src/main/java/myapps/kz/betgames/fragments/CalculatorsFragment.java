package myapps.kz.betgames.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import myapps.kz.betgames.MainActivity;
import myapps.kz.betgames.R;
import myapps.kz.betgames.adapters.CalcPagerAdapter;

/**
 * Created by rauan on 23.07.17.
 */

public class CalculatorsFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private CalcPagerAdapter adapter;
    private Fragment fragmentCalc1, fragmentCalc2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calculators, container, false);

        ((MainActivity) getActivity()).setToolbar("", true);

        viewPager = (ViewPager) view.findViewById(R.id.vpCalculators);
        if (savedInstanceState == null) {
            fragmentCalc1 = new Calc1Fragment();
            fragmentCalc2 = new Calc2Fragment();
        }

        adapter = new CalcPagerAdapter(getChildFragmentManager());
        adapter.addFragment(fragmentCalc1, "Калькулятор 1");
        adapter.addFragment(fragmentCalc2, "Калькулятор 2");
        viewPager.setAdapter(adapter);

        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);


        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_refresh).setVisible(false);
        menu.findItem(R.id.action_silent).setVisible(false);
        menu.findItem(R.id.action_reset).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    public void makeBet() {
        if (viewPager.getCurrentItem() == 0 && fragmentCalc1 != null) {
            ((Calc1Fragment)fragmentCalc1).updateList();
        } else if (viewPager.getCurrentItem() == 1 && fragmentCalc2 != null) {
            ((Calc2Fragment)fragmentCalc2).updateList();
        }
    }

    public void makeReset() {
        if (viewPager.getCurrentItem() == 0 && fragmentCalc1 != null) {
            ((Calc1Fragment)fragmentCalc1).reset();
        } else if (viewPager.getCurrentItem() == 1 && fragmentCalc2 != null) {
            ((Calc2Fragment)fragmentCalc2).reset();
        }
    }
}
