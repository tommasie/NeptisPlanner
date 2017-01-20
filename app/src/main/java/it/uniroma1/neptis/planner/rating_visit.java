package it.uniroma1.neptis.planner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import it.uniroma1.neptis.planner.planning.Best_Rate_Plan;

public class rating_visit extends AppCompatActivity {

    TextView title;
    ListView lv;
    SearchView sv;
    private List<String> message;
    private List<String> lresult;


    ArrayAdapter<ElementRating> adapter;
    private ArrayList<ElementRating> arrayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating_visit);

        lresult = new ArrayList<>();

        Intent intent = getIntent();
        message = intent.getStringArrayListExtra(Best_Rate_Plan.EXTRA_MESSAGE);

        title =  (TextView) findViewById(R.id.textView10_rating);

        lv=(ListView) findViewById(R.id.listView_rating);
        sv=(SearchView) findViewById(R.id.searchView_rating);


        // popola la lista
        arrayList = new ArrayList<>();
        for(int i=0; i<message.size();i++) {
            ElementRating temp = new ElementRating(message.get(i), "0", 0);
            arrayList.add(temp);
        }

        adapter = new ListViewAdapter(this, R.layout.rating_listview, arrayList);
        lv.setAdapter(adapter);



        // FILTRO
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String text) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean onQueryTextChange(String text) {

                adapter.getFilter().filter(text);

                return false;
            }
        });
    }


    public class ListViewAdapter extends ArrayAdapter<ElementRating> {

        private AppCompatActivity activity;
        private List<ElementRating> movieList;

        public ListViewAdapter(AppCompatActivity context, int resource, ArrayList<ElementRating> objects) {
            super(context, resource, objects);
            this.activity = context;
            this.movieList = objects;
        }

        @Override
        public ElementRating getItem(int position) {
            return movieList.get(position);
        }


        private RatingBar.OnRatingBarChangeListener onRatingChangedListener(final RecyclerView.ViewHolder holder, final int position) {
            return new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                    ElementRating item = getItem(position);
                    item.setRatingStar(v);

                    //ler.add(item);
                    //lresult.add(item.getName());
                    //lresult.add(""+item.getRating());
                }
            };
        }




        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.rating_listview, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
                holder.ratingBar.getTag(position);
            }

            holder.ratingBar.setOnRatingBarChangeListener(onRatingChangedListener(holder, position));

            holder.ratingBar.setTag(position);
            holder.ratingBar.setRating(getItem(position).getRatingStar());
            holder.movieName.setText(getItem(position).getName());

            return convertView;
        }


        private RatingBar.OnRatingBarChangeListener onRatingChangedListener(final ViewHolder holder, final int position) {
            return new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                    ElementRating item = getItem(position);
                    item.setRatingStar(v);
                    arrayList.get(position).setRating((int) v);
                    //lresult.add(item.getName());
                    //lresult.add("" + item.getRating());
                   // Log.i("Adapter", "star: " + v);
                }
            };
        }



        private class ViewHolder {
            private RatingBar ratingBar;
            private TextView movieName;

            public ViewHolder(View view) {
                ratingBar = (RatingBar) view.findViewById(R.id.rate_img);
                movieName = (TextView) view.findViewById(R.id.text);
            }
        }

    }


    public void done_rating(View view) {
        lresult = new ArrayList<>();
        for (int i=0;i<arrayList.size();i++) {
            //Toast.makeText(getApplicationContext(), ""+arrayList.get(i).getRating(), Toast.LENGTH_LONG).show();
            //if(arrayList.get(i).getRating()>0)
                lresult.add(arrayList.get(i).getName());
                lresult.add(""+arrayList.get(i).getRating());

        }
        Intent returnIntent = new Intent();
        returnIntent.putStringArrayListExtra("rating2", (ArrayList<String>) lresult);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();




    }

    public static class ElementRating  implements  Serializable{
        String name;
        String id;
        float rating;

        ElementRating(String n, String id, float rat) {
            this.name = n;
            this.id = id;
            this.rating = rat;
        }

        public float getRatingStar() {
            return rating;
        }

        public void setRatingStar(float ratingStar) {
            this.rating = ratingStar;
        }


        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public float getRating() {
            return rating;
        }

        public void setRating(int r) {
            this.rating=r;
        }




    }


}


