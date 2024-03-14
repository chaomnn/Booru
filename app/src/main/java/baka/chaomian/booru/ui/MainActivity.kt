package baka.chaomian.booru.ui

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import baka.chaomian.booru.R
import baka.chaomian.booru.data.Post
import baka.chaomian.booru.databinding.ActivityMainBinding
import baka.chaomian.booru.utils.LoginManager
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawer: DrawerLayout
    private lateinit var navigation: NavigationView
    private lateinit var menu: Menu

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
            replaceFragment(BooruFragment(), false)
        }
        fragmentManager.setFragmentResultListener(KEY_FRAGMENT, this) { _, bundle ->
            val post: Post = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getParcelable(KEY_POST, Post::class.java)!!
            } else {
                @Suppress("Deprecation")
                bundle.getParcelable(KEY_POST)!!
            }
            replaceFragment(PostFragment(post), true)
        }
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        drawer = binding.drawer
        navigation = binding.navigation
        menu = navigation.menu
        drawerToggle = object : ActionBarDrawerToggle(this, drawer, 0, 0) {
            override fun onDrawerOpened(drawerView: View) {
                if (LoginManager.isUserLoggedIn) {
                    menu.findItem(R.id.login).setTitle(R.string.logout)
                }
                super.onDrawerOpened(drawerView)
            }
        }
        drawerToggle.drawerArrowDrawable.color = Color.WHITE
        changeDrawerIconState(false)
        navigation.setNavigationItemSelectedListener(this)
        navigation.itemIconTintList = null
        drawer.addDrawerListener(drawerToggle)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val backStackCount = fragmentManager.backStackEntryCount
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.close()
                } else if (backStackCount == 0) {
                    finish()
                } else {
                    fragmentManager.popBackStackImmediate()
                    if (backStackCount == 1) {
                        changeDrawerIconState(false)
                    }
                }
            }
        })
        LoginManager.sharedPreferences = getSharedPreferences(packageName, Context.MODE_PRIVATE)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true
        } else if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val fragment = when (item.itemId) {
            R.id.danbooru -> {
                supportActionBar!!.title = getString(R.string.danbooru)
                BooruFragment()
            }
            R.id.login -> {
                if (!LoginManager.isUserLoggedIn) {
                    LoginFragment()
                } else {
                    LoginManager.logout()
                    item.setTitle(R.string.login)
                    null
                }
            }
            else ->
                null
        }
        if (fragment != null) {
            replaceFragment(fragment, true)
        }
        drawer.close()
        return true
    }

    private fun replaceFragment(fragment: Fragment, addToBackStack: Boolean) {
        val transaction = fragmentManager
            .beginTransaction()
            .replace(binding.container.id, fragment)
            .setReorderingAllowed(true)
        if (addToBackStack) {
            transaction.addToBackStack(null)
            changeDrawerIconState(true)
        }
        transaction.commit()
    }

    private fun changeDrawerIconState(canNavigateBack: Boolean) {
        drawerToggle.apply {
            isDrawerIndicatorEnabled = !canNavigateBack
            syncState()
        }
    }
}
