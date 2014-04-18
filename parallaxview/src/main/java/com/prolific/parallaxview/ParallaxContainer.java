package com.prolific.parallaxview;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import java.util.ArrayList;
import java.util.List;

public class ParallaxContainer extends FrameLayout implements ViewPager.OnPageChangeListener {

  protected Context mContext;
  protected int mChildCount;
  protected int mCurrentChild;
  protected List<View> mParallaxViewList = new ArrayList<View>();
  protected ViewPager mViewPager;
  protected SpaceFragment mSpaceFragment1;
  protected SpaceFragment mSpaceFragment2;
  protected int mContainerWidth;

  public ParallaxContainer(Context context) {
    super(context);
    mContext = context;
  }

  public ParallaxContainer(Context context, AttributeSet attrs) {
    super(context, attrs);
    mContext = context;
  }

  public ParallaxContainer(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    mContext = context;
  }

  @Override
  public void onWindowFocusChanged (boolean hasFocus) {
    mContainerWidth = getMeasuredWidth();
    super.onWindowFocusChanged(hasFocus);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    // how many steps before the viewpager loops
    mChildCount = getChildCount();

    addChildrenToParallaxViewList();

    // make view pager with same attributes as container
    mViewPager = new ViewPager(mContext);
    mViewPager.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    mViewPager.setOnPageChangeListener(this);
    mViewPager.setId(R.id.PAGER_ID);

    // two empty fragments
    mSpaceFragment1 = new SpaceFragment();
    mSpaceFragment2 = new SpaceFragment();

    // create an adapter that provides 7 blank fragments
    if (ParallaxUtil.mManager != null) {
      mViewPager.setAdapter(new FragmentPagerAdapter(ParallaxUtil.mManager) {
        @Override public Fragment getItem(int i) {
          // switch off which fragment is active, so the other one can be recycled
          return i % 2 == 1 ? mSpaceFragment1 : mSpaceFragment2;
        }

        @Override public int getCount() {
          return Integer.MAX_VALUE;
        }
      });
    }

    addView(mViewPager);
    bringChildToFront(mViewPager);
  }

  // top-level children only
  private void addChildrenToParallaxViewList() {
    for (mCurrentChild = 0; mCurrentChild < mChildCount; mCurrentChild++) {
      View view = getChildAt(mCurrentChild);

      if (view instanceof ViewGroup) {
        addGrandChildrenToParallaxViewList((ViewGroup) view);
      } else {
        addViewToParallaxViewList(view);
      }
    }
  }

  // recursively add all children UNDER top-level
  private void addGrandChildrenToParallaxViewList(ViewGroup viewGroup) {
    int count = viewGroup.getChildCount();
    for (int i = 0; i < count; i++) {
      View view = getChildAt(i);
      if (view instanceof ViewGroup) {
        addGrandChildrenToParallaxViewList((ViewGroup) view);
      } else {
        addViewToParallaxViewList(view);
      }
    }
  }

  // attach attributes in tag
  private void addViewToParallaxViewList(View view) {
    ParallaxViewTag tag = (ParallaxViewTag) view.getTag(R.id.TAG_ID);
    tag.position = mCurrentChild;
    mParallaxViewList.add(view);
  }

  @Override public void onPageScrolled(int position, float offset, int offsetPixels) {
    if (mChildCount > 0) {
      position = position % mChildCount;
    }

    for (View view : mParallaxViewList) {
      applyParallaxEffects(view, position, offsetPixels);
    }
  }

  @Override public void onPageSelected(int position) {
    // recycle whichever fragment isn't active
    if (position % 2 == 1) {
      mSpaceFragment2 = new SpaceFragment();
    } else {
      mSpaceFragment1 = new SpaceFragment();
    }
  }

  @Override public void onPageScrollStateChanged(int i) {}

  private void applyParallaxEffects(View view, int position, float offsetPixels) {

    ParallaxViewTag tag = (ParallaxViewTag) view.getTag(R.id.TAG_ID);

    if ((position == tag.position - 1
        || position == tag.position + (mChildCount-1))
           && mContainerWidth != 0) {

      // make visible
      view.setVisibility(VISIBLE);

      // slide in from right
      view.setTranslationX((mContainerWidth - offsetPixels) * tag.xIn);

      // slide in from top
      view.setTranslationY(0 - (mContainerWidth - offsetPixels) * tag.yIn);

      // fade in
      if (tag.fadeIn) {
        view.setAlpha(1.0f - (mContainerWidth - offsetPixels) / mContainerWidth);
      }

    } else if (position == tag.position) {

      // make visible
      view.setVisibility(VISIBLE);

      // slide out to left
      view.setTranslationX(0 - offsetPixels * tag.xOut);

      // slide out to top
      view.setTranslationY(0 - offsetPixels * tag.yOut);

      // fade out
      if (tag.fadeOut) {
        view.setAlpha(1.0f - offsetPixels / mContainerWidth);
      }

    } else {
      view.setVisibility(GONE);
    }
  }
}
