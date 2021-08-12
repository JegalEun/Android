package com.example.blockchainproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.example.blockchainproject.Adapter.ListViewCandidateAdapter;
import com.example.blockchainproject.Model.ApiClient;
import com.example.blockchainproject.Model.ApiInterface;
import com.example.blockchainproject.Model.UserAccount;
import com.example.blockchainproject.Model.Vote;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import jnr.a64asm.SYSREG_CODE;
import retrofit2.Call;
import retrofit2.Callback;


public class CandidateListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    private ArrayList<ListViewCandidate> listViewCandidateList = new ArrayList<ListViewCandidate>();
    private ListViewCandidateAdapter adapter;

    public String UserNumber;
    public int Userid;

    //Retrofit
    private ApiInterface service;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidate_list);

        TextView textView = findViewById(R.id.tv_candidate);
        Button btn_go_voting;

        Intent UserNumberIntent = getIntent();
        UserNumber = UserNumberIntent.getExtras().getString("UserNumber");
        Userid = UserNumberIntent.getExtras().getInt("Userid");
        System.out.println(Userid+"CandidateListActivity 여기 Userid 넘어와야함");

        Intent intent = getIntent();
        String college = intent.getExtras().getString("college");
        System.out.println(college+"CandidateListActivity의 college");
        //대학은 잘 넘어오는거 확인

        recyclerView = findViewById(R.id.rv_candidate_list);
        adapter = new ListViewCandidateAdapter(this, listViewCandidateList);
        recyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                adapter.notifyDataSetChanged();

                try {
                    JSONArray jsonArray = new JSONArray(response);

                    for (int i = 0; i < response.length(); i++) {
                        JSONObject jObject = jsonArray.getJSONObject(i);
                        String candidate_name = jObject.getString("name");
                        //String imgPath = jObject.getString("imgPath");
                        int voteCount = jObject.getInt("voteCount");
                        int candidateNumber = jObject.getInt("candidateNumber");

//                        imgPath = "http://voting.dothome.co.kr"+imgPath;
//                        listViewCandidateList.add(new ListViewCandidate(candidate_name,imgPath,promisePath));
                        listViewCandidateList.add(new ListViewCandidate(candidate_name, voteCount, candidateNumber));


                        adapter.notifyItemInserted(0);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        RecyclerView list_container = findViewById(R.id.rv_candidate_list);
        ListViewCandidateAdapter adapter = new ListViewCandidateAdapter(this,listViewCandidateList);
        RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(this);

        CandidateListRequest candidatelistRequest = new CandidateListRequest(college,responseListener);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(candidatelistRequest);

        //retrofit
        service = ApiClient.getApiClient().create(ApiInterface.class);

        //투표하러가기 버튼 눌렀을 때
        btn_go_voting = findViewById( R.id.btn_go_voting );
        btn_go_voting.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View view) {


                Call<UserAccount> call_account = service.getAccount(Userid);
                call_account.enqueue(new Callback<UserAccount>() {
                    @Override
                    public void onResponse(Call<UserAccount> call, retrofit2.Response<UserAccount> response) {
                        //성공했을 경우
                        if (response.isSuccessful()) {//응답을 잘 받은 경우
                            String result = response.body().toString();
//                            Log.v(TAG, "result = " + result);
//                            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                        } else {    //통신은 성공했지만 응답에 문제있는 경우
                            System.out.println("error="+String.valueOf(response.code()));
//                            Log.v(TAG, "error = " + String.valueOf(response.code()));
                            Toast.makeText(getApplicationContext(), "error = " + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<UserAccount> call, Throwable t) {//통신 자체 실패
//                       Log.v(TAG, "Fail");
                        Toast.makeText(getApplicationContext(), "Response Fail", Toast.LENGTH_SHORT).show();
                    }
                });

                Intent intent = new Intent(CandidateListActivity.this, VoteActivity.class );
                intent.putExtra("college", college);
                intent.putExtra("UserNumber",UserNumber);

                startActivity(intent);
            }
        });

    }
}
