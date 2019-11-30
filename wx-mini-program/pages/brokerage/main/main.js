var app = getApp();
var util = require('../../../utils/util.js');
var api = require('../../../config/api.js');

Page({
  data: {
    today_type:"active",
    yestoday_type:"disable",
    week_type:"disable",
    month_type: "disable",
    hasLogin: false
  },
  onLoad: function(options) {
  },
  onReady: function() {
    // 页面渲染完成
  },
  onShow: function() {
    // 页面显示
    //获取用户的登录信息
    if (app.globalData.hasLogin) {
      let userInfo = wx.getStorageSync('userInfo');
      this.setData({
        userInfo: userInfo,
        hasLogin: true
      });

      let that = this;
      util.request(api.BrokerageMain).then(function (res) {
        if (res.errno === 0) {
          that.setData({
            totalAmount: res.data.totalAmount,
            remainAmount: res.data.remainAmount,
            lastMonthSettleMoney: res.data.lastMonthSettleMoney,
            currMonthSettleMoney: res.data.currMonthSettleMoney,
            currType: res.data.todayData,
            todayData: res.data.todayData,
            yestodayData: res.data.yestodayData,
            weekData: res.data.weekData,
            monthData: res.data.monthData,
          });
        }
      });
    }
  },
  onHide: function() {
    // 页面隐藏

  },
  onUnload: function() {
    // 页面关闭

  },
  switchTabType:function(e){
    var type = e.currentTarget.dataset.type;
    var currType;
    var today_type = "disable";
    var yestoday_type = "disable";
    var week_type = "disable";
    var month_type = "disable";
    if (type == 1){
      currType = this.data.todayData;
      today_type = "active";
    } else if (type == 2) {
      currType = this.data.yestodayData;
      yestoday_type = "active";
    } else if (type == 3) {
      currType = this.data.weekData;
      week_type = "active";
    } else if (type == 4) {
      currType = this.data.monthData;
      month_type = "active";
    }
    this.setData({
      currType: currType,
      today_type: today_type,
      yestoday_type: yestoday_type,
      week_type: week_type,
      month_type: month_type
    })
  },
  goOrderList: function () {
    if (this.data.hasLogin) {
      wx.navigateTo({
        url: "/pages/brokerage/order/order"
      });
    } else {
      wx.navigateTo({
        url: "/pages/auth/login/login"
      });
    }
  },
  goExtract: function () {
    if (this.data.hasLogin) {
      wx.navigateTo({
        url: "/pages/brokerage/record/record"
      });
    } else {
      wx.navigateTo({
        url: "/pages/auth/login/login"
      });
    }
  },
  extractMoney: function () {
    if (this.data.hasLogin) {
      if (this.data.remainAmount >= 1){
	      wx.navigateTo({
	        url: "/pages/brokerage/withdrawal/withdrawal?remainAmount=" + this.data.remainAmount
	       });
      } else {
         wx.showModal({
			 title: '提示',
			 content: '您的可提现金额小于1元，暂时无法申请提现！',
			 showCancel:false,
			 success (res) {
			   //暂时不做处理
			 }
		 });
      }
    } else {
       wx.navigateTo({
	        url: "/pages/auth/login/login"
	   });
    }
  }
})