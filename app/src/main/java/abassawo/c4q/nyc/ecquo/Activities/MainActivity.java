package abassawo.c4q.nyc.ecquo.Activities;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.andtinder.model.CardModel;
import com.andtinder.model.Orientations;
import com.andtinder.view.CardContainer;
import com.andtinder.view.SimpleCardStackAdapter;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import abassawo.c4q.nyc.ecquo.Model.DBHelper;
import abassawo.c4q.nyc.ecquo.Model.EmailFetcher;
import abassawo.c4q.nyc.ecquo.Model.Task;
import abassawo.c4q.nyc.ecquo.Model.User;
import abassawo.c4q.nyc.ecquo.Model.sPlanner;
import abassawo.c4q.nyc.ecquo.R;
import butterknife.Bind;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, AbsListView.OnScrollListener, AbsListView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.empty_prompt)
    TextView tasksEmptyTV;
    @Bind(R.id.fab2)
    FloatingActionButton fabEdit;

    @Bind(R.id.empty_card_view)
    CardView emptyLayout;
    @Bind(R.id.drawer_view)
    DrawerLayout mDrawerLayout;

    @Bind(R.id.fab_redo)
    FloatingActionButton fabRedo;
    Task redoTask;

    private SimpleCardStackAdapter adapter;

    private FragmentManager fragMan;
    private  SQLiteDatabase  db;
    boolean firstRun;



    private String TAG = "abassawo.c4q.nyc.ecquo.Activities.MainActivity";
    public static int REQUEST_NEW_TASK = 5;

    //save our header or result
    private AccountHeader headerResult = null;
    private Drawer drawerModel = null;
    private static final int PROFILE_SETTING = 1;
    public static List<Task> taskList;
    public static List<Task> todayList;
    private User user;
    private IProfile userProfile;

    private ActionBarDrawerToggle mDrawerToggle;
    @Bind(R.id.deck1) CardContainer deck;





    public void initDB(){
        DBHelper dbHelper = new DBHelper(this);
        db = dbHelper.getWritableDatabase();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        taskList = sPlanner.get(getApplicationContext()).fetchAllTasks();
        todayList = generateDummyData();
        initDB();
        ButterKnife.bind(this);
        setupNavDrawer(savedInstanceState);
        initState();
        initListeners();
        setupActionBar();
        taskList = sPlanner.get(getApplicationContext()).getTasks();
        setupDayStacks(deck);
        emptyLayout.setAlpha(1);


        // alarmMan = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE); //run in background thread or servic.

    }


    public void setupNavDrawer(Bundle savedInstanceState){

            user = new User();
            if(user.points == null) {
                user.pointTally = 100;
            } else {
                user.points = String.valueOf(user.pointTally) + " Points" ;
            }
            userProfile = new ProfileDrawerItem().withName(EmailFetcher.getEmailId(this))
                    .withNameShown(true).withEmail(user.points)
                    .withIcon(getResources()
                            .getDrawable(R.drawable.heart));

        final IProfile abassProfile = new ProfileDrawerItem().withName("Abass Bayo")
                .withNameShown(true)
                .withEmail("86 Points")
                .withIcon(getResources()
                        .getDrawable(R.drawable.abassicon));
        final IProfile hansProfile = new ProfileDrawerItem().withName("Hans")
                .withNameShown(true)
                .withEmail("75 Points")
                .withIcon(getResources()
                        .getDrawable(R.drawable.hansicon));
        final IProfile joshProfile = new ProfileDrawerItem().withName("Joshelyn")
                .withNameShown(true)
                .withEmail("96 Points")
                .withIcon(getResources()
                        .getDrawable(R.drawable.joshelynicon));

                        //14dp for the add account icon in gmail but 20dp for the normal icons (like manage account)

//                        new ProfileSettingDrawerItem().withName("Work").withDescription("Add new Goal").withIcon(new IconicsDrawable(this).actionBarSize().paddingDp(5).colorRes(R.color.material_drawer_primary_text)),
//                        new ProfileSettingDrawerItem().withName("School").withDescription("Add new Goal").withIcon(new IconicsDrawable(this).actionBarSize().paddingDp(5).colorRes(R.color.material_drawer_primary_text)),
                        new ProfileSettingDrawerItem().withName("Coalition for Queens").withIcon(new IconicsDrawable(this).actionBarSize().paddingDp(5).colorRes(R.color.material_drawer_primary_text));
        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .addProfiles(abassProfile, hansProfile, joshProfile,
                        //14dp for the add account icon in gmail but 20dp for the normal icons (like manage account)
//                        new ProfileSettingDrawerItem().withName("Work").withDescription("Add new Goal").withIcon(new IconicsDrawable(this).actionBarSize().paddingDp(5).colorRes(R.color.material_drawer_primary_text)),
//                        new ProfileSettingDrawerItem().withName("School").withDescription("Add new Goal").withIcon(new IconicsDrawable(this).actionBarSize().paddingDp(5).colorRes(R.color.material_drawer_primary_text)),
                        new ProfileSettingDrawerItem().withName("Coalition for Queens").withIcon(new IconicsDrawable(this).actionBarSize().paddingDp(5).colorRes(R.color.material_drawer_primary_text)
                        )
                ).withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        return false;
                    }

                                        })
                                        .withSavedInstance(savedInstanceState)
                                        .build();

        drawerModel = new DrawerBuilder().withActivity(this)
                .withSliderBackgroundColor(getResources().getColor(R.color.primary_dark_material_light))
                .withToolbar(toolbar)
                .withAccountHeader(headerResult).addDrawerItems(
                        new PrimaryDrawerItem().withName("New Task").withIcon(getResources().getDrawable(R.drawable.newtaskicon)).withIdentifier(R.id.nav_new_task),
                        new PrimaryDrawerItem().withName("Places").withIcon(getResources().getDrawable(R.drawable.locationicon_white)).withIdentifier(R.id.nav_places),
                        new PrimaryDrawerItem().withName("All Tasks").withIcon(getResources().getDrawable(R.drawable.alltaskicon)).withIdentifier(R.id.nav_all_tasks),
                        new PrimaryDrawerItem().withName("Contact").withIcon(getResources().getDrawable(R.drawable.twitter_icon)).withIdentifier(R.id.contact_us)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int i, IDrawerItem iDrawerItem) {

                        switch (iDrawerItem.getIdentifier()) {
                            case R.id.nav_new_task: startActivity(new Intent(MainActivity.this, EditActivity.class));
                                break;
                            case R.id.nav_places: startActivity(new Intent(MainActivity.this, MapViewActivity.class));
                                break;
                            case R.id.nav_all_tasks: startActivity(new Intent(MainActivity.this, TaskListActivity.class));
                        }
                        return false;
                    }
                }).withSavedInstance(savedInstanceState).withShowDrawerOnFirstLaunch(true).build();
    }             // Finally we set the drawer toggle sync State


    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    public Bitmap getBitmapFromUri(Uri uri){
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public Bitmap getThumbnail(Uri uri) throws FileNotFoundException, IOException{
        InputStream input = getApplicationContext().getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither=true;//optional
        onlyBoundsOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1))
            return null;

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

        double ratio = (originalSize > 1) ? (originalSize / 1) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither=true;//optional
        bitmapOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;//optional
        input = this.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }

    private static int getPowerOfTwoForSampleRatio(double ratio){
        int k = Integer.highestOneBit((int)Math.floor(ratio));
        if(k==0) return 1;
        else return k;
    }


    public void setupDayStacks(CardContainer decK) {
        Drawable defaultDrawable = getResources().getDrawable(R.drawable.c4qlogo);
        adapter = new SimpleCardStackAdapter(this); //ADAPTER FOR POPULATING DECK LIST.

        //TODO - Sort list before populating deck.
        deck.setOrientation(Orientations.Orientation.Disordered); //ORIENTATION ORDER. PRIOR TO THIS, SORT THE LIST APPROPRIATELY


        //fixme : sort the list by priority factors.
        for (int i = 0; i < taskList.size(); i++) {
            Task iterTask = taskList.get(i);

            if (iterTask.isNotifyToday()) {
                todayList.add(iterTask);
            }
        }
        if (!todayList.isEmpty()) {


//            for (int i = 0; i < todayList.size(); i++) {
//                CardModel card = new CardModel();
////                if(todayList.get(i).isCustomPhotoSet()){
////                    Bitmap drawable = null;
////                    try {
////                        drawable = getThumbnail (Uri.parse(todayList.get(i).getUriStr()));
////                    } catch (IOException e) {
////                        e.printStackTrace();
////                    }
////                    card = new CardModel(todayList.get(i).getTitle(),
////                            todayList.get(i).getLabel(),
////                            drawable);
////
////                } else {
//            }

        //TODO - Sort list before populating deck.
        deck.setOrientation(Orientations.Orientation.Disordered); //ORIENTATION ORDER. PRIOR TO THIS, SORT THE LIST APPROPRIATELY


        //fixme : sort the list by priority factors.
        for (int i = 0; i < todayList.size(); i++) {
            Task iterTask = todayList.get(i);
            CardModel card = new CardModel();


                    card = new CardModel
                            (todayList.get(i).getTitle(),
                            todayList.get(i).getLabel(),
                            defaultDrawable);
               // }


            //if (iterTask.isCustomPhotoSet()) {
            //card = new CardModel(iterTask.getTitle(),  iterTask.getLabel(), getResources().getDrawable(R.drawable.main_screen_rocket));
            //} else {
            //card = new CardModel(todayList.get(i).getTitle(),  iterTask.getLabel(), getResources().getDrawable(R.drawable.main_screen_rocket)); //fixme

            //}

            card = new CardModel(
                    todayList.get(i).getTitle(),
                    iterTask.getLabel(),
                    getResources().getDrawable(R.drawable.c4qlogo));

            redoTask = new Task(todayList.get(i).getTitle(), getApplicationContext());
            redoTask.setLabel(todayList.get(i).getLabel());

            card.setOnCardDimissedListener(new CardModel.OnCardDimissedListener() {
                final View coordinatorLayoutView = findViewById(R.id.main_content);


                    @Override
                    public void onLike() {  //this is swiping left. library is backwards.
                        user.addtoPoints(10);
                        Snackbar
                                .make(coordinatorLayoutView, "Task dismissed for later", Snackbar.LENGTH_SHORT)
                                .show();
                    //todo:set redo button to Visible HERE. (possible to have it fade out after 5 seconds?
                        final Animation animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out);
                        Animation animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);

                        fabRedo.setAnimation(animFadeIn);
                        fabRedo.setVisibility(View.VISIBLE);
                        fabRedo.setClickable(true);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                fabRedo.setAnimation(animFadeOut);
                                fabRedo.setVisibility(View.INVISIBLE);
                                fabRedo.setClickable(false);
                            }
                        }, 5000);

                }

                @Override
                public void onDislike() {  //this is whiping right. hence the positive note.
                    Snackbar
                            .make(coordinatorLayoutView, "Good Job, Keep up the good work", Snackbar.LENGTH_LONG)
                            .show();
                    //todo:set redo Button to Visible HERE. (possible to have it fade out after 5 seconds?
                    final Animation animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out);
                    Animation animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);

                    fabRedo.setAnimation(animFadeIn);
                    fabRedo.setVisibility(View.VISIBLE);
                    fabRedo.setClickable(true);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            fabRedo.setAnimation(animFadeOut);
                            fabRedo.setVisibility(View.INVISIBLE);
                            fabRedo.setClickable(false);
                        }
                    }, 5000);
                }
            });



                //if (iterTask.isCustomPhotoSet()) {
                //card = new CardModel(iterTask.getTitle(),  iterTask.getLabel(), getResources().getDrawable(R.drawable.main_screen_rocket));
                //} else {
                //card = new CardModel(todayList.get(i).getTitle(),  iterTask.getLabel(), getResources().getDrawable(R.drawable.main_screen_rocket)); //fixme

                //}

                adapter.add(card);


            }


        }

        deck.setAdapter(adapter);


        deck.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                if (todayList.isEmpty()) {
                    tasksEmptyTV.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });

    }

    private void initState() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        fragMan = getSupportFragmentManager();
    }

    public void initListeners(){
        fabEdit.setOnClickListener(this);
        deck.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Task task = (Task) deck.getItemAtPosition(position);
//                Intent detailintent = new Intent(getApplicationContext(), TaskDetailActivity.class);
//                detailintent.putExtra("TASK", task.getId()); //fixme
                //startActivity(detailintent);
                Log.d(deck.getSelectedView().toString(), "task to string");
            }
        });

    }


    public void setupActionBar(){
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getResources().getString(R.string.app_name));
        Date date = new Date();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        //actionBar.setHomeAsUpIndicator(R.mipmap.ic_ecquo);
    }

    public List<Task> generateDummyData(){
        List<Task> dummyTasks = new ArrayList();
        Task handwashTask = new Task("Hand Wash Some Clothes",getApplicationContext());
        handwashTask.setLabel("Chores");
        Task codingTask = new Task("Review Java Exercise", getApplicationContext());
        codingTask.setLabel("Coding");
        Task buyPhone = new Task("Buy HTC at MetroPCS", getApplicationContext());
        buyPhone.setLabel("Personal");
        Task harryPotter = new Task("Buy Harry Potter Books", getApplicationContext());
        harryPotter.setLabel("Random");
        Task momi = new Task("GPS directions to MOMI", getApplicationContext());
        momi.setLabel("C4Q");
        dummyTasks.add(handwashTask);
        dummyTasks.add(buyPhone);
        dummyTasks.add(codingTask);
        dummyTasks.add(harryPotter);
        dummyTasks.add(new Task("Pick up Groceries", getApplicationContext()));
        dummyTasks.add(momi);
        return dummyTasks;
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //case R.id.fab2: startActivity(new Intent(MainActivity.this, TabbedEditActivity.class));
            case R.id.fab2:
                startActivityForResult(new Intent(MainActivity.this, EditActivity.class), REQUEST_NEW_TASK); //testing animation
                break;
            case R.id.fabGraph:
                Toast.makeText(getApplicationContext(), "Personalized Results Coming Soon", Toast.LENGTH_SHORT).show();
                //startActivity(new Intent(MainActivity.this, StatsActivity.class));
                break;
            case R.id.fab_redo:
                Toast.makeText(MainActivity.this, "redo button clicked", Toast.LENGTH_SHORT).show();
                //todo: write redo code here!
                todayList.add(redoTask);
                adapter.notifyDataSetChanged();
                break;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                drawerModel.openDrawer();
                break;

        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "QueryTextSubmit: " + query);
                sPlanner.setStoredQuery(getApplicationContext(), query);
                updateSearchQueryItems();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = sPlanner.getStoredQuery(getApplicationContext());
                searchView.setQuery(query, false);
            }
        });
        return true;
    }


    private void updateSearchQueryItems(){
        //new FetchQueryTask().execute();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //sPlanner.get(getApplicationContext()).saveTasks();
    }




}



