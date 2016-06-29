/*
 * Copyright (C) 2015 COR Church in Irvine, CA
 *
 * Licensed under the COR License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Filename: TabFragmentPagerAdapter.java
 * Description: Set up a page adapter for the tab fragment.
 *
 * Changes:             Author  |   Date    |   Description
 *                      J.Jones     23-Nov-15   Create file, added core functionality.
 *
 */

package org.corapp.cor;

import java.util.List;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

/*
 * FragmentStatePagerAdpater is used because it destroys all unneeded fragments.
 * When a fragment is destroyed, the Bundle is saved and when the user navigates back,
 * the new fragment will be restored using that instance state.
 *
 * FragmentPageAdapter leaves the fragment instance alive in the FragmentManager, which
 * means the states still exist and the fragments are never destroyed.
 *
 * Problems arise when swiping from tab to tab, some tabs show the same url as the previous.
 */
public class TabFragmentPagerAdapter extends FragmentStatePagerAdapter {

    List<Fragment> fragments;

    public TabFragmentPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) { return this.fragments.get(position); }

    @Override
    public int getCount() { return fragments.size(); }
}