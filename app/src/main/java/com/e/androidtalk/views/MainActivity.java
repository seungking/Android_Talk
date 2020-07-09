package com.e.androidtalk.views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.View;

import com.e.androidtalk.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tabs)
    TabLayout mTabLayout;

    @BindView(R.id.fab)
    FloatingActionButton mFab;

    @BindView(R.id.viewpager)
    ViewPager mViewPager;

    ViewPagerAdapter mPageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        mTabLayout.setupWithViewPager(mViewPager);
        setUpViewPager();
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment currentFragment = mPageAdapter.getItem(mViewPager.getCurrentItem());
                if (currentFragment instanceof FriendFragment) {
                    ((FriendFragment) currentFragment).toggleSearchBar();
                } else {
                    // 친구 탭으로 이동
                    mViewPager.setCurrentItem(2, true);
                    // 체크박스가 보일수 있도록 처리
                    FriendFragment friendFragment = (FriendFragment) mPageAdapter.getItem(1);
                    friendFragment.toggleSelectionMode();
                }
            }
        });

    }

    private void setUpViewPager(){
        mPageAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mPageAdapter.addFragment(new ChatFragment(), "채팅");
        mPageAdapter.addFragment(new FriendFragment(), "친구");
        mViewPager.setAdapter(mPageAdapter);
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragmentList = new ArrayList<>();
        private List<String> fragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fragmentManager){
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

    }

}