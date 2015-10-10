package info.meizi.ui.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

import java.util.List;

import info.meizi.adapter.MainAdapter;
import info.meizi.base.BaseFragment;
import info.meizi.bean.MainBean;
import info.meizi.net.MainService;
import info.meizi.ui.group.GroupActivity;
import io.realm.Realm;

/**
 * Created by Mr_Wrong on 15/10/9.
 * 首页那几个tab
 */
public class MainFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    String type;
    MainAdapter mAdapter;

    public MainFragment(String type) {
        this.type = type;
    }

    Realm realm;

    private BroadcastReceiver Receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(type)) {
                List<MainBean> latest = MainBean.all(realm, type);
                mAdapter.replaceWith(latest);
                mRefresher.setRefreshing(false);
            }
        }
    };

    @Override
    protected void lazyLoad() {
        if (!isVisible) {
            return;
        }
        SendToLoad();
    }

    //发送去加载首页数据
    private void SendToLoad() {
        Intent intent = new Intent(getActivity(), MainService.class);
        intent.putExtra("type", type);
        getActivity().startService(intent);
    }

    private void startGroupActivity(View view, int position) {
        Intent intent1 = new Intent(getActivity(), GroupActivity.class);
        intent1.putExtra("index", position);
        intent1.putExtra("groupid", url2groupid(mAdapter.get(position).getUrl()));//----这里应该是groupid不是整个URL
        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(getActivity(), view, mAdapter.get(position).getUrl());
        getActivity().startActivity(intent1, options.toBundle());
    }


    private String url2groupid(String url) {
        return url.split("/")[3];
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        IntentFilter filter = new IntentFilter();
        filter.addAction(type);
        getActivity().registerReceiver(Receiver, filter);
        realm = Realm.getInstance(getActivity());

        mAdapter = new MainAdapter(getContext()) {
            @Override
            protected void onItemClick(View v, int position) {
                startGroupActivity(v, position);
            }
        };

        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    public void onDestroyView() {
        getActivity().unregisterReceiver(Receiver);
        super.onDestroyView();
    }

    //刷新的回调
    @Override
    public void onRefresh() {
        SendToLoad();
    }
}
