package com.paulvarry.intra42.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.paulvarry.intra42.R;
import com.paulvarry.intra42.api.model.TeamsUsers;
import com.paulvarry.intra42.utils.UserImage;

import java.util.List;

public class GridAdapterUsersLittle extends BaseAdapter {

    Context activity;
    private List<TeamsUsers> users;

    GridAdapterUsersLittle(Context context, List<TeamsUsers> users) {

        this.activity = context;
        this.users = users;
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public TeamsUsers getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return users.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();

            LayoutInflater vi = LayoutInflater.from(activity);

            convertView = vi.inflate(R.layout.grid_view_users_little, parent, false);
            holder.imageViewUsers = convertView.findViewById(R.id.imageViewUsers);
            holder.imageViewStar = convertView.findViewById(R.id.imageViewStar);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TeamsUsers user = getItem(position);

        if (user.leader)
            holder.imageViewStar.setVisibility(View.VISIBLE);
        else
            holder.imageViewStar.setVisibility(View.GONE);
        UserImage.setImage(activity, user, holder.imageViewUsers);

        return convertView;
    }

    static class ViewHolder {

        private ImageView imageViewUsers;
        private ImageView imageViewStar;

    }
}
