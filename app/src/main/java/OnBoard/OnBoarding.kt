package OnBoard

import  android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.example.shoppinglist.Login
import com.example.shoppinglist.R

class OnBoarding : AppCompatActivity(), FirstFrag.OnClickFirstFrag, SecondFrag.OnClickSecondFrag {

    private lateinit var myViewPager: ViewPager2
    private lateinit var myFragmentViewPagerAdapter: MyFragmentViewPagerAdapter
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding)

        bindViews()
        setUpPagerAdapter()
        setUpSharedPref()
    }

    private fun setUpSharedPref() {
        sharedPreferences = getSharedPreferences("OnBoardingCheck", MODE_PRIVATE)
    }

    private fun setUpPagerAdapter() {
        myFragmentViewPagerAdapter = MyFragmentViewPagerAdapter(supportFragmentManager, lifecycle)
        myViewPager.adapter = myFragmentViewPagerAdapter
    }

    private fun bindViews() {
        myViewPager = findViewById(R.id.viewPagerID)
    }

    override fun onClickNext() {
        myViewPager.currentItem = 1
    }

    override fun onClickBack() {
        myViewPager.currentItem = 0
    }

    override fun onClickDone() {
        // save state in SharedPreference
        var editor = sharedPreferences.edit()
        editor.putBoolean("doneCheck", true)
        editor.apply()
        startActivity(Intent(this, Login::class.java))
        finish()
    }
}