package com.example.dempapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import io.omnitalk.sdk.OmniEventListener
import io.omnitalk.sdk.Omnitalk
import io.omnitalk.sdk.types.EventBroadcast
import io.omnitalk.sdk.types.EventLeave
import io.omnitalk.sdk.types.OmniEvent
import io.omnitalk.sdk.types.PublicTypes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.webrtc.SurfaceViewRenderer
import java.lang.Exception

data class ViewInfo (
    val session: String,
    val view: SurfaceViewRenderer
)

class VideoRoomActivity : AppCompatActivity() {
    private lateinit var selectedRoom: PublicTypes.Room

    // subscribe adapter
    private lateinit var adapter: RecyclerViewAdapter
    private val subscribeViewList = mutableListOf<String>()
    private val viewManager = mutableMapOf<String, SurfaceViewRenderer>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.video_room)

        val backBtn = findViewById<Button>(R.id.backBtn)
        val roomListView = findViewById<ListView>(R.id.roomListView)
        val selectedRoomTextView = findViewById<TextView>(R.id.selectedRoomView)
        val secretInput = findViewById<TextInputEditText>(R.id.secretInput)
        val joinBtn = findViewById<Button>(R.id.joinBtn)

        val buttonLayout = findViewById<ConstraintLayout>(R.id.buttonLayout)
        val viewLayout = findViewById<LinearLayout>(R.id.viewLayout)

        val localView = findViewById<SurfaceViewRenderer>(R.id.localVideo)

        val sdk = Omnitalk.getInstance()
        var mySession: String? = null

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val llm = LinearLayoutManager(this@VideoRoomActivity)
        llm.orientation = LinearLayoutManager.HORIZONTAL
        recyclerView.layoutManager = llm
        this@VideoRoomActivity.adapter = RecyclerViewAdapter(this@VideoRoomActivity.subscribeViewList, sdk)
        recyclerView.adapter = this@VideoRoomActivity.adapter

        sdk.setOnEventListener(object : OmniEventListener {
            override fun onEvent(eventName: OmniEvent, message: Any) {
                if (eventName == OmniEvent.BROADCASTING_EVENT) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val broadcastMsg = message as EventBroadcast
                        val view = SurfaceViewRenderer(this@VideoRoomActivity)
                        viewManager[broadcastMsg.session] = view
                        sdk.subscribe(broadcastMsg.session, view)
                        this@VideoRoomActivity.subscribeViewList.add(broadcastMsg.session)
                        this@VideoRoomActivity.adapter.notifyDataSetChanged()
                    }
                } else if (eventName == OmniEvent.LEAVE_EVENT) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val leaveMsg = message as EventLeave
                        this@VideoRoomActivity.subscribeViewList.remove(leaveMsg.session)
                        this@VideoRoomActivity.adapter.notifyDataSetChanged()
                    }
                }
            }
            override fun onClose() {
                Log.d("videoroom", "onClose Event!!!")
            }
        })

        backBtn.setOnClickListener {
            sdk.leave(mySession)
            finish()
        }

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                sdk.leave(mySession)
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        CoroutineScope(Dispatchers.Default).launch {
            try {
                sdk.createSession(null)
                val roomList = sdk.roomList(PublicTypes.VIDEOROOM_TYPE.videoroom, null)
                withContext(Dispatchers.Main) {
                    val roomAdapter = RoomAdapter(this@VideoRoomActivity, roomList.list)
                    roomListView.adapter = roomAdapter
                    roomListView.setOnItemClickListener { _, _, position, _ ->
                        val selectedItem = roomAdapter.getItem(position)
                        if (selectedItem != null) {
                            selectedRoom = selectedItem
                            selectedRoomTextView.text = selectedRoom.subject
                        }
                    }
                }
            } catch (err: Exception) {
                Log.e("videoroom", "failed to room list, $err")
            }
        }

        joinBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val secret = secretInput.text.toString()
                    sdk.joinRoom(this@VideoRoomActivity.selectedRoom.roomId, secret, null)
                    buttonLayout.visibility = View.INVISIBLE
                    viewLayout.visibility = View.VISIBLE

                    sdk.publish(localView)

                    val partiListResult = sdk.partiList(null, null)
                    val partiList = partiListResult.list
                    for (parti in partiList) {
                        val view = SurfaceViewRenderer(this@VideoRoomActivity)
                        viewManager[parti.session] = view
                        sdk.subscribe(parti.session, view)
                        this@VideoRoomActivity.subscribeViewList.add(parti.session)
                        this@VideoRoomActivity.adapter.notifyDataSetChanged()
                    }
                } catch (err: Exception) {
                    Log.e("videoroom", "failed to join room, $err")
                }
            }
        }
    }
}

// room용
class RoomAdapter(context: Context, items: List<PublicTypes.Room>) :
    ArrayAdapter<PublicTypes.Room>(context, android.R.layout.simple_list_item_1, items) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val item = getItem(position)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = "${item?.subject} (${item?.count ?: 0})" // item?.subject
        return view
    }
}

// Subscribe RecyclerView 예제
class RecyclerViewAdapter(private val viewList: MutableList<String>, private val sdk: Omnitalk): RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var view: SurfaceViewRenderer = itemView.findViewById(R.id.videoViewRenderer)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_surface_view, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = viewList[position]
        CoroutineScope(Dispatchers.Main).launch {
            // RecyclerView를 사용할 경우 `onBindViewHolder` 에서 sdk.bind를 호출해야 합니다.
            sdk.bind(item, holder.view)
        }
    }
    override fun getItemCount(): Int {
        return viewList.size
    }
}
