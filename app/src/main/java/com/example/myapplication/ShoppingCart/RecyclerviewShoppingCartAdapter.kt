package com.example.myapplication.ShoppingCart


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.FireBase.FireBaseCollector
import com.example.myapplication.FireBase.ItemInfo_Firebase_Model
import com.example.myapplication.R
import com.example.myapplication.ShoppingCart.Utils.FireStoreRetrivalUtils
import kotlinx.android.synthetic.main.cardview_shoppingcart.view.*

class RecyclerviewShoppingCartAdapter(
    val userShoppingCartList: MutableList<ShoppingCartModel>,
    val uid: String
) :
    RecyclerView.Adapter<RecyclerviewShoppingCartAdapter.ShoppingCartViewHolder>() {

    var  mCallBack:  CallBack? = null
   // var mCallBackAfterDeleteItem:  CallBackToUpdateAmount? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingCartViewHolder =
        ShoppingCartViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.cardview_shoppingcart,
                parent,
                false
            )
        )

    override fun getItemCount(): Int {
        return userShoppingCartList.size
    }

    fun setCallBack(theCallBack:  CallBack) {
        this.mCallBack= theCallBack
    }



    interface CallBack {
        fun updateTotalAmount(updatePrice: Double)
        fun updateUIAfterDeletedItem()
    }


    override fun onBindViewHolder(holder: ShoppingCartViewHolder, position: Int) {
        holder.also {
        it.initUserCart(userShoppingCartList, position)
        it.selectedNumCalcalculate(mCallBack!!, uid)
       }
        holder.view.delItem.setOnClickListener {
            holder.deleteItem(mCallBack!!, uid)
        }
    }


    class ShoppingCartViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        var totalAmount: Double = 0.0
        val mFireBaseCollector = FireBaseCollector()
        var num: Int = 0
        var price: Long = 0
        val itemInfo = ShoppingCartModel()
        var unicode :String? = null

        fun initUserCart(
            userShoppingCartInfo: MutableList<ShoppingCartModel>
            , position: Int
        ) {
            var category = userShoppingCartInfo.get(position).category
            var subcategory_position = userShoppingCartInfo.get(position).sub_category
            var totalItems = userShoppingCartInfo.get(position).totalItems
            var unicode = userShoppingCartInfo.get(position).unicode

            this.num = totalItems!!
            this.unicode=unicode


            mFireBaseCollector.readData_userShoppingCart(object :
                FireBaseCollector.ShoppingCartDataStatus {
                override fun ShoppingCartData(retriveListByCategoryPosition: MutableList<MutableList<ItemInfo_Firebase_Model>>) {

                    val dataProvider: ItemInfo_Firebase_Model =
                        retriveListByCategoryPosition.get(category!! - 1)
                            .get(subcategory_position!!)

                    price = dataProvider.price!!

                    view.apply {
                        product_name_shoppingcart.text = dataProvider.name
                        product_price_shoppingcart.text = "$" + price.toString()
                        Glide.with(context).load(dataProvider.url_forRecyclerview)
                            .into(product_image_shoppingcart)
                        numberOfItem_shoppingcart.text = num.toString()
                        setTextTotalPrice()
                    }

                }
            })
        }

        fun selectedNumCalcalculate(callBack: CallBack, uid: String) {
            view.apply {
                numberOfItem_shoppingcart.text = num.toString()
                minusBttn_shoppingcart.setOnClickListener {
                    if (num > 1) { //at least 1 item in the list of shoppingcart, use del button instead if destroy
                        num--
                        numberOfItem_shoppingcart.text = num.toString()
                        setTextTotalPrice()
                        callBack.updateTotalAmount(-price.toDouble())
                        updateItem(uid)
                    }

                }
                plusBttn_shoppingcart.setOnClickListener {
                    num++
                    numberOfItem_shoppingcart.text = num.toString()
                    setTextTotalPrice()
                    callBack.updateTotalAmount(price.toDouble())
                    updateItem(uid)
                }
            }
        }

        fun setTextTotalPrice() {
            var totalAmount: Double = num * price.toDouble()
            if (totalAmount == 0.0) {
                view.product_totalPrice_shoppingcart.text = "$" + totalAmount.toInt().toString()
                return
            }
            view.product_totalPrice_shoppingcart.text = "$" + totalAmount.toString()
        }

        fun updateItem(uid: String) {
            val ref = FireStoreRetrivalUtils.mFirebaseFirestore(uid).document(unicode!!)
            ref.update("totalItems", num).addOnSuccessListener {
                Log.i(TAG, "Update item succesfully")
            }.addOnFailureListener { e ->
                Toast.makeText(view.context, "${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        fun deleteItem(callBack : CallBack, uid: String) {
            val ref = FireStoreRetrivalUtils.mFirebaseFirestore(uid).document(unicode!!)
            ref.delete().addOnSuccessListener {
                Toast.makeText(
                    view.context,
                    "Item deleted",
                    Toast.LENGTH_SHORT
                ).show()
                callBack?.updateUIAfterDeletedItem()
            }.addOnFailureListener { e ->
                Toast.makeText(
                    view.context,
                    "${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        companion object const

        val TAG = "RecyclerviewShoppingCartAdapter.ShoppingCartViewHolder"

    }
}






