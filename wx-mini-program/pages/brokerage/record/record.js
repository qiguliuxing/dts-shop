var util = require('../../../utils/util.js');
var api = require('../../../config/api.js');
var app = getApp();

Page({
  data: {
    accountTraceList: [],
    page: 1,
    size: 10,
    totalPages: 1
  },
  onLoad: function(options) {
    // 页面初始化 options为页面跳转所带来的参数
  },
  onReady: function() {
    // 页面渲染完成
  },
  onShow: function() {
    // 页面显示
    this.getRecordList();
  },
  getRecordList() {
    let that = this;
    util.request(api.ExtractList, {
      page: that.data.page,
      size: that.data.size
    }).then(function (res) {
      if (res.errno === 0) {
        console.log(res.data);
        that.setData({
          accountTraceList: res.data.accountTraceList,
          totalPages: res.data.totalPages
        });
      }
    });
  },
  
  onHide: function() {
    // 页面隐藏
  },
  onUnload: function() {
    // 页面关闭
  }
})