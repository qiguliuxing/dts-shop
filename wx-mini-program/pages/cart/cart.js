var util = require('../../utils/util.js');
var api = require('../../config/api.js');
var user = require('../../utils/user.js');

var app = getApp();

Page({
  data: {
    isMultiOrderModel:0,
    cartGoods: [],
    brandCartgoods:[],
    cartTotal: {
      "goodsCount": 0,
      "goodsAmount": 0.00,
      "checkedGoodsCount": 0,
      "checkedGoodsAmount": 0.00
    },
    isEditCart: false,
    checkedAllStatus: true,
    editCartList: [],
    hasLogin: false
  },
  onLoad: function(options) {
    // 页面初始化 options为页面跳转所带来的参数
  },
  onReady: function() {
    // 页面渲染完成
  },
  onPullDownRefresh() {
    wx.showNavigationBarLoading() //在标题栏中显示加载
    this.getCartList();
    wx.hideNavigationBarLoading() //完成停止加载
    wx.stopPullDownRefresh() //停止下拉刷新
  },
  onShow: function() {
    // 页面显示
    if (app.globalData.hasLogin) {
      this.getCartList();
    }

    this.setData({
      hasLogin: app.globalData.hasLogin
    });

  },
  onHide: function() {
    // 页面隐藏
  },
  onUnload: function() {
    // 页面关闭
  },
  goLogin() {
    wx.navigateTo({
      url: "/pages/auth/login/login"
    });
  },
  getCartList: function() {
    let that = this;
    util.request(api.CartList).then(function(res) {
      if (res.errno === 0) {
        if (res.data.isMultiOrderModel === 1){
          that.setData({
            isMultiOrderModel: res.data.isMultiOrderModel,
            brandCartgoods: res.data.brandCartgoods,
            cartTotal: res.data.cartTotal
          });
        } else {
          that.setData({
            isMultiOrderModel: res.data.isMultiOrderModel,
            cartGoods: res.data.cartList,
            cartTotal: res.data.cartTotal
          });
        }

        that.setData({
          checkedAllStatus: that.isCheckedAll()
        });
      }
    });
  },
  isChildCheckedAll: function (cartList){
    return cartList.every(function (element, index, array) {
      if (element.checked == true) {
        return true;
      } else {
        return false;
      }
    });
  },
  isCheckedAll: function() { 
    let that = this;
    if (that.data.isMultiOrderModel === 1){
      //多店铺模式的商品全选判断  
      return that.data.brandCartgoods.every(function (element, index, array) {
        if (that.isChildCheckedAll(element.cartList)){
          return true;
        } else {
          return false;
        }
      });
    } else {
      //判断购物车商品已全选
      return that.data.cartGoods.every(function (element, index, array) {
        if (element.checked == true) {
          return true;
        } else {
          return false;
        }
      });
    }
  },
  doCheckedAll: function() {
    let checkedAll = this.isCheckedAll()
    this.setData({
      checkedAllStatus: this.isCheckedAll()
    });
  },
  getProductChecked:function(productId){
    let that = this;
    let isChecked = null;
    if (that.data.isMultiOrderModel === 1) {
      that.data.brandCartgoods.forEach(function (v) {
        let cartList = v.cartList;
        cartList.forEach(function(o){
          if (o.productId === productId) {
            isChecked = o.checked ? 0 : 1;
          }
        });
      });
    } else {
      that.data.cartGoods.forEach(function(o){
        if (o.productId === productId) {
          isChecked = o.checked ? 0 : 1;
        }
      });
    }
    return isChecked;
  },
  checkedItem: function(event) {
    //let itemIndex = event.target.dataset.itemIndex;
    let productId = event.currentTarget.dataset.productid;
    let that = this;

    let productIds = [];
    productIds.push(productId);
    let isChecked = that.getProductChecked(productId);
    if (!this.data.isEditCart) {
      util.request(api.CartChecked, {
        productIds: productIds,
        isChecked: isChecked
      }, 'POST').then(function(res) {
        if (res.errno === 0) {
          if (res.data.isMultiOrderModel === 1) {
            that.setData({
              isMultiOrderModel: res.data.isMultiOrderModel,
              brandCartgoods: res.data.brandCartgoods,
              cartTotal: res.data.cartTotal
            });
          } else {
            that.setData({
              isMultiOrderModel: res.data.isMultiOrderModel,
              cartGoods: res.data.cartList,
              cartTotal: res.data.cartTotal
            });
          }
        }

        that.setData({
          checkedAllStatus: that.isCheckedAll()
        });
      });
    } else {
      //编辑状态
      if (that.data.isMultiOrderModel === 1) {
        let tmpBrandCartData = this.data.brandCartgoods.map(function (element, index, array) {
          let tmpBrandGoods = element.cartList.map(function (childEle,childIndex,childArr){
            if (childEle.productId === productId) {
              childEle.checked = !childEle.checked;
             }
            return childEle;
          });
          element.cartList = tmpBrandGoods;
          return element;
        });

        that.setData({
          brandCartgoods: tmpBrandCartData,
          checkedAllStatus: that.isCheckedAll(),
          'cartTotal.checkedGoodsCount': that.getCheckedGoodsCount()
        });
      } else {
        let tmpCartData = this.data.cartGoods.map(function (element, index, array) {
          if (element.productId === productId) {
            element.checked = !element.checked;
          }
          return element;
        });

        that.setData({
          cartGoods: tmpCartData,
          checkedAllStatus: that.isCheckedAll(),
          'cartTotal.checkedGoodsCount': that.getCheckedGoodsCount()
        });
      }
    }
  },
  getCheckedGoodsCount: function() {
    let that = this;
    let checkedGoodsCount = 0;
    if (that.data.isMultiOrderModel === 1) {
      that.data.brandCartgoods.forEach(function (v) {
        v.cartList.forEach(function (o){
          if (o.checked === true) {
            checkedGoodsCount += o.number;
          }
        });
      });
    } else {
      that.data.cartGoods.forEach(function (v) {
        if (v.checked === true) {
          checkedGoodsCount += v.number;
        }
      });
    }
    console.log(checkedGoodsCount);
    return checkedGoodsCount;
  },
  checkedAll: function() {
    let that = this;
    if (!this.data.isEditCart) {
      let productIds = [];
      if (that.data.isMultiOrderModel === 1) {
        that.data.brandCartgoods.forEach(function (v) {
          v.cartList.forEach(function (o) {
            productIds.push(o.productId);
          });
        });
      } else {
        var productIds = that.data.cartGoods.map(function (v) {
          return v.productId;
        });
      }

      util.request(api.CartChecked, {
        productIds: productIds,
        isChecked: that.isCheckedAll() ? 0 : 1
      }, 'POST').then(function(res) {
        if (res.errno === 0) {
          console.log(res.data);
          if (res.data.isMultiOrderModel === 1) {
            that.setData({
              isMultiOrderModel: res.data.isMultiOrderModel,
              brandCartgoods: res.data.brandCartgoods,
              cartTotal: res.data.cartTotal
            });
          } else {
            that.setData({
              isMultiOrderModel: res.data.isMultiOrderModel,
              cartGoods: res.data.cartList,
              cartTotal: res.data.cartTotal
            });
          }
        }
        that.setData({
          checkedAllStatus: that.isCheckedAll()
        });
      });
    } else {
      //编辑状态,将所有
      let checkedAllStatus = that.isCheckedAll();

      if (that.data.isMultiOrderModel === 1) {
        let tmpBrandCartData = this.data.brandCartgoods.map(function (element, index, array) {
          let tmpBrandGoods = element.cartList.map(function (childEle, childIndex, childArr) {
            childEle.checked = !checkedAllStatus;
            return childEle;
          });
          element.cartList = tmpBrandGoods;
          return element;
        });

        that.setData({
          brandCartgoods: tmpBrandCartData,
          checkedAllStatus: that.isCheckedAll(),
          'cartTotal.checkedGoodsCount': that.getCheckedGoodsCount()
        });
      } else {
        let tmpCartData = this.data.cartGoods.map(function (element, index, array) {
          element.checked = !checkedAllStatus;
          return element;
        });

        that.setData({
          cartGoods: tmpCartData,
          checkedAllStatus: that.isCheckedAll(),
          'cartTotal.checkedGoodsCount': that.getCheckedGoodsCount()
        });
      }
    }
  },
  editCart: function() {
    var that = this;
    if (this.data.isEditCart) {
      this.getCartList();
      this.setData({
        isEditCart: !this.data.isEditCart
      });
    } else {
      //编辑状态
      if (that.data.isMultiOrderModel === 1) {
        let tmpBrandCartData = that.data.brandCartgoods.map(function (element, index, array) {
          let tmpBrandGoods = element.cartList.map(function (childEle, childIndex, childArr) {
            childEle.checked = false;
            return childEle;
          });
          element.cartList = tmpBrandGoods;
          return element;
        });

        that.setData({
          brandCartgoods: tmpBrandCartData,
          isEditCart: !that.data.isEditCart,
          checkedAllStatus: that.isCheckedAll(),
          'cartTotal.checkedGoodsCount': that.getCheckedGoodsCount()
        });
      } else {
        let tmpCartData = that.data.cartGoods.map(function (element, index, array) {
          element.checked = false;
          return element;
        });

        that.setData({
         // editCartList: this.data.cartGoods,
          cartGoods: tmpCartList,
          isEditCart: !that.data.isEditCart,
          checkedAllStatus: that.isCheckedAll(),
          'cartTotal.checkedGoodsCount': that.getCheckedGoodsCount()
        });
      }
    }
  },

  updateCart: function(productId, goodsId, number, id) {
    let that = this;

    util.request(api.CartUpdate, {
      productId: productId,
      goodsId: goodsId,
      number: number,
      id: id
    }, 'POST').then(function(res) {
      that.setData({
        checkedAllStatus: that.isCheckedAll()
      });
    });

  },
  getProductItem: function (productId){
    let that = this;
    let productItem = null;
    if (that.data.isMultiOrderModel === 1) {
      that.data.brandCartgoods.forEach(function (v) {
        let cartList = v.cartList;
        cartList.forEach(function (o) {
          if (o.productId === productId) {
            productItem = o;
          }
        });
      });
    } else {
      that.data.cartGoods.forEach(function (o) {
        if (o.productId === productId) {
          productItem = o;
        }
      });
    }
    return productItem;
  },
  setProductItem: function (cartItem,productId){
    let that = this;
    if (that.data.isMultiOrderModel === 1) {
      let tmpBrandCartData = this.data.brandCartgoods.map(function (element, index, array) {
        let tmpBrandGoods = element.cartList.map(function (childEle, childIndex, childArr) {
          if (childEle.productId === productId) {
            return cartItem;
          } else {
            return childEle;
          }
        });
        element.cartList = tmpBrandGoods;
        return element;
      });

      that.setData({
        brandCartgoods: tmpBrandCartData,
        checkedAllStatus: that.isCheckedAll(),
        'cartTotal.checkedGoodsCount': that.getCheckedGoodsCount()
      });
    } else {
      let tmpCartData = this.data.cartGoods.map(function (element, index, array) {
        if (element.productId === productId) {
          return cartItem;
        } else {
          return element;
        }
      });
      that.setData({
        cartGoods: tmpCartData,
        checkedAllStatus: that.isCheckedAll(),
        'cartTotal.checkedGoodsCount': that.getCheckedGoodsCount()
      });
    }
  },
  cutNumber: function(event) {
    //let itemIndex = event.target.dataset.itemIndex;
    let productId = event.currentTarget.dataset.productid;
    let cartItem = this.getProductItem(productId);
    let number = (cartItem.number - 1 > 1) ? cartItem.number - 1 : 1;
    cartItem.number = number;
    this.setProductItem(cartItem, productId);
    this.updateCart(cartItem.productId, cartItem.goodsId, number, cartItem.id);
  },
  addNumber: function(event) {
   // let itemIndex = event.target.dataset.itemIndex;
    let productId = event.currentTarget.dataset.productid;
    let cartItem = this.getProductItem(productId);

    let number = cartItem.number + 1;
    cartItem.number = number;
    this.setProductItem(cartItem, productId);
    this.updateCart(cartItem.productId, cartItem.goodsId, number, cartItem.id);
  },
  checkoutOrder: function() {
    //获取已选择的商品
    let that = this;
    /*var checkedGoods = this.data.cartGoods.filter(function(element, index, array) {
      if (element.checked == true) {
        return true;
      } else {
        return false;
      }
    });
    if (checkedGoods.length <= 0) {
      return false;
    }*/

    if(that.getCheckedGoodsCount() <= 0){
      wx.showModal({
        title: '错误信息',
        content: '请勾选需要下单的商品！',
        showCancel: false
      });
      return false;
    }

    // storage中设置了cartId，则是购物车购买
    try {
      wx.setStorageSync('cartId', 0);
      wx.navigateTo({
        url: '/pages/checkout/checkout'
      })
    } catch (e) {}

  },
  deleteCart: function() {
    //获取已选择的商品
    let that = this;
    /*let productIds = this.data.cartGoods.filter(function(element, index, array) {
      if (element.checked == true) {
        return true;
      } else {
        return false;
      }
    });

    if (productIds.length <= 0) {
      return false;
    }*/

    if (that.getCheckedGoodsCount() <= 0) {
      wx.showModal({
        title: '错误信息',
        content: '请勾选需要删除的商品！',
        showCancel: false
      });
      return false;
    }

    let productIds = [];
    if (that.data.isMultiOrderModel === 1) {
      that.data.brandCartgoods.forEach(function (v) {
        v.cartList.forEach(function (o) {
          if (o.checked == true){
            productIds.push(o.productId);
          }
        });
      });
    } else {
      productIds = that.data.cartGoods.map(function (v) {
        if (v.checked == true) {
          return v.productId;
        }
      });
    }

    util.request(api.CartDelete, {
      productIds: productIds
    }, 'POST').then(function(res) {
      if (res.errno === 0) {
        if (res.data.isMultiOrderModel === 1) {
          that.setData({
            isMultiOrderModel: res.data.isMultiOrderModel,
            brandCartgoods: res.data.brandCartgoods,
            cartTotal: res.data.cartTotal
          });
        } else {
          that.setData({
            isMultiOrderModel: res.data.isMultiOrderModel,
            cartGoods: res.data.cartList,
            cartTotal: res.data.cartTotal
          });
        }

        that.setData({
          checkedAllStatus: that.isCheckedAll()
        });
      }
    });
  }
})