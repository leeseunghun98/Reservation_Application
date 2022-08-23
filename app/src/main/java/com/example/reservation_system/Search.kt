package com.example.reservation_system

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_createmanage.view.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.android.synthetic.main.room_list.view.*


class Search : Fragment() {

    private lateinit var searchView : SearchView
    private lateinit var serach_room_recylerView : RecyclerView
    private lateinit var database: DatabaseReference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        database = Firebase.database.reference

        val search_DataList = arrayListOf<room_Data>()
        val adapter = SearchRecyclerViewAdapter(search_DataList)
        val rootView = inflater.inflate(R.layout.fragment_search, container, false)
        var room_cnt = 0
        searchView = rootView.findViewById(R.id.search_view)
        searchView.isSubmitButtonEnabled = true
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            // 제출 버튼 클릭시
            @SuppressLint("NotifyDataSetChanged")
            override fun onQueryTextSubmit(query: String?): Boolean {
                println(query + "완료")
                search_DataList.clear()
                // 코드검색
                if (query!!.startsWith("#")){
                    val code = query.slice(IntRange(1, query.length-1))

                    // Read Database
                    database.child("Room").child(code).get().addOnSuccessListener { it ->
                        val map = it.value as HashMap<*, *>
                        search_DataList.add(room_Data(map["maker"] as String, map["title"] as String, map["information"] as String, (map["code"] as Long).toInt(), map["room_category"] as String, (map["like"] as Long).toInt()))
                        adapter.notifyDataSetChanged()
                    }.addOnFailureListener{
                        Log.e("firebase", "Error getting data", it)
                    }

                } else { // Title 검색
                    // TODO : Title 검색
                    // startAt, endAt은 index, ValueEventLisnter, ChildEventListenter : https://stack07142.tistory.com/282, .startAt(query).endAt(query + "\uf8ff")
                    database.child("Room").orderByChild("like")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                (snapshot.value as HashMap<*, *>).forEach { snap ->
                                    if ((snap.value as HashMap<*, *>)["title"].toString().contains(query)) {
                                        val map = snap.value as HashMap<*, *>
                                        search_DataList.add(room_Data(map["maker"] as String, map["title"] as String, map["information"] as String, (map["code"] as Long).toInt(), map["room_category"] as String, (map["like"] as Long).toInt()))
                                        adapter.notifyDataSetChanged()
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        })

                    database.child("Room").orderByChild("information").get().addOnSuccessListener { it ->
                        Log.d("msg", "굳굳" + it.value)
//                        val map = it.value as HashMap<*, *>
//                        search_DataList.add(room_Data(map["maker"] as String, map["title"] as String, map["information"] as String, (map["code"] as Long).toInt(), map["room_category"] as String))
//                        adapter.notifyDataSetChanged()
                    }.addOnFailureListener{ }


                }
                return true
            }

            // 바뀔때마다
            override fun onQueryTextChange(newText: String?): Boolean {
                println(newText)
                return true
            }
        })

        // RecyclerView
        serach_room_recylerView = rootView.recyclerView_search
        // 구분선
        serach_room_recylerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        serach_room_recylerView.layoutManager = LinearLayoutManager(requireContext())
        serach_room_recylerView.adapter = adapter

        return rootView
    }

    inner class SearchRecyclerViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.room_tittle
        val information : TextView = v.room_info
        val code : TextView = v.room_code

        fun bind(item: room_Data) {
            title.text = item.title
            information.text = item.information
            code.text = item.code.toString()

            itemView.setOnClickListener {
                activity?.let {
                    val intent = Intent(context, EditRoom::class.java)
                    intent.putExtra("code", item.code)
                    startActivity(intent)
                    activity!!.overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit)
                }
            }
        }
    }

    inner class SearchRecyclerViewAdapter(val room_data_list : ArrayList<room_Data>) : RecyclerView.Adapter<SearchRecyclerViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchRecyclerViewHolder {
            // 0, 1, 2 parameter -> 0 : view로 팽창할 것, 1 : 팽창해서 recyclerView(parent)에 표시, 2 : false
            val cellForRow = LayoutInflater.from(parent.context).inflate(R.layout.room_list, parent, false)
            return SearchRecyclerViewHolder(cellForRow)
        }

        override fun onBindViewHolder(holder: SearchRecyclerViewHolder, position: Int) {
            holder.bind(room_data_list[position])
        }

        override fun getItemCount() = room_data_list.size
    }
}