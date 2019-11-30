var app = getApp();
var util = require('../../utils/util.js');
var api = require('../../config/api.js');
var WxParse = require('../../lib/wxParse/wxParse.js');

Page({

  /**
   * 页面的初始数据
   */
  data: {
      'title': '公告详情',
      'content': "",
       id:0,
       "addTime":"2019-01-01",
       "type":1
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    if (options.hasOwnProperty('id')){
      this.setData({ id: options.id});
    }else{
      wx.navigateBack({delta: 1 });
    }
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady: function () {

  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow: function () {
    this.getArticleOne();
  },
  getArticleOne:function(){
    var that = this;
    util.request(api.ArticleDetail, {
      id: that.data.id
    }).then(function(res) {
      if (res.errno === 0) {
         that.setData({ 
             "title": res.data.title, 
             "content": res.data.content,
             "addTime": res.data.addTime,
              "type": res.data.type
         });
         
          //html转wxml
          WxParse.wxParse('content', 'html', res.data.content, that, 0);
      }
    });
  },
  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide: function () {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload: function () {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh: function () {

  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom: function () {

  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage: function () {

  }
})