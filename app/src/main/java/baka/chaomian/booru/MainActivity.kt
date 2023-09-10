package baka.chaomian.booru

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import baka.chaomian.booru.data.Post
import baka.chaomian.booru.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding : ActivityMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawer : DrawerLayout

    private val fragmentManager = supportFragmentManager

    companion object {
        private const val KEY_POST = "post"
        private const val KEY_FRAGMENT = "switch_fragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            fragmentManager.setFragmentResultListener(KEY_FRAGMENT, this) {_, bundle ->
                val post: Post = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getParcelable(KEY_POST, Post::class.java)!!
                } else {
                    @Suppress("Deprecation")
                    bundle.getParcelable(KEY_POST)!!
                }
                fragmentManager
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .add(binding.container.id, PostFragment(post), null)
                    .addToBackStack(null)
                    .commit()
            }
            fragmentManager
                .beginTransaction()
                .add(binding.container.id, BooruFragment())
                .setReorderingAllowed(true)
                .commitNow()
        }
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        drawer = binding.drawer
        drawerToggle = ActionBarDrawerToggle(this, drawer, 0, 0)
        drawerToggle.apply {
            drawerArrowDrawable.color = Color.WHITE
            syncState()
        }
        binding.navigation.setNavigationItemSelectedListener(this)
        drawer.addDrawerListener(drawerToggle)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.close()
                } else if (fragmentManager.fragments.last() is PostFragment) {
                    fragmentManager.popBackStackImmediate()
                } else if (fragmentManager.backStackEntryCount == 0) {
                    finish()
                }
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.danbooru) {
            fragmentManager
                .beginTransaction()
                .replace(binding.container.id, BooruFragment())
                .setReorderingAllowed(true)
                .commitNow()
            supportActionBar!!.title = getString(R.string.danbooru)
            drawer.close()
        }
        return true
    }
}
