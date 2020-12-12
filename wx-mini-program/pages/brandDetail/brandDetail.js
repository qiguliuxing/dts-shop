var util = require('../../utils/util.js');
var api = require('../../config/api.js');


var app = getApp();

Page({
  data: {
    id: 0,
    brand: {},
    goodsList: [],
    page: 1,
    size: 10,
    totalPages: 1
  },
  onLoad: function(options) {
    // 页面初始化 options为页面跳转所带来的参数
    var that = this;
    that.setData({
      id: parseInt(options.id)
    });
    this.getBrand();
  },
  getBrand: function() {
    let that = this;
    util.request(api.BrandDetail, {
      id: that.data.id
    }).then(function(res) {
      if (res.errno === 0) {
        that.setData({
          brand: res.data.brand
        });

        that.getGoodsList();
      }
    });
  },
  getGoodsList() {
    var that = this;

    util.request(api.GoodsList, {
        brandId: that.data.id,
        page: that.data.page,
        size: that.data.size
      })
      .then(function(res) {
        if (res.errno === 0) {
          that.setData({
            goodsList: that.data.goodsList.concat(res.data.goodsList),
            totalPages: res.data.totalPages
          });
        }
      });
  },
  onReady: function() {
    // 页面渲染完成

  },
  onShow: function() {
    // 页面显示

  },
  onHide: function() {
    // 页面隐藏

  },
  onUnload: function() {
    // 页面关闭

  }, 
  onReachBottom:function() {
	if (this.data.totalPages > this.data.page) {
		this.setData({
		  page: this.data.page + 1
		});
	} else {
		wx.showToast({
		  title: '已经到底了!',
		  icon: 'none',
		  duration: 2000
		});
		return false;
	}
	 this.getGoodsList();
   }
})