package com.example.dempapp

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import io.omnitalk.sdk.OmniEventListener
import io.omnitalk.sdk.Omnitalk
import io.omnitalk.sdk.types.EventBroadcast
import io.omnitalk.sdk.types.EventConnected
import io.omnitalk.sdk.types.EventLeave
import io.omnitalk.sdk.types.EventMessage
import io.omnitalk.sdk.types.OmniEvent
import io.omnitalk.sdk.types.PublicTypes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AudioRoomActivity : AppCompatActivity() {
    private lateinit var selectedRoom: PublicTypes.Room
    private lateinit var partiListAdapter: PartiListAdapter
    private lateinit var chatListAdapter: ChatListAdapter

    val sdk = Omnitalk.getInstance()
    var mySession: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.audio_room)

        val backBtn = findViewById<Button>(R.id.backBtn)
        val roomListView = findViewById<ListView>(R.id.roomListView)
        val selectedRoomTextView = findViewById<TextView>(R.id.selectedRoomView)
        val secretInput = findViewById<TextInputEditText>(R.id.secretInput)
        val joinBtn = findViewById<Button>(R.id.joinBtn)

        val buttonLayout = findViewById<ConstraintLayout>(R.id.buttonLayout)
        val viewLayout = findViewById<LinearLayout>(R.id.viewLayout)

        val partiRecyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val partiDataList = mutableListOf<String>()

        val chatRecyclerView = findViewById<RecyclerView>(R.id.chatView)
        val chatInput = findViewById<TextInputEditText>(R.id.chatInput)
        val sendBtn = findViewById<Button>(R.id.sendBtn)
        val chatDataList = mutableListOf<ChatData>()

        // 참여자 리스트 뷰
        val llm = LinearLayoutManager(this)
        partiRecyclerView.layoutManager = llm
        partiListAdapter = PartiListAdapter(partiDataList)
        partiRecyclerView.adapter = partiListAdapter

        // 채팅 뷰
        val llm2 = LinearLayoutManager(this)
        chatRecyclerView.layoutManager = llm2;
        chatListAdapter = ChatListAdapter(chatDataList)
        chatRecyclerView.adapter = chatListAdapter


        sdk.setOnEventListener(object : OmniEventListener {
            override fun onEvent(eventName: OmniEvent, message: Any) {
                if (eventName == OmniEvent.BROADCASTING_EVENT) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val broadcastMsg = message as EventBroadcast
                        partiDataList.add(broadcastMsg.session)
                        partiListAdapter.notifyDataSetChanged()
                    }
                } else if (eventName == OmniEvent.CONNECTED_EVENT) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val connectedMsg = message as EventConnected
                        partiDataList.add(connectedMsg.session)
                        partiListAdapter.notifyDataSetChanged()
                    }
                } else if (eventName == OmniEvent.LEAVE_EVENT) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val broadcastMsg = message as EventLeave
                        partiDataList.remove(broadcastMsg.session)
                        partiListAdapter.notifyDataSetChanged()
                    }
                } else if (eventName == OmniEvent.MESSAGE_EVENT) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val messageMsg = message as EventMessage
                        chatDataList.add(ChatData(userId = messageMsg.userId, messageMsg.action, false, message = messageMsg.message))
                        chatListAdapter.notifyDataSetChanged()
                        chatRecyclerView.scrollToPosition(chatListAdapter.itemCount - 1)
                    }
                }
            }
            override fun onClose() {
                Log.d("audioroom", "onClose Event!!!")
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
                val sessionResult =  sdk.createSession(null)
                mySession = sessionResult.session
                val roomList = sdk.roomList(PublicTypes.VIDEOROOM_TYPE.audioroom, null) // default all type
                withContext(Dispatchers.Main) {
                    val roomAdapter = RoomAdapter(this@AudioRoomActivity, roomList.list)
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
                Log.e("audioroom", "failed to room list, $err")
            }
        }

        joinBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val secret = secretInput.text.toString()
                    sdk.joinRoom(this@AudioRoomActivity.selectedRoom.roomId, secret, null)
                    buttonLayout.visibility = View.INVISIBLE
                    viewLayout.visibility = View.VISIBLE

                    val partiListResult = sdk.partiList(null, null)
                    val partiList = partiListResult.list
                    for (parti in partiList) {
                        partiDataList.add(parti.session)
                    }
                    partiListAdapter.notifyDataSetChanged()
                } catch (err: Exception) {
                    Log.e("audioroom", "failed to join room, $err")
                }
            }
        }

        sendBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val message = chatInput.text.toString()
                    if (message.isNotEmpty()) {
                        sdk.sendMessage(message)
                        chatDataList.add(ChatData("나", PublicTypes.MESSAGE_ACTION.send, true, message))
                        chatListAdapter.notifyDataSetChanged()
                        chatInput.setText("")
                        chatRecyclerView.scrollToPosition(chatListAdapter.itemCount - 1)
                    }
                } catch (err: Exception) {
                    Log.e("audioroom", "failed to send message, $err")
                }
            }
        }
    }
}

class PartiListAdapter(private val localDataSet: MutableList<String>): RecyclerView.Adapter<PartiListAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView
        init {
            textView = itemView.findViewById(R.id.userName)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.parti_list_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val text = localDataSet[position]
        holder.textView.text = text
    }

    override fun getItemCount(): Int {
        return localDataSet.size
    }
}

class ChatListAdapter(private val localDataSet: MutableList<ChatData>): RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView
        init {
            textView = itemView.findViewById(R.id.chat)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_list_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = localDataSet[position]
        if (message.send) {
            holder.textView.gravity = Gravity.RIGHT
        } else {
            holder.textView.gravity = Gravity.LEFT
        }
        if (message.action == PublicTypes.MESSAGE_ACTION.whisper) {
            holder.textView.setTextColor(Color.RED)
        } else if (message.action == PublicTypes.MESSAGE_ACTION.join) {
            message.message = "채팅방에 참가했습니다."
        } else if (message.action == PublicTypes.MESSAGE_ACTION.leave) {
            message.message = "채팅방에서 퇴장했습니다."
        }
        holder.textView.text = "${message.userId}:${message.message}"
    }

    override fun getItemCount(): Int {
        return localDataSet.size
    }
}

data class ChatData (
    val userId: String,
    val action: PublicTypes.MESSAGE_ACTION,
    val send: Boolean,
    var message: String?
)
