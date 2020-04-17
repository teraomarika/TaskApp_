package jp.techacademy.terao.marika.taskapp

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.Sort
import kotlinx.android.synthetic.*
import java.util.*

const val EXTRA_TASK="jp.techacademy.terao.marika.taskapp.TASK"


class MainActivity : AppCompatActivity() {
    private lateinit var mRealm: Realm
    private val mRealmListener=object: RealmChangeListener<Realm> {
        override fun onChange(element:Realm){
            reloadListView()
        }
    }
    private lateinit var mTaskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        // Realmの設定
        mRealm=Realm.getDefaultInstance()
        mRealm.addChangeListener(mRealmListener)

        // ListViewの設定
        mTaskAdapter= TaskAdapter(this@MainActivity)


        // ListViewをタップしたときの処理
        listView1.setOnItemClickListener{ parent, _, position, _ ->
            // 入力・編集する画面に遷移させる
            val task = parent.adapter.getItem(position) as Task
            val intent = Intent(this@MainActivity, InputActivity::class.java)
            intent.putExtra(EXTRA_TASK, task.id)
            startActivity(intent)
        }
        // ListViewを長押ししたときの処理
        listView1.setOnItemLongClickListener { parent, _, position, _ ->
            // タスクを削除する
            val task=parent.adapter.getItem(position)as Task
            // ダイアログを表示する
            val builder = AlertDialog.Builder(this@MainActivity)

            builder.setTitle("削除")
            builder.setMessage(task.title + "を削除しますか")
            builder.setPositiveButton("OK"){_, _ ->
                val results = mRealm.where(Task::class.java).equalTo("id", task.id).findAll()

                mRealm.beginTransaction()
                results.deleteAllFromRealm()
                mRealm.commitTransaction()

                val resultIntent=Intent(applicationContext,TaskAlarmReceiver::class.java)
                val resultPendingIntent=PendingIntent.getBroadcast(
                    this@MainActivity,
                     task.id,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                reloadListView()
            }
                builder.setNegativeButton("CANCEL", null)

            val dialog = builder.create()
            dialog.show()

            true
        }

        reloadListView()
    }
    private fun reloadListView(){
        val taskRealmResults=mRealm.where(Task::class.java).findAll().sort("date", Sort.DESCENDING)
        mTaskAdapter.taskList = mRealm.copyFromRealm(taskRealmResults)
        listView1.adapter = mTaskAdapter
        mTaskAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        mRealm.close()

    }
    private fun addTaskForTest() {
        val task = Task()
        task.title = "作業"
        task.contents = "プログラムを書いてPUSHする"
        task.date = Date()
        task.id = 0
        mRealm.beginTransaction()
        mRealm.copyToRealmOrUpdate(task)
        mRealm.commitTransaction()
    }


}
