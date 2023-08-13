package com.thk.im.android.ui.panel.emoji

import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer

class EmojiImgFragment : Fragment(), Observer<Any> {

    //    private var adapter2: EmoImageAdapter2? = null
//
//
//    lateinit var disposable: CompositeDisposable
//
//    lateinit var _binding: FragmentEmojiImgBinding
//
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return super.onCreateView(inflater, container, savedInstanceState)
//    }
//
//
//    private fun initEmojiImage() {
//        val gridLayoutManager = GridLayoutManager(context, 4)
//        binding.rcv.layoutManager = gridLayoutManager
//        adapter2 = EmoImageAdapter2(context)
//        binding.rcv.adapter = adapter2
//        adapter2!!.setOnItemClickListener { adapter, view, position ->
//            if (position == 0) {
//                launchActivity<EmojiManagerActivity> { }
//            } else {
//                if (activity is ChatActivity) {
//                    (activity as ChatActivity).emojiImgOnItemClick(
//                        position,
//                        adapter.data[position] as EmoBriefEntity
//                    )
//                } else if (activity is DialogChatActivity) {
//                    (activity as DialogChatActivity).emojiImgOnItemClick(
//                        position,
//                        adapter.data[position] as EmoBriefEntity
//                    )
//                }
//            }
//        }
//    }
//
//
//    private fun onRefresh() {
//        //从数据库获取所有标签数据 ,如果取不到就从网络获取
//        val list: MutableList<EmoBriefEntity> = DBInterface.instance().emoJiALl
//        Logs.i("----" + list.size)
//        if (list.size > 0) {
//            list.add(0, EmoBriefEntity())
//            adapter2!!.setNewData(list)
//        } else {
//            getEmojiList()
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        onRefresh()
//    }
//
//
//    override fun initView(savedInstanceState: Bundle?) {
//        initEmojiImage()
//    }
//
//    override fun initData() {
//        XEventBus.observe(this, EmojiEvent.UPDATE_EMOJI, this)
//        onRefresh()
//    }
//
//    fun success(list: List<EmoBriefEntity?>) {
//        //数据库删除现有的表情
//        DBInterface.instance().deleteAllEmoji()
//        //数据库保存最新的表情
//        DBInterface.instance().saveAll(list)
//        val newlist = list.toMutableList().apply { add(0, EmoBriefEntity()) }
//        adapter2?.setNewData(newlist)
//    }
//
//
//    internal class EmoImageAdapter2(val context: Context?) :
//        BaseQuickAdapter<EmoBriefEntity, BaseViewHolder>(R.layout.item_emo_image2) {
//        override fun convert(helper: BaseViewHolder, item: EmoBriefEntity) {
//            val imageView = helper.getView<ImageView>(R.id.iv_emo)
//            val params = imageView.layoutParams as RelativeLayout.LayoutParams
//            params.height =
//                (AppUtils.instance().screenWidth - AppUtils.instance().dp2px(120)) / 4
//            params.width =
//                (AppUtils.instance().screenWidth - AppUtils.instance().dp2px(120)) / 4
//            when (helper.adapterPosition) {
//                0 -> helper.setImageResource(R.id.iv_emo, R.drawable.emo_menu_add1)
//                else -> GlideUtils.load(item.url, imageView)
//            }
//        }
//    }
//
//    private fun getEmojiList() {
//        val subscriber = object : BaseDisposableSubscriber<List<EmoBriefEntity>>() {
//            override fun onSuccess(t: List<EmoBriefEntity>) {
//                success(t.toMutableList())
//            }
//        }
//        repositoryManager.obtainRetrofitManager2(EmojiApi::class.java)
//            .getRemoteEmojiList(UserManager.getUserToken())
//            .compose(Transform.transformListResponse(EmoBriefEntity::class.java))
//            .compose(Transform.flowableToMain())?.subscribe(subscriber)
//        disposable.add(subscriber)
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        disposable.clear()
//    }
//
//    override fun onChanged(t: Any?) {
//        onRefresh()
//    }
    override fun onChanged(t: Any?) {

    }
}