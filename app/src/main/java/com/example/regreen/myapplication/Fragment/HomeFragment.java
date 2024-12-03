package com.example.regreen.myapplication.Fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.regreen.R;
import com.example.regreen.myapplication.Admin.GenericMethod.FirebaseImageHelper;
import com.example.regreen.myapplication.Admin.GenericMethod.FirebaseRepository;
import com.example.regreen.myapplication.EditProfile;
import com.example.regreen.myapplication.LearnMore;
import com.example.regreen.myapplication.ModelData.News;
import com.example.regreen.myapplication.ModelData.User;
import com.example.regreen.myapplication.MyAdapter;
import com.example.regreen.myapplication.RecivingPoint;
import com.example.regreen.myapplication.User.News.ListNewActivity;
import com.example.regreen.myapplication.User.News.ViewNewDetailActivity;
import com.example.regreen.myapplication.User.UserBooking;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private MyAdapter<News> newsAdapter;
    private FirebaseRepository<News> newsRepository;
    private FirebaseRepository<User> userRepository;
    private FirebaseImageHelper imageHelper;
    private CardView cardBook, cardCollectPoint;
    private LinearLayout btn_paper, btn_plastic, btn_metal, btn_glass, btn_Pin, btn_organic, btn_more, btn_diff;

    public HomeFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.listNew);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        newsRepository = new FirebaseRepository<>("News", News.class);
        imageHelper = new FirebaseImageHelper(requireContext());

        TextView tvGreeting = view.findViewById(R.id.tvGreeting);
        TextView tvHomeScore = view.findViewById(R.id.tvHomeScore);
        TextView tvDetailListNew = view.findViewById(R.id.tvDetailListNew);

        btn_paper = view.findViewById(R.id.btn_paper);
        btn_plastic = view.findViewById(R.id.btn_plastic);
        btn_metal = view.findViewById(R.id.btn_metal);
        btn_glass = view.findViewById(R.id.btn_glass);
        btn_Pin = view.findViewById(R.id.btn_Pin);
        btn_organic = view.findViewById(R.id.btn_organic);
        btn_more = view.findViewById(R.id.btn_more);
        btn_diff = view.findViewById(R.id.btn_diff);

        // Hiển thị lời chào
        String userEmail = getArguments().getString("userEmail");

        if (userEmail != null) {
            String emailPath = userEmail.replace(".", ",");

            userRepository = new FirebaseRepository<>("User", User.class);
            userRepository.get(emailPath, new FirebaseRepository.OnFetchListener<User>() {
                @Override
                public void onSuccess(User user) {
                    if (user != null) {
                        tvGreeting.setText("Xin chào " + user.getName() + "!");
                        if(user.getPointReward() == 0){
                            tvHomeScore.setText("Cày điểm nào");
                        }else{
                            tvHomeScore.setText(user.getPointReward() + " điểm");
                        }
                    } else {
                        tvGreeting.setText("Xin chào User!");
                        tvHomeScore.setText("0 điểm");
                    }
                }

                @Override
                public void onFailure(String message) {
                    Log.e("HomeFragment", "Không thể tải hồ sơ: " + message);
                    tvGreeting.setText("Xin chào User!");
                }
            });
        } else {
            Log.e("HomeFragment", "Không tìm thấy email đăng nhập.");
            tvGreeting.setText("Xin chào User!");
        }

        cardBook = view.findViewById(R.id.cardBook);
        cardBook.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), UserBooking.class);
            intent.putExtra("userEmail",userEmail);
            startActivity(intent);
        });

        cardCollectPoint = view.findViewById(R.id.cardCollectPoint);
        cardCollectPoint.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RecivingPoint.class);
            startActivity(intent);
        });

        tvDetailListNew.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ListNewActivity.class);
            startActivity(intent);
        });

        setupNavigationButtons();

        newsAdapter = new MyAdapter<>(new ArrayList<>(), R.layout.new_item_home, new MyAdapter.Binder<News>() {
            @Override
            public void bind(View itemView, News item) {
                TextView newsTitle = itemView.findViewById(R.id.newsTitle);
                TextView newsDate = itemView.findViewById(R.id.newsDate);
                ImageView newsImage = itemView.findViewById(R.id.imgNew1);

                newsTitle.setText(item.getTitle());
                newsDate.setText(item.getDateUp());

                if (item.getImageResource() != null && !item.getImageResource().isEmpty()) {
                    imageHelper.loadImageFromRealtimeDatabase("News/" + item.getIdNew() + "/imageResource", newsImage);
                }

                itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), ViewNewDetailActivity.class);
                    intent.putExtra(ViewNewDetailActivity.EXTRA_NEWS, item); // Pass the News object to the activity
                    startActivity(intent);
                });
            }
        });

        recyclerView.setAdapter(newsAdapter);
        loadLatestNewsData();
    }


    private void loadLatestNewsData() {
        newsRepository.getLatestItems(5, new FirebaseRepository.OnFetchListListener<News>() {
            @Override
            public void onSuccess(List<News> newsList) {
                if (newsList == null || newsList.isEmpty()) {
                    Log.d("HomeFragment", "No news data found in Firebase.");
                    Toast.makeText(getContext(), "Chưa có tin tức nào", Toast.LENGTH_SHORT).show();
                } else {
                    newsAdapter.setData(newsList);
                }
            }

            @Override
            public void onFailure(String message) {
                Log.e("HomeFragment", "Error fetching data from Firebase: " + message);
                Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupNavigationButtons() {
        btn_plastic.setOnClickListener(v -> openLearnMoreActivity(0));
        btn_paper.setOnClickListener(v -> openLearnMoreActivity(1));
        btn_glass.setOnClickListener(v -> openLearnMoreActivity(2));
        btn_metal.setOnClickListener(v -> openLearnMoreActivity(3));
        btn_organic.setOnClickListener(v -> openLearnMoreActivity(4));
        btn_Pin.setOnClickListener(v -> openLearnMoreActivity(5));
        btn_more.setOnClickListener(v -> openLearnMoreActivity(6));

    }

    private void openLearnMoreActivity(int selectedTab) {
        Intent intent = new Intent(getActivity(), LearnMore.class);
        intent.putExtra("selectedTab", selectedTab);
        startActivity(intent);
    }


}

