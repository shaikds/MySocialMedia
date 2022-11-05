package com.shaikds.togather.model;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ScrollView;
import android.widget.Toast;

import com.shaikds.togather.BooleanListener;
import com.shaikds.togather.R;
import com.shaikds.togather.adapters.SearchAdapter;
import com.shaikds.togather.view.fragment.SearchFragment;
import com.shaikds.togather.view.fragment.FragmentGroupView;
import com.shaikds.togather.viewmodel.MainSearchPostsViewModel;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

public class Tours implements BooleanListener {
    private SearchFragment searchFragment;
    private FragmentGroupView groupViewFragment;
    BooleanListener mBooleanListener;


    public Tours(FragmentGroupView fragmentGroupView) {
        this.groupViewFragment = fragmentGroupView;
        this.mBooleanListener = this;

    }

    public Tours(SearchFragment searchFragment) {
        this.searchFragment = searchFragment;
        this.mBooleanListener = this;
    }

    private void tourForFirstTimeUsers(ScrollView sv) {
        final Activity mActivity = groupViewFragment.getActivity();
        SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        final int[] scrollCount = {0};
        Handler handler = new Handler();
        final TapTargetSequence[] tapTargetSequence = new TapTargetSequence[1];
        handler.postDelayed(() -> {
            sv.smoothScrollTo(0, sv.getTop() - 20);
            tapTargetSequence[0] = new TapTargetSequence(mActivity)
                    .targets(TapTarget.forView(groupViewFragment.tvPageTitle, "כותרת הקבוצה", "אם הכותרת היא כוח רכישה - אתם בקבוצה ללא מנהל כרגע.אם קבוצת רכישה, יש מנהל לקבוצה שמוכר את המוצר.")
                                    .titleTextSize(20).targetRadius(80)
                                    .transparentTarget(true)
                                    .cancelable(false)
                                    .descriptionTextSize(18)
                                    .outerCircleColor(R.color.dollarPrimary)
                                    .targetCircleColor(R.color.background)
                                    .textColor(R.color.background).titleTextSize(24),
                            TapTarget.forView(groupViewFragment.tvGroupPrice, "מחיר מיוחד לקבוצה", "המחיר המיוחד לחברי הקבוצה לאחר הנחת קבוצת רכישה").dimColor(R.color.dollarPrimary)
                                    .titleTextSize(20).targetRadius(40)
                                    .transparentTarget(true)
                                    .cancelable(false)
                                    .descriptionTextSize(18)
                                    .outerCircleColor(R.color.dollarPrimary)
                                    .targetCircleColor(R.color.dollarSecondary)
                                    .textColor(R.color.background).titleTextSize(24),
                            TapTarget.forView(groupViewFragment.tvPrice, "מחיר רגיל", "המחיר הרגיל שקיים בשוק למוצר").dimColor(R.color.dollarPrimary)
                                    .titleTextSize(20).targetRadius(40)
                                    .transparentTarget(true)
                                    .cancelable(false)
                                    .descriptionTextSize(18)
                                    .outerCircleColor(R.color.dollarPrimary)
                                    .targetCircleColor(R.color.dollarSecondary)
                                    .textColor(R.color.background).titleTextSize(24),
                            TapTarget.forView(groupViewFragment.tvMaxPeople, "מקסימום אנשים", "מקסימום אנשים שיכולים להצטרף לקבוצה.(ניתן לעריכה על ידי מנהל)").dimColor(R.color.dollarPrimary)
                                    .titleTextSize(20).targetRadius(40)
                                    .transparentTarget(true)
                                    .cancelable(false)
                                    .descriptionTextSize(18)
                                    .outerCircleColor(R.color.dollarPrimary)
                                    .targetCircleColor(R.color.dollarSecondary)
                                    .textColor(R.color.background).titleTextSize(24),
                            TapTarget.forView(groupViewFragment.tvMinPeople, "מינימום אנשים", "כאשר מס׳ חברי הקבוצה יגיע למינימום אנשים הנדרש, המכירה תחל ויהיה ניתן לרכוש את המוצר.")
                                    .dimColor(R.color.dollarPrimary)
                                    .titleTextSize(20).targetRadius(40)
                                    .transparentTarget(true)
                                    .cancelable(false)
                                    .descriptionTextSize(18)
                                    .outerCircleColor(R.color.dollarPrimary)
                                    .targetCircleColor(R.color.dollarSecondary)
                                    .textColor(R.color.background).titleTextSize(24),
                            TapTarget.forView(groupViewFragment.btnMail, "הצעת ניהול במייל", "חפשו מייל של בעלי עסקים שמוכרים את המוצר, ושלחו להם מייל מוכן מראש עם לינק ישירות לקבוצה!")
                                    .dimColor(R.color.dollarPrimary)
                                    .transparentTarget(true)
                                    .titleTextSize(20).targetRadius(60)
                                    .outerCircleColor(R.color.dollarPrimary)
                                    .targetCircleColor(R.color.dollarSecondary)
                                    .textColor(R.color.background).titleTextSize(24),
                            TapTarget.forView(groupViewFragment.btnShare, "שיתוף חברים", "שתפו את הקבוצה עם חבריכם בכדי שיצטרפו עוד אנשים והמכירה תחל")
                                    .titleTextSize(20).targetRadius(50)
                                    .outerCircleColor(R.color.dollarPrimary)
                                    .transparentTarget(true)
                                    .descriptionTextSize(18)
                                    .targetCircleColor(R.color.dollarSecondary)
                                    .textColor(R.color.background).titleTextSize(24),
                            TapTarget.forView(groupViewFragment.btnBeManager, "ניהול הקבוצה", "לחיצה על הכפתור הזה מאפשר למשתמש הקבוצה, אשר מעוניין למכור את המוצר לכולם, למלא כמה פרטים ולהפוך למנהל הקבוצה.למנהל יש את היכולת לערוך את פרטי הקבוצה.").dimColor(R.color.dollarPrimary)
                                    .outerCircleColor(R.color.dollarSecondary)
                                    .targetCircleColor(R.color.dollarPrimary)
                                    .transparentTarget(true)
                                    .descriptionTextSize(18)
                                    .titleTextSize(20).targetRadius(120)
                                    .textColor(R.color.dollarPrimary).titleTextSize(24),
                            TapTarget.forView(groupViewFragment.btnPurchase, "רכישה", "הכפתור יהפוך לירוק כשהמספר של חברי הקבוצה יגיע למספר המינימום אנשים אשר נקבע על-ידי מנהל הקבוצה.").dimColor(R.color.dollarPrimary)
                                    .titleTextSize(20)
                                    .targetRadius(150)
                                    .descriptionTextSize(18)
                                    .transparentTarget(false)
                                    .outerCircleColor(R.color.dollarSecondary)
                                    .targetCircleColor(R.color.dollarPrimary)
                                    .textColor(R.color.background).titleTextSize(24)).listener(new TapTargetSequence.Listener() {
                        @Override
                        public void onSequenceFinish() {
                            editor.putBoolean("tourGroupView", true);
                            editor.apply();
                        }


                        @Override
                        public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                            scrollCount[0]++;
                            if (scrollCount[0] == 5) {
                                sv.smoothScrollTo(0, sv.getBottom());
                            }
                        }

                        @Override
                        public void onSequenceCanceled(TapTarget lastTarget) {
                        }
                    });
            tapTargetSequence[0].continueOnCancel(true);
            tapTargetSequence[0].start();
        }, 100);

    }


    public void checkGroupViewShared(ScrollView sv) {
        SharedPreferences sharedPref = groupViewFragment.getActivity().getPreferences(groupViewFragment.getContext().MODE_PRIVATE);
        boolean defaultValue = sharedPref.getBoolean("tourGroupView", false);
        if (!defaultValue) {
            // if first user, show tour .
            tourForFirstTimeUsers(sv);
        }
    }



    /* FROM HERE AND ABOVE ITS SEARCH FRAGMENT TOUR */
    //_____________________________________________//

    private void tourForFirstTimeUsersSearchFrag(SearchAdapter adapterSearchMain) {
        final Activity mActivity = searchFragment.getActivity();
        final SearchAdapter.PostViewHolder firstViewHolder = adapterSearchMain.getFirstViewHolder();
        if (adapterSearchMain.getFirstViewHolder()==null){
            Toast.makeText(mActivity, "ניתן לצפות בהדרכה לאחר שתקימו כח רכישה", Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences sharedPref = mActivity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        try { // first view holder is not null .
            //final SearchAdapter.PostViewHolder firstViewHolder = adapterSearchMain.getFirstViewHolder();
            Handler handler = new Handler();
            final TapTargetSequence[] tapTargetSequence = new TapTargetSequence[1];
            handler.postDelayed(() -> {
                tapTargetSequence[0] = new TapTargetSequence(mActivity)
                        .targets(TapTarget.forView(searchFragment.btnFilter, "סינון מתקדם", "כאן ניתן לסנן קבוצות לפי ערים וסוגי קבוצות.")
                                        .titleTextSize(20).targetRadius(40)
                                        .descriptionTextSize(20)
                                        .outerCircleColor(R.color.dollarPrimary)
                                        .targetCircleColor(R.color.dollarSecondary)
                                        .textColor(R.color.background).titleTextSize(24),
                                TapTarget.forView(searchFragment.btnSearchUser, "חיפוש קבוצות", "כאן ניתן לחפש קבוצות לפי כותרת").dimColor(R.color.dollarPrimary)
                                        .titleTextSize(20).targetRadius(40)
                                        .descriptionTextSize(20)
                                        .outerCircleColor(R.color.dollarPrimary)
                                        .targetCircleColor(R.color.dollarSecondary)
                                        .textColor(R.color.background).titleTextSize(24),
                                TapTarget.forView(searchFragment.rvCategories, "סינון קבוצות רכישה ", "לפי קטגוריות").dimColor(R.color.dollarPrimary)
                                        .titleTextSize(20).targetRadius(50)
                                        .transparentTarget(true)
                                        .descriptionTextSize(20)
                                        .outerCircleColor(R.color.background)
                                        .targetCircleColor(R.color.dollarSecondary)
                                        .textColor(R.color.dollarPrimary).titleTextSize(24),
                                TapTarget.forView(firstViewHolder.tvDiscountPer, "סוג הקבוצה", "כוח רכישה או אם זה קבוצת רכישה תראו כמה אחוזים חסכתם אם תקנו את המוצר בקבוצת הרכישה")
                                        .dimColor(R.color.dollarPrimary)
                                        .transparentTarget(true)
                                        .titleTextSize(20).targetRadius(50)
                                        .descriptionTextSize(20)
                                        .outerCircleColor(R.color.dollarSecondary)
                                        .targetCircleColor(R.color.dollarPrimary)
                                        .textColor(R.color.dollarPrimary).titleTextSize(24),
                                TapTarget.forView(firstViewHolder.btnLike, "לייק לקבוצה", "סמנו לייק לקבוצה אם תרצו שהמנהל יעלה מוצרים דומים נוספים למכירה באפליקצייה").dimColor(R.color.dollarPrimary)
                                        .titleTextSize(20).targetRadius(40)
                                        .transparentTarget(false)
                                        .descriptionTextSize(20)
                                        .outerCircleColor(R.color.btn_stroke_orange)
                                        .targetCircleColor(R.color.background)
                                        .textColor(R.color.background).titleTextSize(24),
                                TapTarget.forView(firstViewHolder.btnJoinGroup, "הצטרפות לקבוצה", "הצטרפות לקבוצה אם נותר מקום. ")
                                        .dimColor(R.color.dollarSecondary)
                                        .titleTextSize(20).targetRadius(30)
                                        .descriptionTextSize(20)
                                        .outerCircleColor(R.color.dollarSecondary)
                                        .targetCircleColor(R.color.dollarPrimary)
                                        .textColor(R.color.dollarPrimary).titleTextSize(24),
                                TapTarget.forView(firstViewHolder.tvPeopleCount, "כמות אנשים", "כמות מקסימלית של אנשים/כמות אנשים בקבוצה כרגע. אם הקבוצה ללא מנהל ופתוחה להצטרפות יהיה כתוב פתוח.")
                                        .titleTextSize(20).targetRadius(40)
                                        .outerCircleColor(R.color.dollarSecondary)
                                        .transparentTarget(false)
                                        .descriptionTextSize(20)
                                        .targetCircleColor(R.color.dollarPrimary)
                                        .textColor(R.color.dollarPrimary).titleTextSize(24),
                                TapTarget.forView(firstViewHolder.tvLocation, "מיקום", "באיזה איזור קבוצת הרכישה מתבצעת")
                                        .titleTextSize(20).targetRadius(40)
                                        .outerCircleColor(R.color.dollarSecondary)
                                        .transparentTarget(false)
                                        .descriptionTextSize(20)
                                        .targetCircleColor(R.color.dollarPrimary)
                                        .textColor(R.color.dollarPrimary).titleTextSize(24),
                                TapTarget.forView(firstViewHolder.tvPrice, "מחיר מיוחד לקבוצת רכישה", "מחיר מיוחד לאחר הנחה לקבוצת הרכישה. בקבוצה מסוג כח רכישה המחיר שמופיע הוא המחיר שמבקשים בעבור המוצר.").dimColor(R.color.dollarPrimary)
                                        .outerCircleColor(R.color.dollarSecondary)
                                        .targetCircleColor(R.color.dollarPrimary)
                                        .transparentTarget(false)
                                        .descriptionTextSize(20)
                                        .titleTextSize(20).targetRadius(50)
                                        .textColor(R.color.dollarPrimary).titleTextSize(24)).listener(new TapTargetSequence.Listener() {
                            @Override
                            public void onSequenceFinish() {
                                editor.putBoolean("tourSearch", true); // he finished the tour-->never show again.
                                editor.apply();
                                mBooleanListener.result(sharedPref.getBoolean("tourSearch", true));
                            }

                            @Override
                            public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

                            }

                            @Override
                            public void onSequenceCanceled(TapTarget lastTarget) {

                            }
                        });
                tapTargetSequence[0].start();
                tapTargetSequence[0].continueOnCancel(true);
            }, 1000);

        } catch (NullPointerException e) {
            // no first view holder.
        }

    }

    public void checkSearchShared(SearchAdapter adapter) {
        SharedPreferences sharedPref = searchFragment.getActivity().getPreferences(searchFragment.getContext().MODE_PRIVATE);
        boolean defaultValue = sharedPref.getBoolean("tourSearch", false);
        if (!defaultValue) {
            // if first user, show tour .
            tourForFirstTimeUsersSearchFrag(adapter);
        } else {
            mBooleanListener.result(true);
        }

    }

    //TODO : CHECK THIS METHOD WORKS.
    public void deepLinkToGroupView(MainSearchPostsViewModel viewModelMain, Bundle bundleUserType) {
        final Activity mActivity = searchFragment.getActivity();
        SharedPreferences sharedPref = mActivity.getSharedPreferences("tours_sp", Context.MODE_PRIVATE);
        String groupId = sharedPref.getString(mActivity.getString(R.string.deep_link_group_id), null);
        if (groupId != null) {
            // registered from deep link .
            // we selected group in deep link activity.
            Post selectedGroup = viewModelMain.getGroupById(groupId);
            bundleUserType.putString("group_title_deep_link", selectedGroup.getTitle());
            viewModelMain.getNavController().navigate(R.id.usersFragment, bundleUserType);
            sharedPref.edit().remove(searchFragment.getString(R.string.deep_link_group_id)).apply();
            sharedPref.edit().remove(searchFragment.getString(R.string.deep_link_group_position)).apply();

        }
    }


    @Override
    public void result(boolean isTrue) {
        if (isTrue) {// we can use deep link from now cuz tour has ended!!
            Bundle bundle = new Bundle();
            deepLinkToGroupView(searchFragment.getViewModelMain(), bundle);
        }
    }

    private void showSearchTourAgain(SearchAdapter adapter) {
        final SharedPreferences sharedPref = searchFragment.getActivity().getPreferences(Context.MODE_PRIVATE);
        sharedPref.edit().putBoolean("tourSearch", false);
        tourForFirstTimeUsersSearchFrag(adapter);
    }

    public void askUserShowTour(SearchAdapter adapter) {
        AlertDialog.Builder builder = new AlertDialog.Builder(searchFragment.getContext());
        builder.setTitle("סיור באפליקציה").setMessage("האם תרצו לצפות בהדרכה על האפליקציה מחדש?")
                .setPositiveButton("כן", (dialogInterface, i) -> {
                    showSearchTourAgain(adapter);
                    dialogInterface.dismiss();
                }).setNegativeButton("לא", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        }).show();
    }
}
